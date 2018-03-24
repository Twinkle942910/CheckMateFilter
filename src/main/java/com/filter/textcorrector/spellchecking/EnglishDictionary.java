package com.filter.textcorrector.spellchecking;

import com.filter.textcorrector.spellchecking.model.Suggestion;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;

public class EnglishDictionary implements Dictionary {
    private static Logger LOGGER = LoggerFactory.getLogger(EnglishDictionary.class);
    private static final String DICTIONARY_PATH = "/dictionaries/en_common_dictionary_80k.txt";
    private SuggestionSearcher suggestionSearcher;
    private BloomFilter<String> filter;

    public EnglishDictionary() {
        long startTime = System.currentTimeMillis();
        loadDictionary();
        long endTime = System.currentTimeMillis();
        LOGGER.debug("Dictionary with size - " + getSize() + " elements loaded in time: " + (endTime - startTime) / (double) 1000000 + " ms");
    }

    @Override
    public boolean contains(String word) {
        return filter.mightContain(word);
    }

    @Override
    public List<Suggestion> search(String word, float editDistancePercent) {
        return suggestionSearcher.search(word, editDistancePercent);
    }

    @Override
    public int getSize() {
        return suggestionSearcher.getSize();
    }

    private void loadDictionary() {
        suggestionSearcher = new SuggestionSearcher();

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(Spellchecker.class.getResourceAsStream(DICTIONARY_PATH)));

            String line;

            while ((line = reader.readLine()) != null) {
                try {
                    suggestionSearcher.add(line);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            filter = BloomFilter.create(
                    Funnels.stringFunnel(Charset.defaultCharset()),
                    getSize(),
                    0.01);

            while ((line = reader.readLine()) != null) {
                try {
                    filter.put(line.toLowerCase());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
