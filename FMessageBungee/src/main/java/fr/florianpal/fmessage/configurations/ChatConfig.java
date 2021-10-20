package fr.florianpal.fmessage.configurations;

import net.md_5.bungee.config.Configuration;

public class ChatConfig {
    private String targetChatFormat;
    private String senderChatFormat;
    private String spyChatFormat;
    public void load(Configuration config) {
        targetChatFormat = config.getString("format.targetChatFormat");
        senderChatFormat = config.getString("format.senderChatFormat");
        spyChatFormat = config.getString("format.spyChatFormat");
    }

    public String getTargetChatFormat() {
        return targetChatFormat;
    }

    public String getSenderChatFormat() {
        return senderChatFormat;
    }

    public String getSpyChatFormat() {
        return spyChatFormat;
    }
}
