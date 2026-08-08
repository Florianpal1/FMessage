package fr.florianpal.fmessage.core.service;

import co.aikar.locales.MessageKeyProvider;
import fr.florianpal.fmessage.core.languages.MessageKeys;
import fr.florianpal.fmessage.core.protocol.ChatProtocol;
import fr.florianpal.fmessage.fakes.FakeBackendServer;
import fr.florianpal.fmessage.fakes.FakeMessenger;
import fr.florianpal.fmessage.fakes.FakeProxyPlatform;
import fr.florianpal.fmessage.fakes.FakeProxyPlayer;
import fr.florianpal.fmessage.managers.commandManagers.GroupMemberCommandManager;
import fr.florianpal.fmessage.managers.commandManagers.IgnoreCommandManager;
import fr.florianpal.fmessage.managers.commandManagers.NickNameCommandManager;
import fr.florianpal.fmessage.objects.Group;
import fr.florianpal.fmessage.objects.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChatRelayServiceTest {

    private FakeProxyPlatform platform;
    private FakeMessenger messenger;
    private GroupMemberCommandManager groupMembers;
    private IgnoreCommandManager ignores;
    private NickNameCommandManager nicknames;
    private Map<Integer, Group> groups;
    private ChatRelayService service;

    private FakeProxyPlayer author;
    private FakeBackendServer lobby;
    private FakeBackendServer survie;

    @BeforeEach
    void setUp() {
        platform = new FakeProxyPlatform();
        messenger = new FakeMessenger();
        groupMembers = mock(GroupMemberCommandManager.class);
        ignores = mock(IgnoreCommandManager.class);
        nicknames = mock(NickNameCommandManager.class);
        groups = new HashMap<>();

        when(ignores.getAreIgnores(any())).thenReturn(List.of());

        author = platform.addPlayer("Alice");
        lobby = platform.addServer("lobby");
        survie = platform.addServer("survie");

        service = new ChatRelayService(platform, messenger, groupMembers, ignores, nicknames,
                () -> groups);
    }

    @Nested
    @DisplayName("chat public")
    class PublicChat {

        @Test
        void rebroadcasts_to_every_backend_server() {
            service.handle(frame("Message", author.getUniqueId(), "Alice", "fmt", "salut", true, false));

            assertThat(lobby.sent).hasSize(1);
            assertThat(survie.sent).hasSize(1);
        }

        @Test
        void carries_the_authors_ignore_list_so_backends_can_filter_locally() throws IOException {
            UUID hater = UUID.randomUUID();
            when(ignores.getAreIgnores(author.getUniqueId())).thenReturn(List.of(hater));

            service.handle(frame("Message", author.getUniqueId(), "Alice", "fmt", "salut", true, false));

            assertThat(fieldAt(lobby.sent.get(0), 6)).isEqualTo(hater + ";");
        }

        @Test
        void carries_an_empty_ignore_list_as_an_empty_string() throws IOException {
            service.handle(frame("Message", author.getUniqueId(), "Alice", "fmt", "salut", true, false));

            assertThat(fieldAt(lobby.sent.get(0), 6)).isEmpty();
        }

        @Test
        void forwards_both_colour_flags_in_order() throws IOException {
            service.handle(frame("Message", author.getUniqueId(), "Alice", "fmt", "salut", true, false));

            DataInputStream in = new DataInputStream(new ByteArrayInputStream(lobby.sent.get(0)));
            for (int i = 0; i < 7; i++) {
                in.readUTF();
            }
            assertThat(in.readBoolean()).isTrue();
            assertThat(in.readBoolean()).isFalse();
        }

        @Test
        void injects_the_nickname_the_proxy_knows_about() throws IOException {
            when(nicknames.getCachedNickName(author.getUniqueId())).thenReturn("Alix");

            service.handle(frame("Message", author.getUniqueId(), "Alice", "fmt", "salut", true, false));

            assertThat(fieldAt(lobby.sent.get(0), 3)).isEqualTo("Alix");
        }

        @Test
        void writes_an_empty_nickname_when_there_is_none() throws IOException {
            service.handle(frame("Message", author.getUniqueId(), "Alice", "fmt", "salut", true, false));

            assertThat(fieldAt(lobby.sent.get(0), 3)).isEmpty();
        }

        @Test
        void does_not_throw_when_no_backend_server_is_registered() {
            FakeProxyPlatform empty = new FakeProxyPlatform();
            empty.addPlayer(author);
            ChatRelayService isolated = new ChatRelayService(empty, messenger, groupMembers,
                    ignores, nicknames, () -> groups);

            isolated.handle(frame("Message", author.getUniqueId(), "Alice", "fmt", "salut", true, false));

            assertThat(empty.logger.errors).isEmpty();
        }
    }

    @Nested
    @DisplayName("chat staff")
    class StaffChat {

        @Test
        void rebroadcasts_to_every_backend_server() {
            service.handle(frame("StaffMessage", author.getUniqueId(), "Alice", "fmt", "urgent", null, null));

            assertThat(lobby.sent).hasSize(1);
            assertThat(survie.sent).hasSize(1);
        }

        @Test
        void keeps_the_staff_subchannel() throws IOException {
            service.handle(frame("StaffMessage", author.getUniqueId(), "Alice", "fmt", "urgent", null, null));

            assertThat(fieldAt(lobby.sent.get(0), 0)).isEqualTo(ChatProtocol.STAFF_SUBCHANNEL);
        }

        @Test
        void carries_six_fields_and_no_colour_flags() throws IOException {
            service.handle(frame("StaffMessage", author.getUniqueId(), "Alice", "fmt", "urgent", null, null));

            DataInputStream in = new DataInputStream(new ByteArrayInputStream(lobby.sent.get(0)));
            for (int i = 0; i < 6; i++) {
                in.readUTF();
            }
            assertThat(in.available()).isZero();
        }
    }

    @Nested
    @DisplayName("groupe activé par /group toggle")
    class GroupToggle {

        private FakeProxyPlayer member;

        @BeforeEach
        void groupIsToggled() {
            member = platform.addPlayer("Bob");
            when(groupMembers.alreadyToggle(author.getUniqueId())).thenReturn(true);
            when(groupMembers.getGroupByToggle(author.getUniqueId())).thenReturn(7);
        }

        private Group groupWith(UUID... members) {
            Group group = new Group(7, author.getUniqueId(), "Amis");
            group.setMember(java.util.Arrays.stream(members).map(uuid -> new Member(uuid, true)).toList());
            return group;
        }

        @Test
        void delivers_only_to_the_group_members() {
            groups.put(7, groupWith(author.getUniqueId(), member.getUniqueId()));

            service.handle(frame("Message", author.getUniqueId(), "Alice", "fmt", "coucou", true, false));

            verify(messenger.issuerFor(member.getUniqueId())).sendInfo(MessageKeys.GROUP_MSG,
                    "{group}", "Amis", "{player}", "Alice", "{message}", "coucou");
        }

        @Test
        void never_reaches_public_chat() {
            groups.put(7, groupWith(member.getUniqueId()));

            service.handle(frame("Message", author.getUniqueId(), "Alice", "fmt", "coucou", true, false));

            assertThat(lobby.sent).isEmpty();
            assertThat(survie.sent).isEmpty();
        }

        @Test
        void skips_members_who_are_offline() {
            UUID absent = UUID.randomUUID();
            groups.put(7, groupWith(member.getUniqueId(), absent));

            service.handle(frame("Message", author.getUniqueId(), "Alice", "fmt", "coucou", true, false));

            verify(messenger.issuerFor(absent), never())
                    .sendInfo(any(MessageKeyProvider.class), any(String[].class));
        }

        @Test
        void drops_the_message_when_the_group_no_longer_exists() {
            // Le groupe a été supprimé entre le toggle et le message.
            service.handle(frame("Message", author.getUniqueId(), "Alice", "fmt", "coucou", true, false));

            assertThat(lobby.sent).isEmpty();
            assertThat(platform.logger.info).isEmpty();
        }

        @Test
        void logs_the_group_conversation_on_the_console() {
            groups.put(7, groupWith(member.getUniqueId()));

            service.handle(frame("Message", author.getUniqueId(), "Alice", "fmt", "coucou", true, false));

            assertThat(platform.logger.info).containsExactly("[Amis] Alice : coucou");
        }

        @Test
        void displays_the_nickname_as_the_author_when_there_is_one() {
            when(nicknames.getCachedNickName(author.getUniqueId())).thenReturn("Alix");
            groups.put(7, groupWith(member.getUniqueId()));

            service.handle(frame("Message", author.getUniqueId(), "Alice", "fmt", "coucou", true, false));

            verify(messenger.issuerFor(member.getUniqueId())).sendInfo(MessageKeys.GROUP_MSG,
                    "{group}", "Amis", "{player}", "Alix", "{message}", "coucou");
        }

        @Test
        void takes_priority_over_the_staff_subchannel() {
            groups.put(7, groupWith(member.getUniqueId()));

            service.handle(frame("StaffMessage", author.getUniqueId(), "Alice", "fmt", "coucou", null, null));

            assertThat(lobby.sent).isEmpty();
            verify(messenger.issuerFor(member.getUniqueId())).sendInfo(MessageKeys.GROUP_MSG,
                    "{group}", "Amis", "{player}", "Alice", "{message}", "coucou");
        }
    }

    @Nested
    @DisplayName("trame invalide")
    class MalformedFrame {

        @Test
        void is_dropped_with_a_warning_and_nothing_is_relayed() {
            service.handle(new byte[]{0, 7, 'M', 'e', 's'});

            assertThat(platform.logger.warnings).hasSize(1);
            assertThat(lobby.sent).isEmpty();
        }
    }

    // -- helpers -----------------------------------------------------------------------

    /** Builds a frame the way FMessageBukkit does: no nickname field on the way in. */
    private static byte[] frame(String subchannel, UUID uuid, String displayName,
                                String format, String message,
                                Boolean colors, Boolean nickColors) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(bytes)) {
            out.writeUTF(subchannel);
            out.writeUTF(uuid.toString());
            out.writeUTF(displayName);
            out.writeUTF(format);
            out.writeUTF(message);
            if (colors != null) {
                out.writeBoolean(colors);
                out.writeBoolean(nickColors);
            }
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        return bytes.toByteArray();
    }

    private static String fieldAt(byte[] payload, int index) throws IOException {
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(payload));
        String value = null;
        for (int i = 0; i <= index; i++) {
            value = in.readUTF();
        }
        return value;
    }
}
