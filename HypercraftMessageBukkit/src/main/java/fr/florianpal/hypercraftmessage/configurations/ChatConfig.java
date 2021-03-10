package fr.florianpal.hypercraftmessage.configurations;

import org.bukkit.configuration.Configuration;

public class ChatConfig {
    private String chatFormat;
    private String ignoreFormat;
    public void load(Configuration config) {
        chatFormat = config.getString("chatFormat");
        ignoreFormat = config.getString("ignoreFormat");
    }

    public String getChatFormat() {
        return chatFormat;
    }

    public String getIgnoreFormat() {
        return ignoreFormat;
    }
}
