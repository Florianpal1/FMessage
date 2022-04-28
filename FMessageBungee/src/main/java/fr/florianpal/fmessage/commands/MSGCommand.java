

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
import fr.florianpal.fmessage.FMessage;
import fr.florianpal.fmessage.languages.MessageKeys;
import fr.florianpal.fmessage.managers.commandManagers.CommandManager;
import fr.florianpal.fmessage.managers.commandManagers.IgnoreCommandManager;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

@CommandAlias("m|msg")
public class MSGCommand extends BaseCommand {
    private final FMessage plugin;
    private final CommandManager commandManager;
    private final IgnoreCommandManager ignoreCommandManager;

    public MSGCommand(FMessage plugin) {
        this.plugin = plugin;
        this.commandManager = plugin.getCommandManager();
        this.ignoreCommandManager = plugin.getIgnoreCommandManager();
    }

    @Default
    @CommandPermission("fmessage.msg")
    @Description("{@@fmessage.msg_help_description}")
    @CommandCompletion("@players")
    public void onMSG(ProxiedPlayer playerSender, String playerTargetName, String message) {

        ProxiedPlayer playerTarget = plugin.getProxy().getPlayer(playerTargetName);

        if (playerTarget == null) {
            CommandIssuer issuerTarget = commandManager.getCommandIssuer(playerSender);
            issuerTarget.sendInfo(MessageKeys.PLAYER_OFFLINE);
        } else {
            if(ignoreCommandManager.ignoreExist(playerSender, playerTarget)) {
                CommandIssuer issuerTarget = commandManager.getCommandIssuer(playerSender);
                issuerTarget.sendInfo(MessageKeys.SENDER_IGNORE_MESSAGE);
            } else if(ignoreCommandManager.ignoreExist(playerTarget, playerSender)) {
                CommandIssuer issuerTarget = commandManager.getCommandIssuer(playerSender);
                issuerTarget.sendInfo(MessageKeys.TARGET_IGNORE_MESSAGE);
            } else {

                String formatTarget = plugin.getConfigurationManager().getChat().getTargetChatFormat();

                formatTarget = formatTarget.replace("{sender}", playerSender.getDisplayName());
                formatTarget = formatTarget.replace("{target}", playerTarget.getDisplayName());
                formatTarget = plugin.format(formatTarget);

                formatTarget = formatTarget.replace("{message}", message);

                if (playerSender.hasPermission("fmessage.colors")) {
                    formatTarget = plugin.format(formatTarget);
                }
                BaseComponent texteTarget = new TextComponent(formatTarget);

                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("song");
                out.writeUTF(playerTarget.getUniqueId().toString());
                playerTarget.sendData("fmessage:chatbukkit", out.toByteArray());

                playerTarget.sendMessage(texteTarget);

                String formatSender = plugin.getConfigurationManager().getChat().getSenderChatFormat();

                formatSender = formatSender.replace("{sender}", playerSender.getDisplayName());
                formatSender = formatSender.replace("{target}", playerTarget.getDisplayName());
                formatSender = plugin.format(formatSender);

                formatSender = formatSender.replace("{message}", message);
                if (playerSender.hasPermission("fmessage.colors")) {
                    formatSender = plugin.format(formatSender);
                }
                BaseComponent texteSender = new TextComponent(formatSender);
                playerSender.sendMessage(texteSender);

                plugin.setPreviousPlayer(playerSender, playerTarget);
                plugin.setPreviousPlayer(playerTarget, playerSender);

                String formatSpy = plugin.getConfigurationManager().getChat().getSpyChatFormat();

                formatSpy = formatSpy.replace("{sender}", playerSender.getDisplayName());
                formatSpy = formatSpy.replace("{target}", playerTarget.getDisplayName());
                formatSpy = plugin.format(formatSpy);

                formatSpy = formatSpy.replace("{message}", message);

                if (playerSender.hasPermission("fmessage.colors")) {
                    formatSpy = plugin.format(formatSpy);
                }
                BaseComponent texteSpy = new TextComponent(formatSpy);

                System.out.println(formatSpy);

                for (UUID uuid : plugin.getPlayerSpy()) {
                    ProxiedPlayer player = plugin.getProxy().getPlayer(uuid);

                    if (player != null) {
                        player.sendMessage(texteSpy);
                    }
                }
            }
        }
    }
}
