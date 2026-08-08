package fr.florianpal.fmessage.core.commands;

import co.aikar.commands.CommandIssuer;
import fr.florianpal.fmessage.core.FMessageCore;
import fr.florianpal.fmessage.core.state.SessionState;
import fr.florianpal.fmessage.fakes.FakeMessenger;
import fr.florianpal.fmessage.fakes.FakeProxyPlatform;
import fr.florianpal.fmessage.fakes.FakeProxyPlayer;
import fr.florianpal.fmessage.managers.commandManagers.GroupCommandManager;
import fr.florianpal.fmessage.managers.commandManagers.GroupMemberCommandManager;
import fr.florianpal.fmessage.managers.commandManagers.IgnoreCommandManager;
import fr.florianpal.fmessage.managers.commandManagers.NickNameCommandManager;
import fr.florianpal.fmessage.managers.commandManagers.PlayerCommandManager;
import fr.florianpal.fmessage.objects.Group;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Shared rig for the command tests.
 *
 * <p>{@code FMessageCore} is mocked rather than built: its constructor opens a database
 * connection pool. Mockito instantiates it without running any constructor, so the commands
 * can be exercised against stubbed collaborators.</p>
 */
abstract class CommandTestSupport {

    protected FMessageCore core;
    protected FakeProxyPlatform platform;
    protected FakeMessenger messenger;
    protected SessionState state;
    protected Map<Integer, Group> groups;

    protected IgnoreCommandManager ignores;
    protected GroupCommandManager groupCommands;
    protected GroupMemberCommandManager groupMembers;
    protected NickNameCommandManager nicknames;
    protected PlayerCommandManager players;

    protected FakeProxyPlayer alice;
    protected CommandIssuer issuer;

    protected void setUpCore() {
        core = mock(FMessageCore.class);
        platform = new FakeProxyPlatform();
        messenger = new FakeMessenger();
        state = new SessionState();
        groups = new HashMap<>();

        ignores = mock(IgnoreCommandManager.class);
        groupCommands = mock(GroupCommandManager.class);
        groupMembers = mock(GroupMemberCommandManager.class);
        nicknames = mock(NickNameCommandManager.class);
        players = mock(PlayerCommandManager.class);

        when(core.getPlatform()).thenReturn(platform);
        when(core.getMessenger()).thenReturn(messenger);
        when(core.getSessionState()).thenReturn(state);
        when(core.getGroups()).thenReturn(groups);
        when(core.getIgnoreCommandManager()).thenReturn(ignores);
        when(core.getGroupCommandManager()).thenReturn(groupCommands);
        when(core.getGroupMemberCommandManager()).thenReturn(groupMembers);
        when(core.getNickNameCommandManager()).thenReturn(nicknames);
        when(core.getPlayerCommandManager()).thenReturn(players);

        alice = platform.addPlayer("Alice");
        issuer = messenger.issuerFor(alice.getUniqueId());
        when(core.playerOf(issuer)).thenReturn(Optional.of(alice));
    }

    /**
     * @return an issuer the core cannot resolve to a player, i.e. the console
     */
    protected CommandIssuer consoleIssuer() {
        CommandIssuer console = mock(CommandIssuer.class);
        when(core.playerOf(console)).thenReturn(Optional.empty());
        return console;
    }
}
