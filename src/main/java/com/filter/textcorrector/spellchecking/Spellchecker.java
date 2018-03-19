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
            return Arrays.asList(new Suggestion(word, 0, 0));
        }

        List<Suggestion> suggestedWords = dictionary.search(word, MAX_EDIT_DISTANCE);

        if (suggestedWords.isEmpty() && keepUnrecognized) {
            return Arrays.asList(new Suggestion(word, 100, 100));
        }

        Collections.sort(suggestedWords, suggestionDistanceComparator);

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

                if (dictionary.contains(part1Top) && dictionary.contains(part2Top)) {
                    distance -= 1;
                } else if (dictionary.contains(part1Top) || dictionary.contains(part2Top)) {
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
    //TODO: fix issues with numbers (e.g. 39.7k)
    public String checkText(String text) {
        long startTime = System.nanoTime();

        if (text.length() == 0 || text.equals("")) {
            return text;
        }

        Map<String, String> suggestedReplacements = new HashMap<>();

        String preproccessedText = TextPreproccessor.preproccess(text);

        String[] words = TextUtils.splitCleanText(preproccessedText, CleanTextType.SPLIT_WITHOUT_CLEANING);

        for (int i = 0; i < words.length; i++) {
            String word = words[i];

            String cleanWord = TextUtils.cleanText(word, CleanTextType.CLEAR_PUNCTUATION);

            if (TextUtils.isWordDigit(cleanWord)) {
                continue;
            }

            String fixedWord;

            //TODO: possibly, if words ignoring case equal to each other, then don't replace.
            if (!suggestedReplacements.containsKey(word)) {
                List<Suggestion> wordSuggestions = checkCompound(word);

                if (wordSuggestions.isEmpty()) {
                    fixedWord = "";
                } else {
                    fixedWord = wordSuggestions.get(0).getWord();
                }
            } else {
                fixedWord = suggestedReplacements.get(word);
            }

            suggestedReplacements.put(word, fixedWord);

            preproccessedText = TextUtils.replaceWord(preproccessedText, word, fixedWord);
        }

        if (preproccessedText.length() > 0) {
            preproccessedText = Character.toUpperCase(preproccessedText.charAt(0)) + preproccessedText.substring(1);
        }

        long endTime = System.nanoTime();
        System.out.println("Checking took time: " + (endTime - startTime) / (double) 1000000 + " ms");

        return preproccessedText;
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

        System.out.println(spellchecker.checkText("sign in sign up ranker home people entertainment sports culture channels videos create a list about us advertise press ranker insights actors celebrity facts historical figures musicians politicians anime gaming movies music tv athletes baseball basketball football soccer food politics & history relationships travel thought provoking weird history graveyard shift total nerd anime underground weird nature weirdly interesting 40 LISTS Jokes, Jokes, JokesGet your laughing lips ready. Chuck Norris Your Mom Yo Mama Knock Knock! Dad Jokes PG Jokes Ancient Comedy Mitch Hedberg Photo: Meetup.com jokes Dirty Adult Jokes That Will Get You a Laugh on Demand Evan Lambert 37.9k votes 7.7k voters 465.6k views 38 items Follow Embed List Rules Vote up the funniest joke! Looking for a quick and dirty joke to get you an easy laugh? Then these funny adult jokes are for you. We've compiled the funniest jokes about sex that you'll ever come across, so that you can go and tell your friends - hopefully without offending them. Vote on your favorite funny adult joke! 1 3,449 VOTES What do a penis and a Rubik's Cube have in common? The more you play with it, the harder it gets. 2,682 767 Agree or disagree? 14 Famous People Who Have PhDs 2 2,233 VOTES How is a push-up bra like a bag of chips? Once you open it, you realize it's half-empty. 1,692 541 Agree or disagree? 3 2,446 VOTES What do boobs and toys have in common? They were both originally made for kids, but daddies end up playing with them. 1,846 600 Agree or disagree? Adult Jokes Hidden In Dr. Seuss Movies That Went Right Over Your Head 4 1,814 VOTES What do the Mafia and pussies have in common? One slip of the tongue, and you're in deep sh*t. 1,384 430 Agree or disagree? LOAD MORE Filed Under: Polls funnyjokesadult jokesHumor love this list? Dirty Adult Jokes That Will Get You a Laugh on Demand share tweet pin email embed rank your version prev list more popular lists next list 47 Adult Jokes in Cartoons You Didn't Get As A Child Female Sports with the Hottest Athletes This Makeup Artist Creates Mesmerizing Lip Art Inspired By Nature The 20 Most Epic Wedding FAILs of All Time Stereotypes That Republicans Are Tired Of Hearing Anime Characters Ranked By How Tragically Their Parents Died The Best Starter Pokemon The Smartest Anime Characters of All Time The 25+ Greatest Anime Characters With Fire Powers Where Were You on September 11th? The Best Generation 1 Pokemon 25 Signs You Just Don't Care About Star Wars Anymore Funny Names to Give a Sugar Glider The Most Overrated Wrestlers of All Time The Best Tasting Whiskey The Most Powerful Anime Characters of All Time The 25+ Best Anime Water Users of All Time . Top 10 Current Queries: biathlon medals song laura bands from colorado versace celebrities chronicles of riddick cast tiger character michael alig sandlot characters famous teenagers teller young mobile site contact us we're hiring embed a list data blog listopedia like us on facebook follow us on pinterest subscribe to our top lists Information and media on this page and throughout Ranker is supplied by Wikipedia, Ranker users, and other sources. Freebase content is freely licensed under the CC-BY license and Wikipedia content is licensed under the GNU Free Documentation license. © Ranker 2018 terms privacy sitemap"));
       // System.out.println(spellchecker.checkCompound("votes"));
    }
}
