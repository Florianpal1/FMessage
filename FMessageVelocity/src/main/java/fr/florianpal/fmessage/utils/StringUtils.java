package fr.florianpal.fmessage.utils;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class StringUtils {

    public static boolean isNullOrEmpty(String text) {
        if (text == null) {
            return true;
        }

        return text.isEmpty();
    }

    public static TextComponent format(String msg) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(msg);
    }

    public static TextComponent replace(TextComponent textComponent, String stringToChange, String stringYouWant, boolean color) {

        TextReplacementConfig.Builder textReplacementConfigBulder = TextReplacementConfig.builder()
                .matchLiteral(stringToChange);
        if (color) {
            textReplacementConfigBulder.replacement(format(stringYouWant));
        } else {
            textReplacementConfigBulder.replacement(stringYouWant);
        }

        return (TextComponent) textComponent.replaceText(textReplacementConfigBulder.build());
    }
}
