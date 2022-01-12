

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
 * Last modification : 07/01/2022 23:07
 *
 *  @author Florianpal.
 */

package fr.florianpal.fmessage.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.*;
import fr.florianpal.fmessage.FMessage;
import fr.florianpal.fmessage.languages.MessageKeys;
import fr.florianpal.fmessage.managers.commandManagers.CommandManager;
import fr.florianpal.fmessage.managers.commandManagers.GroupCommandManager;
import fr.florianpal.fmessage.managers.commandManagers.GroupMemberCommandManager;
import fr.florianpal.fmessage.objects.Group;
import fr.florianpal.fmessage.objects.Member;
import net.md_5.bungee.api.connection.ProxiedPlayer;

@CommandAlias("group")
public class GroupCommand extends BaseCommand {

    private final FMessage plugin;
    private final CommandManager commandManager;
    private final GroupCommandManager groupCommandManager;
    private final GroupMemberCommandManager groupMemberCommandManager;

    public GroupCommand(FMessage plugin) {
        this.plugin = plugin;
        this.commandManager = plugin.getCommandManager();
        this.groupCommandManager = plugin.getGroupCommandManager();
        this.groupMemberCommandManager = plugin.getGroupMemberCommandManager();
    }

    @Subcommand("create")
    @CommandPermission("fmessage.group.create")
    @Description("{@@fmessage.group_create_help_description}")
    public void onCreate(ProxiedPlayer playerSender, String groupName) {
        if(groupCommandManager.groupExist(playerSender, groupName)) {
            CommandIssuer issuerTarget = commandManager.getCommandIssuer(playerSender);
            issuerTarget.sendInfo(MessageKeys.GROUP_ALREADY_EXIST, "{group}", groupName);
        } else {
            groupCommandManager.addGroup(playerSender, groupName);
            int id_group = groupCommandManager.getGroupId(playerSender, groupName);
            groupMemberCommandManager.addGroupMember(id_group, playerSender);
            plugin.updateGroups();
            CommandIssuer issuerTarget = commandManager.getCommandIssuer(playerSender);
            issuerTarget.sendInfo(MessageKeys.GROUP_CREATE_SUCCESS, "{group}", groupName);
        }
    }

    @Subcommand("remove")
    @CommandPermission("fmessage.group.remove")
    @Description("{@@fmessage.group_create_help_description}")
    public void onRemove(ProxiedPlayer playerSender, String groupName) {
        if(groupCommandManager.groupExist(playerSender, groupName)) {
            CommandIssuer issuerTarget = commandManager.getCommandIssuer(playerSender);
            issuerTarget.sendInfo(MessageKeys.GROUP_REMOVE_SUCCESS, "{group}", groupName);
            int id_group = groupCommandManager.getGroupId(playerSender, groupName);
            groupCommandManager.removeGroup(id_group);
            groupMemberCommandManager.removeGroup(id_group);
            plugin.updateGroups();
        } else {
            CommandIssuer issuerTarget = commandManager.getCommandIssuer(playerSender);
            issuerTarget.sendInfo(MessageKeys.GROUP_CANNOT_EXIST, "{group}", groupName);
        }
    }

    @Subcommand("member add")
    @CommandPermission("fmessage.group.member.add")
    @Description("{@@fmessage.group_member_create_help_description}")
    public void onAddMember(ProxiedPlayer playerSender, String groupName, String playerTargetName) {
        ProxiedPlayer playerTarget = plugin.getProxy().getPlayer(playerTargetName);

        if(playerTarget != null) {
            if (groupCommandManager.groupExist(playerSender, groupName)) {
                int id_group = groupCommandManager.getGroupId(playerSender, groupName);
                if (groupMemberCommandManager.inGroup(id_group, playerSender)) {
                    CommandIssuer issuerTarget = commandManager.getCommandIssuer(playerSender);
                    issuerTarget.sendInfo(MessageKeys.GROUP_ALREADY_IN_GROUP, "{group}", groupName, "{player}", playerTargetName);
                } else {
                    CommandIssuer issuerTarget = commandManager.getCommandIssuer(playerSender);
                    issuerTarget.sendInfo(MessageKeys.GROUP_ADDMEMBER_SUCCESS, "{group}", groupName, "{player}", playerTargetName);
                    groupMemberCommandManager.addGroupMember(id_group, playerTarget);
                    plugin.updateGroups();
                }

            } else {
                CommandIssuer issuerTarget = commandManager.getCommandIssuer(playerSender);
                issuerTarget.sendInfo(MessageKeys.GROUP_CANNOT_EXIST, "{group}", groupName);
            }
        } else {
            CommandIssuer issuerTarget = commandManager.getCommandIssuer(playerSender);
            issuerTarget.sendInfo(MessageKeys.IGNORE_NOT_EXIST, "{player}", playerTargetName);
        }
    }

