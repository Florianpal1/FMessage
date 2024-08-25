

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

import co.aikar.commands.MessageType;
import co.aikar.commands.VelocityCommandManager;
import com.velocitypowered.api.proxy.ProxyServer;
import fr.florianpal.fmessage.FMessage;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Locale;

public class CommandManager extends VelocityCommandManager {
    public CommandManager(ProxyServer proxyServer, FMessage plugin) {
        super(proxyServer, plugin);
        this.enableUnstableAPI("help");

        this.setFormat(MessageType.SYNTAX, NamedTextColor.YELLOW, NamedTextColor.GOLD);
        this.setFormat(MessageType.INFO, NamedTextColor.YELLOW, NamedTextColor.GOLD);
        this.setFormat(MessageType.HELP, NamedTextColor.YELLOW, NamedTextColor.GOLD, NamedTextColor.RED);
        this.setFormat(MessageType.ERROR, NamedTextColor.RED, NamedTextColor.GOLD);
        this.getLocales().loadLanguages();

        this.getLocales().setDefaultLocale(Locale.FRENCH);
    }

    public void reloadLang() {

        this.getLocales().loadLanguages();
    }
}
