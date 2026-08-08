package fr.florianpal.fmessage.core.commands;

import fr.florianpal.fmessage.core.languages.MessageKeys;
import fr.florianpal.fmessage.fakes.FakeProxyPlayer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IgnoreCommandTest extends CommandTestSupport {

    private IgnoreCommand command;

    @BeforeEach
    void setUp() {
        setUpCore();
        command = new IgnoreCommand(core);
    }

    @Test
    void ignores_an_online_player() {
        FakeProxyPlayer bob = platform.addPlayer("Bob");

        command.onIgnore(issuer, "Bob");

        verify(ignores).addIgnore(alice.getUniqueId(), bob.getUniqueId());
        verify(core).updateIgnores();
        verify(issuer).sendInfo(MessageKeys.IGNORE_SUCCESS, "{player}", "Bob");
    }

    @Test
    void resolves_an_offline_player_through_the_name_directory() {
        UUID offline = UUID.randomUUID();
        when(players.getUuid("Bob")).thenReturn(offline);

        command.onIgnore(issuer, "Bob");

        verify(ignores).addIgnore(alice.getUniqueId(), offline);
    }

    @Test
    void refuses_a_name_that_was_never_seen() {
        command.onIgnore(issuer, "Fantome");

        verify(issuer).sendInfo(MessageKeys.IGNORE_NOT_EXIST, "{player}", "Fantome");
        verify(ignores, never()).addIgnore(any(), any());
    }

    @Test
    void refuses_a_player_holding_the_protection_permission() {
        platform.addPlayer(new FakeProxyPlayer("Admin").withPermission("fmessage.cannot_ignore"));

        command.onIgnore(issuer, "Admin");

        verify(issuer).sendInfo(MessageKeys.CANNOT_IGNORE);
        verify(ignores, never()).addIgnore(any(), any());
    }

    @Test
    void refuses_to_ignore_oneself() {
        command.onIgnore(issuer, "Alice");

        verify(issuer).sendInfo(MessageKeys.CANNOT_IGNORE);
        verify(ignores, never()).addIgnore(any(), any());
    }

    @Test
    void refuses_a_player_who_is_already_ignored() {
        FakeProxyPlayer bob = platform.addPlayer("Bob");
        when(ignores.ignoreExist(alice.getUniqueId(), bob.getUniqueId())).thenReturn(true);

        command.onIgnore(issuer, "Bob");

        verify(issuer).sendInfo(MessageKeys.IGNORE_ALREADY, "{player}", "Bob");
        verify(ignores, never()).addIgnore(any(), any());
    }

    @Test
    void refuses_the_console() {
        var console = consoleIssuer();

        command.onIgnore(console, "Bob");

        verify(console).sendError(co.aikar.commands.MessageKeys.NOT_ALLOWED_ON_CONSOLE);
        verify(ignores, never()).addIgnore(any(), any());
    }

    private static UUID any() {
        return org.mockito.ArgumentMatchers.any();
    }
}
