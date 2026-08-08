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

package fr.florianpal.fmessage.core.commands;

import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import fr.florianpal.fmessage.core.FMessageCore;
import fr.florianpal.fmessage.core.languages.MessageKeys;

/**
 * The only command the console may run, so it takes the issuer as-is.
 */
@CommandAlias("fmessage")
public class ReloadCommand extends FMessageCommand {

    public ReloadCommand(FMessageCore core) {
        super(core);
    }

    @Subcommand("reload")
    @CommandPermission("fmessage.reload")
    @Description("{@@acf-fmessage.reload_help_description}")
    public void onReload(CommandIssuer issuer) {
        core.getConfigurationManager().reload();
        core.reloadLanguage();

        // Caches are rebuilt so a manual database edit is picked up as well.
        core.updateIgnores();
        core.updateGroups();
        core.getNickNameCommandManager().reloadNickNames();

        issuer.sendInfo(MessageKeys.RELOAD_SUCCESS);
    }
}
