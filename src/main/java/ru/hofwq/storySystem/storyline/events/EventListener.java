package ru.hofwq.storySystem.storyline.events;

import fr.xephi.authme.api.v3.AuthMeApi;
import fr.xephi.authme.events.LoginEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ru.hofwq.storySystem.StorySystem;
import ru.hofwq.storySystem.storyline.PlayerSit.SitListener;
import ru.hofwq.storySystem.utils.OpenDoorUsingProtocol;
import ru.hofwq.storySystem.utils.PlayerInventoryInteractions;
import ru.hofwq.storySystem.utils.Utils;
import ru.hofwq.storySystem.utils.Voices;

import java.sql.SQLException;
import java.util.*;

public class EventListener implements Listener {
    public static StorySystem plugin = StorySystem.getPlugin();

    private final Map<String, String> items = new HashMap<>();
    private final Map<Player, ItemStack> playerBaskets = new HashMap<>();
    private final Map<UUID, Set<String>> clickedItems = new HashMap<>();
    private final Map<UUID, Set<String>> packedItems = new HashMap<>();
    private final Map<UUID, Set<String>> fridgeItems = new HashMap<>();
    private final Set<UUID> finishedFridge = new HashSet<>();

    String voice_1 = Voices.getSoundById(1);
    String voice_2 = Voices.getSoundById(2);
    String voice_3 = Voices.getSoundById(3);
    String voice_4 = Voices.getSoundById(4);
    String voice_5 = Voices.getSoundById(5);
    String voice_6 = Voices.getSoundById(6);
    String voice_7 = Voices.getSoundById(7);
    String voice_8 = Voices.getSoundById(8);
    String voice_9 = Voices.getSoundById(9);
    String voice_10 = Voices.getSoundById(10);
    String voice_11 = Voices.getSoundById(11);
    String voice_12 = Voices.getSoundById(12);
    String voice_13 = Voices.getSoundById(13);
    String voice_14 = Voices.getSoundById(14);
    String voice_15 = Voices.getSoundById(15);
    String voice_16 = Voices.getSoundById(16);
    String voice_17 = Voices.getSoundById(17);
    String voice_18 = Voices.getSoundById(18);
    String voice_19 = Voices.getSoundById(19);
    String voice_20 = Voices.getSoundById(20);
    String voice_21 = Voices.getSoundById(21);
    String voice_22 = Voices.getSoundById(22);
    String voice_23 = Voices.getSoundById(23);
    String voice_24 = Voices.getSoundById(24);
    String voice_25 = Voices.getSoundById(25);
    String voice_26 = Voices.getSoundById(26);
    String voice_27 = Voices.getSoundById(27);
    String voice_28 = Voices.getSoundById(28);
    String voice_29 = Voices.getSoundById(29);
    String voice_30 = Voices.getSoundById(30);
    String voice_31 = Voices.getSoundById(31);

    String tgNotif = "minecraft:custom.tgnotif";
    String mouseClick = "minecraft:custom.mouse";
    String keyboardClicks = "minecraft:custom.keyb";
    String scannerSound = "minecraft:custom.pik";
    String paidSound = "minecraft:custom.paid";
    String clothingSound = "minecraft:custom.clothing";
    String ambientMusic = "minecraft:custom.farewell_to_the_past";
    String gameAccept = "minecraft:custom.game";
    String heartSound = "minecraft:custom.heart";

    Set<UUID> authenticated = new HashSet<>();
    Set<UUID> resourcePackLoaded = new HashSet<>();

    List<UUID> playersOpenedDoor = new ArrayList<>();

    List<UUID> playersToGoOutside = new ArrayList<>();
    public List<UUID> playersAtStore = new ArrayList<>();
    List<UUID> playersFromStore = new ArrayList<>();
    List<UUID> playersLeavedHome = new ArrayList<>();
    List<UUID> playersDressed = new ArrayList<>();
    List<UUID> playersTookKeys = new ArrayList<>();
    List<UUID> playersTookBasket = new ArrayList<>();
    List<UUID> playersNearEnter = new ArrayList<>();
    List<UUID> playersInteractClothes = new ArrayList<>();
    List<UUID> playersBuying = new ArrayList<>();
    List<UUID> playersEnteredHome = new ArrayList<>();
    public List<UUID> playersNearFridge = new ArrayList<>();
    Location storeLocation = new Location(Bukkit.getWorld("world"), -8513, 79, -1078);
    Location storeSecondLocation = new Location(Bukkit.getWorld("world"), -8513, 79, -1074);
    Location enterLocation = new Location(Bukkit.getWorld("world"), -8803, 97, -981);

    Location semyonRoom = new Location(Bukkit.getWorld("world"), -8805.468, 149.375, -993.498);
    Location homeDoor = new Location(Bukkit.getWorld("world"), -8809, 149, -986);

    List<Location> points = new ArrayList<>();

    World world = Bukkit.getWorld("world");

    public EventListener() {
        items.put("red", "Хлеб");
        items.put("orange", "Печенье");
        items.put("yellow", "Яйца");
        items.put("lime", "Макароны");
        items.put("green", "Энергетик");
    }

    @EventHandler
    public void onAuthMeLogin(LoginEvent e) throws SQLException {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        authenticated.add(uuid);
        tryStartPrologue(player);
    }

    @EventHandler
    public void onResourcePackStatus(PlayerResourcePackStatusEvent e) throws SQLException {
        if (e.getStatus() == PlayerResourcePackStatusEvent.Status.ACCEPTED) return;

        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        resourcePackLoaded.add(uuid);
        tryStartPrologue(player);
    }

    private void tryStartPrologue(Player player) throws SQLException {
        UUID uuid = player.getUniqueId();

        if (authenticated.contains(uuid) && resourcePackLoaded.contains(uuid)) {
            authenticated.remove(uuid);
            resourcePackLoaded.remove(uuid);

            startPrologue(player);
        }
    }

