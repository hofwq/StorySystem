package ru.hofwq.storySystem.storyline.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.hofwq.storySystem.storyline.events.EventListener;

public class BasketHandler implements CommandExecutor {
    private final EventListener eventListener;

    public BasketHandler(EventListener listener) {
        this.eventListener = listener;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        if(!eventListener.playersAtStore.contains(player.getUniqueId())) return true;

        if (args.length == 0) {
            return true;
        }

        String itemName = String.join(" ", args);

        eventListener.handleBasketClick(player, itemName);
        return true;
    }
}
