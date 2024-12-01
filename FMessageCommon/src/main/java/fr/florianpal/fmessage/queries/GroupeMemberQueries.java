
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
import fr.florianpal.fmessage.objects.Group;
import fr.florianpal.fmessage.objects.Member;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GroupeMemberQueries implements IDatabaseTable {

    private static final String GET_GROUPS_MEMBERS = "SELECT * FROM fm_groupMembers";
    private static final String GET_MEMBERS = "SELECT * FROM fm_groupMembers where id_group=?";
    private static final String GET_MEMBER = "SELECT * FROM fm_groupMembers where id_group=? and playerMemberUuid=?";
    private static final String GET_TOGGLE = "SELECT toggle FROM fm_groupMembers where id_group=? and playerMemberUuid=?";
    private static final String GET_MEMBER_TOGGLE = "SELECT toggle FROM fm_groupMembers where playerMemberUuid=?";
    private static final String GET_GROUP_BY_TOGGLE = "SELECT * FROM fm_groupMembers where playerMemberUuid=? and toggle=1";


    private static final String ADD_GROUP_MEMBER = "INSERT INTO fm_groupMembers (id_group, playerMemberUuid, toggle) VALUES(?,?,?)";
    private static final String REMOVE_GROUP_MEMBER = "DELETE FROM fm_groupMembers WHERE id_group=? and playerMemberUuid=?";
    private static final String REMOVE_GROUP = "DELETE FROM fm_groupMembers WHERE id_group=?";
    private static final String UPDATE_GROUP_MEMBER_TOGGLE = "UPDATE fm_groupMembers SET toggle=? WHERE id_group=? and playerMemberUuid=?";

    private final DatabaseManager databaseManager;

    public GroupeMemberQueries(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void addGroupeMember(int idGroup, UUID player) {
        PreparedStatement statement = null;
        try (Connection connection = databaseManager.getConnection()) {
            statement = connection.prepareStatement(ADD_GROUP_MEMBER);
            statement.setInt(1, idGroup);
            statement.setString(2, player.toString());
            statement.setInt(3, 0);
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

    public void removeGroupe(int idGroup) {
        PreparedStatement statement = null;
        try (Connection connection = databaseManager.getConnection()) {
            statement = connection.prepareStatement(REMOVE_GROUP);
            statement.setInt(1, idGroup);
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

    public void removeGroupeMember(int idGroup, UUID player) {
        PreparedStatement statement = null;
        try (Connection connection = databaseManager.getConnection()) {
            statement = connection.prepareStatement(REMOVE_GROUP_MEMBER);
            statement.setInt(1, idGroup);
            statement.setString(2, player.toString());
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

    public void setGroupMembers(Group group) {
        List<Member> members = new ArrayList<>();

        PreparedStatement statement = null;
        ResultSet result = null;
        try (Connection connection = databaseManager.getConnection()) {
            statement = connection.prepareStatement(GET_MEMBERS);
            statement.setInt(1, group.getId());
            result = statement.executeQuery();

            while (result.next()) {
                int id = result.getInt(1);
                UUID playerSenderUuid = UUID.fromString(result.getString(2));
                int toggle = result.getInt(3);

                boolean tog = false;
                if(toggle == 1) tog = true;

                members.add(new Member(playerSenderUuid, tog));
            }

            group.setMember(members);
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
    }

    public boolean inGroupMembers(int idGroup, UUID proxiedPlayer) {

        boolean retour = false;
        PreparedStatement statement = null;
        ResultSet result = null;
        try (Connection connection = databaseManager.getConnection()) {
            statement = connection.prepareStatement(GET_MEMBER);
            statement.setInt(1, idGroup);
            statement.setString(2, proxiedPlayer.toString());
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

    public void setGroupeMemberToggle(int idGroup, UUID player, int toggle) {
        PreparedStatement statement = null;
        try (Connection connection = databaseManager.getConnection()) {
            statement = connection.prepareStatement(UPDATE_GROUP_MEMBER_TOGGLE);
            statement.setInt(1, toggle);
            statement.setInt(2, idGroup);
            statement.setString(3, player.toString());

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

    public boolean getGroupeMemberToggle(int idGroup, UUID proxiedPlayer) {

        boolean retour = false;
        PreparedStatement statement = null;
        ResultSet result = null;
        try (Connection connection = databaseManager.getConnection()) {
            statement = connection.prepareStatement(GET_TOGGLE);
            statement.setInt(1, idGroup);
            statement.setString(2, proxiedPlayer.toString());
            result = statement.executeQuery();

            if (result.next()) {
                int toggle = result.getInt(1);
                if(toggle == 1) {
                    retour = true;
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
        return retour;
    }

    public boolean alreadyToggle(UUID uuid) {

        boolean retour = false;
        PreparedStatement statement = null;
        ResultSet result = null;
        try (Connection connection = databaseManager.getConnection()) {
            statement = connection.prepareStatement(GET_MEMBER_TOGGLE);
            statement.setString(1, uuid.toString());
            result = statement.executeQuery();

            while (result.next()) {
                int toggle = result.getInt(1);
                if(toggle == 1) {
                    retour = true;
                    break;
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
        return retour;
    }

    public int getGroupByToggle(UUID proxiedPlayer) {

        int id = -1;
        PreparedStatement statement = null;
        ResultSet result = null;
        try (Connection connection = databaseManager.getConnection()) {
            statement = connection.prepareStatement(GET_GROUP_BY_TOGGLE);
            statement.setString(1, proxiedPlayer.toString());
            result = statement.executeQuery();

            while (result.next()) {
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
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return id;
    }

    @Override
    public String[] getTable() {
        return new String[]{"fm_groupMembers",
                "`id_group` INTEGER NOT NULL," +
                        "`playerMemberUuid` VARCHAR(36) NOT NULL, " +
                        "`toggle` BIT NOT NULL, " +
                        "PRIMARY KEY (`id_group`, `playerMemberUuid`)",
                "DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci"};
    }
}
