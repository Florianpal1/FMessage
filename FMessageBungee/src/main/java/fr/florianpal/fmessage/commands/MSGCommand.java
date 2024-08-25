

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
import fr.florianpal.fmessage.managers.commandManagers.CommandManager;
import fr.florianpal.fmessage.managers.commandManagers.IgnoreCommandManager;
import fr.florianpal.fmessage.utils.FormatUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

import java.util.Optional;
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
    public void onMSG(Player playerSender, String playerTargetName, String message) {

        Player playerTarget = plugin.getServer().getPlayer(playerTargetName).get();

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

                formatTarget = formatTarget.replace("{sender}", playerSender.getUsername());
                formatTarget = formatTarget.replace("{target}", playerTarget.getUsername());
                formatTarget = FormatUtil.format(formatTarget);

                formatTarget = formatTarget.replace("{message}", message);

                TextComponent texteTarget;
                if (playerSender.hasPermission("fmessage.colors")) {
                    texteTarget = FormatUtil.formatToTextComponent(formatTarget);
                } else {
                    texteTarget = Component.text(formatTarget);
                }

                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("song");
                out.writeUTF(playerTarget.getUniqueId().toString());
                playerTarget.sendPluginMessage(FMessage.BUKKIT_CHAT, out.toByteArray());

                playerTarget.sendMessage(texteTarget);

                String formatSender = plugin.getConfigurationManager().getChat().getSenderChatFormat();

                formatSender = formatSender.replace("{sender}", playerSender.getUsername());
                formatSender = formatSender.replace("{target}", playerTarget.getUsername());
                formatSender = FormatUtil.format(formatSender);

                formatSender = formatSender.replace("{message}", message);

                TextComponent texteSender;
                if (playerSender.hasPermission("fmessage.colors")) {
                    texteSender = FormatUtil.formatToTextComponent(formatSender);
                } else {
                    texteSender = Component.text(formatSender);
                }
                playerSender.sendMessage(texteSender);

                plugin.setPreviousPlayer(playerSender, playerTarget);
                plugin.setPreviousPlayer(playerTarget, playerSender);

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

                for (UUID uuid : plugin.getPlayerSpy()) {
                    Optional<Player> player = plugin.getServer().getPlayer(uuid);

                    if (player.isPresent()) {
                        player.get().sendMessage(texteSpy);
                    }
                }
            }
        }
    }
}
