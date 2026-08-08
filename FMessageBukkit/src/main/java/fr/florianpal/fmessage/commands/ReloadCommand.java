
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

package fr.florianpal.fmessage.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import fr.florianpal.fmessage.FMessage;
import fr.florianpal.fmessage.languages.MessageKeys;
import org.bukkit.command.CommandSender;

@CommandAlias("fmessage|fmessagebukkit")
public class ReloadCommand extends BaseCommand {

    private final FMessage plugin;

    public ReloadCommand(FMessage plugin) {
        this.plugin = plugin;
    }

    @Subcommand("reload")
    @CommandPermission("fmessage.reload")
    @Description("{@@acf-fmessage.reload_help_description}")
    public void onReload(CommandSender sender) {

        plugin.getConfigurationManager().reload();
        plugin.getCommandManager().reloadLang();

        CommandIssuer issuer = plugin.getCommandManager().getCommandIssuer(sender);
        issuer.sendInfo(MessageKeys.RELOAD_SUCCESS);
    }
}
