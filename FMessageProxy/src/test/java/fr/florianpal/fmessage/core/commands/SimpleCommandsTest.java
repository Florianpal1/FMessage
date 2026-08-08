package fr.florianpal.fmessage.core.commands;

import fr.florianpal.fmessage.core.languages.MessageKeys;
import fr.florianpal.fmessage.fakes.FakeProxyPlayer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The four short commands plus /unignore, grouped so the shared rig is set up once.
 */
class SimpleCommandsTest extends CommandTestSupport {

    @BeforeEach
    void setUp() {
        setUpCore();
    }

    @Nested
    @DisplayName("/chatspy")
    class Spy {

        private MSGSpyCommand command;

        @BeforeEach
        void createCommand() {
            command = new MSGSpyCommand(core);
        }

        @Test
        void turns_spy_mode_on() {
            command.onMSGSpy(issuer);

            assertThat(state.isSpy(alice.getUniqueId())).isTrue();
            verify(issuer).sendInfo(MessageKeys.SPY_ACTIVATE);
        }

        @Test
        void turns_spy_mode_off_again() {
            state.addSpy(alice.getUniqueId());

            command.onMSGSpy(issuer);

            assertThat(state.isSpy(alice.getUniqueId())).isFalse();
            verify(issuer).sendInfo(MessageKeys.SPY_DESACTIVATE);
        }

        @Test
        void refuses_the_console() {
            var console = consoleIssuer();

            command.onMSGSpy(console);

            verify(console).sendError(co.aikar.commands.MessageKeys.NOT_ALLOWED_ON_CONSOLE);
        }
    }

    @Nested
    @DisplayName("/msgtoggle")
    class MessageToggle {

        private MSGToggleCommand command;

        @BeforeEach
        void createCommand() {
            command = new MSGToggleCommand(core);
        }

        @Test
        void mutes_private_messages() {
            command.onMSGToggle(issuer);

            assertThat(state.hasMessagesDisabled(alice.getUniqueId())).isTrue();
            verify(issuer).sendInfo(MessageKeys.MSGTOGGLE_DESACTIVATE);
        }

        @Test
        void unmutes_private_messages() {
            state.disableMessages(alice.getUniqueId());

            command.onMSGToggle(issuer);

            assertThat(state.hasMessagesDisabled(alice.getUniqueId())).isFalse();
            verify(issuer).sendInfo(MessageKeys.MSGTOGGLE_ACTIVATE);
        }
    }

    @Nested
    @DisplayName("/nick")
    class Nick {

        private NickCommand command;

        @BeforeEach
        void createCommand() {
            command = new NickCommand(core);
        }

        @Test
        void sets_a_first_nickname() {
            when(nicknames.getCachedNickName(alice.getUniqueId())).thenReturn(null);

            command.onNick(issuer, "Alix");

            verify(nicknames).addNickName(alice.getUniqueId(), "Alix");
            verify(issuer).sendInfo(MessageKeys.NICKNAME_ADD, "{NewNickName}", "Alix");
        }

        @Test
        void updates_an_existing_nickname_and_recalls_the_old_one() {
            when(nicknames.getCachedNickName(alice.getUniqueId())).thenReturn("Ancien");

            command.onNick(issuer, "Alix");

            verify(nicknames).updateNickName(alice.getUniqueId(), "Alix");
            verify(issuer).sendInfo(MessageKeys.NICKNAME_UPDATE,
                    "{NewNickName}", "Alix", "{OldNickName}", "Ancien");
        }

        @Test
        void removes_the_nickname_when_called_without_an_argument() {
            command.onNick(issuer, null);

            verify(nicknames).removeNickName(alice.getUniqueId());
            verify(issuer).sendInfo(MessageKeys.NICKNAME_REMOVE);
        }

        @Test
        void removes_the_nickname_when_called_with_an_empty_argument() {
            command.onNick(issuer, "");

            verify(nicknames).removeNickName(alice.getUniqueId());
        }
    }

