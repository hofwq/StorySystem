package ru.hofwq.storySystem.utils;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.Openable;
import org.bukkit.entity.Player;
import ru.hofwq.storySystem.StorySystem;

import java.util.HashMap;

public class OpenDoorUsingProtocol {
    public static StorySystem plugin = StorySystem.getPlugin();

    public static boolean isClosedDoor;

    public static HashMap<Block, Boolean> doorStates = new HashMap<>();

    public static void openDoorForPlayer(Player player, Block door) {
        Openable doorData = (Openable) door.getBlockData();
        if(!doorData.isOpen()) {
            doorData.setOpen(true);
            door.setBlockData(doorData);

            WrappedBlockData wrappedBlockData = WrappedBlockData.createData(door.getBlockData());
            PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.BLOCK_CHANGE);
            packet.getBlockPositionModifier().write(0, new BlockPosition(door.getLocation().toVector()));
            packet.getBlockData().write(0, wrappedBlockData);

            try {
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);

                if (door.getType() == Material.IRON_DOOR) {
                    player.playSound(door.getLocation(), Sound.BLOCK_IRON_DOOR_OPEN, 1L, 1L);
                } else {
                    player.playSound(door.getLocation(), Sound.BLOCK_WOODEN_DOOR_OPEN, 1L, 1L);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void closeDoorForPlayer(Player player, Block door) {
        Openable doorData = (Openable) door.getBlockData();

        if (doorData.isOpen()) {
            doorData.setOpen(false);
            door.setBlockData(doorData);

            WrappedBlockData wrappedBlockData = WrappedBlockData.createData(door.getBlockData());
            PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.BLOCK_CHANGE);
            packet.getBlockPositionModifier().write(0, new BlockPosition(door.getLocation().toVector()));
            packet.getBlockData().write(0, wrappedBlockData);

            try {
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);

                if (door.getType() == Material.IRON_DOOR) {
                    player.playSound(door.getLocation(), Sound.BLOCK_IRON_DOOR_CLOSE, 1L, 1L);
                } else {
                    player.playSound(door.getLocation(), Sound.BLOCK_WOODEN_DOOR_CLOSE, 1L, 1L);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if(!isClosedDoor) {
                plugin.log.info("Closed door using packets for " + player.getName());
                isClosedDoor = true;
            }
        }
    }

    public static void closeDoorAfter(Block clickedBlock, int delaySeconds, Player player) {
        int delayTicks = delaySeconds * 20;

        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                if(clickedBlock.getType() == Material.IRON_DOOR) {
                    closeDoorForPlayer(player, clickedBlock);

                    doorStates.put(clickedBlock, false);
                }
            }
        }, delayTicks);
    }
}
