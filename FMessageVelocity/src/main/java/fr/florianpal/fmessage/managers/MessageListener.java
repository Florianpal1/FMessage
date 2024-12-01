
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
 * Last modification : 20/10/2021 19:57
 *
 *  @author Florianpal.
 */

package fr.florianpal.fmessage.managers;

import co.aikar.commands.CommandIssuer;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import fr.florianpal.fmessage.FMessage;
import fr.florianpal.fmessage.languages.MessageKeys;
import fr.florianpal.fmessage.managers.commandManagers.CommandManager;
import fr.florianpal.fmessage.managers.commandManagers.GroupMemberCommandManager;
import fr.florianpal.fmessage.managers.commandManagers.IgnoreCommandManager;
import fr.florianpal.fmessage.managers.commandManagers.NickNameCommandManager;
import fr.florianpal.fmessage.objects.Member;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static fr.florianpal.fmessage.FMessage.BUNGEE_CHAT;

public class MessageListener {

    private final FMessage plugin;
    private final GroupMemberCommandManager groupMemberCommandManager;
    private final IgnoreCommandManager ignoreCommandManager;
    private final NickNameCommandManager nickNameCommandManager;
    private final CommandManager commandManager;

    private static final String STAFF_CHAT = "StaffMessage";

    public MessageListener(FMessage plugin) {
        this.plugin = plugin;
        this.groupMemberCommandManager = plugin.getGroupMemberCommandManager();
        this.ignoreCommandManager = plugin.getIgnoreCommandManager();
        this.nickNameCommandManager = plugin.getNickNameCommandManager();
        this.commandManager = plugin.getCommandManager();
    }

    @Subscribe
    public void onMessage(PluginMessageEvent event) {

        if (event.getIdentifier().equals(BUNGEE_CHAT)) {

            ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());

            String subchannel = in.readUTF();
            UUID uuid = UUID.fromString(in.readUTF());
            String displayName = in.readUTF();
            String nickName = nickNameCommandManager.getCachedNickName(uuid);
            String formatWithPlaceholder = in.readUTF();
            String message = in.readUTF();

            if (groupMemberCommandManager.alreadyToggle(uuid)) {
                int id = groupMemberCommandManager.getGroupByToggle(uuid);
                for (Member member : plugin.getGroups().get(id).getMember()) {
                    Optional<Player> playerTarget = plugin.getServer().getPlayer(member.getUuid());
                    if (playerTarget.isPresent()) {
                        CommandIssuer issuerTarget = commandManager.getCommandIssuer(playerTarget);
                        issuerTarget.sendInfo(MessageKeys.GROUP_MSG, "{group}", plugin.getGroups().get(id).getName(), "{player}", playerTarget.get().getUsername(), "{message}", message);
                        plugin.getLogger().info("[{" + plugin.getGroups().get(id).getName() + "}] " + playerTarget.get().getUsername() + " : " + message);
                    }
                }
            } else if (subchannel.equals(STAFF_CHAT)) {
                for (RegisteredServer entry : plugin.getServer().getAllServers()) {
                    ByteArrayDataOutput out = ByteStreams.newDataOutput();
                    out.writeUTF(STAFF_CHAT);
                    out.writeUTF(uuid.toString());
                    out.writeUTF(displayName);
                    out.writeUTF(nickName == null ? "" : nickName);
                    out.writeUTF(formatWithPlaceholder);
                    out.writeUTF(message);
                    entry.sendPluginMessage(FMessage.BUKKIT_CHAT, out.toByteArray());
                }
            } else {

                boolean colors = in.readBoolean();
                boolean nickColors = in.readBoolean();
                for (RegisteredServer entry : plugin.getServer().getAllServers()) {
                    ByteArrayDataOutput out = ByteStreams.newDataOutput();
                    out.writeUTF(subchannel);
                    out.writeUTF(uuid.toString());
                    out.writeUTF(displayName);
                    out.writeUTF(nickName == null ? "" : nickName);
                    out.writeUTF(formatWithPlaceholder);
                    out.writeUTF(message);
                    List<UUID> ignores = new ArrayList<>(ignoreCommandManager.getAreIgnores(uuid));

                    String uuids = "";
                    for (UUID uuid1 : ignores) {
                        uuids = uuid1.toString() + ";";
                    }
                    out.writeUTF(uuids);
                    out.writeBoolean(colors);
                    out.writeBoolean(nickColors);
                    entry.sendPluginMessage(FMessage.BUKKIT_CHAT, out.toByteArray());
                }
            }
        }
    }
}

