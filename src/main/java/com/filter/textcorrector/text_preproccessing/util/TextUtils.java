package com.filter.textcorrector.text_preproccessing.util;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextUtils {
    private static final Pattern IS_DIGIT = Pattern.compile("-?\\d+(\\.\\d+)?");
    private static final Pattern NON_ALPHANUMERIC_CHAR = Pattern.compile("[^a-zA-Z0-9_]");

    private TextUtils() {
        throw new AssertionError("This class is not meant to be instantiated.");
    }

    public static String replaceCompound(String text, String compound, String wordReplacement) {
        String compoundRegex = createCompoundRegex(compound);
        return text.replaceAll(compoundRegex, wordReplacement);
    }

    public static boolean containsCompound(String source, String compound) {
        String compoundRegex = createCompoundRegex(compound);

        Pattern p = Pattern.compile(compoundRegex);
        Matcher m = p.matcher(source);
        return m.find();
    }

    public static String createCompoundRegex(String compound) {
        StringBuilder compoundRegexBuilder = new StringBuilder();

        //This matches to a word boundary before the first word
        compoundRegexBuilder.append("\\b");

        // inserts each character into the regex
        for (int i = 0; i < compound.length(); i++) {
            compoundRegexBuilder.append(compound.charAt(i));

            // between each letter there could be any amount of whitespace
            if (i < compound.length() - 1) {
                compoundRegexBuilder.append("\\s*");
            }
        }

        // Makes sure the last word isn't part of a larger word
        compoundRegexBuilder.append("\\b");

        return compoundRegexBuilder.toString();
    }

    public static String replaceWord(String source, String word, String replacement) {
        return source.replaceAll("\\b" + word + "\\b", replacement);
    }

    public static boolean isWordDigit(String word) {
        return IS_DIGIT.matcher(word).matches();
    }

    public static boolean hasSpecialChar(String originalWord) {
        return NON_ALPHANUMERIC_CHAR.matcher(originalWord).find();
    }

    public static String removeRepeatedLetters(String word) {
        if (word == null) {
            throw new IllegalArgumentException("Sorry, can't check null word.");
        }

        if (word.length() == 1 || word.equals("")) {
            return word;
        }

        StringBuilder result = new StringBuilder();

        word = word.toLowerCase();

        for (int i = 0; i < word.length() - 1; i++) {
            char current = word.charAt(i);
            if(current != word.charAt(i + 1)){
                result.append(current);
            }
        }

        return result.toString() + word.charAt(word.length() - 1);
    }

    public static String[] splitCleanText(String text, CleanTextType cleanTextType) {
        String[] words = null;

        switch (cleanTextType) {
            case SPLIT_WITH_CLEANING:
                words = text.replaceAll("[\\p{Punct}\\s]+|\\s*\\p{Punct}+\\s*$", " ").split("\\s+");
                break;

            case SPLIT_WITHOUT_CLEANING:
                words = text.replaceAll("(\\p{Punct}\\s)|((?<=\\s)\\p{Punct}+(?=\\s*))(\\p{Punct}\\s)|\\s*\\p{Punct}+\\s*$", " ").split("\\s+");
                break;

            case SPLIT_CLEARED_WORDS:
                //words = text.split("\\s+");
                words = text.replaceAll("[^a-zA-Z0-9 ]+", "").split("\\s+");
                break;
        }

        return words;
    }

    public static String cleanText(String text, CleanTextType cleanTextType) {
        switch (cleanTextType) {
            case SPACES_BETWEEN_SINGLE_LETTERS:
                text = text.replaceAll("(?<=\\b\\p{L})\\s+(?=\\p{L}\\b)", "");
                break;

            case WHITE_SPACES:
                text = text.replaceAll("\\s+", " ");
                break;

            case PUNCTUATION_BETWEEN_SINGLE_LETTERS:
                text = text.replaceAll("(?<=(?<!\\p{L})\\p{L})[^a-zA-Z0-9]+(?=\\p{L}(?!\\p{L}))", "");
                break;

            case SYMBOLS_IN_WORDS:
                text = text.replaceAll("(?<=\\b)[^a-zA-Z0-9',-. ]+(?=\\b)", "");
                break;

            case DIGITS_IN_WORDS:
                text = text.replaceAll("(?<=\\p{L})[0-9]+(?=\\W)", "");
                break;

            case CLEAR_PUNCTUATION:
                text = text.replaceAll("[^a-zA-Z0-9_]", "");
                break;

            case CLEAR_IRRELEVANT_SYMBOLS:
                text = text.replaceAll("[^a-zA-Z0-9',-. ]", "");
                break;
        }

        return text;
    }

    public static void main(String[] args) {
        long startProccessingTime = System.nanoTime();
        System.out.println(TextUtils.removeRepeatedLetters("Ssssseeeeettttthhhhhhhh"));
        long endProccessingTime = System.nanoTime();
        System.out.println("Checking took time: " + (endProccessingTime - startProccessingTime) / (double) 1000000 + " ms");
    }

}
