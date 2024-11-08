
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
import fr.florianpal.fmessage.queries.NickNameQueries;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class NickNameCommandManager {
    private final NickNameQueries nickNameQueries;

    private Map<UUID, String> nicknames = new HashMap<>();

    public NickNameCommandManager(FMessage plugin) {
        this.nickNameQueries = plugin.getNickNameQueries();
        this.nicknames = getAllNickName();
    }

    public String getNickName(Player playerSender) {
        return nickNameQueries.getNickName(playerSender.getUniqueId());
    }

    public String getCachedNickName(Player playerSender) {
        return nicknames.get(playerSender.getUniqueId());
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

    public void addNickName(Player playerSender, String name)  {
        nickNameQueries.addNickName(playerSender.getUniqueId(), name);
        nicknames.put(playerSender.getUniqueId(), name);
    }

    public void updateNickName(Player playerSender, String name)  {
        nickNameQueries.updateNickName(playerSender.getUniqueId(), name);
        nicknames.put(playerSender.getUniqueId(), name);
    }


    public void removeNickName(Player playerSender) {
        nickNameQueries.removeNickName(playerSender.getUniqueId());
        nicknames.remove(playerSender.getUniqueId());
    }

}