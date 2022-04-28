

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
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import fr.florianpal.fmessage.FMessage;
import fr.florianpal.fmessage.languages.MessageKeys;
import fr.florianpal.fmessage.managers.commandManagers.CommandManager;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

@CommandAlias("r")
public class RCommand extends BaseCommand {
    private final FMessage plugin;
    private final CommandManager commandManager;

    public RCommand(FMessage plugin) {
        this.plugin = plugin;
        this.commandManager = plugin.getCommandManager();
    }

    @Default
    @CommandPermission("fmessage.r")
    @Description("{@@fmessage.r_help_description}")
    public void onR(ProxiedPlayer playerSender, String message){
        if(!plugin.havePreviousPlayer(playerSender)) {
            CommandIssuer issuerTarget = commandManager.getCommandIssuer(playerSender);
            issuerTarget.sendInfo(MessageKeys.NO_PREVIOUS_PLAYER);
        } else if (!plugin.isPreviousPlayerOnline(playerSender)) {
            CommandIssuer issuerTarget = commandManager.getCommandIssuer(playerSender);
            issuerTarget.sendInfo(MessageKeys.PLAYER_OFFLINE);
        } else {
            ProxiedPlayer playerTarget = plugin.getPreviousPlayer(playerSender);

            String formatTarget = plugin.getConfigurationManager().getChat().getTargetChatFormat();

            formatTarget = formatTarget.replace("{sender}", playerSender.getDisplayName());
            formatTarget = formatTarget.replace("{target}", playerTarget.getDisplayName());
            formatTarget = plugin.format(formatTarget);

            formatTarget = formatTarget.replace("{message}", message);
            if(playerSender.hasPermission("fmessage.colors")) {
                formatTarget = plugin.format(formatTarget);
            }
            BaseComponent texteTarget = new TextComponent(formatTarget);
            playerTarget.sendMessage(texteTarget);

            String formatSender = plugin.getConfigurationManager().getChat().getSenderChatFormat();

            formatSender = formatSender.replace("{sender}", playerSender.getDisplayName());
            formatSender = formatSender.replace("{target}", playerTarget.getDisplayName());
            formatSender = plugin.format(formatSender);

            formatSender = formatSender.replace("{message}", message);
            if(playerSender.hasPermission("fmessage.colors")) {
                formatSender = plugin.format(formatSender);
            }
            BaseComponent texteSender = new TextComponent(formatSender);
            playerSender.sendMessage(texteSender);

            String formatSpy = plugin.getConfigurationManager().getChat().getSpyChatFormat();

            formatSpy = formatSpy.replace("{sender}", playerSender.getDisplayName());
            formatSpy = formatSpy.replace("{target}", playerTarget.getDisplayName());
            formatSpy = plugin.format(formatSpy);

            formatSpy = formatSpy.replace("{message}", message);

            if(playerSender.hasPermission("fmessage.colors")) {
                formatSpy = plugin.format(formatSpy);
            }
            BaseComponent texteSpy = new TextComponent(formatSpy);

            System.out.println(formatSpy);

            for(UUID uuid : plugin.getPlayerSpy()) {
                ProxiedPlayer player = plugin.getProxy().getPlayer(uuid);
                if(player != null) {
                    player.sendMessage(texteSpy);
                }
            }
        }
    }
}
