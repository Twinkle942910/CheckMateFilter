package com.filter.textcorrector.spellchecking;

import com.filter.textcorrector.spellchecking.model.Suggestion;
import com.filter.textcorrector.spellchecking.util.DamerauLevenshteinDistance;
import com.filter.textcorrector.spellchecking.util.Soundex;
import com.filter.textcorrector.text_preproccessing.TextPreproccessor;
import com.filter.textcorrector.text_preproccessing.util.CleanTextType;
import com.filter.textcorrector.text_preproccessing.util.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Spellchecker {

    private static final SuggestionDistanceComparator suggestionDistanceComparator = new SuggestionDistanceComparator();
    private static final int MAX_EDIT_DISTANCE = 2;
    private Dictionary dictionary;
    private boolean keepUnrecognized = true;
    private int suggestionLimit = 5;

    public Spellchecker() {
        dictionary = new Dictionary();
    }

    public List<Suggestion> checkWord(String word) {
        long startTime = System.nanoTime();

        if (word.equals("") || dictionary.contains(word.toLowerCase())) {
            return Arrays.asList(new Suggestion(word, 0, 0));
        }

        List<Suggestion> suggestedWords = dictionary.search(word, MAX_EDIT_DISTANCE);

        if (suggestedWords.isEmpty() && keepUnrecognized) {
            return Arrays.asList(new Suggestion(word, 100, 100));
        }

        Collections.sort(suggestedWords, suggestionDistanceComparator);


        long endTime = System.nanoTime();
      // System.out.println("Checking word took time: " + (endTime - startTime) / (double) 1000000 + " ms");

        return suggestedWords/*.stream()
                .limit(suggestionLimit)
                .collect(Collectors.toList())*/;
    }

    public List<Suggestion> checkCompound(String word) {
        if (word.equals("") || dictionary.contains(word.toLowerCase())) {
            return Arrays.asList(new Suggestion(word, 0, 0));
        }

        List<Suggestion> splitSuggestions = new ArrayList<>();

        //TODO: temp solution, replace later.
        Map<String, Integer> distances = new LinkedHashMap<>();
        List<Suggestion> singleWordSuggestions = checkWord(word);

        Suggestion firstSingleWord;

        if (keepUnrecognized) {
            firstSingleWord = singleWordSuggestions.get(0);
        } else {
            firstSingleWord = new Suggestion(word, 100, 100);
        }

        if (firstSingleWord.getSoundexCodeDistance() == 0 && firstSingleWord.getEditDistance() == 0) {
            return singleWordSuggestions;
        }

        if (word.length() > 1) {
            for (int j = 1; j < word.length(); j++) {
                String part1 = word.substring(0, j);
                String part2 = word.substring(j);

                Suggestion suggestionSplit;

                List<Suggestion> suggestions1 = checkWord(part1);
                List<Suggestion> suggestions2 = checkWord(part2);

                String part1Top = suggestions1.isEmpty() ? part1 : suggestions1.get(0).getWord();
                String part2Top = suggestions2.isEmpty() ? part2 : suggestions2.get(0).getWord();

                //select best suggestion for split pair
                String split = part1Top + " " + part2Top;

                int distance = (int) DamerauLevenshteinDistance.distanceCaseIgnore(word, split);
                double soundexDistance = Soundex.difference(Soundex.translate(word), Soundex.translate(split));

                distances.put(split, distance);

                if (dictionary.contains(part1Top.toLowerCase()) && dictionary.contains(part2Top.toLowerCase())) {
                    distance -= 1;
                } else if (dictionary.contains(part1Top.toLowerCase()) || dictionary.contains(part2Top.toLowerCase())) {
                    soundexDistance += 1;
                } else {
                    soundexDistance += 2;
                    distance += 2;
                }

                suggestionSplit = new Suggestion(split, soundexDistance, distance);

                //TODO: don't add repeated suggestions.
                splitSuggestions.add(suggestionSplit);
            }
        }

        if (splitSuggestions.isEmpty() && !singleWordSuggestions.isEmpty()) {
            return singleWordSuggestions;
        }

        Collections.sort(splitSuggestions, suggestionDistanceComparator);

        Suggestion suggestion = splitSuggestions.get(0);

        if (suggestion.getSoundexCodeDistance() >= 1) {
            if (keepUnrecognized) return singleWordSuggestions;
            else return new ArrayList<>();
        } else {
            Suggestion firstSplitWord = new Suggestion(suggestion.getWord(), suggestion.getSoundexCodeDistance(), distances.get(suggestion.getWord()));

            int best = suggestionDistanceComparator.compare(firstSingleWord, firstSplitWord);

            if (best < 0 || best == 0) {
                return singleWordSuggestions;
            }
        }

        return splitSuggestions/*.stream()
                .limit(suggestionLimit)
                .collect(Collectors.toList())*/;
    }

    //TODO: fix issues with names.
    public String checkText(String text) {
        long startTime = System.nanoTime();

        if (text.length() == 0 || text.equals("")) {
            return text;
        }

        Map<String, String> suggestedReplacements = new HashMap<>();

        long startProccessingTime = System.nanoTime();
        String preproccessedText = TextPreproccessor.preproccess(text);
        long endProccessingTime = System.nanoTime();
        System.out.println("Proccessing took time: " + (endProccessingTime - startProccessingTime) / (double) 1000000 + " ms");

        String[] words = TextUtils.splitCleanText(preproccessedText, CleanTextType.SPLIT_WITHOUT_CLEANING);

        for (int i = 0; i < words.length; i++) {
            String word = words[i];

            String cleanWord = TextUtils.cleanText(word, CleanTextType.CLEAR_PUNCTUATION);

            if (TextUtils.isWordDigit(cleanWord)) {
                continue;
            }

            String fixedWord;

            if (!suggestedReplacements.containsKey(word) && !dictionary.contains(word.toLowerCase())) {
             //   List<Suggestion> wordSuggestions = checkCompound(word);
                List<Suggestion> wordSuggestions = checkWord(word);

                if (wordSuggestions.isEmpty()) {
                    fixedWord = "";
                } else {
                    Suggestion suggestion = wordSuggestions.get(0);

                    if(suggestion.getSoundexCodeDistance() >= 0.09){
                        fixedWord = word;
                    }
                    else {
                        fixedWord = suggestion.getWord();
                    }
                }

                suggestedReplacements.put(word, fixedWord);

                preproccessedText = TextUtils.replaceWord(preproccessedText, word, fixedWord);
            } else {
                continue;
            }
        }

        if (preproccessedText.length() > 0) {
            preproccessedText = Character.toUpperCase(preproccessedText.charAt(0)) + preproccessedText.substring(1);
        }

        long endTime = System.nanoTime();
        System.out.println("Checking took time: " + (endTime - startTime) / (double) 1000000 + " ms");

        return preproccessedText;
    }

    public List<Suggestion> checkOneWord(String word){
        long startTime = System.nanoTime();

        List<Suggestion> suggestions = checkWord(word);

        long endTime = System.nanoTime();
        System.out.println("Checking took time: " + (endTime - startTime) / (double) 1000000 + " ms");

        return suggestions;
    }

    private static final class SuggestionDistanceComparator implements Comparator<Suggestion> {

        @Override
        public int compare(Suggestion suggestion1, Suggestion suggestion2) {
            int dComp = suggestion1.compareTo(suggestion2);

            if (dComp != 0) {
                return dComp;
            } else {
                if (suggestion1.getSoundexCodeDistance() > suggestion2.getSoundexCodeDistance()) {
                    return 1;
                } else if (suggestion1.getSoundexCodeDistance() < suggestion2.getSoundexCodeDistance()) {
                    return -1;
                }
                return 0;
            }
        }

    }

    public static void main(String[] args) {
        Spellchecker spellchecker = new Spellchecker();

        System.out.println(spellchecker.checkText("()-"));
       // System.out.println(spellchecker.checkCompound("Stereotypes"));

       /* System.out.println(spellchecker.checkOneWord("lambert"));
        System.out.println();
        System.out.println(spellchecker.checkCompound("lambert"));*/
    }
}
