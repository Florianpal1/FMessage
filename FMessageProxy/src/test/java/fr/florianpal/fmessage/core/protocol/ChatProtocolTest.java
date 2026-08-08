package fr.florianpal.fmessage.core.protocol;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Pins down the wire format shared with FMessageBukkit.
 *
 * <p>FMessageBukkit is untouched by the proxy merge, so this contract is frozen. The golden
 * base64 frames below were produced from the format as it stood before the merge: if a
 * field moves, is added or is dropped, these tests fail — which is the only thing standing
 * between a refactor and silently broken cross-server chat.</p>
 */
class ChatProtocolTest {

    private static final UUID AUTHOR = UUID.fromString("11111111-2222-3333-4444-555555555555");
    private static final UUID IGNORED = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");

    @Nested
    @DisplayName("trames de référence, octet à octet")
    class GoldenFrames {

        @Test
        void public_chat_frame_matches_the_frozen_bytes() {
            byte[] frame = ChatProtocol.encodePublicChat("Message", AUTHOR, "Bob", "Bobby",
                    "&7{sender}: {message}", "salut", List.of(IGNORED), true, false);

            assertThat(Base64.getEncoder().encodeToString(frame)).isEqualTo(
                    "AAdNZXNzYWdlACQxMTExMTExMS0yMjIyLTMzMzMtNDQ0NC01NTU1NTU1NTU1NTUAA0JvYgAFQm9i"
                            + "YnkAFSY3e3NlbmRlcn06IHttZXNzYWdlfQAFc2FsdXQAJWFhYWFhYWFhLWJiYmItY2NjYy1kZGRk"
                            + "LWVlZWVlZWVlZWVlZTsBAA==");
        }

        @Test
        void staff_frame_matches_the_frozen_bytes() {
            byte[] frame = ChatProtocol.encodeStaffChat(AUTHOR, "Bob", "Bobby",
                    "&c[Staff] {sender}: {message}", "besoin d'aide");

            assertThat(Base64.getEncoder().encodeToString(frame)).isEqualTo(
                    "AAxTdGFmZk1lc3NhZ2UAJDExMTExMTExLTIyMjItMzMzMy00NDQ0LTU1NTU1NTU1NTU1NQADQm9i"
                            + "AAVCb2JieQAdJmNbU3RhZmZdIHtzZW5kZXJ9OiB7bWVzc2FnZX0ADWJlc29pbiBkJ2FpZGU=");
        }

        @Test
        void sound_frame_matches_the_frozen_bytes() {
            byte[] frame = ChatProtocol.encodeSound(AUTHOR, "entity.experience_orb.pickup");

            assertThat(Base64.getEncoder().encodeToString(frame)).isEqualTo(
                    "AAVTb3VuZAAkMTExMTExMTEtMjIyMi0zMzMzLTQ0NDQtNTU1NTU1NTU1NTU1ABxlbnRpdHkuZXhw"
                            + "ZXJpZW5jZV9vcmIucGlja3Vw");
        }
    }

    @Nested
    @DisplayName("ordre des champs à l'encodage")
    class FieldOrder {

        @Test
        void public_chat_writes_the_nine_fields_in_order() throws IOException {
            byte[] frame = ChatProtocol.encodePublicChat("Message", AUTHOR, "Bob", "Bobby",
                    "format", "salut", List.of(IGNORED), true, false);

            DataInputStream in = read(frame);
            assertThat(in.readUTF()).isEqualTo("Message");
            assertThat(in.readUTF()).isEqualTo(AUTHOR.toString());
            assertThat(in.readUTF()).isEqualTo("Bob");
            assertThat(in.readUTF()).isEqualTo("Bobby");
            assertThat(in.readUTF()).isEqualTo("format");
            assertThat(in.readUTF()).isEqualTo("salut");
            assertThat(in.readUTF()).isEqualTo(IGNORED + ";");
            assertThat(in.readBoolean()).isTrue();
            assertThat(in.readBoolean()).isFalse();
            assertThat(in.available()).isZero();
        }

        @Test
        void staff_frame_carries_no_colour_flags() throws IOException {
            byte[] frame = ChatProtocol.encodeStaffChat(AUTHOR, "Bob", "Bobby", "format", "salut");

            DataInputStream in = read(frame);
            assertThat(in.readUTF()).isEqualTo("StaffMessage");
            assertThat(in.readUTF()).isEqualTo(AUTHOR.toString());
            assertThat(in.readUTF()).isEqualTo("Bob");
            assertThat(in.readUTF()).isEqualTo("Bobby");
            assertThat(in.readUTF()).isEqualTo("format");
            assertThat(in.readUTF()).isEqualTo("salut");
            assertThat(in.available()).isZero();
        }

