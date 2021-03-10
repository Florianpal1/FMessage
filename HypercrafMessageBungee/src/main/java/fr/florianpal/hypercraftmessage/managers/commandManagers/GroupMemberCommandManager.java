package fr.florianpal.hypercraftmessage.managers.commandManagers;

import fr.florianpal.hypercraftmessage.HypercraftMessage;
import fr.florianpal.hypercraftmessage.objects.Group;
import fr.florianpal.hypercraftmessage.queries.GroupeMemberQueries;
import fr.florianpal.hypercraftmessage.queries.IgnoreQueries;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.List;
import java.util.Map;
import java.util.UUID;


public class GroupMemberCommandManager {
    private HypercraftMessage plugin;
    private GroupeMemberQueries groupeMemberQueries;

    public GroupMemberCommandManager(HypercraftMessage plugin) {
        this.plugin = plugin;
        this.groupeMemberQueries = plugin.getGroupeMemberQueries();
    }

    public void setGroupsMembers(Group group) {
        groupeMemberQueries.setGroupMembers(group);
    }

    public void addGroupMember(int id_group, ProxiedPlayer playerTarget)  {
        groupeMemberQueries.addGroupeMember(id_group, playerTarget);
    }


    public void removeGroupMember(int id_group, ProxiedPlayer playerTarget) {
        groupeMemberQueries.removeGroupeMember(id_group, playerTarget);
    }

    public boolean inGroup(int id_group, ProxiedPlayer playerTarget) {
        return groupeMemberQueries.inGroupMembers(id_group, playerTarget);
    }

    public boolean getToggle(int id_group, ProxiedPlayer playerTarget) {
        return groupeMemberQueries.getGroupeMemberToggle(id_group, playerTarget);
    }

    public int getGroupByToggle(ProxiedPlayer playerTarget) {
        return groupeMemberQueries.getGroupByToggle(playerTarget);
    }

    public boolean alreadyToggle(UUID playerTarget) {
        return groupeMemberQueries.alreadyToggle(playerTarget);
    }

    public void setToggle(int id_group, ProxiedPlayer playerTarget, int toggle) {
        groupeMemberQueries.setGroupeMemberToggle(id_group, playerTarget,toggle);
    }

    public void removeGroup(int id_group) {
        groupeMemberQueries.removeGroupe(id_group);
    }
}