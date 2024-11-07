

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
 * Last modification : 07/01/2022 23:05
 *
 *  @author Florianpal.
 */

package fr.florianpal.fmessage.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.*;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.proxy.Player;
import fr.florianpal.fmessage.FMessage;
import fr.florianpal.fmessage.languages.MessageKeys;
import fr.florianpal.fmessage.managers.MessageManager;
import fr.florianpal.fmessage.managers.commandManagers.CommandManager;
import fr.florianpal.fmessage.managers.commandManagers.IgnoreCommandManager;
import fr.florianpal.fmessage.managers.commandManagers.NickNameCommandManager;
import fr.florianpal.fmessage.utils.FormatUtil;
import fr.florianpal.fmessage.utils.StringUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.Optional;
import java.util.UUID;

@CommandAlias("m|msg")
public class MSGCommand extends BaseCommand {
    private final FMessage plugin;
    private final CommandManager commandManager;
    private final MessageManager messageManager;

    public MSGCommand(FMessage plugin) {
        this.plugin = plugin;
        this.commandManager = plugin.getCommandManager();
        this.messageManager = plugin.getMessageManager();
    }

    @Default
    @CommandPermission("fmessage.msg")
    @Description("{@@fmessage.msg_help_description}")
    @CommandCompletion("@players")
    public void onMSG(Player playerSender, String playerTargetName, String message) {

        Optional<Player> playerTargetOptional = plugin.getServer().getPlayer(playerTargetName);
        CommandIssuer issuerSender = commandManager.getCommandIssuer(playerSender);

        if (playerTargetOptional.isEmpty()) {

            issuerSender.sendInfo(MessageKeys.PLAYER_OFFLINE);
            return;
        }
        Player playerTarget = playerTargetOptional.get();

        messageManager.sendMessage(playerSender, playerTarget, message);
    }
}
