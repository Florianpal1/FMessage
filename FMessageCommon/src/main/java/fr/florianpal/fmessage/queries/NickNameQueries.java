
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

import fr.florianpal.fmessage.IDatabaseTable;
import fr.florianpal.fmessage.managers.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NickNameQueries implements IDatabaseTable {
    private static final String GET_NICKNAME = "SELECT * FROM fm_nickname where uuid=?";

    private static final String GET_ALL_NICKNAME = "SELECT * FROM fm_nickname";
    private static final String ADD_NICKNAME = "INSERT INTO fm_nickname (uuid, nickname) VALUES(?,?)";

    private static final String UPDATE_NICKNAME = "UPDATE fm_nickname SET nickname=? WHERE uuid=?";
    private static final String REMOVE_NICKNAME = "DELETE FROM fm_nickname WHERE uuid=?";

    private final DatabaseManager databaseManager;

    public NickNameQueries(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void addNickName(UUID uuid, String name) {
        PreparedStatement statement = null;
        try (Connection connection = databaseManager.getConnection()) {
            statement = connection.prepareStatement(ADD_NICKNAME);
            statement.setString(1, uuid.toString());
            statement.setString(2, name);
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

    public void updateNickName(UUID uuid, String name) {
        PreparedStatement statement = null;
        try (Connection connection = databaseManager.getConnection()) {
            statement = connection.prepareStatement(UPDATE_NICKNAME);
            statement.setString(1, name);
            statement.setString(2, uuid.toString());
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

    public void removeNickName(UUID uuid) {
        PreparedStatement statement = null;
        try (Connection connection = databaseManager.getConnection()) {
            statement = connection.prepareStatement(REMOVE_NICKNAME);
            statement.setString(1, uuid.toString());
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

    public String getNickName(UUID uuid) {

        boolean retour = false;
        PreparedStatement statement = null;
        ResultSet result = null;
        try (Connection connection = databaseManager.getConnection()) {
            statement = connection.prepareStatement(GET_NICKNAME);
            statement.setString(1, uuid.toString());
            result = statement.executeQuery();

            if (result.next()) {
                return result.getString(2);
            }
            return null;

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
        return null;
    }

    public Map<UUID, String> getAllNickName() {

        Map<UUID, String> retour = new HashMap<>();
        PreparedStatement statement = null;
        ResultSet result = null;
        try (Connection connection = databaseManager.getConnection()) {
            statement = connection.prepareStatement(GET_ALL_NICKNAME);
            result = statement.executeQuery();

            while (result.next()) {
                retour.put(UUID.fromString(result.getString(1)), result.getString(2));
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
        return new String[]{"fm_nickname",
                "`uuid` VARCHAR(36) NOT NULL," +
                        "`nickname` VARCHAR(200) NOT NULL, " +
                        "PRIMARY KEY (`uuid`)",
                "DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci"};
    }
}
