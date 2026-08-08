package fr.florianpal.fmessage.fakes;

import fr.florianpal.fmessage.platform.ProxyBackendServer;
import fr.florianpal.fmessage.platform.ProxyPlayer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * An in-memory player. Everything the core does to a player ends up in a list here.
 */
public class FakeProxyPlayer implements ProxyPlayer {

    private final UUID uuid;
    private final String name;
    private final Set<String> permissions = new HashSet<>();

    /** Legacy-formatted messages received, in order. */
    public final List<String> received = new ArrayList<>();

    /** Sound keys played to this player, in order. */
    public final List<String> soundsPlayed = new ArrayList<>();

    private ProxyBackendServer currentServer;

    public FakeProxyPlayer(String name) {
        this(UUID.randomUUID(), name);
    }

    public FakeProxyPlayer(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public FakeProxyPlayer withPermission(String node) {
        permissions.add(node);
        return this;
    }

    public FakeProxyPlayer on(ProxyBackendServer server) {
        this.currentServer = server;
        return this;
    }

    @Override
    public UUID getUniqueId() {
        return uuid;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean hasPermission(String node) {
        return permissions.contains(node);
    }

    @Override
    public void sendMessage(String legacyText) {
        received.add(legacyText);
    }

    @Override
    public void playSound(String soundKey) {
        soundsPlayed.add(soundKey);
    }

    @Override
    public Optional<ProxyBackendServer> getCurrentServer() {
        return Optional.ofNullable(currentServer);
    }

    @Override
    public Object nativeHandle() {
        return this;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ProxyPlayer player && uuid.equals(player.getUniqueId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uuid);
    }
}
