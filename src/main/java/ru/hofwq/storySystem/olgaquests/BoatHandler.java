package ru.hofwq.storySystem.olgaquests;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import ru.hofwq.storySystem.StorySystem;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BoatHandler {
    private final StorySystem plugin;
    private final World world;

    private Boat boat1, boat2, boat3, boat4;

    public BoatHandler(StorySystem plugin) {
        this.plugin = plugin;
        this.world = Bukkit.getWorld(plugin.getConfig().getString("world", "world"));
    }

    private List<Location> getBoatLocations() {
        List<Location> locations = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            int x = plugin.getConfig().getInt("boat_" + i + ".x");
            int z = plugin.getConfig().getInt("boat_" + i + ".z");
            int y = (world != null)
                    ? world.getHighestBlockYAt(x, z) + 1
                    : plugin.getConfig().getInt("boat_" + i + ".y", 64);
            if (world != null) {
                locations.add(new Location(world, x, y + 0.5, z));
            }
        }
        return locations;
    }

    public void loadBoatChunks() {
        if (world == null) {
            plugin.getLogger().warning("Cannot find world to load boat chunks.");
            return;
        }
        for (Location loc : getBoatLocations()) {
            loc.getChunk().setForceLoaded(true);
        }
    }

    public void spawnAllBoats() {
        if (world == null) {
            plugin.getLogger().warning("Cannot find world to spawn boats.");
            return;
        }
        List<Location> locs = getBoatLocations();
        boat1 = spawnBoatAt(locs.get(0), "1");
        boat2 = spawnBoatAt(locs.get(1), "2");
        boat3 = spawnBoatAt(locs.get(2), "3");
        boat4 = spawnBoatAt(locs.get(3), "4");
    }

    public void checkBoats() {
        if (world == null) return;
        List<Location> locs = getBoatLocations();
        boat1 = ensureBoat(boat1, locs.get(0), "1");
        boat2 = ensureBoat(boat2, locs.get(1), "2");
        boat3 = ensureBoat(boat3, locs.get(2), "3");
        boat4 = ensureBoat(boat4, locs.get(3), "4");
    }

    private Boat ensureBoat(Boat boat, Location target, String id) {
        if (boat == null || boat.isDead()) {
            return spawnBoatAt(target, id);
        }
        if (!isBoatOccupied(boat)) {
            double dist = boat.getLocation().distance(target);
            if (dist > 2.0) {
                boat.remove();
                return spawnBoatAt(target, id);
            }
        }
        return boat;
    }

    private boolean isBoatOccupied(Boat boat) {
        for (Entity e : boat.getPassengers()) {
            if (e instanceof Player) {
                return true;
            }
        }
        return false;
    }

    private Boat spawnBoatAt(Location loc, String id) {
        for (Entity ent : loc.getWorld().getNearbyEntities(loc, 2, 2, 2)) {
            if (ent instanceof Boat) {
                ent.remove();
            }
        }

        Boat b = (Boat) loc.getWorld().spawnEntity(loc, EntityType.OAK_BOAT);
        b.setMetadata("questBoat", new FixedMetadataValue(plugin, id));
        b.setInvulnerable(true);
        return b;
    }

    public List<Boat> getBoats() {
        return Stream.of(boat1, boat2, boat3, boat4)
                .filter(b -> b != null && !b.isDead())
                .collect(Collectors.toList());
    }
}