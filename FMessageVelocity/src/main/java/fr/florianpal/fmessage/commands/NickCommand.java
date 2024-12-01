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
    @Syntax("(nickname)")
    public void onNick(Player playerSender, @Optional String nickname) {
        CommandIssuer issuerSender = commandManager.getCommandIssuer(playerSender);

        if (StringUtils.isNullOrEmpty(nickname)) {
            nickNameCommandManager.removeNickName(playerSender.getUniqueId());
            issuerSender.sendInfo(MessageKeys.NICKNAME_REMOVE);
            return;
        }
        String currentNickName = nickNameCommandManager.getCachedNickName(playerSender.getUniqueId());

        if (StringUtils.isNullOrEmpty(currentNickName)) {
            nickNameCommandManager.addNickName(playerSender.getUniqueId(), nickname);
            issuerSender.sendInfo(MessageKeys.NICKNAME_ADD, "{NewNickName}", nickname);
            return;
        }

        nickNameCommandManager.updateNickName(playerSender.getUniqueId(), nickname);
        issuerSender.sendInfo(MessageKeys.NICKNAME_UPDATE, "{NewNickName}", nickname, "{OldNickName}", currentNickName);
    }

}
