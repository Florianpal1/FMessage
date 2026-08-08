package fr.florianpal.fmessage.core.commands;

import fr.florianpal.fmessage.core.languages.MessageKeys;
import fr.florianpal.fmessage.core.service.PrivateMessageService;
import fr.florianpal.fmessage.fakes.FakeProxyPlayer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RCommandTest extends CommandTestSupport {

    private PrivateMessageService messages;
    private RCommand command;

    @BeforeEach
    void setUp() {
        setUpCore();
        messages = mock(PrivateMessageService.class);
        when(core.getPrivateMessageService()).thenReturn(messages);
        command = new RCommand(core);
    }

    @Test
    void replies_to_the_last_correspondent() {
        FakeProxyPlayer bob = platform.addPlayer("Bob");
        state.setPreviousPlayer(alice.getUniqueId(), bob.getUniqueId());
        when(messages.replyTargetOf(alice)).thenReturn(Optional.of(bob));

        command.onR(issuer, "ok");

        verify(messages).send(alice, bob, "ok");
    }

    @Test
    void reports_when_there_was_no_conversation_yet() {
        command.onR(issuer, "ok");

        verify(issuer).sendInfo(MessageKeys.NO_PREVIOUS_PLAYER);
        verify(messages, never()).send(any(), any(), anyString());
    }

    @Test
    void reports_when_the_correspondent_went_offline() {
        FakeProxyPlayer bob = platform.addPlayer("Bob");
        state.setPreviousPlayer(alice.getUniqueId(), bob.getUniqueId());
        when(messages.replyTargetOf(alice)).thenReturn(Optional.empty());

        command.onR(issuer, "ok");

        verify(issuer).sendInfo(MessageKeys.PLAYER_OFFLINE);
        verify(messages, never()).send(any(), any(), anyString());
    }

    @Test
    void refuses_the_console() {
        var console = consoleIssuer();

        command.onR(console, "ok");

        verify(console).sendError(co.aikar.commands.MessageKeys.NOT_ALLOWED_ON_CONSOLE);
    }
}
