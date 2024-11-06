package fr.florianpal.fmessage.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.*;
import com.velocitypowered.api.proxy.Player;
import fr.florianpal.fmessage.FMessage;
import fr.florianpal.fmessage.languages.MessageKeys;
import fr.florianpal.fmessage.managers.commandManagers.CommandManager;
import fr.florianpal.fmessage.managers.commandManagers.NickNameCommandManager;
import fr.florianpal.fmessage.utils.StringUtils;

@CommandAlias("nick|nickname")
public class NickCommand extends BaseCommand {

    private final FMessage plugin;
    private final CommandManager commandManager;

    private final NickNameCommandManager nickNameCommandManager;

    public NickCommand(FMessage plugin) {
        this.plugin = plugin;
        this.commandManager = plugin.getCommandManager();
        this.nickNameCommandManager = plugin.getNickNameCommandManager();
    }

    @Default
    @CommandPermission("fmessage.nick")
    @Description("{@@fmessage.nick_help_description}")
    public void onNick(Player playerSender, @Optional String nickname) {
        CommandIssuer issuerTarget = commandManager.getCommandIssuer(playerSender);

        if (StringUtils.isNullOrEmpty(nickname)) {
            nickNameCommandManager.removeNickName(playerSender);
            issuerTarget.sendInfo(MessageKeys.NICKNAME_REMOVE);
        } else {
            String currentNickName = nickNameCommandManager.getCachedNickName(playerSender);

            if (StringUtils.isNullOrEmpty(currentNickName)) {
                nickNameCommandManager.addNickName(playerSender, nickname);
                issuerTarget.sendInfo(MessageKeys.NICKNAME_ADD, "{NewNickName}", nickname);
            } else {
                nickNameCommandManager.updateNickName(playerSender, nickname);
                issuerTarget.sendInfo(MessageKeys.NICKNAME_UPDATE, "{NewNickName}", nickname, "{OldNickName}", currentNickName);
            }
        }
    }
}
