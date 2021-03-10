package fr.florianpal.hypercraftmessage.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import fr.florianpal.hypercraftmessage.HypercraftMessage;
import fr.florianpal.hypercraftmessage.languages.MessageKeys;
import fr.florianpal.hypercraftmessage.managers.commandManagers.CommandManager;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

@CommandAlias("r")
public class RCommand extends BaseCommand {
    private HypercraftMessage plugin;
    private CommandManager commandManager;

    public RCommand(HypercraftMessage plugin) {
        this.plugin = plugin;
        this.commandManager = plugin.getCommandManager();
    }

    @Default
    @CommandPermission("hc.r")
    @Description("{@@hypercraft.r_help_description}")
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
            if(playerSender.hasPermission("hc.colors")) {
                formatTarget = plugin.format(formatTarget);
            }
            BaseComponent texteTarget = new TextComponent(formatTarget);
            playerTarget.sendMessage(texteTarget);

            String formatSender = plugin.getConfigurationManager().getChat().getSenderChatFormat();

            formatSender = formatSender.replace("{sender}", playerSender.getDisplayName());
            formatSender = formatSender.replace("{target}", playerTarget.getDisplayName());
            formatSender = plugin.format(formatSender);

            formatSender = formatSender.replace("{message}", message);
            if(playerSender.hasPermission("hc.colors")) {
                formatSender = plugin.format(formatSender);
            }
            BaseComponent texteSender = new TextComponent(formatSender);
            playerSender.sendMessage(texteSender);

            String formatSpy = plugin.getConfigurationManager().getChat().getSpyChatFormat();

            formatSpy = formatSpy.replace("{sender}", playerSender.getDisplayName());
            formatSpy = formatSpy.replace("{target}", playerTarget.getDisplayName());
            formatSpy = plugin.format(formatSpy);

            formatSpy = formatSpy.replace("{message}", message);

            if(playerSender.hasPermission("hc.colors")) {
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
