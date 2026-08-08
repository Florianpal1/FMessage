package fr.florianpal.fmessage.fakes;

import fr.florianpal.fmessage.platform.ProxyBackendServer;
import fr.florianpal.fmessage.platform.ProxyLogger;
import fr.florianpal.fmessage.platform.ProxyPlatform;
import fr.florianpal.fmessage.platform.ProxyPlayer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * A proxy that exists only in memory.
 *
 * <p>This is the piece that makes the whole core testable: no BungeeCord, no Velocity, no
 * network, no database — just players you add and payloads you read back.</p>
 */
public class FakeProxyPlatform implements ProxyPlatform {

    private final Map<UUID, FakeProxyPlayer> players = new LinkedHashMap<>();
    private final List<ProxyBackendServer> servers = new ArrayList<>();

    public final FakeProxyLogger logger = new FakeProxyLogger();

    private File dataFolder = new File("build/test-data");

    public FakeProxyPlayer addPlayer(String name) {
        FakeProxyPlayer player = new FakeProxyPlayer(name);
        players.put(player.getUniqueId(), player);
        return player;
    }

    public FakeProxyPlayer addPlayer(FakeProxyPlayer player) {
        players.put(player.getUniqueId(), player);
        return player;
    }

    public void disconnect(UUID uuid) {
        players.remove(uuid);
    }

    public FakeBackendServer addServer(String name) {
        FakeBackendServer server = new FakeBackendServer(name);
        servers.add(server);
        return server;
    }

    public FakeProxyPlatform withDataFolder(File dataFolder) {
        this.dataFolder = dataFolder;
        return this;
    }

    @Override
    public Optional<ProxyPlayer> getPlayer(UUID uuid) {
        return Optional.ofNullable(players.get(uuid));
    }

    @Override
    public Optional<ProxyPlayer> getPlayer(String name) {
        return players.values().stream()
                .filter(player -> player.getName().equalsIgnoreCase(name))
                .map(ProxyPlayer.class::cast)
                .findFirst();
    }

    @Override
    public Collection<ProxyPlayer> getOnlinePlayers() {
        return new ArrayList<>(players.values());
    }

    @Override
    public Collection<ProxyBackendServer> getServers() {
        return servers;
    }

    @Override
    public File getDataFolder() {
        return dataFolder;
    }

    @Override
    public ProxyLogger getLogger() {
        return logger;
    }
}
