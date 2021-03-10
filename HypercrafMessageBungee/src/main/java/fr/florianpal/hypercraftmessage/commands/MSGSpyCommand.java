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
import net.md_5.bungee.api.connection.ProxiedPlayer;

@CommandAlias("chatspy")
public class MSGSpyCommand extends BaseCommand {
    private HypercraftMessage plugin;
    private CommandManager commandManager;

    public MSGSpyCommand(HypercraftMessage plugin) {
        this.plugin = plugin;
        this.commandManager = plugin.getCommandManager();
    }

    @Default
    @CommandPermission("hc.chatspy")
    @Description("{@@hypercraft.msgspy_help_description}")
    public void onMSGSpy(ProxiedPlayer playerSender) {
        CommandIssuer issuerTarget = commandManager.getCommandIssuer(playerSender);
        if(plugin.isPlayerSpy(playerSender.getUniqueId())) {
            plugin.supPlayerSpy(playerSender.getUniqueId());
            issuerTarget.sendInfo(MessageKeys.SPY_DESACTIVATE);
        } else {
            plugin.addPlayerSpy(playerSender.getUniqueId());
            issuerTarget.sendInfo(MessageKeys.SPY_ACTIVATE);
        }
    }
}
