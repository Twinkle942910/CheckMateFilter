package com.filter.textcorrector.spellchecking;

import com.filter.textcorrector.spellchecking.model.Suggestion;
import com.filter.textcorrector.spellchecking.util.DamerauLevenshteinDistance;
import com.filter.textcorrector.spellchecking.util.Soundex;
import com.filter.textcorrector.text_preproccessing.model.ProccessedText;
import com.filter.textcorrector.text_preproccessing.util.CleanTextType;
import com.filter.textcorrector.text_preproccessing.util.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        if (word.equals("") || dictionary.contains(word)) {
            return new ArrayList<>();
        }

        List<Suggestion> suggestedWords = dictionary.search(word, MAX_EDIT_DISTANCE);

        if (suggestedWords.isEmpty() && keepUnrecognized) {
            return Arrays.asList(new Suggestion(word, 0, 0));
        }

        Collections.sort(suggestedWords, suggestionDistanceComparator);

        return suggestedWords/*.stream()
                .limit(suggestionLimit)
                .collect(Collectors.toList())*/;
    }

    //TODO: check if word is digit.
    public List<Suggestion> checkCompound(String word) {
        long startTime = System.currentTimeMillis();

        List<Suggestion> splitSuggestions = new ArrayList<>();

        //TODO: temp solution, replace later.
        Map<String, Integer> distances = new LinkedHashMap<>();

        if (word.equals("") || dictionary.contains(word)) {
            return splitSuggestions;
        }

        List<Suggestion> singleWordSuggestions = checkWord(word);

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
                distances.put(split, distance);

                if (dictionary.contains(part1Top) && dictionary.contains(part2Top)) {
                    distance -= 1;
                } else {
                    distance += 2;
                }

                suggestionSplit = new Suggestion(split, Soundex.difference(Soundex.translate(word), Soundex.translate(split)), distance);

                //TODO: don't add repeated suggestions.
                splitSuggestions.add(suggestionSplit);
            }
        }

        Collections.sort(splitSuggestions, suggestionDistanceComparator);

        //TODO: take into account 'keepUnrecognized' tag.
        //TODO: Won't work well, because distance changes if both words are in the dictionary.
        if (!singleWordSuggestions.isEmpty() && !splitSuggestions.isEmpty() && !singleWordSuggestions.get(0).getWord().equals(word)) {
            Suggestion suggestion = splitSuggestions.get(0);
            Suggestion suggestion1 = new Suggestion(suggestion.getWord(), suggestion.getSoundexCodeDistance(), distances.get(suggestion.getWord()));

            //TODO: figure out the problem with 'stayin' and 'staying' words in both lists.
            int best = suggestionDistanceComparator.compare(singleWordSuggestions.get(0), suggestion1);

            if (best < 0 || best == 0) {
                return singleWordSuggestions;
            }

            long endTime = System.currentTimeMillis();
            System.out.println("Checking took time: " + (endTime - startTime) + " ms");

            return splitSuggestions.stream()
                    .limit(suggestionLimit)
                    .collect(Collectors.toList());
        }
        else {
            return splitSuggestions;
        }

        //TODO: think about returning when it didn't find the word.
    }

    //TODO: make first letter always big when checking text.
   /* public String check(String text){

        String[] clearWords = TextUtils.splitCleanText(correctedText, CleanTextType.SPLIT_CLEARED_WORDS);

        List<String> fixedList = new ArrayList<>();

        for (int i = 0; i < clearWords.length; i++) {
            String clearWord = clearWords[i];

            if (TextUtils.isWordDigit(TextUtils.cleanText(clearWord, CleanTextType.CLEAR_PUNCTUATION))) {
                fixedList.add(clearWord);
                continue;
            }

            if (spellCheck.contains(clearWord.toLowerCase())
                    || clearWord.toLowerCase().equals("a")
                    || clearWord.toLowerCase().equals("i")) {
                fixedList.add(clearWord);
                continue;
            }

            String compound = lookupCompound(clearWord.toLowerCase());

            String fixedWord = "";

            if (compound != null) {
                if (clearWord.equals(compound)) {
                    fixedWord = clearWord;
                } else {
                    fixedWord = compound;
                }
            } else if (keepUnrecognized) {
                fixedWord = clearWord;
            }

            fixedList.add(fixedWord);

            correctedText = TextUtils.replaceWord(correctedText, clearWord, fixedWord.toLowerCase());
        }

        String[] fixedWords = fixedList.toArray(new String[]{});

        return new ProccessedText(originalWords, fixedWords, correctedText);

        return null;
    }*/

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

        System.out.println(spellchecker.checkCompound("roughplaces"));
    }
}
