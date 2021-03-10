package fr.florianpal.hypercraftmessage.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.*;
import fr.florianpal.hypercraftmessage.HypercraftMessage;
import fr.florianpal.hypercraftmessage.languages.MessageKeys;
import fr.florianpal.hypercraftmessage.managers.commandManagers.CommandManager;
import fr.florianpal.hypercraftmessage.managers.commandManagers.GroupCommandManager;
import fr.florianpal.hypercraftmessage.managers.commandManagers.GroupMemberCommandManager;
import fr.florianpal.hypercraftmessage.objects.Group;
import fr.florianpal.hypercraftmessage.objects.Member;
import net.md_5.bungee.api.connection.ProxiedPlayer;

@CommandAlias("group")
public class GroupCommand extends BaseCommand {

    private HypercraftMessage plugin;
    private CommandManager commandManager;
    private GroupCommandManager groupCommandManager;
    private GroupMemberCommandManager groupMemberCommandManager;

    public GroupCommand(HypercraftMessage plugin) {
        this.plugin = plugin;
        this.commandManager = plugin.getCommandManager();
        this.groupCommandManager = plugin.getGroupCommandManager();
        this.groupMemberCommandManager = plugin.getGroupMemberCommandManager();
    }

    @Subcommand("create")
    @CommandPermission("hc.group.create")
    @Description("{@@hypercraft.group_create_help_description}")
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
    @CommandPermission("hc.group.remove")
    @Description("{@@hypercraft.group_create_help_description}")
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
    @CommandPermission("hc.group.member.add")
    @Description("{@@hypercraft.group_member_create_help_description}")
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
    @CommandPermission("hc.group.member.kick")
    @Description("{@@hypercraft.group_member_kickv_help_description}")
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
    @CommandPermission("hc.group.msg")
    @Description("{@@hypercraft.group_msg_help_description}")
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
    @CommandPermission("hc.group.toggle")
    @Description("{@@hypercraft.group_toggle_help_description}")
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
