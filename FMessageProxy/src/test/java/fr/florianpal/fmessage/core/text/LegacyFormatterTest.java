package fr.florianpal.fmessage.core.text;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LegacyFormatterTest {

    @Nested
    @DisplayName("codes couleur &")
    class AmpersandCodes {

        @Test
        void converts_an_ampersand_code_to_a_section_code() {
            assertThat(LegacyFormatter.format("&aBonjour")).isEqualTo("§aBonjour");
        }

        @Test
        void lowercases_the_code_character_like_bungeecord_does() {
            assertThat(LegacyFormatter.format("&CRouge")).isEqualTo("§cRouge");
        }

        @Test
        void converts_every_occurrence() {
            assertThat(LegacyFormatter.format("&aVert &cRouge &lGras"))
                    .isEqualTo("§aVert §cRouge §lGras");
        }

        @Test
        void leaves_an_unknown_code_alone() {
            assertThat(LegacyFormatter.format("&zInconnu")).isEqualTo("&zInconnu");
        }

        @Test
        void leaves_a_trailing_lone_ampersand_alone() {
            assertThat(LegacyFormatter.format("fin &")).isEqualTo("fin &");
        }

        @Test
        void leaves_text_without_codes_untouched() {
            assertThat(LegacyFormatter.format("juste du texte")).isEqualTo("juste du texte");
        }
    }

    @Nested
    @DisplayName("couleurs hexadécimales {#rrggbb}")
    class HexTokens {

        @Test
        void converts_a_hex_token_to_the_repeated_x_form() {
            assertThat(LegacyFormatter.format("{#FF0000}Rouge"))
                    .isEqualTo("§x§F§F§0§0§0§0Rouge");
        }

        @Test
        void preserves_the_case_of_the_hex_digits() {
            assertThat(LegacyFormatter.format("{#ff00aa}"))
                    .isEqualTo("§x§f§f§0§0§a§a");
        }

        @Test
        void converts_several_distinct_tokens() {
            assertThat(LegacyFormatter.format("{#FF0000}A{#00FF00}B"))
                    .isEqualTo("§x§F§F§0§0§0§0A§x§0§0§F§F§0§0B");
        }

        @Test
        void converts_the_same_token_repeated() {
            assertThat(LegacyFormatter.format("{#FF0000}A{#FF0000}B"))
                    .isEqualTo("§x§F§F§0§0§0§0A§x§F§F§0§0§0§0B");
        }

        @Test
        void leaves_a_malformed_token_alone() {
            assertThat(LegacyFormatter.format("{#GGGGGG}")).isEqualTo("{#GGGGGG}");
        }

        @Test
        void leaves_a_too_short_token_alone() {
            assertThat(LegacyFormatter.format("{#FFF}")).isEqualTo("{#FFF}");
        }

        @Test
        void mixes_hex_tokens_and_ampersand_codes() {
            assertThat(LegacyFormatter.format("{#FF0000}Rouge &lGras"))
                    .isEqualTo("§x§F§F§0§0§0§0Rouge §lGras");
        }
    }

    @Nested
    @DisplayName("entrées limites")
    class EdgeCases {

        @Test
        void returns_an_empty_string_for_null() {
            assertThat(LegacyFormatter.format(null)).isEmpty();
        }

        @Test
        void returns_an_empty_string_for_an_empty_input() {
            assertThat(LegacyFormatter.format("")).isEmpty();
        }
    }
}
