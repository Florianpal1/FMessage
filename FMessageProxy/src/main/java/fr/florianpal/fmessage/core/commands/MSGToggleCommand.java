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
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import fr.florianpal.fmessage.core.FMessageCore;
import fr.florianpal.fmessage.core.languages.MessageKeys;
import fr.florianpal.fmessage.core.state.SessionState;
import fr.florianpal.fmessage.platform.ProxyPlayer;

@CommandAlias("msgtoggle|mt")
public class MSGToggleCommand extends FMessageCommand {

    public MSGToggleCommand(FMessageCore core) {
        super(core);
    }

    @Default
    @CommandPermission("fmessage.msgtoggle")
    @Description("{@@acf-fmessage.msgtoggle_help_description}")
    public void onMSGToggle(CommandIssuer issuer) {
        ProxyPlayer sender = requirePlayer(issuer);
        if (sender == null) {
            return;
        }

        SessionState state = core.getSessionState();
        if (state.hasMessagesDisabled(sender.getUniqueId())) {
            state.enableMessages(sender.getUniqueId());
            issuer.sendInfo(MessageKeys.MSGTOGGLE_ACTIVATE);
            return;
        }

        state.disableMessages(sender.getUniqueId());
        issuer.sendInfo(MessageKeys.MSGTOGGLE_DESACTIVATE);
    }
}
