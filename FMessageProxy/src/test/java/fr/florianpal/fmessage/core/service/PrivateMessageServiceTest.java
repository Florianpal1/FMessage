package fr.florianpal.fmessage.core.service;

import co.aikar.commands.CommandIssuer;
import co.aikar.locales.MessageKeyProvider;
import fr.florianpal.fmessage.configurations.ChatConfig;
import fr.florianpal.fmessage.core.languages.MessageKeys;
import fr.florianpal.fmessage.core.state.SessionState;
import fr.florianpal.fmessage.fakes.FakeMessenger;
import fr.florianpal.fmessage.fakes.FakeProxyPlatform;
import fr.florianpal.fmessage.fakes.FakeProxyPlayer;
import fr.florianpal.fmessage.managers.commandManagers.IgnoreCommandManager;
import fr.florianpal.fmessage.managers.commandManagers.NickNameCommandManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PrivateMessageServiceTest {

    private static final String COLORS = "fmessage.colors";
    private static final String NICK_COLORS = "fmessage.nick.colors";

    private FakeProxyPlatform platform;
    private FakeMessenger messenger;
    private ChatConfig config;
    private IgnoreCommandManager ignores;
    private NickNameCommandManager nicknames;
    private SessionState state;
    private PrivateMessageService service;

    private FakeProxyPlayer alice;
    private FakeProxyPlayer bob;

    @BeforeEach
    void setUp() {
        platform = new FakeProxyPlatform();
        messenger = new FakeMessenger();
        state = new SessionState();

        ignores = mock(IgnoreCommandManager.class);
        nicknames = mock(NickNameCommandManager.class);

        config = mock(ChatConfig.class);
        when(config.getTargetChatFormat()).thenReturn("T {sender}>{target}: {message}");
        when(config.getSenderChatFormat()).thenReturn("S {sender}>{target}: {message}");
        when(config.getSpyChatFormat()).thenReturn("SPY {sender}>{target}: {message}");
        when(config.isNotificationSoundEnabled()).thenReturn(true);
        when(config.getNotificationSound()).thenReturn("entity.experience_orb.pickup");

        alice = platform.addPlayer("Alice");
        bob = platform.addPlayer("Bob");

        service = new PrivateMessageService(platform, messenger, () -> config,
                ignores, nicknames, state);
    }

    private CommandIssuer issuerOf(FakeProxyPlayer player) {
        return messenger.issuerFor(player.getUniqueId());
    }

    @Nested
    @DisplayName("blocage par /ignore")
    class Ignores {

        @Test
        void tells_the_sender_when_they_ignore_the_target_and_delivers_nothing() {
            when(ignores.ignoreExist(alice.getUniqueId(), bob.getUniqueId())).thenReturn(true);

            service.send(alice, bob, "salut");

            verify(issuerOf(alice)).sendInfo(MessageKeys.SENDER_IGNORE_MESSAGE);
            assertThat(bob.received).isEmpty();
            assertThat(alice.received).isEmpty();
        }

        @Test
        void tells_the_sender_when_the_target_ignores_them_and_delivers_nothing() {
            when(ignores.ignoreExist(bob.getUniqueId(), alice.getUniqueId())).thenReturn(true);

            service.send(alice, bob, "salut");

            verify(issuerOf(alice)).sendInfo(MessageKeys.TARGET_IGNORE_MESSAGE);
            assertThat(bob.received).isEmpty();
        }
    }

    @Nested
    @DisplayName("blocage par /msgtoggle")
    class MessageToggle {

        @Test
        void refuses_when_the_sender_has_muted_their_own_messages() {
            state.disableMessages(alice.getUniqueId());

            service.send(alice, bob, "salut");

            verify(issuerOf(alice)).sendInfo(MessageKeys.SENDER_MSGTOGGLE);
            assertThat(bob.received).isEmpty();
        }

        @Test
        void refuses_and_names_the_target_when_the_target_has_muted_messages() {
            state.disableMessages(bob.getUniqueId());

            service.send(alice, bob, "salut");

            verify(issuerOf(alice)).sendInfo(MessageKeys.TARGET_MSGTOGGLE, "{player}", "Bob");
            assertThat(bob.received).isEmpty();
        }
    }

    @Nested
    @DisplayName("livraison nominale")
    class HappyPath {

        @Test
        void delivers_the_target_format_to_the_target() {
            service.send(alice, bob, "salut");

            assertThat(bob.received).containsExactly("T Alice>Bob: salut");
        }

        @Test
        void delivers_the_sender_format_to_the_sender() {
            service.send(alice, bob, "salut");

            assertThat(alice.received).containsExactly("S Alice>Bob: salut");
        }

        @Test
        void records_the_reply_target_in_both_directions() {
            service.send(alice, bob, "salut");

            assertThat(state.getPreviousPlayer(alice.getUniqueId())).contains(bob.getUniqueId());
            assertThat(state.getPreviousPlayer(bob.getUniqueId())).contains(alice.getUniqueId());
        }

        @Test
        void prefers_the_nickname_over_the_account_name() {
            when(nicknames.getCachedNickName(alice.getUniqueId())).thenReturn("Alix");

            service.send(alice, bob, "salut");

            assertThat(bob.received).containsExactly("T Alix>Bob: salut");
        }

        @Test
        void falls_back_to_the_account_name_when_the_nickname_is_empty() {
            when(nicknames.getCachedNickName(alice.getUniqueId())).thenReturn("");

            service.send(alice, bob, "salut");

            assertThat(bob.received).containsExactly("T Alice>Bob: salut");
        }
    }

    @Nested
    @DisplayName("permissions de couleur")
    class ColourPermissions {

        @Test
        void leaves_message_colours_literal_without_fmessage_colors() {
            service.send(alice, bob, "&csalut");

            assertThat(bob.received).containsExactly("T Alice>Bob: &csalut");
        }

        @Test
        void applies_message_colours_with_fmessage_colors() {
            alice.withPermission(COLORS);

            service.send(alice, bob, "&csalut");

            assertThat(bob.received).containsExactly("T Alice>Bob: §csalut");
        }

        @Test
        void gates_nickname_colours_behind_fmessage_nick_colors() {
            when(nicknames.getCachedNickName(alice.getUniqueId())).thenReturn("&cAlix");

            service.send(alice, bob, "salut");

            assertThat(bob.received).containsExactly("T &cAlix>Bob: salut");
        }

        @Test
        void applies_nickname_colours_independently_of_message_colours() {
            when(nicknames.getCachedNickName(alice.getUniqueId())).thenReturn("&cAlix");
            alice.withPermission(NICK_COLORS);

            service.send(alice, bob, "&esalut");

            // Le pseudo est coloré, le message non : les deux permissions sont distinctes.
            assertThat(bob.received).containsExactly("T §cAlix>Bob: &esalut");
        }

        @Test
        void reads_the_target_nickname_colour_permission_on_the_target() {
            when(nicknames.getCachedNickName(bob.getUniqueId())).thenReturn("&aBobby");
            bob.withPermission(NICK_COLORS);

            service.send(alice, bob, "salut");

            assertThat(bob.received).containsExactly("T Alice>§aBobby: salut");
        }
    }

    @Nested
    @DisplayName("espionnage")
    class Spying {

        @Test
        void delivers_the_spy_format_to_every_spy() {
            FakeProxyPlayer mod = platform.addPlayer("Mod");
            state.addSpy(mod.getUniqueId());

            service.send(alice, bob, "salut");

            assertThat(mod.received).containsExactly("SPY Alice>Bob: salut");
        }

        @Test
        void skips_a_spy_who_went_offline_without_throwing() {
            FakeProxyPlayer mod = platform.addPlayer("Mod");
            state.addSpy(mod.getUniqueId());
            platform.disconnect(mod.getUniqueId());

            service.send(alice, bob, "salut");

            assertThat(bob.received).containsExactly("T Alice>Bob: salut");
        }

        @Test
        void always_colours_the_spy_line_whatever_the_sender_permissions() {
            FakeProxyPlayer mod = platform.addPlayer("Mod");
            state.addSpy(mod.getUniqueId());

            service.send(alice, bob, "&csalut");

            assertThat(mod.received).containsExactly("SPY Alice>Bob: §csalut");
        }

        @Test
        void logs_the_conversation_on_the_console() {
            service.send(alice, bob, "salut");

            assertThat(platform.logger.info).contains("SPY Alice>Bob: salut");
        }
    }

    @Nested
    @DisplayName("son de notification")
    class NotificationSound {

        @Test
        void plays_the_sound_to_the_recipient_only() {
            service.send(alice, bob, "salut");

            assertThat(bob.soundsPlayed).containsExactly("entity.experience_orb.pickup");
            assertThat(alice.soundsPlayed).isEmpty();
        }

        @Test
        void plays_nothing_when_the_sound_is_disabled() {
            when(config.isNotificationSoundEnabled()).thenReturn(false);

            service.send(alice, bob, "salut");

            assertThat(bob.soundsPlayed).isEmpty();
        }

        @Test
        void plays_nothing_when_the_sound_key_is_empty() {
            when(config.getNotificationSound()).thenReturn("");

            service.send(alice, bob, "salut");

            assertThat(bob.soundsPlayed).isEmpty();
        }

        @Test
        void plays_nothing_when_the_message_was_refused() {
            state.disableMessages(bob.getUniqueId());

            service.send(alice, bob, "salut");

            assertThat(bob.soundsPlayed).isEmpty();
        }
    }

    @Nested
    @DisplayName("cible de réponse")
    class ReplyTarget {

        @Test
        void resolves_the_last_correspondent() {
            service.send(alice, bob, "salut");

            assertThat(service.replyTargetOf(alice)).contains(bob);
        }

        @Test
        void is_empty_when_the_correspondent_went_offline() {
            service.send(alice, bob, "salut");
            platform.disconnect(bob.getUniqueId());

            assertThat(service.replyTargetOf(alice)).isEmpty();
        }

        @Test
        void is_empty_when_there_was_no_conversation() {
            assertThat(service.replyTargetOf(alice)).isEmpty();
        }
    }

    @Test
    void never_notifies_the_target_issuer_on_a_successful_delivery() {
        service.send(alice, bob, "salut");

        // Le message passe par sendMessage, pas par une clé de langue.
        verify(issuerOf(bob), never()).sendInfo(any(MessageKeyProvider.class), any(String[].class));
    }
}
