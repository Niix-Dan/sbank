package com.spearforge.sBank.config;

import com.spearforge.sBank.SBank;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class CustomFileConfiguration {

    private final SBank plugin;
    private File configFile;
    @Getter
    private FileConfiguration config;

    public CustomFileConfiguration(SBank plugin, String fileName) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), fileName);
        this.config = YamlConfiguration.loadConfiguration(configFile);
    }

    public void createConfig() {
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            plugin.saveResource(configFile.getName(), false);
        }

        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public void set(String path, Object value) {
        config.set(path, value);
    }

    public Object get(String path) {
        return config.get(path);
    }

    public String getString(String path){
        return config.getString(path);
    }

    public List<String> getStringList(String path){
        return config.getStringList(path);
    }
}
