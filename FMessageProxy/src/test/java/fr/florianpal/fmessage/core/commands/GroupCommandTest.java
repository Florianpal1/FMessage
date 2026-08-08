package fr.florianpal.fmessage.core.commands;

import fr.florianpal.fmessage.core.languages.MessageKeys;
import fr.florianpal.fmessage.fakes.FakeProxyPlayer;
import fr.florianpal.fmessage.objects.Group;
import fr.florianpal.fmessage.objects.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GroupCommandTest extends CommandTestSupport {

    private GroupCommand command;

    @BeforeEach
    void setUp() {
        setUpCore();
        command = new GroupCommand(core);
    }

    private Group registerGroup(int id, String name, UUID owner, UUID... members) {
        Group group = new Group(id, owner, name);
        group.setMember(java.util.Arrays.stream(members).map(uuid -> new Member(uuid, false)).toList());
        groups.put(id, group);
        return group;
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        void creates_the_group_and_adds_the_owner_as_a_member() {
            when(groupCommands.groupExist(alice.getUniqueId(), "Amis")).thenReturn(false);
            when(groupCommands.getGroupId(alice.getUniqueId(), "Amis")).thenReturn(3);

            command.onCreate(issuer, "Amis");

            verify(groupCommands).addGroup(alice.getUniqueId(), "Amis");
            verify(groupMembers).addGroupMember(3, alice.getUniqueId());
            verify(core).updateGroups();
            verify(issuer).sendInfo(MessageKeys.GROUP_CREATE_SUCCESS, "{group}", "Amis");
        }

        @Test
        void refuses_a_name_the_player_already_uses() {
            when(groupCommands.groupExist(alice.getUniqueId(), "Amis")).thenReturn(true);

            command.onCreate(issuer, "Amis");

            verify(issuer).sendInfo(MessageKeys.GROUP_ALREADY_EXIST, "{group}", "Amis");
            verify(groupCommands, never()).addGroup(any(), any());
        }
    }

    @Nested
    @DisplayName("remove")
    class Remove {

        @Test
        void removes_the_group_and_its_members() {
            when(groupCommands.groupExist(alice.getUniqueId(), "Amis")).thenReturn(true);
            when(groupCommands.getGroupId(alice.getUniqueId(), "Amis")).thenReturn(3);

            command.onRemove(issuer, "Amis");

            verify(groupCommands).removeGroup(3);
            verify(groupMembers).removeGroup(3);
            verify(issuer).sendInfo(MessageKeys.GROUP_REMOVE_SUCCESS, "{group}", "Amis");
        }

        @Test
        void refuses_a_group_the_player_does_not_own() {
            when(groupCommands.groupExist(alice.getUniqueId(), "Amis")).thenReturn(false);

            command.onRemove(issuer, "Amis");

            verify(issuer).sendInfo(MessageKeys.GROUP_CANNOT_EXIST, "{group}", "Amis");
            verify(groupCommands, never()).removeGroup(anyInt());
        }
    }

    @Nested
    @DisplayName("member add")
    class AddMember {

        @Test
        void adds_an_online_player() {
            FakeProxyPlayer bob = platform.addPlayer("Bob");
            when(groupCommands.groupExist(alice.getUniqueId(), "Amis")).thenReturn(true);
            when(groupCommands.getGroupId(alice.getUniqueId(), "Amis")).thenReturn(3);
            when(groupMembers.inGroup(3, bob.getUniqueId())).thenReturn(false);

            command.onAddMember(issuer, "Amis", "Bob");

            verify(groupMembers).addGroupMember(3, bob.getUniqueId());
            verify(issuer).sendInfo(MessageKeys.GROUP_ADDMEMBER_SUCCESS, "{group}", "Amis", "{player}", "Bob");
        }

        @Test
        void refuses_an_offline_player() {
            command.onAddMember(issuer, "Amis", "Fantome");

            verify(issuer).sendInfo(MessageKeys.PLAYER_OFFLINE);
            verify(groupMembers, never()).addGroupMember(anyInt(), any());
        }

        @Test
        void refuses_when_the_group_does_not_belong_to_the_player() {
            platform.addPlayer("Bob");
            when(groupCommands.groupExist(alice.getUniqueId(), "Amis")).thenReturn(false);

            command.onAddMember(issuer, "Amis", "Bob");

            verify(issuer).sendInfo(MessageKeys.GROUP_CANNOT_EXIST, "{group}", "Amis");
            verify(groupMembers, never()).addGroupMember(anyInt(), any());
        }

        @Test
        void refuses_a_player_already_in_the_group() {
            FakeProxyPlayer bob = platform.addPlayer("Bob");
            when(groupCommands.groupExist(alice.getUniqueId(), "Amis")).thenReturn(true);
            when(groupCommands.getGroupId(alice.getUniqueId(), "Amis")).thenReturn(3);
            when(groupMembers.inGroup(3, bob.getUniqueId())).thenReturn(true);

            command.onAddMember(issuer, "Amis", "Bob");

            verify(issuer).sendInfo(MessageKeys.GROUP_ALREADY_IN_GROUP, "{group}", "Amis", "{player}", "Bob");
            verify(groupMembers, never()).addGroupMember(anyInt(), any());
        }
    }

    @Nested
    @DisplayName("member kick")
    class KickMember {

        @Test
        void removes_a_member() {
            FakeProxyPlayer bob = platform.addPlayer("Bob");
            when(groupCommands.groupExist(alice.getUniqueId(), "Amis")).thenReturn(true);
            when(groupCommands.getGroupId(alice.getUniqueId(), "Amis")).thenReturn(3);
            when(groupMembers.inGroup(3, bob.getUniqueId())).thenReturn(true);

            command.onRemoveMember(issuer, "Amis", "Bob");

            verify(groupMembers).removeGroupMember(3, bob.getUniqueId());
            verify(issuer).sendInfo(MessageKeys.GROUP_REMOVEMEMBER_SUCCESS, "{group}", "Amis", "{player}", "Bob");
        }

        @Test
        void refuses_a_player_who_is_not_a_member() {
            FakeProxyPlayer bob = platform.addPlayer("Bob");
            when(groupCommands.groupExist(alice.getUniqueId(), "Amis")).thenReturn(true);
            when(groupCommands.getGroupId(alice.getUniqueId(), "Amis")).thenReturn(3);
            when(groupMembers.inGroup(3, bob.getUniqueId())).thenReturn(false);

            command.onRemoveMember(issuer, "Amis", "Bob");

            verify(issuer).sendInfo(MessageKeys.GROUP_MEMBER_NOT_INGROUP, "{group}", "Amis", "{player}", "Bob");
            verify(groupMembers, never()).removeGroupMember(anyInt(), any());
        }
    }

    @Nested
    @DisplayName("msg")
    class GroupMessage {

        @Test
        void delivers_to_every_online_member() {
            FakeProxyPlayer bob = platform.addPlayer("Bob");
            registerGroup(3, "Amis", alice.getUniqueId(), alice.getUniqueId(), bob.getUniqueId());
            when(groupCommands.getMemberGroupId("Amis", alice.getUniqueId())).thenReturn(3);

            command.onMSG(issuer, "Amis", "coucou");

            verify(messenger.issuerFor(bob.getUniqueId())).sendInfo(MessageKeys.GROUP_MSG,
                    "{group}", "Amis", "{player}", "Alice", "{message}", "coucou");
        }

        @Test
        void skips_members_who_are_offline() {
            UUID absent = UUID.randomUUID();
            registerGroup(3, "Amis", alice.getUniqueId(), absent);
            when(groupCommands.getMemberGroupId("Amis", alice.getUniqueId())).thenReturn(3);

            command.onMSG(issuer, "Amis", "coucou");

            verify(messenger.issuerFor(absent), never())
                    .sendInfo(any(co.aikar.locales.MessageKeyProvider.class), any(String[].class));
        }

        @Test
        void tells_a_non_member_that_they_are_not_in_the_group() {
            when(groupCommands.getMemberGroupId("Amis", alice.getUniqueId())).thenReturn(-1);
            when(groupCommands.groupExist("Amis")).thenReturn(true);

            command.onMSG(issuer, "Amis", "coucou");

            verify(issuer).sendInfo(MessageKeys.GROUP_MEMBER_NOT_INGROUP, "{group}", "Amis");
        }

        @Test
        void tells_the_player_when_the_group_does_not_exist_at_all() {
            when(groupCommands.getMemberGroupId("Fantome", alice.getUniqueId())).thenReturn(-1);
            when(groupCommands.groupExist("Fantome")).thenReturn(false);

            command.onMSG(issuer, "Fantome", "coucou");

            verify(issuer).sendInfo(MessageKeys.GROUP_CANNOT_EXIST, "{group}", "Fantome");
        }

        @Test
        void survives_a_group_missing_from_the_cache() {
            // La base connaît le groupe, le cache pas encore : on ne doit pas partir en NPE.
            when(groupCommands.getMemberGroupId("Amis", alice.getUniqueId())).thenReturn(3);
            when(groupCommands.groupExist("Amis")).thenReturn(true);

            command.onMSG(issuer, "Amis", "coucou");

            verify(issuer).sendInfo(MessageKeys.GROUP_MEMBER_NOT_INGROUP, "{group}", "Amis");
        }
    }

    @Nested
    @DisplayName("toggle")
    class Toggle {

        @Test
        void turns_the_toggle_on() {
            when(groupCommands.getMemberGroupId("Amis", alice.getUniqueId())).thenReturn(3);
            when(groupMembers.getToggle(3, alice.getUniqueId())).thenReturn(false);
            when(groupMembers.alreadyToggle(alice.getUniqueId())).thenReturn(false);

            command.onToggle(issuer, "Amis");

            verify(groupMembers).setToggle(3, alice.getUniqueId(), 1);
            verify(issuer).sendInfo(MessageKeys.GROUP_TOGGLE_ACTIVATE, "{group}", "Amis");
        }

        @Test
        void turns_the_toggle_off() {
            when(groupCommands.getMemberGroupId("Amis", alice.getUniqueId())).thenReturn(3);
            when(groupMembers.getToggle(3, alice.getUniqueId())).thenReturn(true);

            command.onToggle(issuer, "Amis");

            verify(groupMembers).setToggle(3, alice.getUniqueId(), 0);
            verify(issuer).sendInfo(MessageKeys.GROUP_TOGGLE_DESACTIVATE, "{group}", "Amis");
        }

        @Test
        void refuses_a_second_toggle_while_another_group_is_active() {
            when(groupCommands.getMemberGroupId("Amis", alice.getUniqueId())).thenReturn(3);
            when(groupMembers.getToggle(3, alice.getUniqueId())).thenReturn(false);
            when(groupMembers.alreadyToggle(alice.getUniqueId())).thenReturn(true);

            command.onToggle(issuer, "Amis");

            verify(issuer).sendInfo(MessageKeys.GROUP_ALREADY_TOGGLE, "{group}", "Amis");
            verify(groupMembers, never()).setToggle(anyInt(), any(), anyInt());
        }
    }

    @Test
    void every_subcommand_refuses_the_console() {
        var console = consoleIssuer();

        command.onCreate(console, "Amis");
        command.onRemove(console, "Amis");
        command.onAddMember(console, "Amis", "Bob");
        command.onRemoveMember(console, "Amis", "Bob");
        command.onMSG(console, "Amis", "coucou");
        command.onToggle(console, "Amis");

        verify(console, org.mockito.Mockito.times(6))
                .sendError(co.aikar.commands.MessageKeys.NOT_ALLOWED_ON_CONSOLE);
    }
}
