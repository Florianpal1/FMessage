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
import fr.florianpal.fmessage.core.acf.Messenger;
import fr.florianpal.fmessage.core.languages.MessageKeys;
import fr.florianpal.fmessage.core.protocol.ChatProtocol;
import fr.florianpal.fmessage.core.protocol.IncomingChat;
import fr.florianpal.fmessage.managers.commandManagers.GroupMemberCommandManager;
import fr.florianpal.fmessage.managers.commandManagers.IgnoreCommandManager;
import fr.florianpal.fmessage.managers.commandManagers.NickNameCommandManager;
import fr.florianpal.fmessage.objects.Group;
import fr.florianpal.fmessage.objects.Member;
import fr.florianpal.fmessage.platform.ProxyBackendServer;
import fr.florianpal.fmessage.platform.ProxyPlatform;
import fr.florianpal.fmessage.platform.ProxyPlayer;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Routes a frame received from a backend server.
 *
 * <p>Three mutually exclusive destinations, in this order of priority:</p>
 * <ol>
 *   <li>the author has a group toggle on — the message goes to that group only, and never
 *       reaches public chat;</li>
 *   <li>the frame is staff chat — rebroadcast to every backend server;</li>
 *   <li>anything else is public chat — rebroadcast with the author's ignore list attached,
 *       so each backend can filter locally.</li>
 * </ol>
 */
public class ChatRelayService {

    private final ProxyPlatform platform;
    private final Messenger messenger;
    private final GroupMemberCommandManager groupMemberCommandManager;
    private final IgnoreCommandManager ignoreCommandManager;
    private final NickNameCommandManager nickNameCommandManager;
    private final Supplier<Map<Integer, Group>> groups;

    public ChatRelayService(ProxyPlatform platform,
                            Messenger messenger,
                            GroupMemberCommandManager groupMemberCommandManager,
                            IgnoreCommandManager ignoreCommandManager,
                            NickNameCommandManager nickNameCommandManager,
                            Supplier<Map<Integer, Group>> groups) {
        this.platform = platform;
        this.messenger = messenger;
        this.groupMemberCommandManager = groupMemberCommandManager;
        this.ignoreCommandManager = ignoreCommandManager;
        this.nickNameCommandManager = nickNameCommandManager;
        this.groups = groups;
    }

    /**
     * @param data the raw payload received on {@link ChatProtocol#CHANNEL_FROM_BUKKIT}
     */
    public void handle(byte[] data) {
        IncomingChat frame;
        try {
            frame = ChatProtocol.decode(data);
        } catch (IOException e) {
            platform.getLogger().warn("Dropped a malformed chat frame: " + e.getMessage());
            return;
        }

        UUID author = frame.uuid();
        String nickName = nickNameCommandManager.getCachedNickName(author);

        if (groupMemberCommandManager.alreadyToggle(author)) {
            relayToGroup(frame, nickName);
        } else if (ChatProtocol.STAFF_SUBCHANNEL.equals(frame.subchannel())) {
            relayToAllServers(ChatProtocol.encodeStaffChat(
                    author, frame.displayName(), nickName, frame.format(), frame.message()));
        } else {
            List<UUID> ignoredBy = ignoreCommandManager.getAreIgnores(author);
            relayToAllServers(ChatProtocol.encodePublicChat(
                    frame.subchannel(), author, frame.displayName(), nickName,
                    frame.format(), frame.message(), ignoredBy,
                    frame.colors(), frame.nickColors()));
        }
    }

    private void relayToGroup(IncomingChat frame, String nickName) {
        int groupId = groupMemberCommandManager.getGroupByToggle(frame.uuid());
        Group group = groups.get().get(groupId);
        if (group == null) {
            // The group was removed between the toggle and this message: drop it silently.
            return;
        }

        String authorName = nickName == null ? frame.displayName() : nickName;

        for (Member member : group.getMember()) {
            java.util.Optional<ProxyPlayer> target = platform.getPlayer(member.getUuid());
            if (target.isEmpty()) {
                continue;
            }
            CommandIssuer issuerTarget = messenger.issuerOf(target.get());
            issuerTarget.sendInfo(MessageKeys.GROUP_MSG,
                    "{group}", group.getName(),
                    "{player}", authorName,
                    "{message}", frame.message());
        }

        platform.getLogger().info("[" + group.getName() + "] " + authorName + " : " + frame.message());
    }

    private void relayToAllServers(byte[] payload) {
        for (ProxyBackendServer server : platform.getServers()) {
            server.sendData(payload);
        }
    }
}
