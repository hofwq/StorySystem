package ru.hofwq.storySystem.storyline.PlayerSit;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.scheduler.BukkitRunnable;
import ru.hofwq.storySystem.StorySystem;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SitListener implements Listener {
    private static StorySystem plugin = StorySystem.getPlugin();
    public static final Set<Player> seated = new HashSet<>();
    public static final Map<Player, ArmorStand> stands = new HashMap<>();

    public static void seatPlayer(Player player) {
        if (seated.contains(player)) return;
        Location loc = player.getLocation();
        ArmorStand stand = loc.getWorld().spawn(loc.add(0,0.2,0), ArmorStand.class);
        stand.setVisible(false);
        stand.setMarker(true);
        stand.setGravity(false);
        stand.setInvulnerable(true);
        stand.addPassenger(player);

        seated.add(player);
        stands.put(player, stand);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || !stand.isValid() || !seated.contains(player)) {
                    cancel();
                    if (stand.isValid()) stand.remove();
                    return;
                }
                if (!player.getVehicle().equals(stand)) {
                    stand.addPassenger(player);
                }
            }
        }.runTaskTimer(plugin, 1L, 5L);
    }

    public static void unseatPlayer(Player player) {
        if (!seated.contains(player)) return;

        ArmorStand stand = stands.remove(player);

        if (stand != null && stand.isValid()) {
            stand.removePassenger(player);
            stand.remove();
        }

        seated.remove(player);
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent e) {
        if (e.getRightClicked() instanceof ArmorStand) {
            ArmorStand as = (ArmorStand) e.getRightClicked();
            Player p = e.getPlayer();

            if (seated.contains(p) && stands.get(p).equals(as)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onToggleSneak(PlayerToggleSneakEvent e){
        Player player = e.getPlayer();

        if(!seated.contains(player)) return;

        e.setCancelled(true);
    }
}
