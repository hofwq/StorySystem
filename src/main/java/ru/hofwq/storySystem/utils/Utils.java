package ru.hofwq.storySystem.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import ru.hofwq.storySystem.StorySystem;

import java.util.*;

public class Utils {
    public static StorySystem plugin = StorySystem.getPlugin();
    public static Map<UUID, ArrowDirection> arrowTasks = new HashMap<>();

    public static final Map<UUID, List<Integer>> playerTasks = new HashMap<>();

    public static void runTaskLaterStory(Player player, Runnable task, long delayTicks) {
        int taskId = Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks).getTaskId();
        playerTasks.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>()).add(taskId);
    }

    public static void removeFromListAfter(Player player, int delaySeconds, List<? extends UUID> list) {
        int delayTicks = delaySeconds * 20;
        Bukkit.getScheduler().runTaskLater(plugin, () -> list.remove(player.getUniqueId()), delayTicks);
    }

    public static void removeFromListAfter(Player player, String message, int delaySeconds, List<? extends UUID> list) {
        int delayTicks = delaySeconds * 20;
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (list.contains(player.getUniqueId())) {
                list.remove(player.getUniqueId());
                player.sendMessage(message.replace("&", "§"));
            }
        }, delayTicks);
    }

    public static void sendDelayedMessage(Player player, String message, int delaySeconds) {
        int delayTicks = delaySeconds * 20;

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    this.cancel();
                    return;
                }

                player.sendMessage(message.replace("&", "§"));
            }
        }.runTaskLater(plugin, delayTicks);
    }

    public static void sendDelayedMessage(Player player, String message, int delaySeconds, String sound) {
        int delayTicks = delaySeconds * 20;

        runTaskLaterStory(player, () -> {
            if (!player.isOnline()) return;

            player.playSound(player.getLocation(), sound, SoundCategory.AMBIENT, 1f, 1f);
            player.sendMessage(message.replace("&", "§"));
        }, delayTicks);
    }

    /**
     * @param sound Main method with sound.
     **/
    public static void sendDelayedMessage(Player player, String message, int delaySeconds, Sound sound) {
        int delayTicks = delaySeconds * 20;

        runTaskLaterStory(player, () -> {
            if (!player.isOnline()) return;

            player.playSound(player.getLocation(), sound, SoundCategory.AMBIENT, 1L, 1L);
            player.sendMessage(message.replace("&", "§"));
        }, delayTicks);
    }

    public static void startArrow(Player player, List<Location> points) {
        stopArrow(player);
        ArrowDirection task = new ArrowDirection(player, points);
        arrowTasks.put(player.getUniqueId(), task);
        task.runTaskTimer(plugin, 0L, 1L);
    }

    public static void stopArrow(Player player) {
        ArrowDirection task = arrowTasks.remove(player.getUniqueId());
        if (task != null) task.stop();
    }
}