    private void startPrologue(Player player) throws SQLException {
        int delay = 0;

        semyonRoom.setPitch(1.8F);
        semyonRoom.setYaw(-86);

        if(!AuthMeApi.getInstance().isAuthenticated(player)) return;

        if(plugin.db.isPlayerExists(player.getUniqueId())) return;

        plugin.db.insertPlayer(player.getUniqueId(), "inPrologue");

        world.setChunkForceLoaded(semyonRoom.getChunk().getX(), semyonRoom.getChunk().getZ(), true);

        PlayerInventoryInteractions.saveInventory(player);

        player.getInventory().clear();

        player.updateInventory();

        player.teleport(semyonRoom);
        player.setPlayerTime(18000L, false);
        player.setPlayerWeather(WeatherType.DOWNFALL);

        for(Player p : Bukkit.getOnlinePlayers()) {
            p.hidePlayer(plugin, player);
            player.hidePlayer(plugin, p);
        }

        if(!player.isOnline()){
            return;
        }

        Utils.runTaskLaterStory(player, () -> SitListener.seatPlayer(player), 1L);

        Utils.sendDelayedMessage(player, ChatColor.GOLD + "- " + ChatColor.GRAY + "Я сидел дома и не выходил на улицу уже 5-ые сутки подряд...", delay, voice_1);
        Utils.sendDelayedMessage(player, "", delay);
        delay += 4;

        Utils.sendDelayedMessage(player, ChatColor.GOLD + "- " + ChatColor.GRAY + "Такая жизнь мне, конечно, успела поднадоесть, но что мне остаётся делать?", delay, voice_2);
        Utils.sendDelayedMessage(player, "", delay);
        delay += 5;

        Utils.sendDelayedMessage(player, ChatColor.GOLD + "- " + ChatColor.GRAY + "Связь со многими друзьями в реальной жизни уже ушла в небытие, хоть раньше и казалось, что у нас очень крепкая дружба.", delay, voice_3);
        Utils.sendDelayedMessage(player, "", delay);
        delay += 7;

        Utils.sendDelayedMessage(player, ChatColor.GOLD + "- " + ChatColor.GRAY + "Из реальных друзей, с которыми можно встретиться остались лишь парочка, но встретиться с ними будет трудно, все занятые...", delay, voice_4);
        Utils.sendDelayedMessage(player, "", delay);
        delay += 7;

        Utils.sendDelayedMessage(player, ChatColor.GOLD + "- " + ChatColor.GRAY + "А из тех, с кем я часто общаюсь, у меня есть несколько интернет-друзей, с которыми я познакомился в онлайн-играх.", delay, voice_5);
        Utils.sendDelayedMessage(player, "", delay);
        delay += 6;

        Utils.sendDelayedMessage(player, ChatColor.GOLD + "- " + ChatColor.GRAY + "Но так ли это важно, если они лишь по ту сторону экрана?", delay, voice_6);
        Utils.sendDelayedMessage(player, "", delay);
        delay += 4;

        Utils.sendDelayedMessage(player, ChatColor.GOLD + "- " + ChatColor.GRAY + "Хоть я и больше не представляю свою жизнь без них, так хотя бы кажется, что я не одинок.", delay, voice_7);
        Utils.sendDelayedMessage(player, "", delay);
        delay += 7;

        Utils.runTaskLaterStory(player, () -> player.playSound(player.getLocation(), tgNotif, SoundCategory.AMBIENT, 1L, 1L), (delay - 1) * 20L);

        Utils.sendDelayedMessage(player, ChatColor.GOLD + "- " + ChatColor.GRAY + "Вот и один из них написал.", delay, voice_8);
        Utils.sendDelayedMessage(player, "", delay);
        delay += 5;

        Utils.runTaskLaterStory(player, () -> player.playSound(player.getLocation(), mouseClick, SoundCategory.AMBIENT, 1L, 1L), (delay - 1) * 20L);

        Utils.sendDelayedMessage(player, ChatColor.GOLD + "Саша: " + ChatColor.GRAY + "Пошли поиграем, обновление вышло.", delay, voice_19);
        Utils.sendDelayedMessage(player, "", delay);
        delay += 3;

        Utils.sendDelayedMessage(player, ChatColor.GOLD + "- " + ChatColor.GRAY + "Я бы прям щас и пошел, если бы не знал, что у меня в холодильнике пусто...", delay, voice_9);
        Utils.sendDelayedMessage(player, "", delay);
        delay += 4;

        Utils.sendDelayedMessage(player, ChatColor.GOLD + "- " + ChatColor.GRAY + "Надо сходить в магазин, потом можно и играть, сколько влезет.", delay, voice_10);
        Utils.sendDelayedMessage(player, "", delay);
        delay += 4;

        Utils.sendDelayedMessage(player, ChatColor.GOLD + "- " + ChatColor.GRAY + "Напишу \"давай через 20 минут, в круглосуточный схожу\".", delay, voice_11);
        Utils.sendDelayedMessage(player, "", delay);
        delay += 5;

        Utils.runTaskLaterStory(player, () -> player.playSound(player.getLocation(), keyboardClicks, SoundCategory.AMBIENT, 1L, 1L), delay * 20L);

        delay += 2;
        Utils.runTaskLaterStory(player, () -> player.stopSound(keyboardClicks, SoundCategory.AMBIENT), delay * 20L);

        delay += 1;

        Utils.runTaskLaterStory(player, () -> player.playSound(player.getLocation(), mouseClick, SoundCategory.AMBIENT, 1L, 1L), delay * 20L);

        delay += 2;

        Utils.runTaskLaterStory(player, () -> player.playSound(player.getLocation(), tgNotif, SoundCategory.AMBIENT, 1L, 1L), delay * 20L);

        Utils.sendDelayedMessage(player, ChatColor.GOLD + "- " + ChatColor.GRAY + "Ответ не заставил себя долго ждать.", delay, voice_12);
        Utils.sendDelayedMessage(player, "", delay);
        delay += 3;

        Utils.sendDelayedMessage(player, ChatColor.GOLD + "Саша: " + ChatColor.GRAY + "Я жду, давай недолго.", delay, voice_20);
        Utils.sendDelayedMessage(player, "", delay);
        delay += 3;

        Utils.sendDelayedMessage(player, ChatColor.GOLD + "- " + ChatColor.GRAY + "Ладно, пора одеваться и сходить в магазин.", delay, voice_13);
        Utils.sendDelayedMessage(player, "", delay);
        delay += 4;

        Utils.sendDelayedMessage(player, ChatColor.GOLD + "* " + ChatColor.GRAY + "Оденьтесь и возьмите ключи (находятся возле входной двери), после выходите на улицу.", delay, Sound.BLOCK_NOTE_BLOCK_PLING);
        Utils.sendDelayedMessage(player, "", delay);
        delay += 1;

        Utils.runTaskLaterStory(player, () -> {
            if (!player.isOnline()) return;

            SitListener.unseatPlayer(player);
            playersToGoOutside.add(player.getUniqueId());
        }, delay * 20L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) throws SQLException {
        Player player = e.getPlayer();

        if(plugin.db.getFlag(player.getUniqueId()) != null && plugin.db.getFlag(player.getUniqueId()).equals("inPrologue")){
            List<Integer> tasks = Utils.playerTasks.get(player.getUniqueId());

            if (tasks != null) {
                for (int taskId : tasks) {
                    Bukkit.getScheduler().cancelTask(taskId);
                }
                Utils.playerTasks.remove(player.getUniqueId());
            }

            PlayerInventoryInteractions.restoreInventory(player);
            plugin.getNavigator().updateCompass(player);
            player.resetPlayerTime();
            playersToGoOutside.remove(player.getUniqueId());
            playersLeavedHome.remove(player.getUniqueId());
            playersAtStore.remove(player.getUniqueId());
            playersFromStore.remove(player.getUniqueId());
            playersDressed.remove(player.getUniqueId());
            playersTookKeys.remove(player.getUniqueId());
            playersTookBasket.remove(player.getUniqueId());
            playersNearEnter.remove(player.getUniqueId());
            playerBaskets.remove(player);
            playersInteractClothes.remove(player.getUniqueId());
            playersBuying.remove(player.getUniqueId());
            player.removePotionEffect(PotionEffectType.BLINDNESS);
            Utils.stopArrow(player);
            authenticated.remove(player.getUniqueId());
            resourcePackLoaded.remove(player.getUniqueId());
            plugin.db.removePlayer(player.getUniqueId());
            playersNearFridge.remove(player.getUniqueId());
            playersEnteredHome.remove(player.getUniqueId());
            SitListener.unseatPlayer(player);
            player.stopSound(ambientMusic);
            player.resetPlayerWeather();

            playerBaskets.remove(player);
            clickedItems.remove(player.getUniqueId());
            packedItems.remove(player.getUniqueId());
            fridgeItems.remove(player.getUniqueId());
            finishedFridge.remove(player.getUniqueId());

            for(Player p : Bukkit.getOnlinePlayers()) {
                p.showPlayer(plugin, player);
                player.showPlayer(plugin, p);
            }
            return;
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent e) throws SQLException {
        Player player = e.getPlayer();

        if(plugin.db.getFlag(player.getUniqueId()).equals("inPrologue")) e.setCancelled(true);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) throws SQLException {
        Player player = e.getPlayer();

        if(plugin.db.getFlag(player.getUniqueId()).equals("inPrologue")) e.setCancelled(true);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) throws SQLException {
        Player player = e.getPlayer();
        Block clickedBlock = e.getClickedBlock();
        Location wardrobeLocation = new Location(player.getWorld(), -8809, 149, -993);
        Location doorLocation = new Location(player.getWorld(), -8809, 149, -985);

        if (e.getHand() != EquipmentSlot.HAND) return;
        if (e.getAction() == Action.PHYSICAL) return;

        if(plugin.db.getFlag(player.getUniqueId()) != null && !plugin.db.getFlag(player.getUniqueId()).equals("inPrologue")) return;

        if (clickedBlock != null && !Tag.DOORS.isTagged(clickedBlock.getType()) && !Tag.BUTTONS.isTagged(clickedBlock.getType())) e.setCancelled(true);

        if (clickedBlock != null && clickedBlock.getType() == Material.SPRUCE_DOOR) {
            e.setCancelled(true);

            if (player.getLocation().distance(wardrobeLocation) <= 3) {
                if (playersToGoOutside.contains(player.getUniqueId()) && !playersDressed.contains(player.getUniqueId()) && !playersInteractClothes.contains(player.getUniqueId())) {
                    playersInteractClothes.add(player.getUniqueId());
                    Utils.runTaskLaterStory(player, () -> player.playSound(player.getLocation(), clothingSound, SoundCategory.AMBIENT, 1L, 1L), 0L);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 255, false, false));
                    Utils.runTaskLaterStory(player, () -> playersDressed.add(player.getUniqueId()), 3 * 20L);
                    Utils.runTaskLaterStory(player, () -> player.removePotionEffect(PotionEffectType.BLINDNESS), 3 * 20L);
                    Utils.runTaskLaterStory(player, () -> playersInteractClothes.remove(player.getUniqueId()), 3 * 20L);
                    Utils.sendDelayedMessage(player, ChatColor.GOLD + "* " + ChatColor.GRAY + "После того как я оделся, можно было выходить на улицу.", 3, Sound.BLOCK_NOTE_BLOCK_PLING);
                } else if(playersFromStore.contains(player.getUniqueId()) && !playersInteractClothes.contains(player.getUniqueId())){
                    if(playersTookKeys.contains(player.getUniqueId())) {
                        Utils.sendDelayedMessage(player, ChatColor.GOLD + "* " + ChatColor.GRAY + "Сначала надо положить ключи на место.", 0, Sound.BLOCK_NOTE_BLOCK_PLING);
                        return;
                    }

                    if(!finishedFridge.contains(player.getUniqueId())){
                        Utils.sendDelayedMessage(player, ChatColor.GOLD + "* " + ChatColor.GRAY + "Нужно разложить продукты в холодильник.", 0, Sound.BLOCK_NOTE_BLOCK_PLING);
                        return;
                    }

                    playersInteractClothes.add(player.getUniqueId());
                    Utils.runTaskLaterStory(player, () -> player.playSound(player.getLocation(), clothingSound, SoundCategory.AMBIENT, 1L, 1L), 0L);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 255, false, false));
                    Utils.runTaskLaterStory(player, () -> player.removePotionEffect(PotionEffectType.BLINDNESS), 3 * 20L);
                    Utils.runTaskLaterStory(player, () -> playersInteractClothes.remove(player.getUniqueId()), 3 * 20L);
                    Utils.runTaskLaterStory(player, () -> playersDressed.remove(player.getUniqueId()), 3 * 20L);
                    Utils.sendDelayedMessage(player, ChatColor.GOLD + "* " + ChatColor.GRAY + "Я переоделся в домашнюю одежду и сел за компьютер.", 3);
                    Utils.sendDelayedMessage(player, "", 4);
                    Utils.sendDelayedMessage(player, ChatColor.GOLD + "* " + ChatColor.GRAY + "Чтобы сесть за компьютер нажмите ПКМ на кресло.", 4, Sound.BLOCK_NOTE_BLOCK_PLING);
                    playersFromStore.remove(player.getUniqueId());
                }
            }
            return;
        }

        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && clickedBlock != null) {
            Location fridgeLoc = new Location(player.getWorld(), -8804, 149, -1000);

            if(playersFromStore.contains(player.getUniqueId()) && !playersTookKeys.contains(player.getUniqueId())){
                if (!finishedFridge.contains(player.getUniqueId())) {
                    if (clickedBlock.getType() == Material.BARRIER && clickedBlock.getLocation().distance(fridgeLoc) <= 2.5) {
                        if(!playersNearFridge.contains(player.getUniqueId())) playersNearFridge.add(player.getUniqueId());

                        sendFridgeList(player);
                        return;
                    }
                }
            }
        }

        if ((clickedBlock != null && clickedBlock.getType() == Material.GRAY_CANDLE) && e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if(finishedFridge.contains(player.getUniqueId())){
                if(playersDressed.contains(player.getUniqueId())){
                    Utils.sendDelayedMessage(player, ChatColor.GOLD + "* " + ChatColor.GRAY + "Переоденьтесь в домашнюю одежду прежде чем садиться за компьютер.", 0, Sound.BLOCK_NOTE_BLOCK_PLING);
                    return;
                }

                int delay = 0;

                Utils.runTaskLaterStory(player, () -> player.teleport(semyonRoom), 0L);
                Utils.runTaskLaterStory(player, () -> SitListener.seatPlayer(player), 1L);
                Utils.sendDelayedMessage(player, ChatColor.GOLD + "- " + ChatColor.GRAY + "Давай, сейчас зайду в игру.", delay, voice_18);
                Utils.sendDelayedMessage(player, "", delay);

                delay += 4;

                Utils.runTaskLaterStory(player, () -> player.playSound(player.getLocation(), mouseClick, SoundCategory.AMBIENT, 1L, 1L), delay * 20L);

                delay += 2;

                Utils.sendDelayedMessage(player, ChatColor.GOLD + "- " + ChatColor.GRAY + "Хм, тут новый ивент, с завлекающим названием \"Бесконечное лето\".", delay, voice_21);
                Utils.sendDelayedMessage(player, "", delay);

                delay += 4;

                Utils.sendDelayedMessage(player, ChatColor.GOLD + "- " + ChatColor.GRAY + "Ладно, посмотрим что добавили.", delay, voice_22);
                Utils.sendDelayedMessage(player, "", delay);

                delay += 2;

                Utils.runTaskLaterStory(player, () -> player.playSound(player.getLocation(), gameAccept, SoundCategory.AMBIENT, 1L, 1L), delay * 20L);

                delay += 1;

                Utils.runTaskLaterStory(player, () -> player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 255, false, false)), delay * 20L);
                Utils.sendDelayedMessage(player, ChatColor.GOLD + "- " + ChatColor.GRAY + "Ч-что за..? Что произошло? Ничего не вижу...", delay, voice_23);
                Utils.sendDelayedMessage(player, "", delay);

                delay += 5;

                Utils.runTaskLaterStory(player, () -> player.playSound(player.getLocation(), heartSound, SoundCategory.AMBIENT, 1L, 1L), delay * 20L);

                delay += 1;

                Utils.sendDelayedMessage(player, ChatColor.GOLD + "- " + ChatColor.GRAY + "Голова.. Настолько раскалывается.. Словно кто-то воткнул раскаленный гвоздь в виски...", delay, voice_24);
                Utils.sendDelayedMessage(player, "", delay);

                delay += 7;

                Utils.sendDelayedMessage(player, ChatColor.GOLD + "- " + ChatColor.GRAY + "Чтобы она у меня так сильно болела, я еще не помню.. Но почему же я ничего не вижу?", delay, voice_25);
                Utils.sendDelayedMessage(player, "", delay);

                delay += 6;

                Utils.sendDelayedMessage(player, ChatColor.GOLD + "- " + ChatColor.GRAY + "Что происходит? Даже встать не могу.. Тело не слушается.", delay, voice_26);
                Utils.sendDelayedMessage(player, "", delay);

                delay += 6;

                Utils.sendDelayedMessage(player, ChatColor.GOLD + "- " + ChatColor.GRAY + "Усталость так резко накатила после похода в магазин?", delay, voice_27);
                Utils.sendDelayedMessage(player, "", delay);

                delay += 4;

                Utils.sendDelayedMessage(player, ChatColor.GOLD + "- " + ChatColor.GRAY + "Я как будто куда-то проваливаюсь.. Темнота и шум в ушах, неприятно...", delay, voice_28);
                Utils.sendDelayedMessage(player, "", delay);

                delay += 8;

                Utils.runTaskLaterStory(player, () -> player.stopSound(heartSound, SoundCategory.AMBIENT), delay * 20L);

                Utils.runTaskLaterStory(player, () -> finishPrologue(player), delay * 20L);

                delay += 1;

                Utils.runTaskLaterStory(player, () -> player.removePotionEffect(PotionEffectType.BLINDNESS), delay * 20L);

                delay += 1;

                Utils.sendDelayedMessage(player, ChatColor.GOLD + "- " + ChatColor.GRAY + "Где я? У меня глюки или все это мне просто снится?...", delay, voice_29);
                Utils.sendDelayedMessage(player, "", delay);
            }
        }

        if ((clickedBlock != null && clickedBlock.getType() == Material.IRON_DOOR)
                && e.getAction() == Action.RIGHT_CLICK_BLOCK) {

            if (playersOpenedDoor.contains(player.getUniqueId())) {
                e.setCancelled(true);
                return;
            }

            if (playersDressed.contains(player.getUniqueId()) && playersTookKeys.contains(player.getUniqueId())) {
                if(player.getLocation().distance(doorLocation) <= 2){
                    ItemStack item = player.getInventory().getItemInMainHand();
                    if (!isHouseKey(item)) {
                        ItemStack off = player.getInventory().getItemInOffHand();
                        if (isHouseKey(off)) {
                            item = off;
                        } else {
                            player.sendMessage(ChatColor.GRAY + "Возьмите ключи в руки.");
                            e.setCancelled(true);
                            return;
                        }
                    }

                    playersOpenedDoor.add(player.getUniqueId());

                    OpenDoorUsingProtocol.openDoorForPlayer(player, clickedBlock);
                    OpenDoorUsingProtocol.closeDoorAfter(clickedBlock, 5, player);

                    Utils.runTaskLaterStory(player, () -> {
                        playersOpenedDoor.remove(player.getUniqueId());
                    }, 5 * 20L);

                    e.setCancelled(true);
                    return;
                }
            }
        }

        if (playersAtStore.contains(player.getUniqueId())) {
            if (clickedBlock != null && clickedBlock.getType() == Material.OAK_LOG) {
                if (!playersTookBasket.contains(player.getUniqueId())) {
                    ItemStack cart = createBasket();
                    player.getInventory().addItem(cart);
                    playersTookBasket.add(player.getUniqueId());
                    Utils.sendDelayedMessage(player, ChatColor.GOLD + "- " + ChatColor.GRAY + "Так, надо купить хлеб, печенье, яйца, макароны и энергетик.", 0, voice_16);
                } else {
                    player.sendMessage(ChatColor.RED + "Вы уже взяли корзину.");
                }
            }

            if (clickedBlock != null && e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                Material type = clickedBlock.getType();
                String woolColor = null;

                if (type == Material.RED_WOOL) {
                    woolColor = "red";
                } else if (type == Material.ORANGE_WOOL) {
                    woolColor = "orange";
                } else if (type == Material.YELLOW_WOOL) {
                    woolColor = "yellow";
                } else if (type == Material.LIME_WOOL) {
                    woolColor = "lime";
                } else if (type == Material.GREEN_WOOL) {
                    woolColor = "green";
                } else {
                    return;
                }

                String itemName = items.get(woolColor);
                if (itemName == null) return;

                ItemStack basket = findBasket(player);
                if (basket == null) {
                    basket = createBasket();
                    player.getInventory().addItem(basket);
                }

                boolean added = updateBasket(player, basket, itemName);

                if (added) {
                    player.sendMessage(ChatColor.GREEN + "Вы взяли " + ChatColor.DARK_GREEN + itemName + ChatColor.GREEN + ".");
                } else {
                    player.sendMessage(ChatColor.RED + "Вы уже взяли " + ChatColor.DARK_RED + itemName + ChatColor.RED + "!");
                }
            }
        }
    }

    public void handleFridgeClick(Player player, String itemName) {
        Set<String> fridge = fridgeItems.getOrDefault(player.getUniqueId(), new HashSet<>());

        if (fridge.contains(itemName)) return;

        fridge.add(itemName);
        fridgeItems.put(player.getUniqueId(), fridge);

        sendFridgeList(player);

        if (fridge.size() == items.size() - 1) {
            finishedFridge.add(player.getUniqueId());

            Utils.sendDelayedMessage(player, ChatColor.GOLD + "- " + ChatColor.GRAY + "Энергетик возьму с собой, не хочу заставлять Сашу ждать.", 0, voice_30);
            playersNearFridge.remove(player.getUniqueId());
        }
    }

    private void sendFridgeList(Player player) {
        Set<String> fridge = fridgeItems.getOrDefault(player.getUniqueId(), new HashSet<>());

        for (int i = 0; i < 100; i++) player.sendMessage("");

        TextComponent title = new TextComponent("🧊 Холодильник");
        title.setColor(net.md_5.bungee.api.ChatColor.GOLD);
        title.setBold(true);
        title.setUnderlined(true);
        player.spigot().sendMessage(title);

        TextComponent hint = new TextComponent("Нажмите на предмет, чтобы положить его в холодильник");
        hint.setColor(net.md_5.bungee.api.ChatColor.GRAY);
        hint.setItalic(true);
        player.spigot().sendMessage(hint);
        player.spigot().sendMessage(new TextComponent(""));

        for (Map.Entry<String, String> entry : items.entrySet()) {
            if (entry.getValue().equalsIgnoreCase("Энергетик")) continue;

            String itemName = entry.getValue();
            TextComponent tc = new TextComponent("• " + itemName);

            if (fridge.contains(itemName)) {
                tc.setStrikethrough(true);
                tc.setColor(net.md_5.bungee.api.ChatColor.GRAY);
            } else {
                tc.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
                tc.setHoverEvent(new HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder("Нажмите, чтобы положить предмет в холодильник").color(net.md_5.bungee.api.ChatColor.GOLD).create()
                ));
                tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/fridgeclick " + itemName));
            }

            player.spigot().sendMessage(tc);
        }

        TextComponent status = new TextComponent("🧊 " + fridge.size() + "/" + (items.size() - 1) + " предметов в холодильнике");
        status.setColor(net.md_5.bungee.api.ChatColor.GREEN);
        status.setItalic(true);
        player.spigot().sendMessage(new TextComponent(""));
        player.spigot().sendMessage(status);
    }

    @EventHandler
    public void onInteractNPC(NPCRightClickEvent e) {
        Player player = e.getClicker().getPlayer();
        int id = e.getNPC().getId();

        if(id != 237) return;

        if(!playersAtStore.contains(player.getUniqueId())) return;

        ItemStack basket = findBasket(player);
        if (basket == null) return;

        List<String> lore = basket.getItemMeta().getLore();
        if (getBoughtItemsCount(lore) < items.size()) {
            player.sendMessage(ChatColor.RED + "Вы ещё не собрали все товары!");
            return;
        }

        for (int i = 0; i < 100; i++) player.sendMessage("");

        clickedItems.putIfAbsent(player.getUniqueId(), new HashSet<>());

        sendClickableBasket(player);
        playersBuying.add(player.getUniqueId());
    }

    private void sendClickableBasket(Player player) {
        Set<String> picked = clickedItems.getOrDefault(player.getUniqueId(), new HashSet<>());

        for (int i = 0; i < 100; i++) player.sendMessage("");

        TextComponent title = new TextComponent("🛒 Ваша корзина");
        title.setColor(net.md_5.bungee.api.ChatColor.GOLD);
        title.setBold(true);
        title.setUnderlined(true);
        player.spigot().sendMessage(title);

        TextComponent hint = new TextComponent("Нажмите на предмет, чтобы выбрать его");
        hint.setColor(net.md_5.bungee.api.ChatColor.GRAY);
        hint.setItalic(true);
        player.spigot().sendMessage(hint);
        player.spigot().sendMessage(new TextComponent(""));

        for (String itemName : items.values()) {
            TextComponent tc = new TextComponent("• " + itemName);

            if (picked.contains(itemName)) {
                tc.setStrikethrough(true);
                tc.setColor(net.md_5.bungee.api.ChatColor.GRAY);
            } else {
                tc.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
                tc.setHoverEvent(new HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder("Нажмите, чтобы выбрать предмет").color(net.md_5.bungee.api.ChatColor.GOLD).create()
                ));
                tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/basketclick " + itemName));
            }

            player.spigot().sendMessage(tc);
        }

        TextComponent status = new TextComponent("✅ " + picked.size() + "/" + items.size() + " выбрано");
        status.setColor(net.md_5.bungee.api.ChatColor.GREEN);
        status.setItalic(true);
        player.spigot().sendMessage(new TextComponent(""));
        player.spigot().sendMessage(status);
    }

    public void handleBasketClick(Player player, String itemName) {
        Set<String> clicked = clickedItems.getOrDefault(player.getUniqueId(), new HashSet<>());

        if (clicked.contains(itemName)) return;

        clicked.add(itemName);
        clickedItems.put(player.getUniqueId(), clicked);

        Utils.runTaskLaterStory(player,
                () -> player.playSound(player.getLocation(), scannerSound, SoundCategory.AMBIENT, 1L, 1L), 0L);

        sendClickableBasket(player);

        if (clicked.size() == items.size()) {
            Utils.sendDelayedMessage(player, ChatColor.GOLD + "Кассир: " +
                    ChatColor.GRAY + "Можете прикладывать карту.", 0);
            Utils.runTaskLaterStory(player,
                    () -> player.playSound(player.getLocation(), paidSound, SoundCategory.AMBIENT, 1L, 1L), 20L);

            Utils.runTaskLaterStory(player, () -> sendPackingList(player), 40L);
        }
    }

    public void handlePackClick(Player player, String itemName) {
        Set<String> packed = packedItems.getOrDefault(player.getUniqueId(), new HashSet<>());
        if (packed.contains(itemName)) return;

        packed.add(itemName);
        packedItems.put(player.getUniqueId(), packed);

        Utils.runTaskLaterStory(player,
                () -> player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, SoundCategory.AMBIENT, 1L, 1L), 0L);

        sendPackingList(player);

        if (packed.size() == items.size()) {
            Utils.sendDelayedMessage(player, ChatColor.GOLD + "- " + ChatColor.GRAY + "Так, продукты сложил, пора идти домой, меня ждёт Саша.", 0, voice_31);

            removeBasket(player);

            Utils.runTaskLaterStory(player, () -> playersAtStore.remove(player.getUniqueId()), 2 * 20L);
            Utils.runTaskLaterStory(player, () -> playersBuying.remove(player.getUniqueId()), 2 * 20L);
            Utils.runTaskLaterStory(player, () -> playersFromStore.add(player.getUniqueId()), 2 * 20L);

            List<Location> points = new ArrayList<>();
            points.add(new Location(world, -8515, 79, -1078));
            points.add(new Location(world, -8586, 79, -1092));
            points.add(new Location(world, -8716, 79, -1088));
            points.add(new Location(world, -8724, 79, -966));
            points.add(new Location(world, -8776, 95, -966));
            points.add(enterLocation);

            Utils.runTaskLaterStory(player, () -> Utils.startArrow(player, points), 2 * 20L);

            Utils.runTaskLaterStory(player, () -> player.playSound(player.getLocation(), ambientMusic, SoundCategory.AMBIENT, 1L, 1L), 3 * 20L);
        }
    }

    private void sendPackingList(Player player) {
        Set<String> packed = packedItems.getOrDefault(player.getUniqueId(), new HashSet<>());

        for (int i = 0; i < 100; i++) player.sendMessage("");

        TextComponent title = new TextComponent("📦 Пакет с покупками");
        title.setColor(net.md_5.bungee.api.ChatColor.GOLD);
        title.setBold(true);
        title.setUnderlined(true);
        player.spigot().sendMessage(title);

        TextComponent hint = new TextComponent("Нажмите на предмет, чтобы положить его в пакет");
        hint.setColor(net.md_5.bungee.api.ChatColor.GRAY);
        hint.setItalic(true);
        player.spigot().sendMessage(hint);
        player.spigot().sendMessage(new TextComponent(""));

        for (String itemName : items.values()) {
            TextComponent tc = new TextComponent("• " + itemName);

            if (packed.contains(itemName)) {
                tc.setStrikethrough(true);
                tc.setColor(net.md_5.bungee.api.ChatColor.GRAY);
            } else {
                tc.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
                tc.setHoverEvent(new HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder("Нажмите, чтобы положить предмет в пакет").color(net.md_5.bungee.api.ChatColor.GOLD).create()
                ));
                tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/packclick " + itemName));
            }

            player.spigot().sendMessage(tc);
        }

        TextComponent status = new TextComponent("🛒 " + packed.size() + "/" + items.size() + " предметов сложено");
        status.setColor(net.md_5.bungee.api.ChatColor.GREEN);
        status.setItalic(true);
        player.spigot().sendMessage(new TextComponent(""));
        player.spigot().sendMessage(status);
    }

    private ItemStack createBasket() {
        ItemStack basket = new ItemStack(Material.BOWL);
        ItemMeta meta = basket.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD.toString() + ChatColor.BOLD + "Корзина покупок (0/" + items.size() + ")");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Корзина пуста");
            meta.setLore(lore);
            basket.setItemMeta(meta);
        }

        return basket;
    }

    public void removeBasket(Player player) {
        ItemStack[] contents = player.getInventory().getContents();

        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];

            if (item != null && item.getType() == Material.BOWL) {
                ItemMeta meta = item.getItemMeta();

                if (meta != null && meta.hasDisplayName() &&
                        meta.getDisplayName().startsWith(ChatColor.GOLD.toString() + ChatColor.BOLD + "Корзина покупок")) {

                    player.getInventory().setItem(i, null);
                }
            }
        }

        player.updateInventory();
    }

    private boolean updateBasket(Player player, ItemStack basket, String itemName) {
        ItemMeta meta = basket.getItemMeta();
        if (meta == null) return false;

        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();

        if (lore.contains(ChatColor.GRAY + "Корзина пуста")) {
            lore.clear();
            lore.add(ChatColor.DARK_GREEN + "" + ChatColor.UNDERLINE + "Ваши покупки:");
        }

        String entry = "✓ " + itemName;

        boolean alreadyAdded = lore.stream()
                .map(ChatColor::stripColor)
                .anyMatch(s -> s.equals(entry));
        if (alreadyAdded) return false;

        lore.removeIf(s -> ChatColor.stripColor(s).equals("Все товары собраны!"));

        lore.add(ChatColor.GREEN + entry);

        int bought = getBoughtItemsCount(lore);

        meta.setDisplayName(ChatColor.GOLD.toString() + ChatColor.BOLD +
                "Корзина покупок (" + bought + "/" + items.size() + ")");

        if (bought == items.size()) {
            lore.add(ChatColor.YELLOW + "Все товары собраны!");
            Utils.sendDelayedMessage(player, ChatColor.GOLD + "- " +
                    ChatColor.GRAY + "Вроде взял всё, что нужно, теперь к кассе.", 0, voice_17);
        }

        meta.setLore(lore);
        basket.setItemMeta(meta);
        return true;
    }

    private int getBoughtItemsCount(List<String> lore) {
        int count = 0;
        for (String line : lore) {
            String stripped = ChatColor.stripColor(line).trim();
            if (stripped.startsWith("✓ ")) count++;
        }
        return count;
    }

    private ItemStack findBasket(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.hasItemMeta()) {
                ItemMeta m = item.getItemMeta();
                if (m != null && m.getDisplayName() != null &&
                        m.getDisplayName().startsWith(ChatColor.GOLD.toString() + ChatColor.BOLD + "Корзина покупок")) {
                    return item;
                }
            }
        }
        return null;
    }

    private boolean isHouseKey(ItemStack item) {
        if (item == null || item.getType() != Material.TRIAL_KEY) return false;
        if (!item.hasItemMeta()) return false;

        ItemMeta meta = item.getItemMeta();

        return meta.hasDisplayName() && (ChatColor.GRAY + "Ключи от дома.").equals(meta.getDisplayName());
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent e) throws SQLException {
        if (!(e.getRightClicked() instanceof ItemFrame)) return;

        ItemFrame frame = (ItemFrame) e.getRightClicked();
        ItemStack inside = frame.getItem();

        if (inside == null) return;

        ItemMeta meta = inside.getItemMeta();
        Player player = e.getPlayer();

        if(plugin.db.getFlag(player.getUniqueId()) != null && !plugin.db.getFlag(player.getUniqueId()).equals("inPrologue")) return;

        e.setCancelled(true);

        if (inside.getType() == Material.TRIAL_KEY && meta != null) {
            if(!playersTookKeys.contains(player.getUniqueId()) && playersDressed.contains(player.getUniqueId())){
                if(playersFromStore.contains(player.getUniqueId())) return;

                ItemStack give = new ItemStack(Material.TRIAL_KEY, 1);
                ItemMeta gm = give.getItemMeta();

                if (gm != null) {
                    gm.setDisplayName(ChatColor.GRAY + "Ключи от дома.");
                    give.setItemMeta(gm);
                }

                player.getInventory().addItem(give);
                playersTookKeys.add(player.getUniqueId());
            } else if(!playersDressed.contains(player.getUniqueId())){
                player.sendMessage(ChatColor.RED + "Мне сначала нужно одеться.");
            } else if(playersTookKeys.contains(player.getUniqueId()) && !playersFromStore.contains(player.getUniqueId())){
                player.sendMessage(ChatColor.RED + "Я уже взял ключи.");
            }

            if (playersFromStore.contains(player.getUniqueId())) {
                ItemStack keyInHand = null;
                ItemStack keyInInventory = null;

                if (isHouseKey(player.getInventory().getItemInMainHand())) {
                    keyInHand = player.getInventory().getItemInMainHand();
                } else if (isHouseKey(player.getInventory().getItemInOffHand())) {
                    keyInHand = player.getInventory().getItemInOffHand();
                }

                if (keyInHand == null) {
                    for (ItemStack item : player.getInventory().getContents()) {
                        if (isHouseKey(item)) {
                            keyInInventory = item;
                            break;
                        }
                    }

                    if (keyInInventory != null) {
                        player.sendMessage(ChatColor.GRAY + "Возьмите ключи в руки.");
                        e.setCancelled(true);
                        return;
                    } else {
                        return;
                    }
                }

                player.getInventory().remove(keyInHand);
                playersTookKeys.remove(player.getUniqueId());
            }

            e.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) throws SQLException {
        Player player = e.getPlayer();

        if(plugin.db.getFlag(player.getUniqueId()) != null && !plugin.db.getFlag(player.getUniqueId()).equals("inPrologue")) return;

        if(playersInteractClothes.contains(player.getUniqueId())) e.setCancelled(true);
        if(playersBuying.contains(player.getUniqueId())) e.setCancelled(true);
        if(playersNearFridge.contains(player.getUniqueId())) e.setCancelled(true);

        points.add(new Location(world, -8727, 79, -1023));
        points.add(new Location(world, -8715, 79, -1088));
        points.add(new Location(world, -8621, 79, -1092));
        points.add(new Location(world, -8538, 79, -1087));
        points.add(storeLocation);

        if(player.getLocation().distance(enterLocation) <= 1 && (playersToGoOutside.contains(player.getUniqueId()) && !playersLeavedHome.contains(player.getUniqueId()))) {
            Utils.sendDelayedMessage(player, ChatColor.GOLD + "- " + ChatColor.GRAY + "Благо круглосуточный магазин, где можно купить продукты, находился недалеко от меня.", 0, voice_14);
            playersLeavedHome.add(player.getUniqueId());
            Utils.runTaskLaterStory(player, () -> player.playSound(player.getLocation(), ambientMusic, SoundCategory.AMBIENT, 1L, 1L), 5 * 20L);

            Utils.startArrow(player, points);
            return;
        }

        if((player.getLocation().distance(storeLocation) <= 3 || player.getLocation().distance(storeSecondLocation) <= 3) && !playersAtStore.contains(player.getUniqueId())){
            if(playersFromStore.contains(player.getUniqueId())) return;

            Utils.runTaskLaterStory(player, () -> player.stopSound(ambientMusic, SoundCategory.AMBIENT), 0L);

            Utils.sendDelayedMessage(player, ChatColor.GOLD + "- " + ChatColor.GRAY + "Надо взять корзину, в руках не унесу.", 0, voice_15);
            playersAtStore.add(player.getUniqueId());
            playersToGoOutside.remove(player.getUniqueId());
            playersLeavedHome.remove(player.getUniqueId());
            Utils.runTaskLaterStory(player, () -> Utils.stopArrow(player), 3L);
        }

        if(player.getLocation().distance(enterLocation) <= 4 && (playersFromStore.contains(player.getUniqueId()) && !playersNearEnter.contains(player.getUniqueId()))){
            Utils.runTaskLaterStory(player, () -> Utils.stopArrow(player), 0L);
            Utils.sendDelayedMessage(player, ChatColor.GOLD + "* " + ChatColor.GRAY + "Поднимайтесь на 14 этаж.", 0);
            playersNearEnter.add(player.getUniqueId());
            Utils.runTaskLaterStory(player, () -> player.stopSound(ambientMusic, SoundCategory.AMBIENT), 0L);
        }

        if(player.getLocation().distance(homeDoor) <= 1 && (!playersEnteredHome.contains(player.getUniqueId()) && playersFromStore.contains(player.getUniqueId()))){
            Utils.sendDelayedMessage(player, ChatColor.GOLD + "* " + ChatColor.GRAY + "Поставьте ключи на место и отнесите продукты в холодильник.", 0, Sound.BLOCK_NOTE_BLOCK_PLING);
            playersEnteredHome.add(player.getUniqueId());
        }
    }

    public void finishPrologue(Player player){
        Utils.runTaskLaterStory(player, () -> {
            if (!player.isOnline()) {
                return;
            }

            try {
                plugin.db.updateFlag(player.getUniqueId(), "nextLevel");
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }

            player.teleport(new Location(player.getWorld(), -159, 71, -71));
            PlayerInventoryInteractions.restoreInventory(player);
            plugin.getNavigator().updateCompass(player);
            player.resetPlayerTime();
            playersToGoOutside.remove(player.getUniqueId());
            playersLeavedHome.remove(player.getUniqueId());
            playersAtStore.remove(player.getUniqueId());
            playersFromStore.remove(player.getUniqueId());
            playersDressed.remove(player.getUniqueId());
            playersTookKeys.remove(player.getUniqueId());
            playersTookBasket.remove(player.getUniqueId());
            playersNearEnter.remove(player.getUniqueId());
            playerBaskets.remove(player);
            playersInteractClothes.remove(player.getUniqueId());
            playersBuying.remove(player.getUniqueId());
            playersNearFridge.remove(player.getUniqueId());
            Utils.stopArrow(player);
            authenticated.remove(player.getUniqueId());
            resourcePackLoaded.remove(player.getUniqueId());
            SitListener.unseatPlayer(player);
            player.stopSound(ambientMusic);
            playerBaskets.remove(player);
            clickedItems.remove(player.getUniqueId());
            packedItems.remove(player.getUniqueId());
            fridgeItems.remove(player.getUniqueId());
            finishedFridge.remove(player.getUniqueId());
            playersEnteredHome.remove(player.getUniqueId());
            player.resetPlayerWeather();

            for(Player p : Bukkit.getOnlinePlayers()) {
                p.showPlayer(plugin, player);
                player.showPlayer(plugin, p);
            }
        }, 0L);
    }
}
