package com.filter.textcorrector.spellchecking.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Improved SoundEx algorithm implementation for comparing words phonetics.
 */
public class Soundex {

    /**
     * Language codes for comparing.
     */
    private static Map<Character, Character> encoding;

    /**
     * Generates phonetic code for word.
     *
     * @param word word to convert.
     * @return phonetic code of the word.
     */
    public static String translate(String word) {
        encoding = englishCodes();
        return soundexEnglish(word);
    }

    public static float difference(String code1, String code2) {

        int lengthDiff = Math.abs(code1.length() - code2.length());

        if (code1.length() > code2.length()) {
            for (int i = 0; i < lengthDiff; i++) {
                code2 = code2 + "0";
            }
        } else {
            for (int i = 0; i < lengthDiff; i++) {
                code1 = code1 + "0";
            }
        }

        float firstCode1 = Float.parseFloat((int) code1.charAt(0) + code1.substring(1, code1.length()));
        float firstCode2 = Float.parseFloat((int) code2.charAt(0) + code2.substring(1, code2.length()));

        float biggerNew = Math.max(firstCode1, firstCode2);
        float smallerOriginal = Math.min(firstCode1, firstCode2);

        float difference = biggerNew - smallerOriginal;
        float percentage = ((difference / smallerOriginal) * 100);

        return percentage;
    }

    private static String soundexEnglish(String word) {
        if (word.isEmpty() || word == null) {
            return "000";
        }

        String casedString = word.toUpperCase();
        int length = word.length();
        String letter = String.valueOf(casedString.charAt(0));
        String code = letter + "", soundex;


        char previousCode = '-';

        for (int i = 1; i < length; i++) {
            char currentLetter = casedString.charAt(i);

            if (currentLetter == 'H' || currentLetter == 'W') {
                currentLetter = casedString.charAt((i + 1) % length);
            }

            char currentCode = getCode(currentLetter);

            if (currentCode != '\0' && !sameAdjacentCodes(previousCode, currentCode)) {
                code = code + currentCode;
            }

            previousCode = currentCode;
        }

        soundex = code;
        return convertToScramble(soundex.substring(1, code.length()), letter);
    }

    private static Map<Character, Character> englishCodes() {
        Map<Character, Character> englishCodes = new HashMap<>();

        /* Consonants*/
        englishCodes.put('B', '1');
        englishCodes.put('F', '1');
        englishCodes.put('P', '1');
        englishCodes.put('V', '1');

        englishCodes.put('C', '2');
        englishCodes.put('G', '2');
        englishCodes.put('J', '2');
        englishCodes.put('K', '2');
        englishCodes.put('Q', '2');
        englishCodes.put('S', '2');
        englishCodes.put('X', '2');
        englishCodes.put('Z', '2');

        englishCodes.put('D', '3');
        englishCodes.put('T', '3');

        englishCodes.put('L', '4');

        englishCodes.put('M', '5');
        englishCodes.put('N', '5');

        englishCodes.put('R', '6');

     /*    Ignore List
         Vowels*/
        englishCodes.put('A', '\0');
        englishCodes.put('E', '\0');
        englishCodes.put('I', '\0');
        englishCodes.put('O', '\0');
        englishCodes.put('U', '\0');
        englishCodes.put('Y', '\0');
        englishCodes.put('H', '\0');
        englishCodes.put('W', '\0');

        return englishCodes;
    }

    private static char getCode(char letterPattern) {
        Character letterCode = encoding.get(letterPattern);
        return letterCode == null ? '\0' : letterCode;
    }

    private static boolean sameAdjacentCodes(char previousCode, char currentCode) {
        return currentCode == previousCode;
    }

    /**
     * Improvement for phonetic code generation, that produces more generic code.
     *
     * @param token soundex code.
     * @param letter
     * @return improved code.
     */
    private static String convertToScramble(String token, String letter) {
        char[] soundexChars = Arrays.copyOf(token.toCharArray(), token.length());

        Arrays.sort(soundexChars);
        StringBuilder soundexToken = new StringBuilder();
        soundexToken.append(soundexChars);
        soundexToken.append(letter);
        soundexToken.reverse();

        return soundexToken.toString();
    }

    public static void main(String[] args) {
        String word1 = "youreretarded";
        String word2 = "youreretarded";

        System.out.println(Soundex.translate(word1));
        System.out.println(Soundex.translate(word2));

        System.out.println(Soundex.difference(Soundex.translate(word1), Soundex.translate(word2)));
    }
}
