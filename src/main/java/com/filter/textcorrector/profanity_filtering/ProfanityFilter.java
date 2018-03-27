package com.filter.textcorrector.profanity_filtering;

import com.filter.textcorrector.profanity_filtering.model.Censored;
import com.filter.textcorrector.text_preproccessing.util.TextUtils;
import com.hankcs.algorithm.AhoCorasickDoubleArrayTrie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Checks given text for bad words.
 */
//TODO: separate dictionary and functionality (for replacing dict.).
public class ProfanityFilter {
    private static Logger LOGGER = LoggerFactory.getLogger(ProfanityFilter.class);

    private List<AhoCorasickDoubleArrayTrie<String>.Hit<String>> badWordList;
    private AhoCorasickDoubleArrayTrie<String> dictionary = new AhoCorasickDoubleArrayTrie<>();
    private Map<String, String> badWordsCompounds = new HashMap<>();
    private final String dictionaryPath;
    private String wordReplacement;

    private ProfanityFilter(final String dictionaryPath, final String wordReplacement) {
        this.dictionaryPath = dictionaryPath;
        this.wordReplacement = wordReplacement;
        loadDictionary();
    }

    /**
     * Returns censored text with custom word or nothing instead of bad one.
     *
     * @param input text to censor.
     * @return censored String.
     */
    public String censor(String input) {
        badWordList = null;

        LOGGER.debug("Given text: " + input);
        long startProccessingTime = System.nanoTime();

        Set<String> badWords = badWordsFound(input);

        if (badWords.size() > 0) {
            input = clearMultipleProfanity(input.toLowerCase(), badWords);
        }

        long endProccessingTime = System.nanoTime();

        LOGGER.debug("Censored text: " + input);
        LOGGER.debug("Censoring took time: " + (endProccessingTime - startProccessingTime) / (double) 1000000 + " ms");

        return input;
    }

    public Censored searchForProfanity(String input) {
        badWordList = null;

        LOGGER.debug("Given text: " + input);
        long startProccessingTime = System.nanoTime();

        Set<String> badWords = badWordsFound(input);

        if (badWords.size() > 0) {
            input = clearMultipleProfanity(input.toLowerCase(), badWords);
        }

        long endProccessingTime = System.nanoTime();

        LOGGER.debug("Censored text: " + input);
        LOGGER.debug("Censoring took time: " + (endProccessingTime - startProccessingTime) / (double) 1000000 + " ms");

        return new Censored(input, badWords);
    }

    public static class Builder {
        private String dictionaryPath = "dictionaries/bad-word_list.txt";
        private String wordReplacement = "[censored]";
        private boolean wordRemoval = false;

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

        public ProfanityFilter build() {
            return new ProfanityFilter(dictionaryPath, wordReplacement);
        }

    }

    private void loadDictionary() {
        long startProccessingTime = System.nanoTime();
        Map<String, String> words = new HashMap<>();

        try (Stream<String> lines = new BufferedReader(
                new InputStreamReader(ClassLoader.getSystemResourceAsStream(dictionaryPath))).lines()) {

            lines.forEach(badPhrase -> {
                badWordsCompounds.put(badPhrase.replaceAll(" ", ""), badPhrase);
                String word = badPhrase.replaceAll(" ", "").toLowerCase();
                words.put(word, word);
            });
        } catch (Exception e) {
            LOGGER.debug("Something went wrong with loading a file.");
        }

        long endProccessingTime = System.nanoTime();

        dictionary.build(words);
        LOGGER.debug("Loading dictionary took time: " + (endProccessingTime - startProccessingTime) / (double) 1000000 + " ms");
    }

    /**
     * Returns a list with all bad words that occurred in the given text.
     *
     * @param input text that is checked for profanity.
     * @return list of bad words.
     */
    private Set<String> badWordsFound(String input) {
        if (input == null) {
            return new LinkedHashSet<>();
        }

        Set<String> badWords = new LinkedHashSet<>();
        String parseInput = input.toLowerCase().replaceAll("[^a-zA-Z0-9]", "");

        if (badWordList == null) {
            badWordList = getBadWordList(parseInput);
        }

        String checkText = input.toLowerCase();

        for (AhoCorasickDoubleArrayTrie<String>.Hit<String> hit : badWordList) {
            String profanePhrase = hit.value;
            LOGGER.debug(profanePhrase + " - " + "[" + hit.begin + ":" + hit.end + "]" + " - qualified as a bad word");

            if (TextUtils.containsCompound(checkText, profanePhrase)) {
                badWords.add(badWordsCompounds.get(profanePhrase));
                checkText = TextUtils.replaceCompound(checkText, profanePhrase, wordReplacement);
            }
        }

        return badWords;
    }

    private List<AhoCorasickDoubleArrayTrie<String>.Hit<String>> getBadWordList(String input) {
        List<AhoCorasickDoubleArrayTrie<String>.Hit<String>> badWords = dictionary.parseText(input);
        badWords.sort((word1, word2) -> Integer.compare(word2.value.length(), word1.value.length()));
        return badWords;
    }

    private String clearMultipleProfanity(String input, Set<String> badWords) {
        Iterator<String> badWordIterator = badWords.iterator();

        while (badWordIterator.hasNext()) {
            String badWord = badWordIterator.next().replaceAll(" ", "");
            input = TextUtils.replaceCompound(input, badWord, wordReplacement);
        }

        return input;
    }

    public static void main(String[] args) {
        ProfanityFilter profanityFilter = new ProfanityFilter.Builder()
                .withWordReplacement("[censored]")
                .build();

        System.out.println(profanityFilter.censor("Hello fucking world holy cow"));
        System.out.println(profanityFilter.searchForProfanity("stupid motherfucker"));
        System.out.println(profanityFilter.censor("Little piece of shit and silly cunt"));
    }
}