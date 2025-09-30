package ru.hofwq.storySystem;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import ru.hofwq.storySystem.config.Config;
import ru.hofwq.storySystem.olgaquests.OlgaQuestListener;
import ru.hofwq.storySystem.olgaquests.BoatHandler;
import ru.hofwq.storySystem.storyline.PlayerSit.SitListener;
import ru.hofwq.storySystem.storyline.commands.*;
import ru.hofwq.storySystem.storyline.events.EventListener;
import ru.hofwq.storySystem.storyline.SQL.SQLite;
import ru.hofwq.storySystem.utils.NavigatorHandler;
import ru.hofwq.storySystem.utils.PlayerInventoryInteractions;

import java.sql.SQLException;
import java.util.logging.Logger;

public final class StorySystem extends JavaPlugin implements Listener {
    public SQLite db;
    public Logger log = getLogger();
    private static StorySystem plugin;
    public BoatHandler boatHandler;
    public Config configManager;
    private NavigatorHandler navigator;

    @Override
    public void onEnable() {
        plugin = this;

        //Initializing config
        configManager = new Config(this);
        configManager.checkConfig();
        configManager.setup();

        //Initializing SQL
        db = new SQLite(this, "storylinePlayers.db");
        db.initDatabase();
        
        //Initializing boatHandler
        boatHandler = new BoatHandler(this);
        boatHandler.loadBoatChunks();
        boatHandler.spawnAllBoats();
        boatHandler.checkBoats();

        //Initializing navigatorHandler
        navigator = new NavigatorHandler(this, configManager);
        for(Player p : Bukkit.getOnlinePlayers()) navigator.updateCompass(p);

        Bukkit.getScheduler().runTaskTimer(
                this,
                boatHandler::checkBoats,
                20L * 60 * 3,
                20L * 60 * 3
        );

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (World world : Bukkit.getWorlds()) {
                for (Entity e : world.getEntities()) {
                    if (e.getType() == EntityType.VILLAGER) {
                        e.setSilent(true);
                    }
                }
            }
        }, 0L, 20L);

        //Registering events
        EventListener listener = new EventListener();
        getServer().getPluginManager().registerEvents(listener, this);
        getServer().getPluginManager().registerEvents(new OlgaQuestListener(configManager), this);
        getServer().getPluginManager().registerEvents(new NavigatorHandler(this, configManager), this);
        getServer().getPluginManager().registerEvents(new SitListener(), this);

        //Registering commands
        getCommand("setstoryflag").setExecutor(new SetPlayerStoryFlag());
        getCommand("resetstoryflag").setExecutor(new ResetPlayerStoryFlag());
        getCommand("basketclick").setExecutor(new BasketHandler(listener));
        getCommand("packclick").setExecutor(new PackHandler(listener));
        getCommand("fridgeclick").setExecutor(new FridgeHandler(listener));

        log.info(ChatColor.GREEN + "StorySystem enabled.");
    }

    @Override
    public void onDisable() {
        World world = Bukkit.getWorld("world");
        if (world != null) {
            for (Boat boat : world.getEntitiesByClass(Boat.class)) {
                if (boat.hasMetadata("questBoat")) {
                    boat.remove();
                }
            }
        }

        for (ArmorStand stand : SitListener.stands.values()) {
            if (stand != null && stand.isValid()) stand.remove();
        }

        for(Player p : Bukkit.getOnlinePlayers()) {
            try {
                if(plugin.db.getFlag(p.getUniqueId()) != null && plugin.db.getFlag(p.getUniqueId()).equals("inPrologue")){
                    plugin.db.removePlayer(p.getUniqueId());
                    SitListener.unseatPlayer(p);
                    PlayerInventoryInteractions.restoreInventory(p);
                    return;
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        SitListener.stands.clear();
        SitListener.seated.clear();

        db.closeConnection();

        plugin = null;

        log.info(ChatColor.RED + "StorySystem disabled.");
    }

    public static StorySystem getPlugin(){
        return plugin;
    }

    public NavigatorHandler getNavigator() {
        return navigator;
    }
}
