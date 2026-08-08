
/*
 * Copyright (C) 2022 Florianpal
 *
 * This program is free software;
 * you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 *  @author Florianpal.
 */

package fr.florianpal.fmessage.managers.commandManagers;

import fr.florianpal.fmessage.queries.PlayerQueries;

import java.util.Map;
import java.util.UUID;

public class PlayerCommandManager {

    private final PlayerQueries playerQueries;

    private Map<String, UUID> players;

    public PlayerCommandManager(PlayerQueries playerQueries) {
        this.playerQueries = playerQueries;
        this.players = playerQueries.getPlayers();
    }

    /**
     * Records a player at login so their name can be resolved while they are offline.
     */
    public void savePlayer(UUID uuid, String name) {
        playerQueries.savePlayer(uuid, name);
        players.put(name.toLowerCase(), uuid);
    }

    /**
     * @return the UUID behind a name, even for an offline player, or null when never seen.
     */
    public UUID getUuid(String name) {
        return players.get(name.toLowerCase());
    }

    public void reloadPlayers() {
        this.players = playerQueries.getPlayers();
    }
}
