
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

import fr.florianpal.fmessage.FMessage;
import fr.florianpal.fmessage.queries.IgnoreQueries;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.*;


public class IgnoreCommandManager {
    private FMessage plugin;
    private IgnoreQueries ignoreQueries;

    public IgnoreCommandManager(FMessage plugin) {
        this.plugin = plugin;
        this.ignoreQueries = plugin.getIgnoreQueries();
    }

    public Map<UUID, List<UUID>> getIgnores() {
        return ignoreQueries.getIgnores();
    }

    public List<UUID> getAreIgnores(UUID uuid) {
        return ignoreQueries.getAreIgnores(uuid);
    }

    public void addIgnore(ProxiedPlayer playerSender, ProxiedPlayer playerTarget)  {
        ignoreQueries.addIgnore(playerSender, playerTarget);
    }


    public void removeIgnore(ProxiedPlayer playerSender, ProxiedPlayer playerTarget) {
        ignoreQueries.removeIgnore(playerSender, playerTarget);
    }

    public boolean ignoreExist(ProxiedPlayer playerSender, ProxiedPlayer playerTarget) {
        return ignoreQueries.ignoreExist(playerSender, playerTarget);
    }
}