    @Nested
    @DisplayName("/unignore")
    class UnIgnore {

        private UnIgnoreCommand command;

        @BeforeEach
        void createCommand() {
            command = new UnIgnoreCommand(core);
        }

        @Test
        void stops_ignoring_an_online_player_and_prunes_the_cache() {
            FakeProxyPlayer bob = platform.addPlayer("Bob");
            when(ignores.ignoreExist(alice.getUniqueId(), bob.getUniqueId())).thenReturn(true);

            List<UUID> cached = new ArrayList<>(List.of(bob.getUniqueId()));
            when(core.getIgnores()).thenReturn(Map.of(alice.getUniqueId(), cached));

            command.onUnIgnore(issuer, "Bob");

            verify(ignores).removeIgnore(alice.getUniqueId(), bob.getUniqueId());
            assertThat(cached).isEmpty();
            verify(issuer).sendInfo(MessageKeys.UNIGNORE_SUCCESS, "{player}", "Bob");
        }

        @Test
        void stops_ignoring_an_offline_player_through_the_name_directory() {
            UUID offline = UUID.randomUUID();
            when(players.getUuid("Bob")).thenReturn(offline);
            when(ignores.ignoreExist(alice.getUniqueId(), offline)).thenReturn(true);
            when(core.getIgnores()).thenReturn(Map.of());

            command.onUnIgnore(issuer, "Bob");

            verify(ignores).removeIgnore(alice.getUniqueId(), offline);
        }

        @Test
        void reports_a_name_that_was_never_seen() {
            command.onUnIgnore(issuer, "Fantome");

            verify(issuer).sendInfo(MessageKeys.IGNORE_NOT_EXIST, "{player}", "Fantome");
            verify(ignores, never()).removeIgnore(any(), any());
        }

        @Test
        void reports_a_player_who_was_not_ignored() {
            FakeProxyPlayer bob = platform.addPlayer("Bob");
            when(ignores.ignoreExist(alice.getUniqueId(), bob.getUniqueId())).thenReturn(false);

            command.onUnIgnore(issuer, "Bob");

            verify(issuer).sendInfo(MessageKeys.UNIGNORE_ALREADY, "{player}", "Bob");
            verify(ignores, never()).removeIgnore(any(), any());
        }

        @Test
        void does_not_throw_when_the_cache_holds_no_entry_for_the_sender() {
            FakeProxyPlayer bob = platform.addPlayer("Bob");
            when(ignores.ignoreExist(alice.getUniqueId(), bob.getUniqueId())).thenReturn(true);
            when(core.getIgnores()).thenReturn(Map.of());

            command.onUnIgnore(issuer, "Bob");

            verify(issuer).sendInfo(MessageKeys.UNIGNORE_SUCCESS, "{player}", "Bob");
        }
    }

    @Nested
    @DisplayName("/fmessage reload")
    class Reload {

        @Test
        void reloads_configuration_language_and_every_cache() {
            var configuration = org.mockito.Mockito.mock(
                    fr.florianpal.fmessage.managers.ConfigurationManager.class);
            when(core.getConfigurationManager()).thenReturn(configuration);

            new ReloadCommand(core).onReload(issuer);

            verify(configuration).reload();
            verify(core).reloadLanguage();
            verify(core).updateIgnores();
            verify(core).updateGroups();
            verify(nicknames).reloadNickNames();
            verify(issuer).sendInfo(MessageKeys.RELOAD_SUCCESS);
        }

        @Test
        void is_the_one_command_the_console_may_run() {
            var configuration = org.mockito.Mockito.mock(
                    fr.florianpal.fmessage.managers.ConfigurationManager.class);
            when(core.getConfigurationManager()).thenReturn(configuration);
            var console = consoleIssuer();

            new ReloadCommand(core).onReload(console);

            verify(console).sendInfo(MessageKeys.RELOAD_SUCCESS);
            verify(console, never()).sendError(co.aikar.commands.MessageKeys.NOT_ALLOWED_ON_CONSOLE);
        }
    }
}
