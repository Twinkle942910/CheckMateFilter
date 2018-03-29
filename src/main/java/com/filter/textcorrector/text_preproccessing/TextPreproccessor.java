package com.filter.textcorrector.text_preproccessing;

import com.filter.textcorrector.text_preproccessing.util.CleanTextType;
import com.filter.textcorrector.text_preproccessing.util.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//TODO: Don't map cyrillic symbols when its Russian or Ukrainian language.
public class TextPreproccessor {
    private static Logger LOGGER = LoggerFactory.getLogger(TextPreproccessor.class);
    private static SymbolMapper symbolMapper;
    private boolean removeRepeatedLetters = false;

    private static TextPreproccessor INSTANCE = new TextPreproccessor();

    public static TextPreproccessor getInstance(){
        return INSTANCE;
    }

    private TextPreproccessor() {
        symbolMapper = new SymbolMapper();
        //throw new AssertionError("This class is not meant to be instantiated.");
    }

    public String preproccess(String text, boolean removeRepeatedLetters){
        this.removeRepeatedLetters = removeRepeatedLetters;
        return preproccess(text);
    }

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

            //TODO: check if it works well for all cases.
            //TODO: Removes double letters where they should be. A bit bad, cause then on dict. lookup will be false.
           String uniqueWord = word;

            if(removeRepeatedLetters){
                uniqueWord = TextUtils.removeRepeatedLetters(word); //Consumes aprx. 0.5 ms for word. For large text it's pretty expensive.
            }
            String transliteratedWord = symbolMapper.mapCharacters(uniqueWord);

            if(!TextUtils.hasSpecialChar(originalWords[j])){
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

    private static String cleanText(String text) {
        text = TextUtils.cleanText(text, CleanTextType.PUNCTUATION_BETWEEN_SINGLE_LETTERS);
        text = TextUtils.cleanText(text, CleanTextType.WHITE_SPACES);
        text = TextUtils.cleanText(text, CleanTextType.SYMBOLS_IN_WORDS);
        text = TextUtils.cleanText(text, CleanTextType.DIGITS_IN_WORDS);
        return text;
    }
}
