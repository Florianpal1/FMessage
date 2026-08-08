package fr.florianpal.fmessage.core.text;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MessageTemplateTest {

    @Test
    void isNullOrEmpty_recognises_null_and_empty() {
        assertThat(MessageTemplate.isNullOrEmpty(null)).isTrue();
        assertThat(MessageTemplate.isNullOrEmpty("")).isTrue();
        assertThat(MessageTemplate.isNullOrEmpty(" ")).isFalse();
        assertThat(MessageTemplate.isNullOrEmpty("Bob")).isFalse();
    }

    @Test
    void substitutes_the_placeholder() {
        assertThat(MessageTemplate.replace("salut {sender}", "{sender}", "Bob", false))
                .isEqualTo("salut Bob");
    }

    @Test
    void keeps_colour_codes_literal_when_colours_are_not_granted() {
        assertThat(MessageTemplate.replace("{sender}", "{sender}", "&cBob", false))
                .isEqualTo("&cBob");
    }

    @Test
    void converts_colour_codes_when_colours_are_granted() {
        assertThat(MessageTemplate.replace("{sender}", "{sender}", "&cBob", true))
                .isEqualTo("§cBob");
    }

    @Test
    void converts_hex_colours_when_colours_are_granted() {
        assertThat(MessageTemplate.replace("{sender}", "{sender}", "{#FF0000}Bob", true))
                .isEqualTo("§x§F§F§0§0§0§0Bob");
    }

    @Test
    void does_not_reinterpret_a_placeholder_carried_by_the_injected_value() {
        // A message quoting "{sender}" must stay literal, otherwise a player could forge
        // someone else's name into the format.
        String rendered = MessageTemplate.replace("[{sender}] {message}", "{message}",
                "j'ai dit {sender}", false);

        assertThat(MessageTemplate.replace(rendered, "{sender}", "Bob", false))
                .isEqualTo("[Bob] j'ai dit Bob");
    }

    @Test
    void substitutes_every_occurrence_of_the_placeholder() {
        assertThat(MessageTemplate.replace("{p} et {p}", "{p}", "Bob", false))
                .isEqualTo("Bob et Bob");
    }

    @Test
    void leaves_the_template_untouched_when_the_placeholder_is_absent() {
        assertThat(MessageTemplate.replace("rien à voir", "{sender}", "Bob", false))
                .isEqualTo("rien à voir");
    }

    @Test
    void treats_a_null_value_as_empty_instead_of_throwing() {
        assertThat(MessageTemplate.replace("[{sender}]", "{sender}", null, false))
                .isEqualTo("[]");
    }

    @Test
    void returns_an_empty_string_for_a_null_template() {
        assertThat(MessageTemplate.replace(null, "{sender}", "Bob", false)).isEmpty();
    }
}
