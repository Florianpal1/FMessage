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
 *  @author Florianpal.
 */

package fr.florianpal.fmessage.bungee;

import co.aikar.commands.BungeeCommandManager;
import co.aikar.commands.MessageType;
import fr.florianpal.fmessage.core.FMessageCore;
import fr.florianpal.fmessage.core.acf.AcfLanguageLoader;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.IOException;
import java.util.Locale;

/**
 * All that is left of the old 92-line {@code CommandManager}: the colour formats, which
 * are typed against {@code ChatColor} and cannot be shared, plus the {@code super} call.
 * The language loading moved to {@link AcfLanguageLoader}.
 */
public class BungeeCommandManagerImpl extends BungeeCommandManager {

    public BungeeCommandManagerImpl(Plugin plugin, FMessageCore core) {
        super(plugin);
        this.enableUnstableAPI("help");

        this.setFormat(MessageType.SYNTAX, ChatColor.YELLOW, ChatColor.GOLD);
        this.setFormat(MessageType.INFO, ChatColor.YELLOW, ChatColor.GOLD);
        this.setFormat(MessageType.HELP, ChatColor.YELLOW, ChatColor.GOLD, ChatColor.RED);
        this.setFormat(MessageType.ERROR, ChatColor.RED, ChatColor.GOLD);

        try {
            AcfLanguageLoader.load(this, core.getPlatform().getDataFolder(),
                    core.languageFileName(), Locale.FRENCH);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.getLocales().setDefaultLocale(Locale.FRENCH);
    }
}
