
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

import fr.florianpal.fmessage.objects.Group;
import fr.florianpal.fmessage.queries.GroupeMemberQueries;

import java.util.UUID;


public class GroupMemberCommandManager {
    private final GroupeMemberQueries groupeMemberQueries;

    public GroupMemberCommandManager(GroupeMemberQueries groupeMemberQueries) {
        this.groupeMemberQueries = groupeMemberQueries;
    }

    public void setGroupsMembers(Group group) {
        groupeMemberQueries.setGroupMembers(group);
    }

    public void addGroupMember(int idGroup, UUID playerTarget)  {
        groupeMemberQueries.addGroupeMember(idGroup, playerTarget);
    }


    public void removeGroupMember(int idGroup, UUID playerTarget) {
        groupeMemberQueries.removeGroupeMember(idGroup, playerTarget);
    }

    public boolean inGroup(int idGroup, UUID playerTarget) {
        return groupeMemberQueries.inGroupMembers(idGroup, playerTarget);
    }

    public boolean getToggle(int idGroup, UUID playerTarget) {
        return groupeMemberQueries.getGroupeMemberToggle(idGroup, playerTarget);
    }

    public int getGroupByToggle(UUID playerTarget) {
        return groupeMemberQueries.getGroupByToggle(playerTarget);
    }

    public boolean alreadyToggle(UUID playerTarget) {
        return groupeMemberQueries.alreadyToggle(playerTarget);
    }

    public void setToggle(int idGroup, UUID playerTarget, int toggle) {
        groupeMemberQueries.setGroupeMemberToggle(idGroup, playerTarget, toggle);
    }

    public void removeGroup(int idGroup) {
        groupeMemberQueries.removeGroupe(idGroup);
    }
}