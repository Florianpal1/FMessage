
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

package fr.florianpal.fmessage.managers.commandManagers;

import com.velocitypowered.api.proxy.Player;
import fr.florianpal.fmessage.FMessage;
import fr.florianpal.fmessage.objects.Group;
import fr.florianpal.fmessage.queries.GroupeMemberQueries;

import java.util.UUID;


public class GroupMemberCommandManager {
    private FMessage plugin;
    private GroupeMemberQueries groupeMemberQueries;

    public GroupMemberCommandManager(FMessage plugin) {
        this.plugin = plugin;
        this.groupeMemberQueries = plugin.getGroupeMemberQueries();
    }

    public void setGroupsMembers(Group group) {
        groupeMemberQueries.setGroupMembers(group);
    }

    public void addGroupMember(int id_group, Player playerTarget)  {
        groupeMemberQueries.addGroupeMember(id_group, playerTarget);
    }


    public void removeGroupMember(int id_group, Player playerTarget) {
        groupeMemberQueries.removeGroupeMember(id_group, playerTarget);
    }

    public boolean inGroup(int id_group, Player playerTarget) {
        return groupeMemberQueries.inGroupMembers(id_group, playerTarget);
    }

    public boolean getToggle(int id_group, Player playerTarget) {
        return groupeMemberQueries.getGroupeMemberToggle(id_group, playerTarget);
    }

    public int getGroupByToggle(Player playerTarget) {
        return groupeMemberQueries.getGroupByToggle(playerTarget);
    }

    public boolean alreadyToggle(UUID playerTarget) {
        return groupeMemberQueries.alreadyToggle(playerTarget);
    }

    public void setToggle(int id_group, Player playerTarget, int toggle) {
        groupeMemberQueries.setGroupeMemberToggle(id_group, playerTarget,toggle);
    }

    public void removeGroup(int id_group) {
        groupeMemberQueries.removeGroupe(id_group);
    }
}