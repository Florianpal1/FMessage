package fr.florianpal.fmessage.managers;

import fr.florianpal.fmessage.FMessage;
import fr.florianpal.fmessage.configurations.ChatConfig;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class ConfigurationManager {
    private final FMessage core;

    private final ChatConfig chat = new ChatConfig();
    private final File chatFile;
    private final FileConfiguration chatConfig;

    public ConfigurationManager(FMessage core) {
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
