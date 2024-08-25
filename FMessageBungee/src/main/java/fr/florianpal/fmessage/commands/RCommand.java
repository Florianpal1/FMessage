

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
import com.velocitypowered.api.proxy.Player;
import fr.florianpal.fmessage.FMessage;
import fr.florianpal.fmessage.languages.MessageKeys;
import fr.florianpal.fmessage.managers.commandManagers.CommandManager;
import fr.florianpal.fmessage.utils.FormatUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

import java.util.Optional;
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
    public void onR(Player playerSender, String message){
        if(!plugin.havePreviousPlayer(playerSender)) {
            CommandIssuer issuerTarget = commandManager.getCommandIssuer(playerSender);
            issuerTarget.sendInfo(MessageKeys.NO_PREVIOUS_PLAYER);
        } else if (!plugin.isPreviousPlayerOnline(playerSender)) {
            CommandIssuer issuerTarget = commandManager.getCommandIssuer(playerSender);
            issuerTarget.sendInfo(MessageKeys.PLAYER_OFFLINE);
        } else {
            Player playerTarget = plugin.getPreviousPlayer(playerSender);

            String formatTarget = plugin.getConfigurationManager().getChat().getTargetChatFormat();

            formatTarget = formatTarget.replace("{sender}", playerSender.getUsername());
            formatTarget = formatTarget.replace("{target}", playerTarget.getUsername());
            formatTarget = FormatUtil.format(formatTarget);

            formatTarget = formatTarget.replace("{message}", message);
            if(playerSender.hasPermission("fmessage.colors")) {
                formatTarget = FormatUtil.format(formatTarget);
            }

            TextComponent texteTarget = Component.text(formatTarget);
            playerTarget.sendMessage(texteTarget);

            String formatSender = plugin.getConfigurationManager().getChat().getSenderChatFormat();

            formatSender = formatSender.replace("{sender}", playerSender.getUsername());
            formatSender = formatSender.replace("{target}", playerTarget.getUsername());
            formatSender = FormatUtil.format(formatSender);

            formatSender = formatSender.replace("{message}", message);
            if(playerSender.hasPermission("fmessage.colors")) {
                formatSender = FormatUtil.format(formatSender);
            }
            TextComponent texteSender = Component.text(formatSender);
            playerSender.sendMessage(texteSender);

            String formatSpy = plugin.getConfigurationManager().getChat().getSpyChatFormat();

            formatSpy = formatSpy.replace("{sender}", playerSender.getUsername());
            formatSpy = formatSpy.replace("{target}", playerTarget.getUsername());
            formatSpy = FormatUtil.format(formatSpy);

            formatSpy = formatSpy.replace("{message}", message);

            TextComponent texteSpy;
            if (playerSender.hasPermission("fmessage.colors")) {
                texteSpy = FormatUtil.formatToTextComponent(formatSpy);
            } else {
                texteSpy = Component.text(formatSpy);
            }

            plugin.getLogger().info(formatSpy);

            for(UUID uuid : plugin.getPlayerSpy()) {
                Optional<Player> player = plugin.getServer().getPlayer(uuid);
                if(player.isPresent()) {
                    player.get().sendMessage(texteSpy);
                }
            }
        }
    }
}
