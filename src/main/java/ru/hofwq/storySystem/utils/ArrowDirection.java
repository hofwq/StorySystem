package ru.hofwq.storySystem.utils;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.LinkedList;
import java.util.List;

public class ArrowDirection extends BukkitRunnable {
    private final Player player;
    private final List<Location> waypoints = new LinkedList<>();
    private final Location finalTarget;
    private static final double REACH_DISTANCE = 7;

    public ArrowDirection(Player player, List<Location> points) {
        this.player = player;
        this.waypoints.addAll(points);
        this.finalTarget = points.get(points.size() - 1).clone();
        this.finalTarget.setY(player.getLocation().getY());
    }

    @Override
    public void run() {
        if (!player.isOnline()) {
            this.cancel();
            return;
        }

        if (waypoints.isEmpty()) {
            this.cancel();
            return;
        }

        Location p = player.getLocation();
        Location currentTarget = waypoints.get(0).clone();
        currentTarget.setY(p.getY());

        boolean reached = p.distance(currentTarget) <= REACH_DISTANCE;

        if (!reached && waypoints.size() > 1) {
            Location nextTarget = waypoints.get(1).clone();
            nextTarget.setY(p.getY());

            if (p.distance(nextTarget) < p.distance(currentTarget)) {
                reached = true;
            }
        }

        if (reached) {
            waypoints.remove(0);
            if (waypoints.isEmpty()) {
                this.cancel();
                return;
            }
            currentTarget = waypoints.get(0).clone();
            currentTarget.setY(p.getY());
        }

        Vector toTarget = currentTarget.toVector().subtract(p.toVector());
        toTarget.setY(0).normalize();

        Vector forward = p.getDirection().setY(0).normalize();

        String arrow = "⬆";
        if (toTarget.lengthSquared() != 0 && forward.lengthSquared() != 0) {
            double angle = Math.toDegrees(Math.atan2(
                    forward.getX() * toTarget.getZ() - forward.getZ() * toTarget.getX(),
                    forward.dot(toTarget)
            ));

            if (angle >= -22.5 && angle < 22.5) arrow = "⬆";
            else if (angle >= 22.5 && angle < 67.5) arrow = "⬈";
            else if (angle >= 67.5 && angle < 112.5) arrow = "➡";
            else if (angle >= 112.5 && angle < 157.5) arrow = "⬊";
            else if (angle >= 157.5 || angle < -157.5) arrow = "⬇";
            else if (angle >= -157.5 && angle < -112.5) arrow = "⬋";
            else if (angle >= -112.5 && angle < -67.5) arrow = "⬅";
            else arrow = "⬉";
        }

        double dx = finalTarget.getX() - p.getX();
        double dz = finalTarget.getZ() - p.getZ();
        int dist = (int) Math.round(Math.hypot(dx, dz));

        TextComponent open = new TextComponent("◆ ");
        open.setColor(net.md_5.bungee.api.ChatColor.DARK_GRAY);

        TextComponent distComp = new TextComponent(dist - 2 + "м");
        distComp.setColor(net.md_5.bungee.api.ChatColor.GOLD);
        distComp.setBold(true);

        TextComponent arrowComp = new TextComponent(" " + arrow + " ");
        arrowComp.setColor(net.md_5.bungee.api.ChatColor.GREEN);
        arrowComp.setBold(true);

        TextComponent close = new TextComponent("◆");
        close.setColor(net.md_5.bungee.api.ChatColor.DARK_GRAY);

        player.spigot().sendMessage(
                ChatMessageType.ACTION_BAR,
                new net.md_5.bungee.api.chat.BaseComponent[]{open, distComp, arrowComp, close}
        );
    }

    public void stop() {
        this.cancel();
    }
}