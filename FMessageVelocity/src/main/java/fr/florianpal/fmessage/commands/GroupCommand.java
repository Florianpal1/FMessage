

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
import com.velocitypowered.api.proxy.Player;
import fr.florianpal.fmessage.FMessage;
import fr.florianpal.fmessage.languages.MessageKeys;
import fr.florianpal.fmessage.managers.commandManagers.CommandManager;
import fr.florianpal.fmessage.managers.commandManagers.GroupCommandManager;
import fr.florianpal.fmessage.managers.commandManagers.GroupMemberCommandManager;
import fr.florianpal.fmessage.objects.Group;
import fr.florianpal.fmessage.objects.Member;

import java.util.Optional;

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
    public void onCreate(Player playerSender, String groupName) {

        CommandIssuer issuerSender = commandManager.getCommandIssuer(playerSender);
        if (groupCommandManager.groupExist(playerSender.getUniqueId(), groupName)) {

            issuerSender.sendInfo(MessageKeys.GROUP_ALREADY_EXIST, "{group}", groupName);
            return;
        }

        groupCommandManager.addGroup(playerSender.getUniqueId(), groupName);
        int groupId = groupCommandManager.getGroupId(playerSender.getUniqueId(), groupName);
        groupMemberCommandManager.addGroupMember(groupId, playerSender.getUniqueId());
        plugin.updateGroups();

        issuerSender.sendInfo(MessageKeys.GROUP_CREATE_SUCCESS, "{group}", groupName);
    }

    @Subcommand("remove")
    @CommandPermission("fmessage.group.remove")
    @Description("{@@fmessage.group_create_help_description}")
    @Syntax("[groupName]")
    public void onRemove(Player playerSender, String groupName) {

        CommandIssuer issuerSender = commandManager.getCommandIssuer(playerSender);
        if (groupCommandManager.groupExist(playerSender.getUniqueId(), groupName)) {

            int groupId = groupCommandManager.getGroupId(playerSender.getUniqueId(), groupName);
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
    public void onAddMember(Player playerSender, String groupName, String playerTargetName) {
        Optional<Player> playerTargetOptional = plugin.getServer().getPlayer(playerTargetName);
        CommandIssuer issuerSender = commandManager.getCommandIssuer(playerSender);

        if (playerTargetOptional.isEmpty()) {

            issuerSender.sendInfo(MessageKeys.PLAYER_OFFLINE);
            return;
        }

        Player playerTarget = playerTargetOptional.get();
        if (groupCommandManager.groupExist(playerSender.getUniqueId(), groupName)) {

            int groupId = groupCommandManager.getGroupId(playerSender.getUniqueId(), groupName);
            if (groupMemberCommandManager.inGroup(groupId, playerSender.getUniqueId())) {

                issuerSender.sendInfo(MessageKeys.GROUP_ALREADY_IN_GROUP, "{group}", groupName, "{player}", playerTargetName);
                return;
            }

            groupMemberCommandManager.addGroupMember(groupId, playerTarget.getUniqueId());
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
    public void onRemoveMember(Player playerSender, String groupName, String playerTargetName) {

        Optional<Player> playerTargetOptional = plugin.getServer().getPlayer(playerTargetName);
        CommandIssuer issuerSender = commandManager.getCommandIssuer(playerSender);

        if (playerTargetOptional.isEmpty()) {

            issuerSender.sendInfo(MessageKeys.PLAYER_OFFLINE);
            return;
        }

        Player playerTarget = playerTargetOptional.get();


        if (groupCommandManager.groupExist(playerSender.getUniqueId(), groupName)) {

            int groupId = groupCommandManager.getGroupId(playerSender.getUniqueId(), groupName);
            if (groupMemberCommandManager.inGroup(groupId, playerSender.getUniqueId())) {

                groupMemberCommandManager.removeGroupMember(groupId, playerTarget.getUniqueId());
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
    public void onMSG(Player playerSender, String groupName, String message) {

        CommandIssuer issuerSender = commandManager.getCommandIssuer(playerSender);

        if (groupCommandManager.groupExist(groupName)) {
            int groupId = groupCommandManager.getGroupId(groupName);
            if (groupMemberCommandManager.inGroup(groupId, playerSender.getUniqueId())) {
                Group group = plugin.getGroups().get(groupId);
                for (Member member : group.getMember()) {
                    Optional<Player> playerTargetOptional = plugin.getServer().getPlayer(member.getUuid());

                    if (playerTargetOptional.isEmpty()) {
                        return;
                    }

                    Player playerTarget = playerTargetOptional.get();

                    CommandIssuer issuerTarget = commandManager.getCommandIssuer(playerTarget);
                    issuerTarget.sendInfo(MessageKeys.GROUP_MSG, "{group}", group.getName(), "{player}", playerSender.getUsername(), "{message}", message);
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
    public void onToggle(Player playerSender, String groupName) {

        CommandIssuer issuerSender = commandManager.getCommandIssuer(playerSender);

        if (groupCommandManager.groupExist(groupName)) {

            int groupId = groupCommandManager.getGroupId(groupName);
            if (groupMemberCommandManager.inGroup(groupId, playerSender.getUniqueId())) {

                if (groupMemberCommandManager.alreadyToggle(playerSender.getUniqueId())) {

                    groupMemberCommandManager.setToggle(groupId, playerSender.getUniqueId(), 0);

                    issuerSender.sendInfo(MessageKeys.GROUP_TOGGLE_DESACTIVATE, "{group}", groupName);
                    return;
                }
                groupMemberCommandManager.setToggle(groupId, playerSender.getUniqueId(), 1);

                issuerSender.sendInfo(MessageKeys.GROUP_TOGGLE_ACTIVATE, "{group}", groupName);
                return;
            }

            issuerSender.sendInfo(MessageKeys.GROUP_MEMBER_NOT_INGROUP);
            return;
        }

        issuerSender.sendInfo(MessageKeys.GROUP_CANNOT_EXIST);
    }
}
