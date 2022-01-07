package fr.florianpal.fmessage.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.*;
import fr.florianpal.fmessage.FMessage;
import fr.florianpal.fmessage.languages.MessageKeys;
import fr.florianpal.fmessage.managers.commandManagers.CommandManager;
import fr.florianpal.fmessage.managers.commandManagers.IgnoreCommandManager;
import net.md_5.bungee.api.connection.ProxiedPlayer;

@CommandAlias("ignore")
public class IgnoreCommand extends BaseCommand {

    private final FMessage plugin;
    private final CommandManager commandManager;
    private final IgnoreCommandManager ignoreCommandManager;

    public IgnoreCommand(FMessage plugin) {
        this.plugin = plugin;
        this.commandManager = plugin.getCommandManager();
        this.ignoreCommandManager = plugin.getIgnoreCommandManager();
    }

    @Default
    @CommandPermission("fmessage.ignore")
    @Description("{@@fmessage.ignore_help_description}")
    @CommandCompletion("@players")
    public void onIgnore(ProxiedPlayer playerSender, String playerTargetName) {
        ProxiedPlayer playerTarget = plugin.getProxy().getPlayer(playerTargetName);

        if(playerTarget != null) {
            if(playerTarget.hasPermission("hc.cannot_ignore")) {
                CommandIssuer issuerTarget = commandManager.getCommandIssuer(playerSender);
                issuerTarget.sendInfo(MessageKeys.CANNOT_IGNORE);
            } else if(ignoreCommandManager.ignoreExist(playerSender, playerTarget)) {
                CommandIssuer issuerTarget = commandManager.getCommandIssuer(playerSender);
                issuerTarget.sendInfo(MessageKeys.IGNORE_ALREADY, "{player}",playerTargetName);
            } else {
                ignoreCommandManager.addIgnore(playerSender, playerTarget);
                plugin.updateIgnores();

                CommandIssuer issuerTarget = commandManager.getCommandIssuer(playerSender);
                issuerTarget.sendInfo(MessageKeys.IGNORE_SUCCESS, "{player}",playerTargetName);
            }
        } else {
            CommandIssuer issuerTarget = commandManager.getCommandIssuer(playerSender);
            issuerTarget.sendInfo(MessageKeys.IGNORE_NOT_EXIST, "{player}",playerTargetName);
        }
    }
}