    @Subcommand("member kick")
    @CommandPermission("fmessage.group.member.kick")
    @Description("{@@fmessage.group_member_kickv_help_description}")
    public void onRemoveMember(ProxiedPlayer playerSender, String groupName, String playerTargetName) {
        ProxiedPlayer playerTarget = plugin.getProxy().getPlayer(playerTargetName);
        if(playerTarget != null) {
            if (groupCommandManager.groupExist(playerSender, groupName)) {
                int id_group = groupCommandManager.getGroupId(playerSender, groupName);
                if (groupMemberCommandManager.inGroup(id_group, playerSender)) {
                    groupMemberCommandManager.removeGroupMember(id_group, playerTarget);
                    plugin.updateGroups();
                    CommandIssuer issuerTarget = commandManager.getCommandIssuer(playerSender);
                    issuerTarget.sendInfo(MessageKeys.GROUP_REMOVEMEMBER_SUCCESS);
                } else {
                    CommandIssuer issuerTarget = commandManager.getCommandIssuer(playerSender);
                    issuerTarget.sendInfo(MessageKeys.GROUP_MEMBER_NOT_INGROUP);
                }
            } else {
                CommandIssuer issuerTarget = commandManager.getCommandIssuer(playerSender);
                issuerTarget.sendInfo(MessageKeys.GROUP_CANNOT_EXIST);
            }
        } else {
            CommandIssuer issuerTarget = commandManager.getCommandIssuer(playerSender);
            issuerTarget.sendInfo(MessageKeys.IGNORE_NOT_EXIST, "{player}", playerTargetName);
        }
    }

    @Subcommand("msg")
    @CommandPermission("fmessage.group.msg")
    @Description("{@@fmessage.group_msg_help_description}")
    public void onMSG(ProxiedPlayer playerSender, String groupName, String message) {
        if (groupCommandManager.groupExist(groupName)) {
            int id_group = groupCommandManager.getGroupId(groupName);
            if (groupMemberCommandManager.inGroup(id_group, playerSender)) {
                Group group = plugin.getGroups().get(id_group);
                for(Member member : group.getMember()) {
                    ProxiedPlayer playerTarget = plugin.getProxy().getPlayer(member.getUuid());
                    if(playerTarget != null) {
                        CommandIssuer issuerTarget = commandManager.getCommandIssuer(playerTarget);
                        issuerTarget.sendInfo(MessageKeys.GROUP_MSG, "{group}", group.getName(), "{player}", playerSender.getDisplayName(), "{message}", message);
                    }
                }
            } else {
                CommandIssuer issuerTarget = commandManager.getCommandIssuer(playerSender);
                issuerTarget.sendInfo(MessageKeys.GROUP_MEMBER_NOT_INGROUP);
            }
        } else {
            CommandIssuer issuerTarget = commandManager.getCommandIssuer(playerSender);
            issuerTarget.sendInfo(MessageKeys.GROUP_CANNOT_EXIST);
        }
    }

    @Subcommand("toggle")
    @CommandPermission("fmessage.group.toggle")
    @Description("{@@fmessage.group_toggle_help_description}")
    public void onToggle(ProxiedPlayer playerSender, String groupName) {
        if (groupCommandManager.groupExist(groupName)) {
            int id_group = groupCommandManager.getGroupId(groupName);
            if (groupMemberCommandManager.inGroup(id_group, playerSender)) {
                if (groupMemberCommandManager.alreadyToggle(playerSender.getUniqueId())) {
                    groupMemberCommandManager.setToggle(id_group, playerSender, 0);
                    CommandIssuer issuerTarget = commandManager.getCommandIssuer(playerSender);
                    issuerTarget.sendInfo(MessageKeys.GROUP_TOGGLE_DESACTIVATE, "{group}", groupName);
                } else {
                    groupMemberCommandManager.setToggle(id_group, playerSender, 1);
                    CommandIssuer issuerTarget = commandManager.getCommandIssuer(playerSender);
                    issuerTarget.sendInfo(MessageKeys.GROUP_TOGGLE_ACTIVATE, "{group}", groupName);
                }

            } else {
                CommandIssuer issuerTarget = commandManager.getCommandIssuer(playerSender);
                issuerTarget.sendInfo(MessageKeys.GROUP_MEMBER_NOT_INGROUP);

            }
        } else {
            CommandIssuer issuerTarget = commandManager.getCommandIssuer(playerSender);
            issuerTarget.sendInfo(MessageKeys.GROUP_CANNOT_EXIST);
        }
    }
}
