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

package fr.florianpal.fmessage.core.protocol;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.UUID;

/**
 * The wire format shared with FMessageBukkit.
 *
 * <p><strong>This is a frozen contract.</strong> FMessageBukkit is not modified by the
 * proxy merge, so a single misplaced field silently breaks cross-server chat in production
 * with no error anywhere. {@code ChatProtocolTest} pins it down to the byte.</p>
 *
 * <p>Plain {@code java.io} is used rather than Guava's {@code ByteStreams}: the byte output
 * is identical (both are a {@code DataOutputStream} over a byte array) and it keeps this
 * class free of any dependency, testable on its own.</p>
 *
 * <h2>Bukkit → proxy, on {@value #CHANNEL_FROM_BUKKIT}</h2>
 * <pre>
 * "Message"      : subchannel, uuid, displayName, format, message, colors, nickColors
 * "StaffMessage" : subchannel, uuid, displayName, format, message
 * </pre>
 *
 * <h2>Proxy → Bukkit, on {@value #CHANNEL_TO_BUKKIT}</h2>
 * <pre>
 * public chat : subchannel, uuid, displayName, nickName, format, message, ignoresCsv,
 *               colors, nickColors
 * staff       : "StaffMessage", uuid, displayName, nickName, format, message
 * sound       : "Sound", targetUuid, soundKey
 * </pre>
 */
public final class ChatProtocol {

    /** Channel the proxy writes to; FMessageBukkit listens on it. */
    public static final String CHANNEL_TO_BUKKIT = "fmessage:chatbukkit";

    /** Channel the proxy listens on; FMessageBukkit writes to it. */
    public static final String CHANNEL_FROM_BUKKIT = "fmessage:chatbungee";

    public static final String STAFF_SUBCHANNEL = "StaffMessage";

    public static final String SOUND_SUBCHANNEL = "Sound";

    private ChatProtocol() {
    }

    /**
     * Reads a frame coming from a backend server.
     *
     * <p>The two trailing booleans are optional: staff frames stop after {@code message}.
     * They are read only when bytes remain, and default to {@code false} otherwise.</p>
     *
     * @throws IOException on a truncated or malformed frame
     */
    public static IncomingChat decode(byte[] data) throws IOException {
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(data))) {
            String subchannel = in.readUTF();
            UUID uuid = UUID.fromString(in.readUTF());
            String displayName = in.readUTF();
            String format = in.readUTF();
            String message = in.readUTF();

            boolean colors = false;
            boolean nickColors = false;
            if (in.available() >= 2) {
                colors = in.readBoolean();
                nickColors = in.readBoolean();
            }

            return new IncomingChat(subchannel, uuid, displayName, format, message, colors, nickColors);
        } catch (IllegalArgumentException e) {
            throw new IOException("Malformed UUID in chat frame", e);
        }
    }

    /**
     * Public chat, rebroadcast to every backend server so each can render it locally.
     */
    public static byte[] encodePublicChat(String subchannel,
                                          UUID uuid,
                                          String displayName,
                                          String nickName,
                                          String format,
                                          String message,
                                          Collection<UUID> ignoredBy,
                                          boolean colors,
                                          boolean nickColors) {
        return write(out -> {
            out.writeUTF(subchannel);
            out.writeUTF(uuid.toString());
            out.writeUTF(nullToEmpty(displayName));
            out.writeUTF(nullToEmpty(nickName));
            out.writeUTF(nullToEmpty(format));
            out.writeUTF(nullToEmpty(message));
            out.writeUTF(serializeUuids(ignoredBy));
            out.writeBoolean(colors);
            out.writeBoolean(nickColors);
        });
    }

    /**
     * Staff chat, rebroadcast without the colour flags: staff formatting is not gated.
     */
    public static byte[] encodeStaffChat(UUID uuid,
                                         String displayName,
                                         String nickName,
                                         String format,
                                         String message) {
        return write(out -> {
            out.writeUTF(STAFF_SUBCHANNEL);
            out.writeUTF(uuid.toString());
            out.writeUTF(nullToEmpty(displayName));
            out.writeUTF(nullToEmpty(nickName));
            out.writeUTF(nullToEmpty(format));
            out.writeUTF(nullToEmpty(message));
        });
    }

    /**
     * Asks the backend server hosting {@code target} to play a sound, since BungeeCord has
     * no sound API of its own.
     */
    public static byte[] encodeSound(UUID target, String soundKey) {
        return write(out -> {
            out.writeUTF(SOUND_SUBCHANNEL);
            out.writeUTF(target.toString());
            out.writeUTF(nullToEmpty(soundKey));
        });
    }

    /**
     * {@code uuid1;uuid2;} — trailing separator included, as FMessageBukkit splits on
     * {@code ";"} and tolerates it. An empty collection yields an empty string, never
     * the literal {@code "null"}.
     */
    static String serializeUuids(Collection<UUID> uuids) {
        if (uuids == null || uuids.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (UUID uuid : uuids) {
            builder.append(uuid).append(';');
        }
        return builder.toString();
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static byte[] write(FrameWriter writer) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(bytes)) {
            writer.write(out);
        } catch (IOException e) {
            // A ByteArrayOutputStream cannot fail; anything here is a programming error.
            throw new UncheckedIOException(e);
        }
        return bytes.toByteArray();
    }

    @FunctionalInterface
    private interface FrameWriter {
        void write(DataOutputStream out) throws IOException;
    }
}
