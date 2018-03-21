package com.filter.textcorrector.text_preproccessing;

import com.filter.textcorrector.text_preproccessing.util.CleanTextType;
import com.filter.textcorrector.text_preproccessing.util.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

//TODO: possibly make it an object to load resources once.
//TODO: make Singleton.
public class TextPreproccessor {
    private static final Pattern NON_ALPHANUMERIC_CHAR = Pattern.compile("[^a-zA-Z0-9_]");
    private static Logger LOGGER = LoggerFactory.getLogger(TextPreproccessor.class);
    private static SymbolMapper symbolMapper;

    public TextPreproccessor() {
        symbolMapper = new SymbolMapper();
    }

    //TODO: Why does it take so much time?
    public String preproccess(String text){

        long startProccessingTime = System.nanoTime();

        text = symbolMapper.mapNumbers(text);
        text = TextUtils.cleanText(text, CleanTextType.SPACES_BETWEEN_SINGLE_LETTERS);

        String correctedText = text;

        String[] originalWords = TextUtils.splitCleanText(correctedText, CleanTextType.SPLIT_WITHOUT_CLEANING);

        for (int j = 0; j < originalWords.length; j++) {
            String word = originalWords[j];
            String possibleDigit = TextUtils.cleanText(word, CleanTextType.CLEAR_PUNCTUATION);

            if(TextUtils.isWordDigit(possibleDigit.substring(0, possibleDigit.length() > 0 ? possibleDigit.length() - 1 : 0))){
                continue;
            }

            String transliteratedWord = symbolMapper.mapCharacters(word);

            if(!hasSpecialChar(originalWords[j])){
                correctedText = TextUtils.replaceWord(correctedText, originalWords[j], transliteratedWord);
            }
            else {
                correctedText = correctedText.replace(originalWords[j], transliteratedWord);
            }
        }

        correctedText = cleanText(correctedText);

        long endProccessingTime = System.nanoTime();

        LOGGER.debug("Preproccessing took time: " + (endProccessingTime - startProccessingTime) / (double) 1000000 + " ms");

        return correctedText;
    }

    //TODO: move to TextUtils?
    public static boolean hasSpecialChar(String originalWord) {
        return NON_ALPHANUMERIC_CHAR.matcher(originalWord).find();
    }

    private static String cleanText(String text) {
        text = TextUtils.cleanText(text, CleanTextType.PUNCTUATION_BETWEEN_SINGLE_LETTERS);
        text = TextUtils.cleanText(text, CleanTextType.WHITE_SPACES);
        text = TextUtils.cleanText(text, CleanTextType.SYMBOLS_IN_WORDS);
        text = TextUtils.cleanText(text, CleanTextType.DIGITS_IN_WORDS);
        return text;
    }

    public static void main(String[] args) {
        TextPreproccessor textPreproccessor = new TextPreproccessor();
        System.out.println(textPreproccessor.preproccess("Evan Lambert 37.9k votes 7.7k voters 465.6k views push-up and 7k 38 items Follow Embed"));
    }
}
