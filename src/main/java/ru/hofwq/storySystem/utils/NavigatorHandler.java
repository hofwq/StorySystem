package ru.hofwq.storySystem.utils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import ru.hofwq.storySystem.StorySystem;
import ru.hofwq.storySystem.config.Config;

public class NavigatorHandler implements Listener {
    private final StorySystem plugin;
    private final Config cfg;

    private final NamespacedKey compassKey;
    private final NamespacedKey statusKey;

    public NavigatorHandler(StorySystem plugin, Config cfg) {
        this.plugin = plugin;
        this.cfg = cfg;
        this.compassKey = new NamespacedKey(plugin, "quest_compass");
        this.statusKey = new NamespacedKey(plugin, "quest_status");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) throws SQLException {
        Player p = e.getPlayer();

        Bukkit.getScheduler().runTaskLater(plugin, () -> updateCompass(p), 2L);
    }

    private void giveCompassIfAbsent(Player p) {
        for (ItemStack is : p.getInventory().getContents()) {
            if (is == null || !is.hasItemMeta()) continue;

            if (is.getItemMeta()
                    .getPersistentDataContainer()
                    .has(compassKey, PersistentDataType.BYTE)) {
                return;
            }
        }
        p.getInventory().addItem(createCompassFor(p));
    }

    private ItemStack createCompassFor(Player p) {
        UUID uuid = p.getUniqueId();
        List<String> statuses = cfg.getQuestStatus(uuid);
        String status = statuses.isEmpty()
                ? ""
                : statuses.get(statuses.size() - 1);

        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();

        meta.getPersistentDataContainer().set(
                compassKey, PersistentDataType.BYTE, (byte) 1);

        meta.setDisplayName(
                ChatColor.DARK_RED + "✯ "
                        + ChatColor.RED + "Компас Пионера"
                        + ChatColor.DARK_RED + " ✯"
        );

        meta.setLore(buildLore(status));
        meta.getPersistentDataContainer().set(
                statusKey, PersistentDataType.STRING, status);

        compass.setItemMeta(meta);
        return compass;
    }

    public void updateCompass(Player p) {
        UUID uuid = p.getUniqueId();
        List<String> statuses = cfg.getQuestStatus(uuid);

        String newStatus = statuses.isEmpty() ? "" : statuses.get(statuses.size() - 1);

        giveCompassIfAbsent(p);

        Location target = determineTarget(newStatus);
        p.setCompassTarget(target);

        for (ItemStack item : p.getInventory().getContents()) {
            if (item == null || !item.hasItemMeta()) continue;

            ItemMeta meta = item.getItemMeta();
            if (!meta.getPersistentDataContainer()
                    .has(compassKey, PersistentDataType.BYTE)) {
                continue;
            }

            String oldStatus = meta.getPersistentDataContainer()
                    .get(statusKey, PersistentDataType.STRING);
            oldStatus = (oldStatus == null ? "" : oldStatus);

            if (!oldStatus.equals(newStatus)) {
                meta.setLore(buildLore(newStatus));
                meta.getPersistentDataContainer()
                        .set(statusKey, PersistentDataType.STRING, newStatus);
                item.setItemMeta(meta);
            }
        }
    }

    private Location determineTarget(String status) {
        World world = Bukkit.getWorld("world");
        return switch (status) {
            case "OlgaDone" -> new Location(world, 87, 69, -134);
            case "TOISLAND" -> new Location(world, 62, 65, 139);
            case "BACKTOOLGA" -> new Location(world, 103, 69, -132);
            default -> new Location(world, 103, 69, -132);
        };
    }

    private List<String> buildLore(String status) {
        List<String> lore = new ArrayList<>();
        lore.add("");
        switch (status) {
            case "OlgaDone":
                lore.add("§4⚑ §cЦель: §eПлощадь");
                break;
            case "TOISLAND":
                lore.add("§4⚑ §cЦель: §eДобраться до острова с ягодами");
                break;
            case "BACKTOOLGA":
                lore.add("§4⚑ §cЦель: §eВернуться к Ольге Дмитриевне");
                break;
            default:
                lore.add("§4⚑ §cЦель: §eЗадание от Ольги Дмитриевны");
        }
        return lore;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent e) {
        if (isQuestCompass(e.getItemDrop().getItemStack())) {
            e.setCancelled(true);
            e.getItemDrop().remove();
            final Player p = e.getPlayer();
            plugin.getServer().getScheduler().runTaskLater(plugin, p::updateInventory, 1L);
        }
    }

    private boolean isQuestCompass(ItemStack is) {
        if (is == null || !is.hasItemMeta()) return false;
        return is.getItemMeta()
                .getPersistentDataContainer()
                .has(compassKey, PersistentDataType.BYTE);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent e) {
        Player p = (e.getWhoClicked() instanceof Player) ? (Player)e.getWhoClicked() : null;
        if (p == null) return;

        ItemStack current = e.getCurrentItem();
        ItemStack cursor = e.getCursor();
        if (!isQuestCompass(current) && !isQuestCompass(cursor)) return;

        InventoryAction action = e.getAction();

        if (action == InventoryAction.DROP_ONE_SLOT
                || action == InventoryAction.DROP_ALL_SLOT) {
            e.setCancelled(true);
            scheduleUpdate(p);
            return;
        }

        if (e.isShiftClick()
                || action == InventoryAction.MOVE_TO_OTHER_INVENTORY
                || action == InventoryAction.HOTBAR_SWAP
                || action == InventoryAction.HOTBAR_MOVE_AND_READD) {
            e.setCancelled(true);
            scheduleUpdate(p);
            return;
        }

        int topSize = e.getView().getTopInventory().getSize();
        if (e.getRawSlot() < topSize) {
            e.setCancelled(true);
            scheduleUpdate(p);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent e) {
        Player p = (e.getWhoClicked() instanceof Player) ? (Player)e.getWhoClicked() : null;
        if (p == null) return;

        ItemStack cursor = e.getOldCursor();
        if (!isQuestCompass(cursor)) return;

        InventoryView view = e.getView();
        if (view.getTopInventory().getType() == InventoryType.CRAFTING) return;

        int topSize = view.getTopInventory().getSize();
        for (int rawSlot : e.getRawSlots()) {
            if (rawSlot < topSize) {
                e.setCancelled(true);
                scheduleUpdate(p);
                return;
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onCreativeDrop(InventoryCreativeEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;

        ItemStack cursor = e.getCursor();
        ItemStack current = e.getCurrentItem();

        if (!isQuestCompass(cursor) && !isQuestCompass(current)) return;

        if (e.getClickedInventory() == null) {
            e.setCancelled(true);
            plugin.getServer().getScheduler().runTaskLater(plugin, p::updateInventory, 1L);
            return;
        }

        if (e.getClickedInventory() instanceof PlayerInventory) {
            return;
        }

        e.setCancelled(true);
        scheduleUpdate(p);
    }


    private void scheduleUpdate(Player p) {
        plugin.getServer().getScheduler().runTaskLater(plugin, p::updateInventory, 1L);
    }

    @EventHandler
    public void onInventoryMoveItem(InventoryMoveItemEvent e) {
        if (isQuestCompass(e.getItem())) {
            e.setCancelled(true);
        }
    }
}
