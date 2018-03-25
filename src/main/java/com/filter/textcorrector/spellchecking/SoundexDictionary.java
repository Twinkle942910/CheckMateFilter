package com.filter.textcorrector.spellchecking;

import com.filter.textcorrector.spellchecking.model.Suggestion;
import com.filter.textcorrector.spellchecking.util.DamerauLevenshteinDistance;
import com.filter.textcorrector.spellchecking.util.Soundex;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Loads a words from the file.
 */
public class SoundexDictionary {

    private static final String DICTIONARY_PATH = "/dictionaries/en_common_dictionary_80k.txt";
    private static final Database dictionaryDatabase = Database.getInstance();

    private SuggestionSearcher suggestionSearcher = new SuggestionSearcher();

    /**
     * Creates an object of Dictionary class.
     */
    public SoundexDictionary() {
        loadDictionary();
    }

    private void loadDictionary() {

        try {
            InputStream inputStream = new BufferedInputStream(Spellchecker.class.getResourceAsStream(DICTIONARY_PATH));
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;

            while ((line = reader.readLine()) != null) {
                try {
                    String word = line;
                    String soundexCode = Soundex.translate(word);

                    Set<String> setOfMatches = dictionaryDatabase.getSetOfMatches(soundexCode);

                    if (setOfMatches != null) {
                        setOfMatches.add(word.toLowerCase());
                    } else {
                        setOfMatches = new HashSet<>();
                        setOfMatches.add(word.toLowerCase());
                    }

                    dictionaryDatabase.addMatchingPair(soundexCode, setOfMatches);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }

            inputStream.close();
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Suggestion> suggestionList(String word){
        String soundexCode = Soundex.translate(word);
        Set<String> setOfMatches = dictionaryDatabase.getSetOfMatches(soundexCode);

        long startProccessingTime = System.nanoTime();

        List<Suggestion> suggestedWords = new ArrayList<>();

        setOfMatches.forEach((w) -> {
            int currentDistance = DamerauLevenshteinDistance.distance(w.toLowerCase(), word.toLowerCase());
            float percentageDifference = DamerauLevenshteinDistance.getPercentageDifference(w, word, currentDistance);
            float soundexDistance = Soundex.difference(Soundex.translate(w), Soundex.translate(word));

            suggestedWords.add(new Suggestion(w, soundexDistance, currentDistance, percentageDifference));
        });

        long endProccessingTime = System.nanoTime();
        System.out.println("Search took time: " + (endProccessingTime - startProccessingTime) / (double) 1000000 + " ms");

        return suggestedWords
                .stream()
                .sorted(Spellchecker.suggestionDistanceComparator)
                .limit(5)
                .collect(Collectors.toList());
    }

    public Database getDictionaryDatabase() {
        return dictionaryDatabase;
    }

    public static void main(String[] args) {
        SoundexDictionary soundexDictionary = new SoundexDictionary();
        long startProccessingTime = System.nanoTime();
        System.out.println(soundexDictionary.suggestionList("beilevin"));
        long endProccessingTime = System.nanoTime();
        System.out.println("Checking took time: " + (endProccessingTime - startProccessingTime) / (double) 1000000 + " ms");
    }
}
