package fr.florianpal.fmessage.core.commands;

import fr.florianpal.fmessage.core.languages.MessageKeys;
import fr.florianpal.fmessage.core.service.PrivateMessageService;
import fr.florianpal.fmessage.fakes.FakeProxyPlayer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MSGCommandTest extends CommandTestSupport {

    private PrivateMessageService messages;
    private MSGCommand command;

    @BeforeEach
    void setUp() {
        setUpCore();
        messages = mock(PrivateMessageService.class);
        when(core.getPrivateMessageService()).thenReturn(messages);
        command = new MSGCommand(core);
    }

    @Test
    void delivers_to_an_online_target() {
        FakeProxyPlayer bob = platform.addPlayer("Bob");

        command.onMSG(issuer, "Bob", "salut");

        verify(messages).send(alice, bob, "salut");
    }

    @Test
    void resolves_the_target_case_insensitively() {
        FakeProxyPlayer bob = platform.addPlayer("Bob");

        command.onMSG(issuer, "bob", "salut");

        verify(messages).send(alice, bob, "salut");
    }

    @Test
    void reports_an_offline_target_and_delivers_nothing() {
        command.onMSG(issuer, "Fantome", "salut");

        verify(issuer).sendInfo(MessageKeys.PLAYER_OFFLINE);
        verify(messages, never()).send(any(), any(), anyString());
    }

    @Test
    void refuses_the_console() {
        var console = consoleIssuer();

        command.onMSG(console, "Bob", "salut");

        verify(console).sendError(co.aikar.commands.MessageKeys.NOT_ALLOWED_ON_CONSOLE);
        verify(messages, never()).send(any(), any(), anyString());
    }
}
