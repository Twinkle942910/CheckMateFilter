package com.filter.textcorrector.spellchecking.dictionary;

import com.filter.textcorrector.spellchecking.Spellchecker;
import com.filter.textcorrector.spellchecking.SuggestionSearcher;
import com.filter.textcorrector.spellchecking.WordSuggester;
import com.filter.textcorrector.spellchecking.model.Suggestion;
import com.filter.textcorrector.spellchecking.util.DamerauLevenshteinDistance;
import com.filter.textcorrector.spellchecking.util.Soundex;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EnglishDictionary implements Dictionary {
    private static Logger LOGGER = LoggerFactory.getLogger(EnglishDictionary.class);
    private static final String DICTIONARY_PATH = "/dictionaries/english_words_400k.txt";
    private WordSuggester wordSuggester;
    private BloomFilter<String> filter;

    public EnglishDictionary() {
        long startTime = System.nanoTime();
        loadDictionary();
        long endTime = System.nanoTime();
        LOGGER.debug("Dictionary with size - " + getSize() + " elements loaded in time: " + (endTime - startTime) / (double) 1000000 + " ms");
    }

    @Override
    public boolean contains(String word) {
        return filter.mightContain(word);
    }

    @Override
    public List<Suggestion> search(String word, float editDistancePercent) {
        //int distanceThreshold = DamerauLevenshteinDistance.convertPercentageToEditDistance(word, editDistancePercent);

        long startTime = System.nanoTime();
        Map<String, Integer> suggestionMap = wordSuggester.getSimilarityMap(word, 2);
        long endTime = System.nanoTime();

        List<Suggestion> suggestions = new ArrayList<>();
        suggestionMap.forEach((suggestedWord, suggestedDistance) -> {

            float soundexDistance = Soundex.difference(Soundex.translate(suggestedWord), Soundex.translate(word));
            float percentageDifference = DamerauLevenshteinDistance.getPercentageDifference(suggestedWord, word, suggestedDistance);
            Suggestion suggestion = new Suggestion(suggestedWord, soundexDistance, suggestedDistance, percentageDifference);

            suggestions.add(suggestion);
        });

        LOGGER.debug("Getting suggestions in: " + (endTime - startTime) / (double) 1000000 + " ms");

        return suggestions;
    }

    @Override
    public int getSize() {
        return wordSuggester.getNumberOfWords();
    }

    private void loadDictionary() {
        wordSuggester = new WordSuggester(true, StandardCharsets.UTF_8);

        try {
            InputStream inputStream = new BufferedInputStream(Spellchecker.class.getResourceAsStream(DICTIONARY_PATH));
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;

            while ((line = reader.readLine()) != null) {
                try {
                    wordSuggester.add(line);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            inputStream.close();
            reader.close();

            filter = BloomFilter.create(
                    Funnels.stringFunnel(Charset.defaultCharset()),
                    getSize(),
                    0.01);

            inputStream = new BufferedInputStream(Spellchecker.class.getResourceAsStream(DICTIONARY_PATH));
            reader = new BufferedReader(new InputStreamReader(inputStream));

            while ((line = reader.readLine()) != null) {
                try {
                    filter.put(line.toLowerCase());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            reader.close();
            inputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
