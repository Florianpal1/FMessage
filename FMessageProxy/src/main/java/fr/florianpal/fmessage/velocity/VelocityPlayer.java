/*
 * Copyright (C) 2022 Florianpal
 *
 * This program is free software;
 * you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, see <http://www.gnu.org/licenses/>.
 *
 *  @author Florianpal.
 */

package fr.florianpal.fmessage.velocity;

import com.velocitypowered.api.proxy.Player;
import fr.florianpal.fmessage.platform.ProxyBackendServer;
import fr.florianpal.fmessage.platform.ProxyLogger;
import fr.florianpal.fmessage.platform.ProxyPlayer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class VelocityPlayer implements ProxyPlayer {

    /**
     * Reads the {@code §} form the core produces — including {@code §x§r§r§g§g§b§b} hex,
     * which the previous {@code legacyAmpersand()} serializer silently dropped. That is why
     * {@code {#rrggbb}} used to work on BungeeCord and not on Velocity.
     */
    static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.builder()
            .character(LegacyComponentSerializer.SECTION_CHAR)
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    private final Player handle;
    private final ProxyLogger logger;

    public VelocityPlayer(Player handle, ProxyLogger logger) {
        this.handle = handle;
        this.logger = logger;
    }

    @Override
    public UUID getUniqueId() {
        return handle.getUniqueId();
    }

    @Override
    public String getName() {
        return handle.getUsername();
    }

    @Override
    public boolean hasPermission(String node) {
        return handle.hasPermission(node);
    }

    @Override
    public void sendMessage(String legacyText) {
        handle.sendMessage(SERIALIZER.deserialize(legacyText));
    }

    @Override
    public void playSound(String soundKey) {
        try {
            handle.playSound(Sound.sound(Key.key(soundKey), Sound.Source.MASTER, 1f, 1f));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid notification.sound in config.yml: " + soundKey);
        }
    }

    @Override
    public Optional<ProxyBackendServer> getCurrentServer() {
        return handle.getCurrentServer()
                .map(connection -> new VelocityBackendServer(connection.getServer()));
    }

    @Override
    public Object nativeHandle() {
        return handle;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ProxyPlayer player && getUniqueId().equals(player.getUniqueId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getUniqueId());
    }
}
