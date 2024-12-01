package fr.florianpal.fmessage.managers;

import co.aikar.commands.CommandIssuer;
import fr.florianpal.fmessage.FMessage;
import fr.florianpal.fmessage.languages.MessageKeys;
import fr.florianpal.fmessage.managers.commandManagers.CommandManager;
import fr.florianpal.fmessage.managers.commandManagers.IgnoreCommandManager;
import fr.florianpal.fmessage.managers.commandManagers.NickNameCommandManager;
import fr.florianpal.fmessage.utils.FormatUtil;
import fr.florianpal.fmessage.utils.StringUtils;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

public class MessageManager {

    private final FMessage plugin;

    private final CommandManager commandManager;

    private final NickNameCommandManager nickNameCommandManager;

    private final IgnoreCommandManager ignoreCommandManager;

    public MessageManager(FMessage plugin) {
        this.plugin = plugin;
        this.commandManager = plugin.getCommandManager();
        this.nickNameCommandManager = plugin.getNickNameCommandManager();
        this.ignoreCommandManager = plugin.getIgnoreCommandManager();
    }

    public void sendMessage(ProxiedPlayer playerSender, ProxiedPlayer playerTarget, String message) {

        if(ignoreCommandManager.ignoreExist(playerSender, playerTarget)) {
            CommandIssuer issuerTarget = commandManager.getCommandIssuer(playerSender);
            issuerTarget.sendInfo(MessageKeys.SENDER_IGNORE_MESSAGE);
            return;
        } else if(ignoreCommandManager.ignoreExist(playerTarget, playerSender)) {
            CommandIssuer issuerTarget = commandManager.getCommandIssuer(playerSender);
            issuerTarget.sendInfo(MessageKeys.TARGET_IGNORE_MESSAGE);
            return;
        }

        String formatTarget = FormatUtil.format(plugin.getConfigurationManager().getChat().getTargetChatFormat());
        String nickNameSender = nickNameCommandManager.getCachedNickName(playerSender.getUniqueId());
        String nickNameTarget = nickNameCommandManager.getCachedNickName(playerTarget.getUniqueId());
        boolean colors = playerSender.hasPermission("fmessage.colors");

        // Target

        formatTarget = StringUtils.replace(formatTarget, "{sender}", StringUtils.isNullOrEmpty(nickNameSender) ? playerSender.getName() : nickNameSender, true);
        formatTarget = StringUtils.replace(formatTarget, "{target}", StringUtils.isNullOrEmpty(nickNameTarget) ? playerTarget.getName() : nickNameTarget, true);

        formatTarget = StringUtils.replace(formatTarget, "{message}", message, colors);

        playerTarget.sendMessage(formatTarget);

        // Sender

        String formatSender = FormatUtil.format(plugin.getConfigurationManager().getChat().getSenderChatFormat());

        formatSender = StringUtils.replace(formatSender, "{sender}", StringUtils.isNullOrEmpty(nickNameSender) ? playerSender.getName() : nickNameSender, true);
        formatSender = StringUtils.replace(formatSender, "{target}", StringUtils.isNullOrEmpty(nickNameTarget) ? playerTarget.getName() : nickNameTarget, true);

        formatSender = StringUtils.replace(formatSender, "{message}", message, colors);

        playerSender.sendMessage(formatSender);

        plugin.setPreviousPlayer(playerSender, playerTarget);
        plugin.setPreviousPlayer(playerTarget, playerSender);

        // Spy

        String formatSpy = FormatUtil.format(plugin.getConfigurationManager().getChat().getSpyChatFormat());

        formatSpy = StringUtils.replace(formatSpy, "{sender}", StringUtils.isNullOrEmpty(nickNameSender) ? playerSender.getName() : nickNameSender, true);
        formatSpy = StringUtils.replace(formatSpy, "{target}", StringUtils.isNullOrEmpty(nickNameTarget) ? playerTarget.getName() : nickNameTarget, true);

        formatSpy = StringUtils.replace(formatSpy, "{message}", message, true);

        plugin.getLogger().info(FormatUtil.format(formatSpy));

        for (UUID uuid : plugin.getPlayerSpy()) {
            ProxiedPlayer player = plugin.getProxy().getPlayer(uuid);
            player.sendMessage(formatSpy);
        }
    }
}
