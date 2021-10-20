package fr.florianpal.fmessage.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.*;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import fr.florianpal.fmessage.HypercraftMessage;
import fr.florianpal.fmessage.languages.MessageKeys;
import fr.florianpal.fmessage.managers.commandManagers.CommandManager;
import fr.florianpal.fmessage.managers.commandManagers.IgnoreCommandManager;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@CommandAlias("m|msg")
public class MSGCommand extends BaseCommand {
    private HypercraftMessage plugin;
    private CommandManager commandManager;
    private IgnoreCommandManager ignoreCommandManager;

    public MSGCommand(HypercraftMessage plugin) {
        this.plugin = plugin;
        this.commandManager = plugin.getCommandManager();
        this.ignoreCommandManager = plugin.getIgnoreCommandManager();
    }

    @Default
    @CommandPermission("hc.msg")
    @Description("{@@hypercraft.msg_help_description}")
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

                if (playerSender.hasPermission("hc.colors")) {
                    formatTarget = plugin.format(formatTarget);
                }
                BaseComponent texteTarget = new TextComponent(formatTarget);

                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("song");
                out.writeUTF(playerTarget.getUniqueId().toString());
                playerTarget.sendData("hc:chatbukkit", out.toByteArray());

                playerTarget.sendMessage(texteTarget);

                String formatSender = plugin.getConfigurationManager().getChat().getSenderChatFormat();

                formatSender = formatSender.replace("{sender}", playerSender.getDisplayName());
                formatSender = formatSender.replace("{target}", playerTarget.getDisplayName());
                formatSender = plugin.format(formatSender);

                formatSender = formatSender.replace("{message}", message);
                if (playerSender.hasPermission("hc.colors")) {
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

                if (playerSender.hasPermission("hc.colors")) {
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
