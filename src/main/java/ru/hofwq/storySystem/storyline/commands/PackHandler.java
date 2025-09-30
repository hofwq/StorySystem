package ru.hofwq.storySystem.storyline.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.hofwq.storySystem.storyline.events.EventListener;

public class PackHandler implements CommandExecutor {
    private final EventListener eventListener;

    public PackHandler(EventListener listener) {
        this.eventListener = listener;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;

        Player player = (Player) sender;

        if(!eventListener.playersAtStore.contains(player.getUniqueId())) return true;

        if (args.length == 0) {
            return true;
        }

        String itemName = String.join(" ", args);
        eventListener.handlePackClick(player, itemName);

        return true;
    }
}
