
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

package fr.florianpal.fmessage.queries;

import fr.florianpal.fmessage.IDatabaseTable;
import fr.florianpal.fmessage.managers.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Name to UUID directory, filled every time a player joins the network.
 * It is what lets /ignore and /unignore accept a player who is currently offline.
 */
public class PlayerQueries implements IDatabaseTable {

    private static final String GET_PLAYERS = "SELECT * FROM fm_players";
    private static final String SAVE_PLAYER = "INSERT INTO fm_players (uuid, name) VALUES(?,?) ON DUPLICATE KEY UPDATE name=?";

    private final DatabaseManager databaseManager;

    public PlayerQueries(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void savePlayer(UUID uuid, String name) {
        PreparedStatement statement = null;
        try (Connection connection = databaseManager.getConnection()) {
            statement = connection.prepareStatement(SAVE_PLAYER);
            statement.setString(1, uuid.toString());
            statement.setString(2, name);
            statement.setString(3, name);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @return every known player, keyed by lower-cased name so lookups are case-insensitive.
     */
    public Map<String, UUID> getPlayers() {
        Map<String, UUID> players = new HashMap<>();

        PreparedStatement statement = null;
        ResultSet result = null;
        try (Connection connection = databaseManager.getConnection()) {
            statement = connection.prepareStatement(GET_PLAYERS);
            result = statement.executeQuery();

            while (result.next()) {
                UUID uuid = UUID.fromString(result.getString(1));
                String name = result.getString(2);
                players.put(name.toLowerCase(), uuid);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (result != null) {
                    result.close();
                }
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return players;
    }

    @Override
    public String[] getTable() {
        return new String[]{"fm_players",
                "`uuid` VARCHAR(36) NOT NULL," +
                        "`name` VARCHAR(16) NOT NULL, " +
                        "PRIMARY KEY (`uuid`), " +
                        "KEY `idx_fm_players_name` (`name`)",
                "DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci"};
    }
}
