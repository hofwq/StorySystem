package ru.hofwq.storySystem.olgaquests;

import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import ru.hofwq.storySystem.StorySystem;
import ru.hofwq.storySystem.config.Config;
import ru.hofwq.storySystem.utils.Border;
import ru.hofwq.storySystem.utils.Utils;
import org.bukkit.block.data.Ageable;

import java.util.*;

public class OlgaQuestListener implements Listener {
    public static StorySystem plugin = StorySystem.getPlugin();
    private final Border border;
    private final Config cfg;

    private final int berriesLoc_x = plugin.getConfig().getInt("berriesLoc.x");
    private final int berriesLoc_y = plugin.getConfig().getInt("berriesLoc.y");
    private final int berriesLoc_z = plugin.getConfig().getInt("berriesLoc.z");
    private final Map<UUID, Set<Location>> pickedBushes = new HashMap<>();

    int berriesLocRadius = plugin.getConfig().getInt("berriesLocRadius.blocks");

    List<UUID> playersTakingOlgaQuest = new ArrayList<>();
    public static final List<UUID> playersAllowedToUseBoat = new ArrayList<>();
    List<UUID> playersInQuest = new ArrayList<>();
    List<UUID> messagedToPlayer = new ArrayList<>();
    List<UUID> playerStartedQuest = new ArrayList<>();
    World world = Bukkit.getWorld("world");
    Location berriesLoc = new Location(world, berriesLoc_x, berriesLoc_y, berriesLoc_z); //location from which quest will begin
    List<UUID> alreadyTiltedBack = new ArrayList<>();
    public static final List<UUID> playerCompletedQuest = new ArrayList<>();

    Map<UUID, Integer> progressMap = new HashMap<>();
    Map<UUID, BossBar> bossBarMap = new HashMap<>();

    final int TOTAL_REQUIRED = 400;
    final int INCREMENT_PER_CLICK = 16;

    int borderFirst_x = plugin.getConfig().getInt("borderFirst.x");
    int borderFirst_y = plugin.getConfig().getInt("borderFirst.y");
    int borderFirst_z = plugin.getConfig().getInt("borderFirst.z");

    int borderSecond_x = plugin.getConfig().getInt("borderSecond.x");
    int borderSecond_y = plugin.getConfig().getInt("borderSecond.y");
    int borderSecond_z = plugin.getConfig().getInt("borderSecond.z");

    public OlgaQuestListener(Config cfg){
        Vector p1 = new Vector(borderFirst_x, borderFirst_y, borderFirst_z); //total area on which player can move
        Vector p2 = new Vector(borderSecond_x, borderSecond_y, borderSecond_z);
        this.border = new Border(p1, p2);
        this.cfg = cfg;
    }

    @EventHandler
    public void onBerryClick(PlayerInteractEvent e){
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getHand() != EquipmentSlot.HAND) return;

        Block clickedBlock = e.getClickedBlock();
        if (clickedBlock == null || clickedBlock.getType() != Material.SWEET_BERRY_BUSH) {
            return;
        }

        e.setCancelled(true);

        Player player = e.getPlayer();

        if (!playerStartedQuest.contains(player.getUniqueId())) {
            return;
        }

        if (isHoldingQuestItemInMain(player)) {
            player.sendMessage(ChatColor.RED + "Ягоды нужно собирать с ведром в руках.");
            return;
        }

