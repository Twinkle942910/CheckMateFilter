package com.filter.textcorrector.profanity_filtering.dictionary;

import com.filter.textcorrector.text_preproccessing.util.TextUtils;
import com.hankcs.algorithm.AhoCorasickDoubleArrayTrie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class PlainDictionary implements Dictionary {
    private static Logger LOGGER = LoggerFactory.getLogger(PlainDictionary.class);
    private final String DICTIONARY_PATH;

    private List<AhoCorasickDoubleArrayTrie<String>.Hit<String>> badWordList;
    private AhoCorasickDoubleArrayTrie<String> dictionary = new AhoCorasickDoubleArrayTrie<>();
    private Map<String, String> badWordsCompounds = new HashMap<>();

    public PlainDictionary(final String dictionaryPath) {
        this.DICTIONARY_PATH = dictionaryPath;
        loadDictionary();
    }

    @Override
    public Set<String> search(String text) {
        badWordList = null;
        return searchForBadWords(text);
    }

    @Override
    public boolean isProfane(String phrase) {
        int exactMatch = dictionary.exactMatchSearch(phrase.replaceAll("[^a-zA-Z0-9]", "").toLowerCase());
        return exactMatch > 0;
    }

    @Override
    public int size() {
        return dictionary.size();
    }

    private void loadDictionary() {
        long startProccessingTime = System.nanoTime();
        Map<String, String> words = new HashMap<>();

        try (Stream<String> lines = new BufferedReader(
                new InputStreamReader(ClassLoader.getSystemResourceAsStream(DICTIONARY_PATH))).lines()) {

            lines.forEach(badPhrase -> {
                String word = badPhrase.replaceAll(" ", "").toLowerCase();
                badWordsCompounds.put(word, badPhrase);
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
    private Set<String> searchForBadWords(String input) {
        if (input == null) {
            return new LinkedHashSet<>();
        }

        Set<String> badWords = new LinkedHashSet<>();
        String parseInput = input.toLowerCase().replaceAll("[^a-zA-Z0-9]", "");

        if(isInList(parseInput) > 0){
            badWords.add(input);
            return badWords;
        }

        if (badWordList == null) {
            badWordList = getBadWordList(parseInput);
        }

        String checkText = input.toLowerCase();

        for (AhoCorasickDoubleArrayTrie<String>.Hit<String> hit : badWordList) {
            String profanePhrase = hit.value;
            LOGGER.debug(profanePhrase + " - " + "[" + hit.begin + ":" + hit.end + "]" + " - qualified as a bad word");

            if (TextUtils.containsCompound(checkText, profanePhrase)) {
                badWords.add(badWordsCompounds.get(profanePhrase));
                checkText = TextUtils.replaceCompound(checkText, profanePhrase, "");
            }
        }
        return badWords;
    }

    private List<AhoCorasickDoubleArrayTrie<String>.Hit<String>> getBadWordList(String input) {
        List<AhoCorasickDoubleArrayTrie<String>.Hit<String>> badWords = dictionary.parseText(input);
        badWords.sort((word1, word2) -> Integer.compare(word2.value.length(), word1.value.length()));
        return badWords;
    }

    private int isInList(String phrase){
        return dictionary.exactMatchSearch(phrase);
    }
}