package fr.florianpal.fmessage.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.*;
import fr.florianpal.fmessage.FMessage;
import fr.florianpal.fmessage.languages.MessageKeys;
import fr.florianpal.fmessage.managers.commandManagers.CommandManager;
import fr.florianpal.fmessage.managers.commandManagers.IgnoreCommandManager;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

@CommandAlias("unignore")
public class UnIgnoreCommand extends BaseCommand {

    private final FMessage plugin;
    private final CommandManager commandManager;
    private final IgnoreCommandManager ignoreCommandManager;

    public UnIgnoreCommand(FMessage plugin) {
        this.plugin = plugin;
        this.commandManager = plugin.getCommandManager();
        this.ignoreCommandManager = plugin.getIgnoreCommandManager();
    }

    @Default
    @CommandPermission("fmessage.unignore")
    @Description("{@@fmessage.unignore_help_description}")
    @CommandCompletion("@players")
    public void onUnIgnore(ProxiedPlayer playerSender, String playerTargetName) {
        ProxiedPlayer playerTarget = plugin.getProxy().getPlayer(playerTargetName);
        if(playerTarget != null) {
            if(ignoreCommandManager.ignoreExist(playerSender, playerTarget)) {
                CommandIssuer issuerTarget = commandManager.getCommandIssuer(playerSender);
                issuerTarget.sendInfo(MessageKeys.UNIGNORE_SUCCESS,"{player}",playerTargetName);
                ignoreCommandManager.removeIgnore(playerSender, playerTarget);

                UUID playerSenderUuid = playerSender.getUniqueId();
                UUID playerTargetUuid = playerTarget.getUniqueId();

                if (plugin.getIgnores().containsKey(playerSenderUuid)) {
                    plugin.getIgnores().get(playerSenderUuid).remove(playerTargetUuid);
                }
            } else {
                CommandIssuer issuerTarget = commandManager.getCommandIssuer(playerSender);
                issuerTarget.sendInfo(MessageKeys.UNIGNORE_ALREADY,"{player}",playerTargetName);
            }
        } else {
            CommandIssuer issuerTarget = commandManager.getCommandIssuer(playerSender);
            issuerTarget.sendInfo(MessageKeys.IGNORE_NOT_EXIST,"{player}",playerTargetName);
        }
    }
}
