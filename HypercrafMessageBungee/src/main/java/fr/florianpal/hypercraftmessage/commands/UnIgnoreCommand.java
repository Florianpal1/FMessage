package fr.florianpal.hypercraftmessage.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.*;
import fr.florianpal.hypercraftmessage.HypercraftMessage;
import fr.florianpal.hypercraftmessage.languages.MessageKeys;
import fr.florianpal.hypercraftmessage.managers.commandManagers.CommandManager;
import fr.florianpal.hypercraftmessage.managers.commandManagers.IgnoreCommandManager;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

@CommandAlias("unignore")
public class UnIgnoreCommand extends BaseCommand {

    private HypercraftMessage plugin;
    private CommandManager commandManager;
    private IgnoreCommandManager ignoreCommandManager;

    public UnIgnoreCommand(HypercraftMessage plugin) {
        this.plugin = plugin;
        this.commandManager = plugin.getCommandManager();
        this.ignoreCommandManager = plugin.getIgnoreCommandManager();
    }

    @Default
    @CommandPermission("hc.unignore")
    @Description("{@@hypercraft.unignore_help_description}")
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
