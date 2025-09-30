package ru.hofwq.storySystem.storyline.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.hofwq.storySystem.storyline.events.EventListener;

public class FridgeHandler implements CommandExecutor {

    private final EventListener eventListener;

    public FridgeHandler(EventListener listener) {
        this.eventListener = listener;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;

        Player player = (Player) sender;

        if(!eventListener.playersNearFridge.contains(player.getUniqueId())) return true;

        if (args.length == 0) {
            return true;
        }

        String itemName = String.join(" ", args);
        eventListener.handleFridgeClick(player, itemName);

        return true;
    }
}
