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

package fr.florianpal.fmessage.velocity;

import co.aikar.commands.MessageType;
import co.aikar.commands.VelocityCommandManager;
import com.velocitypowered.api.proxy.ProxyServer;
import fr.florianpal.fmessage.core.FMessageCore;
import fr.florianpal.fmessage.core.acf.AcfLanguageLoader;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.IOException;
import java.util.Locale;

/**
 * Mirror of {@code BungeeCommandManagerImpl}: only the colour formats differ, because
 * {@code setFormat} is typed against the platform's colour class.
 */
public class VelocityCommandManagerImpl extends VelocityCommandManager {

    public VelocityCommandManagerImpl(ProxyServer proxyServer, Object plugin, FMessageCore core) {
        super(proxyServer, plugin);
        this.enableUnstableAPI("help");

        this.setFormat(MessageType.SYNTAX, NamedTextColor.YELLOW, NamedTextColor.GOLD);
        this.setFormat(MessageType.INFO, NamedTextColor.YELLOW, NamedTextColor.GOLD);
        this.setFormat(MessageType.HELP, NamedTextColor.YELLOW, NamedTextColor.GOLD, NamedTextColor.RED);
        this.setFormat(MessageType.ERROR, NamedTextColor.RED, NamedTextColor.GOLD);

        try {
            AcfLanguageLoader.load(this, core.getPlatform().getDataFolder(),
                    core.languageFileName(), Locale.FRENCH);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.getLocales().setDefaultLocale(Locale.FRENCH);
    }
}
