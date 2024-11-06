package fr.florianpal.fmessage.utils;

public class StringUtils {

    public static boolean isNullOrEmpty(String text) {
        if (text == null) {
            return true;
        }

        return text.isEmpty();
    }
}
