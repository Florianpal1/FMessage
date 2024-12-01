package fr.florianpal.fmessage.utils;

public class StringUtils {

    public static boolean isNullOrEmpty(String text) {
        if (text == null) {
            return true;
        }

        return text.isEmpty();
    }

    public static String replace(String textComponent, String stringToChange, String stringYouWant, boolean color) {

        if (color) {
            textComponent = textComponent.replace(stringToChange, FormatUtil.format(stringYouWant));
        } else {
            textComponent = textComponent.replace(stringToChange, stringYouWant);
        }

        return textComponent;
    }
}
