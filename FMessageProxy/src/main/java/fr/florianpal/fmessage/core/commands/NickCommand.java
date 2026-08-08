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
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Syntax;
import fr.florianpal.fmessage.core.FMessageCore;
import fr.florianpal.fmessage.core.languages.MessageKeys;
import fr.florianpal.fmessage.core.text.MessageTemplate;
import fr.florianpal.fmessage.managers.commandManagers.NickNameCommandManager;
import fr.florianpal.fmessage.platform.ProxyPlayer;

@CommandAlias("nick|nickname")
public class NickCommand extends FMessageCommand {

    private final NickNameCommandManager nickNameCommandManager;

    public NickCommand(FMessageCore core) {
        super(core);
        this.nickNameCommandManager = core.getNickNameCommandManager();
    }

    @Default
    @CommandPermission("fmessage.nick")
    @Description("{@@acf-fmessage.nick_help_description}")
    @Syntax("(nickname)")
    public void onNick(CommandIssuer issuer, @Optional String nickname) {
        ProxyPlayer sender = requirePlayer(issuer);
        if (sender == null) {
            return;
        }

        if (MessageTemplate.isNullOrEmpty(nickname)) {
            nickNameCommandManager.removeNickName(sender.getUniqueId());
            issuer.sendInfo(MessageKeys.NICKNAME_REMOVE);
            return;
        }

        String currentNickName = nickNameCommandManager.getCachedNickName(sender.getUniqueId());

        if (MessageTemplate.isNullOrEmpty(currentNickName)) {
            nickNameCommandManager.addNickName(sender.getUniqueId(), nickname);
            issuer.sendInfo(MessageKeys.NICKNAME_ADD, "{NewNickName}", nickname);
            return;
        }

        nickNameCommandManager.updateNickName(sender.getUniqueId(), nickname);
        issuer.sendInfo(MessageKeys.NICKNAME_UPDATE, "{NewNickName}", nickname, "{OldNickName}", currentNickName);
    }
}
