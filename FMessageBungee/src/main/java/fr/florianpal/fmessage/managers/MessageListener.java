
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
import com.google.common.eventbus.Subscribe;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import fr.florianpal.fmessage.FMessage;
import fr.florianpal.fmessage.languages.MessageKeys;
import fr.florianpal.fmessage.managers.commandManagers.CommandManager;
import fr.florianpal.fmessage.managers.commandManagers.GroupMemberCommandManager;
import fr.florianpal.fmessage.managers.commandManagers.IgnoreCommandManager;
import fr.florianpal.fmessage.managers.commandManagers.NickNameCommandManager;
import fr.florianpal.fmessage.objects.Member;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static fr.florianpal.fmessage.FMessage.BUNGEE_CHAT;

public class MessageListener implements Listener {

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

        if (event.getTag().equalsIgnoreCase(BUNGEE_CHAT)) {

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
                    ProxiedPlayer playerTarget = plugin.getProxy().getPlayer(member.getUuid());

                        CommandIssuer issuerTarget = commandManager.getCommandIssuer(playerTarget);
                        issuerTarget.sendInfo(MessageKeys.GROUP_MSG, "{group}", plugin.getGroups().get(id).getName(), "{player}", playerTarget.getName(), "{message}", message);
                        plugin.getLogger().info("[{" + plugin.getGroups().get(id).getName() + "}] " + playerTarget.getName() + " : " + message);

                }
            } else if (subchannel.equals(STAFF_CHAT)) {
                for (Map.Entry<String, ServerInfo> entry : plugin.getProxy().getServers().entrySet()) {
                    ByteArrayDataOutput out = ByteStreams.newDataOutput();
                    out.writeUTF(STAFF_CHAT);
                    out.writeUTF(uuid.toString());
                    out.writeUTF(displayName);
                    out.writeUTF(nickName == null ? "" : nickName);
                    out.writeUTF(formatWithPlaceholder);
                    out.writeUTF(message);
                    entry.getValue().sendData(FMessage.BUKKIT_CHAT, out.toByteArray());
                }
            } else {

                boolean colors = in.readBoolean();
                boolean nickColors = in.readBoolean();
                for (Map.Entry<String, ServerInfo> entry : plugin.getProxy().getServers().entrySet()) {
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
                    entry.getValue().sendData(FMessage.BUKKIT_CHAT, out.toByteArray());
                }
            }
        }
    }
}

