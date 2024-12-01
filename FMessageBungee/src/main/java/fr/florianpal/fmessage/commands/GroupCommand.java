

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
    @Syntax("[groupName]")
    public void onCreate(ProxiedPlayer playerSender, String groupName) {

        CommandIssuer issuerSender = commandManager.getCommandIssuer(playerSender);
        if (groupCommandManager.groupExist(playerSender, groupName)) {

            issuerSender.sendInfo(MessageKeys.GROUP_ALREADY_EXIST, "{group}", groupName);
            return;
        }

        groupCommandManager.addGroup(playerSender, groupName);
        int groupId = groupCommandManager.getGroupId(playerSender, groupName);
        groupMemberCommandManager.addGroupMember(groupId, playerSender);
        plugin.updateGroups();

        issuerSender.sendInfo(MessageKeys.GROUP_CREATE_SUCCESS, "{group}", groupName);
    }

    @Subcommand("remove")
    @CommandPermission("fmessage.group.remove")
    @Description("{@@fmessage.group_create_help_description}")
    @Syntax("[groupName]")
    public void onRemove(ProxiedPlayer playerSender, String groupName) {

        CommandIssuer issuerSender = commandManager.getCommandIssuer(playerSender);
        if (groupCommandManager.groupExist(playerSender, groupName)) {

            int groupId = groupCommandManager.getGroupId(playerSender, groupName);
            groupCommandManager.removeGroup(groupId);
            groupMemberCommandManager.removeGroup(groupId);
            plugin.updateGroups();


            issuerSender.sendInfo(MessageKeys.GROUP_REMOVE_SUCCESS, "{group}", groupName);
            return;
        }

        issuerSender.sendInfo(MessageKeys.GROUP_CANNOT_EXIST, "{group}", groupName);
    }

    @Subcommand("member add")
    @CommandPermission("fmessage.group.member.add")
    @Description("{@@fmessage.group_member_create_help_description}")
    @Syntax("[groupName] [playerTargetName]")
    public void onAddMember(ProxiedPlayer playerSender, String groupName, String playerTargetName) {
        ProxiedPlayer playerTargetOptional = plugin.getProxy().getPlayer(playerTargetName);
        CommandIssuer issuerSender = commandManager.getCommandIssuer(playerSender);

        if (playerTargetOptional == null) {

            issuerSender.sendInfo(MessageKeys.PLAYER_OFFLINE);
            return;
        }

        ProxiedPlayer playerTarget = playerTargetOptional;
        if (groupCommandManager.groupExist(playerSender, groupName)) {

            int groupId = groupCommandManager.getGroupId(playerSender, groupName);
            if (groupMemberCommandManager.inGroup(groupId, playerSender)) {

                issuerSender.sendInfo(MessageKeys.GROUP_ALREADY_IN_GROUP, "{group}", groupName, "{player}", playerTargetName);
                return;
            }

            groupMemberCommandManager.addGroupMember(groupId, playerTarget);
            plugin.updateGroups();

            issuerSender.sendInfo(MessageKeys.GROUP_ADDMEMBER_SUCCESS, "{group}", groupName, "{player}", playerTargetName);
            return;
        }

        issuerSender.sendInfo(MessageKeys.GROUP_CANNOT_EXIST, "{group}", groupName);
    }

    @Subcommand("member kick")
    @CommandPermission("fmessage.group.member.kick")
    @Description("{@@fmessage.group_member_kickv_help_description}")
    @Syntax("[groupName] [playerTargetName]")
    public void onRemoveMember(ProxiedPlayer playerSender, String groupName, String playerTargetName) {

        ProxiedPlayer playerTargetOptional = plugin.getProxy().getPlayer(playerTargetName);
        CommandIssuer issuerSender = commandManager.getCommandIssuer(playerSender);

        if (playerTargetOptional == null) {

            issuerSender.sendInfo(MessageKeys.PLAYER_OFFLINE);
            return;
        }

        ProxiedPlayer playerTarget = playerTargetOptional;


        if (groupCommandManager.groupExist(playerSender, groupName)) {

            int groupId = groupCommandManager.getGroupId(playerSender, groupName);
            if (groupMemberCommandManager.inGroup(groupId, playerSender)) {

                groupMemberCommandManager.removeGroupMember(groupId, playerTarget);
                plugin.updateGroups();

                issuerSender.sendInfo(MessageKeys.GROUP_REMOVEMEMBER_SUCCESS);
                return;
            }

            issuerSender.sendInfo(MessageKeys.GROUP_MEMBER_NOT_INGROUP);
            return;
        }

        issuerSender.sendInfo(MessageKeys.GROUP_CANNOT_EXIST);
    }

    @Subcommand("msg")
    @CommandPermission("fmessage.group.msg")
    @Description("{@@fmessage.group_msg_help_description}")
    @Syntax("[groupName] [message]")
    public void onMSG(ProxiedPlayer playerSender, String groupName, String message) {

        CommandIssuer issuerSender = commandManager.getCommandIssuer(playerSender);

        if (groupCommandManager.groupExist(groupName)) {
            int groupId = groupCommandManager.getGroupId(groupName);
            if (groupMemberCommandManager.inGroup(groupId, playerSender)) {
                Group group = plugin.getGroups().get(groupId);
                for (Member member : group.getMember()) {
                    ProxiedPlayer playerTargetOptional = plugin.getProxy().getPlayer(member.getUuid());

                    if (playerTargetOptional == null) {
                        return;
                    }

                    ProxiedPlayer playerTarget = playerTargetOptional;

                    CommandIssuer issuerTarget = commandManager.getCommandIssuer(playerTarget);
                    issuerTarget.sendInfo(MessageKeys.GROUP_MSG, "{group}", group.getName(), "{player}", playerSender.getName(), "{message}", message);
                }
                return;
            }

            issuerSender.sendInfo(MessageKeys.GROUP_MEMBER_NOT_INGROUP);
            return;
        }

        issuerSender.sendInfo(MessageKeys.GROUP_CANNOT_EXIST);
    }

    @Subcommand("toggle")
    @CommandPermission("fmessage.group.toggle")
    @Description("{@@fmessage.group_toggle_help_description}")
    @Syntax("[groupName]")
    public void onToggle(ProxiedPlayer playerSender, String groupName) {

        CommandIssuer issuerSender = commandManager.getCommandIssuer(playerSender);

        if (groupCommandManager.groupExist(groupName)) {

            int groupId = groupCommandManager.getGroupId(groupName);
            if (groupMemberCommandManager.inGroup(groupId, playerSender)) {

                if (groupMemberCommandManager.alreadyToggle(playerSender.getUniqueId())) {

                    groupMemberCommandManager.setToggle(groupId, playerSender, 0);

                    issuerSender.sendInfo(MessageKeys.GROUP_TOGGLE_DESACTIVATE, "{group}", groupName);
                    return;
                }
                groupMemberCommandManager.setToggle(groupId, playerSender, 1);

                issuerSender.sendInfo(MessageKeys.GROUP_TOGGLE_ACTIVATE, "{group}", groupName);
                return;
            }

            issuerSender.sendInfo(MessageKeys.GROUP_MEMBER_NOT_INGROUP);
            return;
        }

        issuerSender.sendInfo(MessageKeys.GROUP_CANNOT_EXIST);
    }
}
