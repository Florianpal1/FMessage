
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

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import fr.florianpal.fmessage.configurations.ChatConfig;
import fr.florianpal.fmessage.configurations.DatabaseConfig;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class ConfigurationManager {
    private final DatabaseConfig database = new DatabaseConfig();

    private final ChatConfig chat = new ChatConfig();

    public ConfigurationManager(File dataFolder) {

        try {
            YamlDocument databaseConfig = YamlDocument.create(new File(dataFolder, "database.yml"),
                    Objects.requireNonNull(getClass().getResourceAsStream("/database.yml")),
                    GeneralSettings.DEFAULT,
                    LoaderSettings.builder().setAutoUpdate(true).build(),
                    DumperSettings.DEFAULT,
                    UpdaterSettings.builder().setVersioning(new BasicVersioning("version")).setOptionSorting(UpdaterSettings.DEFAULT_OPTION_SORTING).build()
            );

            YamlDocument chatConfig = YamlDocument.create(new File(dataFolder, "config.yml"),
                    Objects.requireNonNull(getClass().getResourceAsStream("/config.yml")),
                    GeneralSettings.DEFAULT,
                    LoaderSettings.builder().setAutoUpdate(true).build(),
                    DumperSettings.DEFAULT,
                    UpdaterSettings.builder().setVersioning(new BasicVersioning("version")).setOptionSorting(UpdaterSettings.DEFAULT_OPTION_SORTING).build()
            );


            chat.load(chatConfig);
            database.load(databaseConfig);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ChatConfig getChat() {
        return chat;
    }

    public DatabaseConfig getDatabase() {
        return database;
    }
}
