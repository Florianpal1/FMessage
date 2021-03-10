package fr.florianpal.hypercraftmessage.managers;

import fr.florianpal.hypercraftmessage.HypercraftMessage;
import fr.florianpal.hypercraftmessage.configurations.ChatConfig;
import fr.florianpal.hypercraftmessage.configurations.DatabaseConfig;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigurationManager {
    private HypercraftMessage core;

    private ChatConfig chat = new ChatConfig();
    private DatabaseConfig database = new DatabaseConfig();
    private File File;
    private Configuration config;

    private File langFile;
    private Configuration langConfig;

    public ConfigurationManager(HypercraftMessage core) {
        this.core = core;

        File = new File(this.core.getDataFolder(), "config.yml");
        core.createDefaultConfiguration(File, "config.yml");
        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(File);
        } catch (IOException e) {
            e.printStackTrace();
        }

        chat.load(config);
        database.load(config);
    }

    public ChatConfig getChat() {
        return chat;
    }

    public DatabaseConfig getDatabase() {
        return database;
    }
}
