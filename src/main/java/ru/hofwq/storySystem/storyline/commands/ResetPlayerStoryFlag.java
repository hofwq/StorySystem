package ru.hofwq.storySystem.storyline.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.hofwq.storySystem.StorySystem;

import java.sql.SQLException;

public class ResetPlayerStoryFlag implements CommandExecutor {
    public static StorySystem plugin = StorySystem.getPlugin();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("StorySystem.resetstoryflag")) {
            sender.sendMessage(ChatColor.RED + "У вас нет прав использовать эту команду!");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Использование: /resetstoryflag <Player>");
            return true;
        }

        String targetName = args[0];
        Player target = plugin.getServer().getPlayerExact(targetName);

        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Игрок " + targetName + " не найден.");
            return true;
        }

        try {
            plugin.db.removePlayer(target.getUniqueId());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        target.kickPlayer("Успешный сброс сюжета. Перезайдите на сервер.");

        return true;
    }
}
