package fr.florianpal.hypercraftmessage.managers;

import fr.florianpal.hypercraftmessage.HypercraftMessage;
import fr.florianpal.hypercraftmessage.configurations.ChatConfig;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class ConfigurationManager {
    private HypercraftMessage core;

    private ChatConfig chat = new ChatConfig();
    private File chatFile;
    private FileConfiguration chatConfig;

    public ConfigurationManager(HypercraftMessage core) {
        this.core = core;

        chatFile = new File(this.core.getDataFolder(), "config.yml");
        core.createDefaultConfiguration(chatFile, "config.yml");
        chatConfig = YamlConfiguration.loadConfiguration(chatFile);

        chat.load(chatConfig);
    }

    public ChatConfig getChat() {
        return chat;
    }
}
