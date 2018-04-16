package com.filter.textcorrector.profanity_filtering;

import com.filter.textcorrector.profanity_filtering.dictionary.Dictionary;
import com.filter.textcorrector.profanity_filtering.dictionary.DictionaryFactory;
import com.filter.textcorrector.profanity_filtering.model.Censored;
import com.filter.textcorrector.spellchecking.Language;
import com.filter.textcorrector.text_preproccessing.util.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Set;

/**
 * Checks given text for bad words.
 */
public class ProfanityFilter {
    private static Logger LOGGER = LoggerFactory.getLogger(ProfanityFilter.class);
    private Dictionary dictionary;
    private String wordReplacement;
    private boolean wordRemoval;

    private ProfanityFilter(Builder builder) {
        this.wordReplacement = builder.wordReplacement;
        this.wordRemoval = builder.wordRemoval;
        dictionary = DictionaryFactory.create(builder.language, builder.dictionaryPath);
    }

    /**
     * Returns censored text with custom word or nothing instead of bad one.
     *
     * @param input text to censor.
     * @return censored String.
     */
    public String censor(String input) {
        LOGGER.debug("Given text: " + input);
        long startProccessingTime = System.nanoTime();

        Set<String> badWords = dictionary.search(input);

        if (badWords.size() > 0) {
            input = clearMultipleProfanity(input.toLowerCase(), badWords);
        }

        long endProccessingTime = System.nanoTime();

        LOGGER.debug("Censored text: " + input);
        LOGGER.debug("Censoring took time: " + (endProccessingTime - startProccessingTime) / (double) 1000000 + " ms");

        return input;
    }

    public Censored searchForProfanity(String input) {
        LOGGER.debug("Given text: " + input);
        long startProccessingTime = System.nanoTime();

        Set<String> badWords = dictionary.search(input);

        if (badWords.size() > 0) {
            input = clearMultipleProfanity(input.toLowerCase(), badWords);
        }

        long endProccessingTime = System.nanoTime();

        LOGGER.debug("Censored text: " + input);
        LOGGER.debug("Censoring took time: " + (endProccessingTime - startProccessingTime) / (double) 1000000 + " ms");

        return new Censored(input, badWords, !badWords.isEmpty());
    }

    public void changeLanguage(Language language){
        dictionary = DictionaryFactory.create(language, "");
    }

    public boolean isProfane(String word){
        return dictionary.isProfane(word);
    }

    public void setProfanityReplacement(String wordReplacement){
        if (wordRemoval) {
            this.wordReplacement = "";
        } else {
            this.wordReplacement = wordReplacement;
        }
    }

    public void removeProfaneWord(boolean removeProfaneWord){
        this.wordRemoval = removeProfaneWord;

        if (wordRemoval) {
            this.wordReplacement = "";
        }
    }

    private String clearMultipleProfanity(String input, Set<String> badWords) {
        Iterator<String> badWordIterator = badWords.iterator();

        while (badWordIterator.hasNext()) {
            String badWord = badWordIterator.next().replaceAll(" ", "");
            input = TextUtils.replaceCompound(input, badWord.toLowerCase(), wordReplacement);
        }

        return input;
    }

    public static class Builder {
        private String wordReplacement = "[censored]";
        private String dictionaryPath = "";
        private boolean wordRemoval = false;
        private Language language = Language.ENGLISH;

        /**
         * Replaces profanity with given replacement.
         *
         * @param wordReplacement replacement.
         * @return returns censored text;
         */
        public Builder withWordReplacement(final String wordReplacement) {
            if (wordRemoval) {
                this.wordReplacement = "";
            } else {
                this.wordReplacement = wordReplacement;
            }

            return this;
        }

        /**
         * Removes or replaces profanity.
         *
         * @param wordRemoval true if remove and false - otherwise.
         * @return censored string.
         */
        public Builder withWordRemoval(final boolean wordRemoval) {
            this.wordRemoval = wordRemoval;

            if (wordRemoval) {
                wordReplacement = "";
            }

            return this;
        }

        public Builder withDictionary(final String dictionaryPath) {
            this.dictionaryPath = dictionaryPath;
            return this;
        }

        public Builder withLanguage(final Language language){
            this.language = language;
            return this;
        }

        public ProfanityFilter build() {
            return new ProfanityFilter(this);
        }
    }

    public static void main(String[] args) {
        ProfanityFilter profanityFilter = new ProfanityFilter.Builder()
                .withWordReplacement("[profanity]")
                .withLanguage(Language.ENGLISH)
                .build();

        long startProccessingTime = System.nanoTime();
        System.out.println(profanityFilter.dictionary.size());
        long endProccessingTime = System.nanoTime();
        LOGGER.debug("Checking size took time: " + (endProccessingTime - startProccessingTime) / (double) 1000000 + " ms");

        long startProccessingTime1 = System.nanoTime();
        System.out.println("cunt - " + profanityFilter.dictionary.isProfane("Cunt"));
        long endProccessingTime1 = System.nanoTime();
        LOGGER.debug("Checking for word took time: " + (endProccessingTime1 - startProccessingTime1) / (double) 1000000 + " ms");

        long startProccessingTime2 = System.nanoTime();
        System.out.println("assassination - " + profanityFilter.dictionary.isProfane("assassination"));
        long endProccessingTime2 = System.nanoTime();
        LOGGER.debug("Checking for word took time: " + (endProccessingTime2 - startProccessingTime2) / (double) 1000000 + " ms");

        long startProccessingTime3 = System.nanoTime();
        System.out.println("ass-fucker - " + profanityFilter.dictionary.isProfane("suck-off"));
        long endProccessingTime3 = System.nanoTime();
        LOGGER.debug("Checking for word took time: " + (endProccessingTime3 - startProccessingTime3) / (double) 1000000 + " ms");

        System.out.println(profanityFilter.censor("Hello fucking world holy cow"));
        System.out.println(profanityFilter.censor("Hello, STFU"));
        System.out.println(profanityFilter.searchForProfanity("stupid motherfucker"));
        System.out.println(profanityFilter.searchForProfanity("clit"));
        System.out.println(profanityFilter.censor("Little piece of shit and silly cunt"));
    }
}