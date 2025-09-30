package ru.hofwq.storySystem.storyline.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.hofwq.storySystem.StorySystem;

import java.sql.SQLException;

public class SetPlayerStoryFlag implements CommandExecutor {
    public static StorySystem plugin = StorySystem.getPlugin();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("StorySystem.setstoryflag")) {
            sender.sendMessage(ChatColor.RED + "У вас нет прав использовать эту команду!");
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage(ChatColor.RED + "Использование: /setstoryflag <Player> <Flag>");
            return true;
        }

        String targetName = args[0];
        Player target = plugin.getServer().getPlayerExact(targetName);

        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Игрок " + targetName + " не найден.");
            return true;
        }

        String flag = args[1];

        try {
            plugin.db.updateFlag(target.getUniqueId(), flag);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        target.kickPlayer("Успешно выставлен флаг: " + flag + " перезайдите на сервер.");

        return true;
    }
}
