/*
 * Copyright (C) 2022 Florianpal
 *
 * This program is free software;
 * you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, see <http://www.gnu.org/licenses/>.
 *
 *  @author Florianpal.
 */

package fr.florianpal.fmessage.core.service;

import co.aikar.commands.CommandIssuer;
import fr.florianpal.fmessage.configurations.ChatConfig;
import fr.florianpal.fmessage.core.acf.Messenger;
import fr.florianpal.fmessage.core.languages.MessageKeys;
import fr.florianpal.fmessage.core.state.SessionState;
import fr.florianpal.fmessage.core.text.LegacyFormatter;
import fr.florianpal.fmessage.core.text.MessageTemplate;
import fr.florianpal.fmessage.managers.commandManagers.IgnoreCommandManager;
import fr.florianpal.fmessage.managers.commandManagers.NickNameCommandManager;
import fr.florianpal.fmessage.platform.ProxyPlatform;
import fr.florianpal.fmessage.platform.ProxyPlayer;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Delivers a private message: the {@code /msg} and {@code /r} path.
 *
 * <p>Collaborators are injected rather than pulled from the plugin instance, which is what
 * makes the whole flow — ignores, msgtoggle, nicknames, colour permissions, spies and the
 * notification sound — testable without a running proxy.</p>
 */
public class PrivateMessageService {

    private static final String PERMISSION_COLORS = "fmessage.colors";
    private static final String PERMISSION_NICK_COLORS = "fmessage.nick.colors";

    private final ProxyPlatform platform;
    private final Messenger messenger;
    private final Supplier<ChatConfig> chatConfig;
    private final IgnoreCommandManager ignoreCommandManager;
    private final NickNameCommandManager nickNameCommandManager;
    private final SessionState sessionState;

    public PrivateMessageService(ProxyPlatform platform,
                                 Messenger messenger,
                                 Supplier<ChatConfig> chatConfig,
                                 IgnoreCommandManager ignoreCommandManager,
                                 NickNameCommandManager nickNameCommandManager,
                                 SessionState sessionState) {
        this.platform = platform;
        this.messenger = messenger;
        this.chatConfig = chatConfig;
        this.ignoreCommandManager = ignoreCommandManager;
        this.nickNameCommandManager = nickNameCommandManager;
        this.sessionState = sessionState;
    }

    public void send(ProxyPlayer sender, ProxyPlayer target, String message) {
        CommandIssuer issuerSender = messenger.issuerOf(sender);

        if (ignoreCommandManager.ignoreExist(sender.getUniqueId(), target.getUniqueId())) {
            issuerSender.sendInfo(MessageKeys.SENDER_IGNORE_MESSAGE);
            return;
        } else if (ignoreCommandManager.ignoreExist(target.getUniqueId(), sender.getUniqueId())) {
            issuerSender.sendInfo(MessageKeys.TARGET_IGNORE_MESSAGE);
            return;
        }

        if (sessionState.hasMessagesDisabled(sender.getUniqueId())) {
            issuerSender.sendInfo(MessageKeys.SENDER_MSGTOGGLE);
            return;
        } else if (sessionState.hasMessagesDisabled(target.getUniqueId())) {
            issuerSender.sendInfo(MessageKeys.TARGET_MSGTOGGLE, "{player}", target.getName());
            return;
        }

        ChatConfig config = chatConfig.get();
        String nickNameSender = nickNameCommandManager.getCachedNickName(sender.getUniqueId());
        String nickNameTarget = nickNameCommandManager.getCachedNickName(target.getUniqueId());

        boolean colors = sender.hasPermission(PERMISSION_COLORS);
        // Nickname colours obey fmessage.nick.colors here too, like they do in public chat.
        boolean senderNickColors = sender.hasPermission(PERMISSION_NICK_COLORS);
        boolean targetNickColors = target.hasPermission(PERMISSION_NICK_COLORS);

        String displaySender = displayName(nickNameSender, sender);
        String displayTarget = displayName(nickNameTarget, target);

        // Target
        String formatTarget = render(config.getTargetChatFormat(), displaySender, displayTarget, message,
                senderNickColors, targetNickColors, colors);
        target.sendMessage(formatTarget);
        playNotification(target);

        // Sender
        String formatSender = render(config.getSenderChatFormat(), displaySender, displayTarget, message,
                senderNickColors, targetNickColors, colors);
        sender.sendMessage(formatSender);

        sessionState.setPreviousPlayer(sender.getUniqueId(), target.getUniqueId());
        sessionState.setPreviousPlayer(target.getUniqueId(), sender.getUniqueId());

        // Spy: colours always apply, the log line is meant for staff eyes.
        String formatSpy = render(config.getSpyChatFormat(), displaySender, displayTarget, message,
                true, true, true);

        platform.getLogger().info(formatSpy);

        for (UUID uuid : sessionState.getSpies()) {
            platform.getPlayer(uuid).ifPresent(spy -> spy.sendMessage(formatSpy));
        }
    }

    private String render(String format,
                          String sender,
                          String target,
                          String message,
                          boolean senderNickColors,
                          boolean targetNickColors,
                          boolean messageColors) {
        String rendered = LegacyFormatter.format(format);
        rendered = MessageTemplate.replace(rendered, "{sender}", sender, senderNickColors);
        rendered = MessageTemplate.replace(rendered, "{target}", target, targetNickColors);
        return MessageTemplate.replace(rendered, "{message}", message, messageColors);
    }

    private String displayName(String nickName, ProxyPlayer player) {
        return MessageTemplate.isNullOrEmpty(nickName) ? player.getName() : nickName;
    }

    /**
     * Plays the configured notification sound to the recipient of a private message.
     */
    private void playNotification(ProxyPlayer target) {
        ChatConfig config = chatConfig.get();
        if (!config.isNotificationSoundEnabled()) {
            return;
        }

        String sound = config.getNotificationSound();
        if (MessageTemplate.isNullOrEmpty(sound)) {
            return;
        }

        target.playSound(sound);
    }

    /**
     * @return the reply target of {@code player}, if they have one and it is still online.
     */
    public Optional<ProxyPlayer> replyTargetOf(ProxyPlayer player) {
        return sessionState.getPreviousPlayer(player.getUniqueId()).flatMap(platform::getPlayer);
    }
}
