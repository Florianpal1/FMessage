

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
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.IOException;
import java.util.Locale;

public class CommandManager extends BungeeCommandManager {
    public CommandManager(Plugin plugin) {
        super(plugin);
        this.enableUnstableAPI("help");

        this.setFormat(MessageType.SYNTAX, ChatColor.YELLOW, ChatColor.GOLD);
        this.setFormat(MessageType.INFO, ChatColor.YELLOW, ChatColor.GOLD);
        this.setFormat(MessageType.HELP, ChatColor.YELLOW, ChatColor.GOLD, ChatColor.RED);
        this.setFormat(MessageType.ERROR, ChatColor.RED, ChatColor.GOLD);
        try {
            this.getLocales().loadYamlLanguageFile("lang_fr.yml", Locale.FRENCH);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to load ACF core language file");
            e.printStackTrace();
        }

        this.getLocales().setDefaultLocale(Locale.FRENCH);
    }
}
