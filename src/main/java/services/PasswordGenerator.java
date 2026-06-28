package services;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PasswordGenerator {

    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "!@#$%^&*()_+-=[]{}|;:,.<>?";

    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generate(int minLength, int maxLength, boolean requireUpper,
                                   boolean requireLower, boolean requireDigit, boolean requireSpecial) {
        StringBuilder chars = new StringBuilder();
        StringBuilder password = new StringBuilder();

        if (requireUpper) { chars.append(UPPER); password.append(randomChar(UPPER)); }
        if (requireLower) { chars.append(LOWER); password.append(randomChar(LOWER)); }
        if (requireDigit) { chars.append(DIGITS); password.append(randomChar(DIGITS)); }
        if (requireSpecial) { chars.append(SPECIAL); password.append(randomChar(SPECIAL)); }

        int length = minLength + RANDOM.nextInt(maxLength - minLength + 1);

        String charPool = chars.length() > 0 ? chars.toString() : LOWER + DIGITS;

        for (int i = password.length(); i < length; i++) {
            password.append(charPool.charAt(RANDOM.nextInt(charPool.length())));
        }

        List<Character> list = new ArrayList<>();
        for (char c : password.toString().toCharArray()) list.add(c);
        Collections.shuffle(list, RANDOM);
        StringBuilder result = new StringBuilder();
        for (char c : list) result.append(c);

        return result.toString();
    }

    private static char randomChar(String chars) {
        return chars.charAt(RANDOM.nextInt(chars.length()));
    }
}
