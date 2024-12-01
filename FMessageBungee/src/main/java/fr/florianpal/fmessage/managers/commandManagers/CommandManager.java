

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

package fr.florianpal.fmessage.managers.commandManagers;

import co.aikar.commands.BungeeCommandManager;
import co.aikar.commands.MessageType;
import co.aikar.locales.MessageKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.route.Route;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import fr.florianpal.fmessage.FMessage;
import net.md_5.bungee.api.ChatColor;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Objects;

public class CommandManager extends BungeeCommandManager {
    public CommandManager(FMessage plugin) {
        super(plugin);
        this.enableUnstableAPI("help");

        this.setFormat(MessageType.SYNTAX, ChatColor.YELLOW, ChatColor.GOLD);
        this.setFormat(MessageType.INFO, ChatColor.YELLOW, ChatColor.GOLD);
        this.setFormat(MessageType.HELP, ChatColor.YELLOW, ChatColor.GOLD, ChatColor.RED);
        this.setFormat(MessageType.ERROR, ChatColor.RED, ChatColor.GOLD);
        try {
            loadYamlLanguageFile(plugin, "lang_" + plugin.getConfigurationManager().getChat().getLang() + ".yml", Locale.FRENCH);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.getLocales().setDefaultLocale(Locale.FRENCH);
    }

    public boolean loadYamlLanguageFile(FMessage plugin, String file, Locale locale) throws IOException {

        YamlDocument yamlConfiguration = YamlDocument.create(new File(plugin.getDataFolder(), file),
                Objects.requireNonNull(getClass().getResourceAsStream("/"+file)),
                GeneralSettings.DEFAULT,
                DumperSettings.DEFAULT
        );
        return loadLanguage(yamlConfiguration, locale);
    }

    public boolean loadLanguage(YamlDocument config, Locale locale) {
        boolean loaded = false;
        for (Object parentKey : config.getKeys()) {
            Section inner = config.getSection(Route.from(parentKey));
            if (inner == null) {
                continue;
            }
            for (Object key : inner.getKeys()) {
                String value = inner.getString(Route.from(key));
                if (value != null && !value.isEmpty()) {
                    this.getLocales().addMessage(locale, MessageKey.of(parentKey + "." + key), value);
                    loaded = true;
                }
            }
        }
        return loaded;
    }

    public void reloadLang() {

        this.getLocales().loadLanguages();
    }
}
