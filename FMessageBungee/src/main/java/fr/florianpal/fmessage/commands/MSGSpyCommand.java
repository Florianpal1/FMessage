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
import net.md_5.bungee.api.connection.ProxiedPlayer;

@CommandAlias("chatspy")
public class MSGSpyCommand extends BaseCommand {
    private final FMessage plugin;
    private final CommandManager commandManager;

    public MSGSpyCommand(FMessage plugin) {
        this.plugin = plugin;
        this.commandManager = plugin.getCommandManager();
    }

    @Default
    @CommandPermission("fmessage.chatspy")
    @Description("{@@fmessage.msgspy_help_description}")
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
