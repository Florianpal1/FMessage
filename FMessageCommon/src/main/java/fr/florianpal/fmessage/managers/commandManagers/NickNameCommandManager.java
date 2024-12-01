
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

import fr.florianpal.fmessage.queries.NickNameQueries;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class NickNameCommandManager {
    private final NickNameQueries nickNameQueries;

    private Map<UUID, String> nicknames = new HashMap<>();

    public NickNameCommandManager(NickNameQueries nickNameQueries) {
        this.nickNameQueries = nickNameQueries;
        this.nicknames = getAllNickName();
    }

    public String getNickName(UUID playerSender) {
        return nickNameQueries.getNickName(playerSender);
    }

    public String getCachedNickName(UUID playerSender) {
        if (nicknames.containsKey(playerSender)) {
            return nicknames.get(playerSender);
        }
        return null;
    }

    public Map<UUID, String> getAllNickName() {
        return nickNameQueries.getAllNickName();
    }

    public void addNickName(UUID playerSender, String name)  {
        nickNameQueries.addNickName(playerSender, name);
        nicknames.put(playerSender, name);
    }

    public void updateNickName(UUID playerSender, String name)  {
        nickNameQueries.updateNickName(playerSender, name);
        nicknames.put(playerSender, name);
    }


    public void removeNickName(UUID playerSender) {
        nickNameQueries.removeNickName(playerSender);
        nicknames.remove(playerSender);
    }

}