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

package fr.florianpal.fmessage.core.commands;

import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import fr.florianpal.fmessage.core.FMessageCore;
import fr.florianpal.fmessage.core.languages.MessageKeys;
import fr.florianpal.fmessage.managers.commandManagers.GroupCommandManager;
import fr.florianpal.fmessage.managers.commandManagers.GroupMemberCommandManager;
import fr.florianpal.fmessage.objects.Group;
import fr.florianpal.fmessage.objects.Member;
import fr.florianpal.fmessage.platform.ProxyPlayer;

import java.util.Optional;

@CommandAlias("group")
public class GroupCommand extends FMessageCommand {

    private final GroupCommandManager groupCommandManager;
    private final GroupMemberCommandManager groupMemberCommandManager;

    public GroupCommand(FMessageCore core) {
        super(core);
        this.groupCommandManager = core.getGroupCommandManager();
        this.groupMemberCommandManager = core.getGroupMemberCommandManager();
    }

    @Subcommand("create")
    @CommandPermission("fmessage.group.create")
    @Description("{@@acf-fmessage.group_create_help_description}")
    @Syntax("[groupName]")
    public void onCreate(CommandIssuer issuer, String groupName) {
        ProxyPlayer sender = requirePlayer(issuer);
        if (sender == null) {
            return;
        }

        if (groupCommandManager.groupExist(sender.getUniqueId(), groupName)) {
            issuer.sendInfo(MessageKeys.GROUP_ALREADY_EXIST, "{group}", groupName);
            return;
        }

        groupCommandManager.addGroup(sender.getUniqueId(), groupName);
        int groupId = groupCommandManager.getGroupId(sender.getUniqueId(), groupName);
        groupMemberCommandManager.addGroupMember(groupId, sender.getUniqueId());
        core.updateGroups();

        issuer.sendInfo(MessageKeys.GROUP_CREATE_SUCCESS, "{group}", groupName);
    }

    @Subcommand("remove")
    @CommandPermission("fmessage.group.remove")
    @Description("{@@acf-fmessage.group_remove_help_description}")
    @CommandCompletion("@owngroups")
    @Syntax("[groupName]")
    public void onRemove(CommandIssuer issuer, String groupName) {
        ProxyPlayer sender = requirePlayer(issuer);
        if (sender == null) {
            return;
        }

        if (groupCommandManager.groupExist(sender.getUniqueId(), groupName)) {
            int groupId = groupCommandManager.getGroupId(sender.getUniqueId(), groupName);
            groupCommandManager.removeGroup(groupId);
            groupMemberCommandManager.removeGroup(groupId);
            core.updateGroups();

            issuer.sendInfo(MessageKeys.GROUP_REMOVE_SUCCESS, "{group}", groupName);
            return;
        }

        issuer.sendInfo(MessageKeys.GROUP_CANNOT_EXIST, "{group}", groupName);
    }

    @Subcommand("member add")
    @CommandPermission("fmessage.group.member.add")
    @Description("{@@acf-fmessage.group_member_add_help_description}")
    @CommandCompletion("@owngroups @players")
    @Syntax("[groupName] [playerTargetName]")
    public void onAddMember(CommandIssuer issuer, String groupName, String playerTargetName) {
        ProxyPlayer sender = requirePlayer(issuer);
        if (sender == null) {
            return;
        }

        Optional<ProxyPlayer> target = core.getPlatform().getPlayer(playerTargetName);
        if (target.isEmpty()) {
            issuer.sendInfo(MessageKeys.PLAYER_OFFLINE);
            return;
        }

        if (!groupCommandManager.groupExist(sender.getUniqueId(), groupName)) {
            issuer.sendInfo(MessageKeys.GROUP_CANNOT_EXIST, "{group}", groupName);
            return;
        }

        int groupId = groupCommandManager.getGroupId(sender.getUniqueId(), groupName);
        if (groupMemberCommandManager.inGroup(groupId, target.get().getUniqueId())) {
            issuer.sendInfo(MessageKeys.GROUP_ALREADY_IN_GROUP, "{group}", groupName, "{player}", playerTargetName);
            return;
        }

        groupMemberCommandManager.addGroupMember(groupId, target.get().getUniqueId());
        core.updateGroups();

        issuer.sendInfo(MessageKeys.GROUP_ADDMEMBER_SUCCESS, "{group}", groupName, "{player}", playerTargetName);
    }