        Location loc = clickedBlock.getLocation();
        Set<Location> set = pickedBushes.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>());
        if (set.contains(loc)) {
            player.sendMessage(ChatColor.RED + "Вы уже собрали ягоды с этого куста.");
            return;
        }

        set.add(loc);

        BlockData data = clickedBlock.getBlockData();
        if (!(data instanceof Ageable ageable)) return;

        ageable.setAge(1);
        new BukkitRunnable() {
            @Override
            public void run() {
                if(playersInQuest.contains(player.getUniqueId())) player.sendBlockChange(loc, (BlockData) ageable);
            }
        }.runTaskTimer(plugin, 0L, 10L);

        player.playSound(clickedBlock.getLocation(), Sound.BLOCK_SWEET_BERRY_BUSH_PICK_BERRIES, SoundCategory.AMBIENT, 1L, 1L);

        int currentProgress = progressMap.getOrDefault(player.getUniqueId(), 0);
        currentProgress += INCREMENT_PER_CLICK;

        if (currentProgress > TOTAL_REQUIRED) {
            currentProgress = TOTAL_REQUIRED;
        }
        progressMap.put(player.getUniqueId(), currentProgress);

        double progressFraction = (double) currentProgress / TOTAL_REQUIRED;
        int progressPercent = (int) (progressFraction * 100);

        BossBar bossBar = bossBarMap.get(player.getUniqueId());
        if (bossBar == null) return;

        bossBar.setProgress(progressFraction);
        bossBar.setTitle("Прогресс: " + progressPercent + "%");

        if (currentProgress >= TOTAL_REQUIRED) {
            bossBar.removeAll();
            player.sendMessage(ChatColor.GREEN + "Вы собрали нужное количество, возвращайтесь к Ольге Дмитриевне и отдайте ведро.");
            playerStartedQuest.remove(player.getUniqueId());
            playersInQuest.remove(player.getUniqueId());
            playerCompletedQuest.add(player.getUniqueId());
            cfg.setQuestStatus(player.getUniqueId(), "BACKTOOLGA");
            StorySystem.getPlugin().getNavigator().updateCompass(player);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();

        if (playerStartedQuest.contains(player.getUniqueId())) {
            e.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent e) {
        for (BossBar bossBar : bossBarMap.values()) {
            bossBar.removeAll();
        }

        List<List<UUID>> collections = List.of(
                playerCompletedQuest,
                playerStartedQuest,
                playersInQuest,
                playersAllowedToUseBoat,
                playersTakingOlgaQuest,
                messagedToPlayer
        );

        for(Player p : Bukkit.getOnlinePlayers()){
            UUID uuid = p.getUniqueId();

            boolean isInAny = collections.stream()
                    .anyMatch(list -> list.contains(uuid));

            if(isInAny){
                removeQuestItemFromPlayer(p);
            }

            List<String> statuses = cfg.getQuestStatus(p.getUniqueId());

            if (!statuses.contains("OlgaDone")) {

                cfg.setQuestStatus(p.getUniqueId(), "");

                StorySystem.getPlugin()
                        .getNavigator()
                        .updateCompass(p);
            }
        }

        pickedBushes.clear();
        playerCompletedQuest.clear();
        playerStartedQuest.clear();
        playersInQuest.clear();
        playersAllowedToUseBoat.clear();
        playersTakingOlgaQuest.clear();
        messagedToPlayer.clear();
    }

    private boolean isHoldingQuestItemInMain(Player player) {
        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();

        if (!itemInMainHand.hasItemMeta()) {
            return true;
        }

        ItemMeta meta = itemInMainHand.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return true;
        }

        return !meta.getDisplayName().equals(ChatColor.GRAY + "Ведро от Ольги Дмитриевны");
    }

    @EventHandler
    public void onVehicleEnter(VehicleEnterEvent e){
        if (!(e.getEntered() instanceof Player player)) return;
        if (!(e.getVehicle() instanceof Boat boat)) return;

        BoatHandler boatHandler = plugin.boatHandler;

        if(boatHandler != null && boatHandler.getBoats().contains(boat)){
            if(!playersAllowedToUseBoat.contains(player.getUniqueId())){
                player.sendMessage(ChatColor.RED + "Мне пока не нужна лодка, нет нужды плыть на другие острова.");
                e.setCancelled(true);
            }
        }
    }

    private void knockbackPlayer(Player player, boolean facingAway) {
        if (player.isFlying()) return;

        Vector look = player.getLocation()
                .getDirection()
                .setY(0)
                .normalize();

        Vector forward = look.clone().multiply(0.5);
        Vector backward = look.multiply(-0.5);

        Vector knockback = facingAway ? backward : forward;
        knockback.setY(0.2);

        if (player.getVehicle() instanceof Boat) {
            Boat boat = (Boat) player.getVehicle();
            boat.setVelocity(knockback.setY(0));
        } else {
            player.setVelocity(knockback);
        }

        if (!alreadyTiltedBack.contains(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Кажется, мне не в эту сторону.");
            alreadyTiltedBack.add(player.getUniqueId());

            new BukkitRunnable() {
                @Override
                public void run() {
                    alreadyTiltedBack.remove(player.getUniqueId());
                }
            }.runTaskLater(plugin, 40L);
        }
    }

    private boolean isPlayerFacingAway(Player player, Location location) {
        Vector toLocation = location.toVector().subtract(player.getLocation().toVector());
        Vector direction = player.getLocation().getDirection();

        double angle = toLocation.angle(direction);

        return Math.abs(angle) > Math.PI / 2;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e){
        Player player = e.getPlayer();
        Location firstLocation = new Location(world, borderFirst_x, borderFirst_y, borderFirst_z);
        Location secondLocation = new Location(world, borderSecond_x, borderSecond_y, borderSecond_z);

        if(border.contains(player.getLocation()) && playersInQuest.contains(player.getUniqueId())){
            if(!messagedToPlayer.contains(player.getUniqueId()) && berriesLoc.distance(player.getLocation()) <= berriesLocRadius){
                player.sendMessage(ChatColor.GREEN + "Вы прибыли на место, начинайте сбор ягод.");
                messagedToPlayer.add(player.getUniqueId());
                playerStartedQuest.add(player.getUniqueId());

                BossBar bossBar = Bukkit.createBossBar("Прогресс: 0%", BarColor.RED, BarStyle.SEGMENTED_12);
                bossBar.addPlayer(player);
                bossBar.setProgress(0.0);
                bossBarMap.put(player.getUniqueId(), bossBar);
            }
        } else if(!border.contains(player.getLocation()) && playersInQuest.contains(player.getUniqueId())) {
            Location center = border.getCenter(firstLocation, secondLocation);
            boolean facingAway = isPlayerFacingAway(player, center);

            knockbackPlayer(player, facingAway);
        }
    }

    @EventHandler
    public void onBoatMove(VehicleMoveEvent e) {
        if (!(e.getVehicle() instanceof Boat)) return;

        Location firstLocation = new Location(world, borderFirst_x, borderFirst_y, borderFirst_z);
        Location secondLocation = new Location(world, borderSecond_x, borderSecond_y, borderSecond_z);

        Boat boat = (Boat) e.getVehicle();
        if (boat.getPassengers().isEmpty()
                || !(boat.getPassengers().get(0) instanceof Player)) {
            return;
        }
        Player player = (Player) boat.getPassengers().get(0);

        if (!playersInQuest.contains(player.getUniqueId())) return;

        Location boatLoc = boat.getLocation();
        if (!border.contains(boatLoc)) {
            Location center = border.getCenter(firstLocation, secondLocation);
            boolean facingAway = isPlayerFacingAway(player, center);

            Vector dir = player.getLocation()
                    .getDirection()
                    .setY(0)
                    .normalize();
            Vector knockVec = facingAway
                    ? dir.clone().multiply(-0.5)
                    : dir.clone().multiply(0.5);
            knockVec.setY(0.2);

            boat.setVelocity(knockVec.setY(0));
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        List<List<UUID>> collections = List.of(
                playerCompletedQuest,
                playerStartedQuest,
                playersInQuest,
                playersAllowedToUseBoat,
                playersTakingOlgaQuest,
                messagedToPlayer
        );

        boolean isInAny = collections.stream()
                .anyMatch(list -> list.contains(uuid));

        if(isInAny){
            removeQuestItemFromPlayer(player);
        }

        pickedBushes.remove(player.getUniqueId());
        playerCompletedQuest.remove(player.getUniqueId());
        playerStartedQuest.remove(player.getUniqueId());
        playersInQuest.remove(player.getUniqueId());
        playersAllowedToUseBoat.remove(player.getUniqueId());
        playersTakingOlgaQuest.remove(player.getUniqueId());
        messagedToPlayer.remove(player.getUniqueId());

        List<String> statuses = cfg.getQuestStatus(player.getUniqueId());

        if (!statuses.contains("OlgaDone")) {

            cfg.setQuestStatus(player.getUniqueId(), "");

            StorySystem.getPlugin()
                    .getNavigator()
                    .updateCompass(player);
        }
    }

    @EventHandler
    public void onNPCRightClickEvent(NPCRightClickEvent e){
        Player player = e.getClicker().getPlayer();
        int npcID = e.getNPC().getId();
        int Olga_ID = plugin.getConfig().getInt("NPC_Olga.ID");
        NPC npc = e.getNPC();

        if(npc == null) return;

        e.setCancelled(true);

        if (npc.getEntity() instanceof Villager villager) {
            assert player != null;
            player.playSound(npc.getEntity().getLocation(), Sound.ENTITY_VILLAGER_NO, SoundCategory.AMBIENT, 1L, 1L);
            villager.shakeHead();
        }

        assert player != null;
        List<String> statuses = cfg.getQuestStatus(player.getUniqueId());
        if (statuses.contains("OlgaDone") && npcID == Olga_ID) {
            player.sendMessage(ChatColor.RED + "Вы уже помогли Ольге Дмитриевне.");
            e.setCancelled(true);
            return;
        }

        if(playersInQuest.contains(player.getUniqueId()) && !playerCompletedQuest.contains(player.getUniqueId()) && npcID == Olga_ID){
            player.sendMessage(ChatColor.RED + "Вы уже начали выполнение задания.");
            return;
        }

        if (playerCompletedQuest.contains(player.getUniqueId()) && npcID == Olga_ID) {
            if(isHoldingQuestItemInMain(player)){
                player.sendMessage(ChatColor.RED + "Отдайте ведро Ольге Дмитриевне.");
                return;
            }

            Utils.sendDelayedMessage(player, ChatColor.GOLD + "Ольга Дмитриевна: " + ChatColor.GREEN + "&f&l(Деловито проверяет ведро с ягодами, одобрительно кивает) &7Молодец, пионер! Ты отлично справился с заданием — видно, что не подвёл доверие. &f&l(Делает пометку в блокноте) &7Твой труд — вклад в общее дело, и отряд может на тебя рассчитывать.".replace("&","§"), 0, Sound.BLOCK_NOTE_BLOCK_PLING);

            removeQuestItemFromPlayer(player);
            pickedBushes.remove(player.getUniqueId());
            playerCompletedQuest.remove(player.getUniqueId());
            playerStartedQuest.remove(player.getUniqueId());
            playersInQuest.remove(player.getUniqueId());
            playersAllowedToUseBoat.remove(player.getUniqueId());
            playersTakingOlgaQuest.remove(player.getUniqueId());
            messagedToPlayer.remove(player.getUniqueId());
            cfg.setQuestStatus(player.getUniqueId(), "OlgaDone");
            StorySystem.getPlugin().getNavigator().updateCompass(player);
            return;
        }

        if(npcID == Olga_ID && !playersTakingOlgaQuest.contains(player.getUniqueId()) && !playersInQuest.contains(player.getUniqueId())) {
            playersTakingOlgaQuest.add(player.getUniqueId());
            Utils.sendDelayedMessage(player, ChatColor.GOLD + "Ольга Дмитриевна: " + ChatColor.GRAY + "Доброе утро, пионер! Нам нужна помощь с приготовлением компота для столовой. На завтрак компота ещё хватает, но к обеду пить будет нечего… А кого ни попрошу помочь — все отказываются. То ли уже всех запрягли с утра, то ли пионеры не хотят плыть на ближний остров… Ну ладно, ты то нам поможешь? > > &fС этими словами её голос наполнился ласковой уверенностью, сопровождаясь искренней улыбкой, которая сразу вселяла доверие.", 0, Sound.BLOCK_NOTE_BLOCK_PLING);

            TextComponent prompt = new TextComponent("Согласиться? ");
            prompt.setColor(net.md_5.bungee.api.ChatColor.GOLD);

            TextComponent yes = new TextComponent("[Да]");
            yes.setColor(net.md_5.bungee.api.ChatColor.GREEN);
            yes.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "да"));
            yes.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Согласиться").create()));

            TextComponent no = new TextComponent("[Нет]");
            no.setColor(net.md_5.bungee.api.ChatColor.RED);
            no.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "нет"));
            no.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Отказаться").create()));

            int delayTicks = 2 * 20;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
                player.spigot().sendMessage(prompt, yes, new TextComponent(" / "), no);
            }, delayTicks);

            Utils.removeFromListAfter(player, ChatColor.RED + "Время на принятие решения истекло.", 45, playersTakingOlgaQuest);
            return;
        }
    }

    @EventHandler
    public void onChatEvent(AsyncPlayerChatEvent e){
        Player player = e.getPlayer();
        String message = e.getMessage();

        if(playersTakingOlgaQuest.contains(player.getUniqueId())){
            e.setCancelled(true);
        }

        if(playersTakingOlgaQuest.contains(player.getUniqueId())){
            if(message.equalsIgnoreCase("да")){
                Utils.sendDelayedMessage(player, ChatColor.GOLD + player.getDisplayName() + ": " + ChatColor.GRAY + "Хорошо, я помогу с этим.", 0, Sound.BLOCK_NOTE_BLOCK_PLING);
                Utils.sendDelayedMessage(player, ChatColor.GOLD + "Ольга Дмитриевна: " + ChatColor.GRAY + "Большое тебе спасибо! Есть все таки трудолюбивые и готовые помочь пионеры! Тебе нужно сплавать на лодке на ближний остров, там насобирать ягод, лодки есть у причала. Успей до обеда, пожалуйста. Не забудь ведро только &f(передаёт ведро). &7Удачного тебе пути!", 1, Sound.BLOCK_NOTE_BLOCK_PLING);
                Utils.removeFromListAfter(player, 2, playersTakingOlgaQuest);
                giveQuestItemToPlayer(player);
                playersAllowedToUseBoat.add(player.getUniqueId());
                cfg.setQuestStatus(player.getUniqueId(), "TOISLAND");
                StorySystem.getPlugin().getNavigator().updateCompass(player);
                return;
            } else if(message.equalsIgnoreCase("нет")) {
                Utils.sendDelayedMessage(player, ChatColor.GOLD + player.getDisplayName() + ": " + ChatColor.GRAY + "Сейчас не смогу помочь.", 0, Sound.BLOCK_NOTE_BLOCK_PLING);
                Utils.sendDelayedMessage(player, ChatColor.GOLD + "Ольга Дмитриевна: " + ChatColor.GRAY + "Что ж, видимо пионеры останутся без питья, совсем пионеры расстраивают меня...", 1, Sound.BLOCK_NOTE_BLOCK_PLING);
                Utils.removeFromListAfter(player, 2, playersTakingOlgaQuest);
                return;
            }

            Utils.sendDelayedMessage(player, ChatColor.RED + "Сначала нужно ответить Ольге: (Да/Нет).", 0, Sound.BLOCK_NOTE_BLOCK_PLING);
        }
    }

    public void giveQuestItemToPlayer(Player player){
        ItemStack bucket = new ItemStack(Material.BUCKET, 1);

        ItemMeta meta = bucket.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GRAY + "Ведро от Ольги Дмитриевны");
            bucket.setItemMeta(meta);
        }

        playersInQuest.add(player.getUniqueId());

        if(player.getInventory().firstEmpty() != -1){
            player.getInventory().addItem(bucket);
        } else {
            Location loc = player.getLocation();
            Item droppedBucket = player.getWorld().dropItem(loc, bucket);
            droppedBucket.setMetadata("bucketOwner", new FixedMetadataValue(plugin, player.getUniqueId().toString()));
        }
    }

    public void removeQuestItemFromPlayer(Player player) {
        ItemStack questBucket = new ItemStack(Material.BUCKET, 1);
        ItemMeta meta = questBucket.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GRAY + "Ведро от Ольги Дмитриевны");
            questBucket.setItemMeta(meta);
        }

        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null && item.isSimilar(questBucket)) {
                player.getInventory().clear(i);
            }
        }

        for (Item droppedItem : player.getWorld().getEntitiesByClass(Item.class)) {
            if (droppedItem.hasMetadata("bucketOwner")) {
                for (MetadataValue metaValue : droppedItem.getMetadata("bucketOwner")) {
                    if (metaValue.asString().equals(player.getUniqueId().toString()) &&
                            droppedItem.getItemStack().isSimilar(questBucket)) {
                        droppedItem.remove();
                        break;
                    }
                }
            }
        }

        playersInQuest.remove(player.getUniqueId());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;

        if (playersInQuest.contains(player.getUniqueId())) {
            ItemStack item = e.getCurrentItem();
            if (isQuestItem(item)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;

        if (playersInQuest.contains(player.getUniqueId())) {
            for (ItemStack item : e.getNewItems().values()) {
                if (isQuestItem(item)) {
                    e.setCancelled(true);
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent e) {
        Player player = e.getPlayer();
        if (playersInQuest.contains(player.getUniqueId())) {
            ItemStack item = e.getItemDrop().getItemStack();
            if (isQuestItem(item)) {
                e.setCancelled(true);
            }
        }
    }
    
    private boolean isQuestItem(ItemStack item) {
        if(item == null || !item.hasItemMeta()) return false;

        ItemMeta meta = item.getItemMeta();
        if(meta == null || !meta.hasDisplayName()) return false;

        return meta.getDisplayName().equals(ChatColor.GRAY + "Ведро от Ольги Дмитриевны");
    }

    @EventHandler
    public void onPlayerPickupBucket(@SuppressWarnings("deprecation") PlayerPickupItemEvent event) {
        Item item = event.getItem();
        if (item.hasMetadata("bucketOwner")) {
            String ownerUUID = item.getMetadata("bucketOwner").getFirst().asString();
            if (!event.getPlayer().getUniqueId().toString().equals(ownerUUID)) {
                event.setCancelled(true);
            }
        }
    }
}
