
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
 * Last modification : 07/01/2022 23:05
 *
 *  @author Florianpal.
 */

package fr.florianpal.fmessage.queries;

import fr.florianpal.fmessage.FMessage;
import fr.florianpal.fmessage.IDatabaseTable;
import fr.florianpal.fmessage.managers.DatabaseManager;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class IgnoreQueries implements IDatabaseTable {

    private static final String GET_IGNORES = "SELECT * FROM hcm_ignores";
    private static final String GET_IGNORE = "SELECT * FROM hcm_ignores where playerSenderUuid=? and playerTargetUuid=?";
    private static final String GET_ARE_IGNORES = "SELECT * FROM hcm_ignores where playerTargetUuid=?";
    private static final String ADD_IGNORE = "INSERT INTO hcm_ignores (playerSenderUuid, playerTargetUuid) VALUES(?,?)";
    private static final String REMOVE_IGNORE = "DELETE FROM hcm_ignores WHERE playerSenderUuid=? and playerTargetUuid=?";

    private final DatabaseManager databaseManager;

    public IgnoreQueries(FMessage plugin) {
        this.databaseManager = plugin.getDatabaseManager();
    }

    public void addIgnore(ProxiedPlayer playerSender, ProxiedPlayer playerTarget) {
        PreparedStatement statement = null;
        try (Connection connection = databaseManager.getConnection()) {
            statement = connection.prepareStatement(ADD_IGNORE);
            statement.setString(1, playerSender.getUniqueId().toString());
            statement.setString(2, playerTarget.getUniqueId().toString());
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

    public void removeIgnore(ProxiedPlayer playerSender, ProxiedPlayer playerTarget) {
        PreparedStatement statement = null;
        try (Connection connection = databaseManager.getConnection()) {
            statement = connection.prepareStatement(REMOVE_IGNORE);
            statement.setString(1, playerSender.getUniqueId().toString());
            statement.setString(2, playerTarget.getUniqueId().toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public List<UUID> getAreIgnores(UUID uuid) {
        List<UUID> uuids = new ArrayList<>();

        PreparedStatement statement = null;
        ResultSet result = null;
        try (Connection connection = databaseManager.getConnection()) {
            statement = connection.prepareStatement(GET_ARE_IGNORES);
            statement.setString(1, uuid.toString());
            result = statement.executeQuery();

            while (result.next()) {
                UUID playerSenderUuid = UUID.fromString(result.getString(1));
                UUID playerTargetUuid = UUID.fromString(result.getString(2));

                uuids.add(playerSenderUuid);
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
        return uuids;
    }

    public Map<UUID, List<UUID>> getIgnores() {
        Map<UUID, List<UUID>> ignores = new HashMap<>();

        PreparedStatement statement = null;
        ResultSet result = null;
        try (Connection connection = databaseManager.getConnection()) {
            statement = connection.prepareStatement(GET_IGNORES);
            result = statement.executeQuery();

            while (result.next()) {
                UUID playerSenderUuid = UUID.fromString(result.getString(1));
                UUID playerTargetUuid = UUID.fromString(result.getString(2));

                if (ignores.containsKey(playerSenderUuid)) {
                    ignores.get(playerSenderUuid).add(playerTargetUuid);
                } else {
                    List<UUID> uuids = new ArrayList<>();
                    uuids.add(playerTargetUuid);
                    ignores.put(playerSenderUuid, uuids);
                }
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
        return ignores;
    }

    public boolean ignoreExist(ProxiedPlayer playerSender, ProxiedPlayer playerTarget) {
        PreparedStatement statement = null;
        ResultSet result = null;
        boolean retour = false;
        try (Connection connection = databaseManager.getConnection()) {
            statement = connection.prepareStatement(GET_IGNORE);
            statement.setString(1, playerSender.getUniqueId().toString());
            statement.setString(2, playerTarget.getUniqueId().toString());
            result = statement.executeQuery();

            if (result.next()) {
                retour = true;
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

        return retour;
    }

    @Override
    public String[] getTable() {
        return new String[]{"hcm_ignores",
                "`playerSenderUuid` VARCHAR(36) NOT NULL, " +
                        "`playerTargetUuid` VARCHAR(36) NOT NULL, " +
                        "PRIMARY KEY (`playerSenderUuid`, `playerTargetUuid`)",
                "DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci"};
    }
}
