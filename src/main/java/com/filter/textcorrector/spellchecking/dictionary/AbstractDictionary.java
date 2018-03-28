package com.filter.textcorrector.spellchecking.dictionary;

import com.filter.textcorrector.spellchecking.Spellchecker;
import com.filter.textcorrector.spellchecking.WordSuggester;
import com.filter.textcorrector.spellchecking.model.Suggestion;
import com.filter.textcorrector.spellchecking.util.DamerauLevenshteinDistance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

//TODO: implement white and black list, add custom words to list.
public class AbstractDictionary implements Dictionary {
    private static Logger LOGGER = LoggerFactory.getLogger(AbstractDictionary.class);
    private final String DICTIONARY_PATH;
    private WordSuggester wordSuggester;
   /* private SuggestionSearcher wordSuggester;
    private BloomFilter<String> filter;*/

    public AbstractDictionary(final String dictionaryPath) {
        long startTime = System.nanoTime();
        this.DICTIONARY_PATH = dictionaryPath;
        loadDictionary();
        long endTime = System.nanoTime();
        LOGGER.debug(this.getClass().getSimpleName() + " with size - " + getSize() + " elements loaded in time: " + (endTime - startTime) / (double) 1000000 + " ms");
    }

    @Override
    public boolean contains(String word) {
        return wordSuggester.search(word);
        // return filter.mightContain(word.toLowerCase());
    }

    @Override
    public List<Suggestion> search(String word, float editDistancePercent) {
        int distanceThreshold = DamerauLevenshteinDistance.convertPercentageToEditDistance(word, editDistancePercent);
        return wordSuggester.getSuggestions(word, distanceThreshold);

        // return wordSuggester.search(word, editDistancePercent);
    }

    @Override
    public int getSize() {
        return wordSuggester.getNumberOfWords();
        //return wordSuggester.getSize();
    }

    private void loadDictionary() {
        wordSuggester = new WordSuggester(false, StandardCharsets.UTF_8);
        //wordSuggester = new SuggestionSearcher();

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

           /* filter = BloomFilter.create(
                    Funnels.stringFunnel(Charset.defaultCharset()),
                    getSize(),
                    0.001);

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
            inputStream.close();*/

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
