package ru.hofwq.storySystem.config;

import java.io.File;
import java.io.IOException;
import java.util.EventListener;
import java.util.List;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.hofwq.storySystem.StorySystem;

public class Config implements EventListener{
    public static StorySystem plugin = StorySystem.getPlugin();
    private final FileConfiguration cfg = plugin.getConfig();
    private File dataFile;
    private YamlConfiguration yml;

    public Config(StorySystem plugin) {
        this.plugin = plugin;
    }

    public void checkConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        ensureBoatDefaults(1, 0, 0);
        ensureBoatDefaults(2, 0, 0);
        ensureBoatDefaults(3, 0, 0);
        ensureBoatDefaults(4, 0, 0);

        plugin.saveConfig();
    }

    private void ensureBoatDefaults(int id, int defX, int defZ) {
        String path = "boat_" + id;
        if (!cfg.contains(path + ".x")) {
            cfg.set(path + ".x", defX);
            cfg.set(path + ".z", defZ);
        }
    }

    public void setup() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        dataFile = new File(plugin.getDataFolder(), "playerData.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create playerData.yml");
                e.printStackTrace();
            }
        }

        yml = YamlConfiguration.loadConfiguration(dataFile);
    }

    public void save() {
        try {
            yml.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Error saving playerData.yml");
            e.printStackTrace();
        }
    }

    public List<String> getQuestStatus(UUID uuid) {
        return yml.getStringList(uuid.toString());
    }

    public void setQuestStatus(UUID uuid, String status) {
        List<String> statuses = yml.getStringList(uuid.toString());
        if (!statuses.contains(status)) {
            statuses.add(status);
            yml.set(uuid.toString(), statuses);
            save();
        }
    }

    public void removeQuestStatus(UUID uuid, String status) {
        List<String> statuses = yml.getStringList(uuid.toString());
        if (statuses.remove(status)) {
            yml.set(uuid.toString(), statuses);
            save();
        }
    }
}
