
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
import fr.florianpal.fmessage.queries.GroupeQueries;

import java.util.Map;
import java.util.UUID;


public class GroupCommandManager {
    private GroupeQueries groupeQueries;

    public GroupCommandManager(GroupeQueries groupeQueries) {
        this.groupeQueries = groupeQueries;
    }

    public Map<Integer, Group> getGroups() {
        return groupeQueries.getGroups();
    }

    public void addGroup(UUID playerSender, String name)  {
        groupeQueries.addGroupe(playerSender, name);
    }

    public int getGroupId(UUID playerSender, String name)  {
        return groupeQueries.getGroupId(playerSender, name);
    }
    public int getGroupId(String name)  {
        return groupeQueries.getGroupId(name);
    }


    public void removeGroup(int id_group) {
        groupeQueries.removeGroup(id_group);
    }

    public boolean groupExist(UUID playerSender, String name) {
        return groupeQueries.groupExist(playerSender, name);
    }

    public boolean groupExist(String name) {
        return groupeQueries.groupExist(name);
    }
}