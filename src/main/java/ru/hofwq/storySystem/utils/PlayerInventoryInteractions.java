package ru.hofwq.storySystem.utils;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;

public class PlayerInventoryInteractions {
    public static final HashMap<UUID, ItemStack[]> storedInventories = new HashMap<>();
    public static final HashMap<UUID, ItemStack[]> storedArmor = new HashMap<>();

    public static void saveInventory(Player player) {
        UUID uuid = player.getUniqueId();
        storedInventories.put(uuid, player.getInventory().getContents());
        storedArmor.put(uuid, player.getInventory().getArmorContents());
    }

    public static void restoreInventory(Player player) {
        UUID uuid = player.getUniqueId();

        ItemStack[] contents = storedInventories.get(uuid);
        ItemStack[] armor = storedArmor.get(uuid);

        if (contents != null) {
            player.getInventory().setContents(contents);
        } else {
            player.getInventory().clear();
        }

        if (armor != null) {
            player.getInventory().setArmorContents(armor);
        } else {
            player.getInventory().setHelmet(null);
            player.getInventory().setChestplate(null);
            player.getInventory().setLeggings(null);
            player.getInventory().setBoots(null);
        }

        player.updateInventory();
    }
}