    @Subcommand("member kick")
    @CommandPermission("fmessage.group.member.kick")
    @Description("{@@acf-fmessage.group_member_kick_help_description}")
    @CommandCompletion("@owngroups @players")
    @Syntax("[groupName] [playerTargetName]")
    public void onRemoveMember(CommandIssuer issuer, String groupName, String playerTargetName) {
        ProxyPlayer sender = requirePlayer(issuer);
        if (sender == null) {
            return;
        }

        Optional<ProxyPlayer> target = core.getPlatform().getPlayer(playerTargetName);
        if (target.isEmpty()) {
            issuer.sendInfo(MessageKeys.PLAYER_OFFLINE);
            return;
        }

        if (!groupCommandManager.groupExist(sender.getUniqueId(), groupName)) {
            issuer.sendInfo(MessageKeys.GROUP_CANNOT_EXIST, "{group}", groupName);
            return;
        }

        int groupId = groupCommandManager.getGroupId(sender.getUniqueId(), groupName);
        if (!groupMemberCommandManager.inGroup(groupId, target.get().getUniqueId())) {
            issuer.sendInfo(MessageKeys.GROUP_MEMBER_NOT_INGROUP, "{group}", groupName, "{player}", playerTargetName);
            return;
        }

        groupMemberCommandManager.removeGroupMember(groupId, target.get().getUniqueId());
        core.updateGroups();

        issuer.sendInfo(MessageKeys.GROUP_REMOVEMEMBER_SUCCESS, "{group}", groupName, "{player}", playerTargetName);
    }

    @Subcommand("msg")
    @CommandPermission("fmessage.group.msg")
    @Description("{@@acf-fmessage.group_msg_help_description}")
    @CommandCompletion("@groups")
    @Syntax("[groupName] [message]")
    public void onMSG(CommandIssuer issuer, String groupName, String message) {
        ProxyPlayer sender = requirePlayer(issuer);
        if (sender == null) {
            return;
        }

        int groupId = groupCommandManager.getMemberGroupId(groupName, sender.getUniqueId());
        if (groupId == -1) {
            sendNotAMemberError(issuer, groupName);
            return;
        }

        Group group = core.getGroups().get(groupId);
        if (group == null) {
            sendNotAMemberError(issuer, groupName);
            return;
        }

        for (Member member : group.getMember()) {
            Optional<ProxyPlayer> target = core.getPlatform().getPlayer(member.getUuid());
            if (target.isEmpty()) {
                continue;
            }
            core.getMessenger().issuerOf(target.get()).sendInfo(MessageKeys.GROUP_MSG,
                    "{group}", group.getName(),
                    "{player}", sender.getName(),
                    "{message}", message);
        }
    }

    @Subcommand("toggle")
    @CommandPermission("fmessage.group.toggle")
    @Description("{@@acf-fmessage.group_toggle_help_description}")
    @CommandCompletion("@groups")
    @Syntax("[groupName]")
    public void onToggle(CommandIssuer issuer, String groupName) {
        ProxyPlayer sender = requirePlayer(issuer);
        if (sender == null) {
            return;
        }

        int groupId = groupCommandManager.getMemberGroupId(groupName, sender.getUniqueId());
        if (groupId == -1) {
            sendNotAMemberError(issuer, groupName);
            return;
        }

        if (groupMemberCommandManager.getToggle(groupId, sender.getUniqueId())) {
            groupMemberCommandManager.setToggle(groupId, sender.getUniqueId(), 0);
            core.updateGroups();

            issuer.sendInfo(MessageKeys.GROUP_TOGGLE_DESACTIVATE, "{group}", groupName);
            return;
        }

        // A toggle on another group is still active: tell the player instead of silently doing nothing.
        if (groupMemberCommandManager.alreadyToggle(sender.getUniqueId())) {
            issuer.sendInfo(MessageKeys.GROUP_ALREADY_TOGGLE, "{group}", groupName);
            return;
        }

        groupMemberCommandManager.setToggle(groupId, sender.getUniqueId(), 1);
        core.updateGroups();

        issuer.sendInfo(MessageKeys.GROUP_TOGGLE_ACTIVATE, "{group}", groupName);
    }

    private void sendNotAMemberError(CommandIssuer issuer, String groupName) {
        if (groupCommandManager.groupExist(groupName)) {
            issuer.sendInfo(MessageKeys.GROUP_MEMBER_NOT_INGROUP, "{group}", groupName);
            return;
        }
        issuer.sendInfo(MessageKeys.GROUP_CANNOT_EXIST, "{group}", groupName);
    }
}
