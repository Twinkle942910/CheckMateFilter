package com.filter.textcorrector.text_preproccessing;

import com.filter.textcorrector.text_preproccessing.util.CleanTextType;
import com.filter.textcorrector.text_preproccessing.util.TextUtils;

import java.util.regex.Pattern;

public class TextPreproccessor {
    private static final SymbolMapper symbolMapper = new SymbolMapper();

    public static String preproccess(String text){
        text = symbolMapper.mapNumbers(text);

        text = TextUtils.cleanText(text, CleanTextType.SPACES_BETWEEN_SINGLE_LETTERS);
        String correctedText = text;

        String[] originalWords = TextUtils.splitCleanText(correctedText, CleanTextType.SPLIT_WITHOUT_CLEANING);

        for (int j = 0; j < originalWords.length; j++) {
            String word = originalWords[j];
            String possibleDigit = TextUtils.cleanText(word, CleanTextType.CLEAR_PUNCTUATION);

            if(TextUtils.isWordDigit(possibleDigit)){
                word = possibleDigit;
            }

            String transliteratedWord = symbolMapper.mapCharacters(word);

            Pattern p = Pattern.compile("[^a-zA-Z0-9_]");
            boolean hasSpecialChar = p.matcher(originalWords[j]).find();

            if(!hasSpecialChar){
                correctedText = TextUtils.replaceWord(correctedText, originalWords[j], transliteratedWord);
            }
            else {
                correctedText = correctedText.replace(originalWords[j], transliteratedWord);
            }
        }

        correctedText = cleanText(correctedText);

        return correctedText;
    }

    private static String cleanText(String text) {
        text = TextUtils.cleanText(text, CleanTextType.PUNCTUATION_BETWEEN_SINGLE_LETTERS);
        text = TextUtils.cleanText(text, CleanTextType.WHITE_SPACES);
        text = TextUtils.cleanText(text, CleanTextType.SYMBOLS_IN_WORDS);
        text = TextUtils.cleanText(text, CleanTextType.DIGITS_IN_WORDS);
        return text;
    }

    public static void main(String[] args) {
        System.out.println(TextPreproccessor.preproccess("Watch you’re words! Spell-check may not sea words that are miss used because they are spelled rite!"));
    }
}
