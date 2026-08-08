package fr.florianpal.fmessage.fakes;

import co.aikar.commands.CommandIssuer;
import fr.florianpal.fmessage.core.acf.Messenger;
import fr.florianpal.fmessage.platform.ProxyPlayer;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Hands out one stable mock issuer per player, so a test can verify the localized messages
 * the core sent without instantiating ACF.
 */
public class FakeMessenger implements Messenger {

    private final Map<UUID, CommandIssuer> issuers = new HashMap<>();

    @Override
    public CommandIssuer issuerOf(ProxyPlayer player) {
        return issuerFor(player.getUniqueId());
    }

    /**
     * @return the same mock the core received, ready for {@code verify(...)}
     */
    public CommandIssuer issuerFor(UUID uuid) {
        return issuers.computeIfAbsent(uuid, ignored -> Mockito.mock(CommandIssuer.class));
    }
}
