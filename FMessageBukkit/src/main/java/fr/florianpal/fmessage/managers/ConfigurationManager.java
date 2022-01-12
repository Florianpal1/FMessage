

/*
 * Copyright (C) 2022 Florianpal
 *
 * This program is free software;
 * you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, see <http://www.gnu.org/licenses/>.
 *
 * Last modification : 07/01/2022 23:05
 *
 *  @author Florianpal.
 */

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
