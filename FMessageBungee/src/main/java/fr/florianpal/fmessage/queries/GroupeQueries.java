
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
import fr.florianpal.fmessage.objects.Group;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class GroupeQueries implements IDatabaseTable {

    private static final String GET_GROUPS = "SELECT * FROM hcm_groups";
    private static final String GET_GROUP = "SELECT * FROM hcm_groups where playerOwnerUuid=? and name=?";
    private static final String GET_GROUP_BY_NAME = "SELECT * FROM hcm_groups where name=?";

    private static final String ADD_GROUP = "INSERT INTO hcm_groups (playerOwnerUuid, name) VALUES(?,?)";
    private static final String REMOVE_GROUP = "DELETE FROM hcm_groups WHERE id=?";

    private final DatabaseManager databaseManager;

    public GroupeQueries(FMessage plugin) {
        this.databaseManager = plugin.getDatabaseManager();
    }

    public void addGroupe(ProxiedPlayer playerSender, String name) {
        PreparedStatement statement = null;
        try (Connection connection = databaseManager.getConnection()) {
            statement = connection.prepareStatement(ADD_GROUP);
            statement.setString(1, playerSender.getUniqueId().toString());
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

    public void removeGroup(int id) {
        PreparedStatement statement = null;
        try (Connection connection = databaseManager.getConnection()) {
            statement = connection.prepareStatement(REMOVE_GROUP);
            statement.setInt(1, id);
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

    public Map<Integer, Group> getGroups() {
        Map<Integer, Group> groups = new HashMap<>();

        PreparedStatement statement = null;
        ResultSet result = null;
        try (Connection connection = databaseManager.getConnection()) {
            statement = connection.prepareStatement(GET_GROUPS);
            result = statement.executeQuery();

            while (result.next()) {
                int id = result.getInt(1);
                UUID playerSenderUuid = UUID.fromString(result.getString(2));
                String name = result.getString(3);

                groups.put(id, new Group(id, playerSenderUuid, name));
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
            } catch (SQLException ignored) {
            }
        }

        return groups;
    }

    public int getGroupId(ProxiedPlayer playerSender, String name) {
        PreparedStatement statement = null;
        ResultSet result = null;
        int id = -1;
        try (Connection connection = databaseManager.getConnection()) {
            statement = connection.prepareStatement(GET_GROUP);
            statement.setString(1, playerSender.getUniqueId().toString());
            statement.setString(2, name);
            result = statement.executeQuery();

            if (result.next()) {
                id = result.getInt(1);
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
            } catch (SQLException ignored) {
            }
        }

        return id;
    }

    public int getGroupId(String name) {
        PreparedStatement statement = null;
        ResultSet result = null;
        int id = -1;
        try (Connection connection = databaseManager.getConnection()) {
            statement = connection.prepareStatement(GET_GROUP_BY_NAME);
            statement.setString(1, name);
            result = statement.executeQuery();

            if (result.next()) {
                id = result.getInt(1);
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
            } catch (SQLException ignored) {
            }
        }

        return id;
    }

    public boolean groupExist(ProxiedPlayer playerSender, String name) {
        PreparedStatement statement = null;
        ResultSet result = null;
        boolean retour = false;
        try (Connection connection = databaseManager.getConnection()) {
            statement = connection.prepareStatement(GET_GROUP);
            statement.setString(1, playerSender.getUniqueId().toString());
            statement.setString(2, name);
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
            } catch (SQLException ignored) {
            }
        }

        return retour;
    }

    public boolean groupExist(String name) {
        PreparedStatement statement = null;
        ResultSet result = null;
        boolean retour = false;
        try (Connection connection = databaseManager.getConnection()) {
            statement = connection.prepareStatement(GET_GROUP_BY_NAME);
            statement.setString(1, name);
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
            } catch (SQLException ignored) {
            }
        }

        return retour;
    }

    @Override
    public String[] getTable() {
        return new String[]{"hcm_groups",
                "`id` INTEGER AUTO_INCREMENT," +
                "`playerOwnerUuid` VARCHAR(36) NOT NULL, " +
                        "`name` VARCHAR(36) NOT NULL, " +
                        "PRIMARY KEY (`id`)",
                "DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci"};
    }
}