        @Test
        void sound_frame_carries_target_then_key() throws IOException {
            DataInputStream in = read(ChatProtocol.encodeSound(AUTHOR, "block.note_block.pling"));
            assertThat(in.readUTF()).isEqualTo("Sound");
            assertThat(in.readUTF()).isEqualTo(AUTHOR.toString());
            assertThat(in.readUTF()).isEqualTo("block.note_block.pling");
            assertThat(in.available()).isZero();
        }
    }

    @Nested
    @DisplayName("valeurs absentes")
    class NullHandling {

        @Test
        void an_absent_nickname_is_written_as_an_empty_string_never_null() throws IOException {
            byte[] frame = ChatProtocol.encodeStaffChat(AUTHOR, "Bob", null, "format", "salut");

            DataInputStream in = read(frame);
            in.readUTF();
            in.readUTF();
            in.readUTF();
            assertThat(in.readUTF()).isEmpty();
        }

        @Test
        void an_empty_ignore_list_yields_an_empty_string() {
            assertThat(ChatProtocol.serializeUuids(List.of())).isEmpty();
        }

        @Test
        void a_null_ignore_list_yields_an_empty_string() {
            assertThat(ChatProtocol.serializeUuids(null)).isEmpty();
        }

        @Test
        void ignores_are_separated_and_terminated_by_a_semicolon() {
            UUID other = UUID.fromString("99999999-8888-7777-6666-555555555555");
            assertThat(ChatProtocol.serializeUuids(List.of(IGNORED, other)))
                    .isEqualTo(IGNORED + ";" + other + ";");
        }
    }

    @Nested
    @DisplayName("décodage")
    class Decoding {

        @Test
        void round_trips_a_public_chat_frame() throws IOException {
            // Bukkit sends five strings then the two flags: no nickname on the way in.
            byte[] frame = incoming("Message", AUTHOR, "Bob", "format", "salut", true, false);

            IncomingChat decoded = ChatProtocol.decode(frame);

            assertThat(decoded.subchannel()).isEqualTo("Message");
            assertThat(decoded.uuid()).isEqualTo(AUTHOR);
            assertThat(decoded.displayName()).isEqualTo("Bob");
            assertThat(decoded.format()).isEqualTo("format");
            assertThat(decoded.message()).isEqualTo("salut");
            assertThat(decoded.colors()).isTrue();
            assertThat(decoded.nickColors()).isFalse();
        }

        @Test
        void reads_both_flags_independently() throws IOException {
            IncomingChat decoded = ChatProtocol.decode(
                    incoming("Message", AUTHOR, "Bob", "format", "salut", false, true));

            assertThat(decoded.colors()).isFalse();
            assertThat(decoded.nickColors()).isTrue();
        }

        @Test
        void defaults_both_flags_to_false_on_a_staff_frame_that_carries_none() throws IOException {
            IncomingChat decoded = ChatProtocol.decode(
                    incoming("StaffMessage", AUTHOR, "Bob", "format", "salut", null, null));

            assertThat(decoded.subchannel()).isEqualTo("StaffMessage");
            assertThat(decoded.colors()).isFalse();
            assertThat(decoded.nickColors()).isFalse();
        }

        @Test
        void rejects_a_truncated_frame() {
            byte[] truncated = new byte[]{0, 7, 'M', 'e', 's'};

            assertThatThrownBy(() -> ChatProtocol.decode(truncated))
                    .isInstanceOf(EOFException.class);
        }

        @Test
        void rejects_a_frame_whose_uuid_is_not_a_uuid() throws IOException {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (DataOutputStream out = new DataOutputStream(bytes)) {
                out.writeUTF("Message");
                out.writeUTF("pas-un-uuid");
                out.writeUTF("Bob");
                out.writeUTF("format");
                out.writeUTF("salut");
            }

            assertThatThrownBy(() -> ChatProtocol.decode(bytes.toByteArray()))
                    .isInstanceOf(IOException.class)
                    .hasMessageContaining("Malformed UUID");
        }
    }

    private static DataInputStream read(byte[] frame) {
        return new DataInputStream(new ByteArrayInputStream(frame));
    }

    /** Builds a frame the way FMessageBukkit does, i.e. without the nickname field. */
    private static byte[] incoming(String subchannel, UUID uuid, String displayName,
                                   String format, String message,
                                   Boolean colors, Boolean nickColors) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(bytes)) {
            out.writeUTF(subchannel);
            out.writeUTF(uuid.toString());
            out.writeUTF(displayName);
            out.writeUTF(format);
            out.writeUTF(message);
            if (colors != null) {
                out.writeBoolean(colors);
                out.writeBoolean(nickColors);
            }
        }
        return bytes.toByteArray();
    }
}
