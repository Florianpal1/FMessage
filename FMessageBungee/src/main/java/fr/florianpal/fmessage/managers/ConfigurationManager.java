
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
 * Last modification : 20/10/2021 19:57
 *
 *  @author Florianpal.
 */

package fr.florianpal.fmessage.managers;

import fr.florianpal.fmessage.FMessage;
import fr.florianpal.fmessage.configurations.ChatConfig;
import fr.florianpal.fmessage.configurations.DatabaseConfig;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigurationManager {
    private FMessage core;

    private ChatConfig chat = new ChatConfig();
    private DatabaseConfig database = new DatabaseConfig();
    private File File;
    private Configuration config;

    private File langFile;
    private Configuration langConfig;

    public ConfigurationManager(FMessage core) {
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
