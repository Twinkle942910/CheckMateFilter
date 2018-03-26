package com.filter.textcorrector.spellchecking;

import com.filter.textcorrector.spellchecking.dictionary.Dictionary;
import com.filter.textcorrector.spellchecking.dictionary.DictionaryFactory;
import com.filter.textcorrector.spellchecking.model.Suggestion;
import com.filter.textcorrector.spellchecking.util.DamerauLevenshteinDistance;
import com.filter.textcorrector.spellchecking.util.Soundex;
import com.filter.textcorrector.text_preproccessing.TextPreproccessor;
import com.filter.textcorrector.text_preproccessing.util.CleanTextType;
import com.filter.textcorrector.text_preproccessing.util.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Spellchecker {
    private static Logger LOGGER = LoggerFactory.getLogger(Spellchecker.class);
    private static final SuggestionDistanceComparator suggestionDistanceComparator = new SuggestionDistanceComparator();

    public static final float MAX_EDIT_DISTANCE_PERCENT = 70f;
    public static final double MAX_SOUNDEX_DISTANCE = 0.09;

    private Dictionary dictionary;
    private TextPreproccessor textPreproccessor;

    private boolean keepUnrecognized = true;
    private int suggestionLimit = 5;
    private boolean checkCompounds = false;

    public Spellchecker(Language language) {
        dictionary = DictionaryFactory.create(language);
        textPreproccessor = TextPreproccessor.getInstance();
    }

    public List<String> checkWord(String word) {
        long startProccessingTime1 = System.nanoTime();
        List<Suggestion> suggestions = getWordSuggestions(textPreproccessor.preproccess(word));
        long endProccessingTime1 = System.nanoTime();
        LOGGER.debug("Word c took time: " + (endProccessingTime1 - startProccessingTime1) / (double) 1000000 + " ms");
        return convertList(suggestions, Suggestion::getWord);
    }

    public List<String> checkCompound(String compound) {
        long startProccessingTime1 = System.nanoTime();
        List<Suggestion> suggestions = getCompoundSuggestions(textPreproccessor.preproccess(compound));
        long endProccessingTime1 = System.nanoTime();
         LOGGER.debug("Word c took time: " + (endProccessingTime1 - startProccessingTime1) / (double) 1000000 + " ms");
        return convertList(suggestions, Suggestion::getWord);
    }

    private <F, T> List<T> convertList(List<F> from, Function<F, T> by){
        return from.stream().limit(suggestionLimit).map(by).collect(Collectors.toList());
    }

    private List<Suggestion> getWordSuggestions(String word) {
        if(word == null){
            throw new IllegalArgumentException("Can't give suggestions fo null word.");
        }

        if (word.equals("") || isValid(word)) {
            return Collections.singletonList(new Suggestion(word, 0, 0, 100));
        }

        List<Suggestion> suggestedWords = dictionary.search(word, MAX_EDIT_DISTANCE_PERCENT);

        if (suggestedWords.isEmpty() && keepUnrecognized) {
            return Collections.singletonList(new Suggestion(word, 100, 100, 0));
        }

        return suggestedWords.stream()
                .sorted(suggestionDistanceComparator)
                .limit(suggestionLimit)
                .collect(Collectors.toList());
    }

    private List<Suggestion> getCompoundSuggestions(String word) {
        if(word == null){
            throw new IllegalArgumentException("Can't give suggestions fo null word.");
        }

        if (word.equals("") || dictionary.contains(word.toLowerCase())) {
            return Collections.singletonList(new Suggestion(word, 0, 0, 100));
        }

        List<Suggestion> splitSuggestions = new ArrayList<>();

        //TODO: temp solution, replace later.
        Map<String, Integer> distances = new LinkedHashMap<>();
        List<Suggestion> singleWordSuggestions = getWordSuggestions(word);

        Suggestion firstSingleWord;

        if (keepUnrecognized) {
            firstSingleWord = singleWordSuggestions.get(0);
        } else {
            firstSingleWord = new Suggestion(word, 100, 100, 0);
        }

        if (firstSingleWord.getSoundexCodeDistance() == 0 && firstSingleWord.getEditDistance() == 0) {
            return singleWordSuggestions;
        }

        if (word.length() > 1) {
            for (int j = 1; j < word.length(); j++) {
                String part1 = word.substring(0, j);
                String part2 = word.substring(j);

                Suggestion suggestionSplit;

                List<Suggestion> suggestions1 = getWordSuggestions(part1);
                List<Suggestion> suggestions2 = getWordSuggestions(part2);

                String part1Top = suggestions1.isEmpty() ? part1 : suggestions1.get(0).getWord();
                String part2Top = suggestions2.isEmpty() ? part2 : suggestions2.get(0).getWord();

                //select best suggestion for split pair
                String split = part1Top + " " + part2Top;

                int distance = DamerauLevenshteinDistance.distance(word.toLowerCase(), split.toLowerCase());
                float soundexDistance = Soundex.difference(Soundex.translate(word), Soundex.translate(split));
                float splitPercentage = DamerauLevenshteinDistance.getPercentageDifference(word.toLowerCase(), split.toLowerCase(), distance);

                distances.put(split, distance);

                //TODO: fix percentage count.
                if (dictionary.contains(part1Top.toLowerCase()) && dictionary.contains(part2Top.toLowerCase())) {
                    distance -= 1;
                    splitPercentage += DamerauLevenshteinDistance.getPercentageDifference(word.toLowerCase(), split.toLowerCase(), distance);
                } else if (dictionary.contains(part1Top.toLowerCase()) || dictionary.contains(part2Top.toLowerCase())) {
                    soundexDistance += 1;
                } else {
                    soundexDistance += 2;
                    distance += 2;
                    splitPercentage -= DamerauLevenshteinDistance.getPercentageDifference(word.toLowerCase(), split.toLowerCase(), distance);
                }

                suggestionSplit = new Suggestion(split, soundexDistance, distance, splitPercentage);

                //TODO: don't add repeated suggestions.
                splitSuggestions.add(suggestionSplit);
            }
        }

        if (splitSuggestions.isEmpty() && !singleWordSuggestions.isEmpty()) {
            return singleWordSuggestions;
        }

        splitSuggestions.sort(suggestionDistanceComparator);

        Suggestion suggestion = splitSuggestions.get(0);

        if (suggestion.getSoundexCodeDistance() >= 1) {
            if (keepUnrecognized) return singleWordSuggestions;
            else return new ArrayList<>();
        } else {
            Suggestion firstSplitWord = new Suggestion(suggestion.getWord(), suggestion.getSoundexCodeDistance(), distances.get(suggestion.getWord()), 100);

            int best = suggestionDistanceComparator.compare(firstSingleWord, firstSplitWord);

            if (best < 0 || best == 0) {
                return singleWordSuggestions;
            }
        }

        return splitSuggestions.stream()
                .limit(suggestionLimit)
                .collect(Collectors.toList());
    }

    public boolean isValid(String word) {
        return dictionary.contains(word.toLowerCase());
    }

    //TODO: when similar words are misspelled differently there will be two occurrences in map
    public String checkText(String text) {

        long startProccessingTime = System.nanoTime();

        if (text.length() == 0) {
            return text;
        }

        boolean firstLetterUpper = Character.isUpperCase(text.charAt(0));

        Map<String, String> suggestedReplacements = new HashMap<>();

        String preproccessedText = textPreproccessor.preproccess(text);

        String[] words = TextUtils.splitCleanText(preproccessedText, CleanTextType.SPLIT_WITHOUT_CLEANING);

        for (int i = 0; i < words.length; i++) {
            String word = words[i];

            String cleanWord = TextUtils.cleanText(word, CleanTextType.CLEAR_PUNCTUATION);

            if (TextUtils.isWordDigit(cleanWord)) {
                continue;
            }

            String fixedWord;

            if (TextUtils.hasSpecialChar(word)) {
                word = TextUtils.cleanText(word, CleanTextType.CLEAR_IRRELEVANT_SYMBOLS);
                // word = cleanWord;
            }

            if (!suggestedReplacements.containsKey(word) && !dictionary.contains(word.toLowerCase())) {

                List<Suggestion> wordSuggestions;

                if(checkCompounds){
                    wordSuggestions = getCompoundSuggestions(word);
                }
                else {
                    wordSuggestions = getWordSuggestions(word);
                }

                if (wordSuggestions == null || wordSuggestions.isEmpty()) {
                    fixedWord = "";
                } else {
                    Suggestion suggestion = wordSuggestions.get(0);

                    if (suggestion.getSoundexCodeDistance() >= MAX_SOUNDEX_DISTANCE) {
                        fixedWord = word;
                    } else {
                        fixedWord = suggestion.getWord();
                    }
                }

                suggestedReplacements.put(word, fixedWord);

                preproccessedText = TextUtils.replaceWord(preproccessedText, word, fixedWord);

            }
        }

        if (preproccessedText.length() > 0) {
            preproccessedText = firstLetterUpper ?
                    Character.toUpperCase(preproccessedText.charAt(0)) + preproccessedText.substring(1) :
                    preproccessedText;
        }

        long endProccessingTime = System.nanoTime();

        LOGGER.debug("Checking took time: " + (endProccessingTime - startProccessingTime) / (double) 1000000 + " ms");

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
        Spellchecker spellchecker = new Spellchecker(Language.ENGLISH);

       //System.out.println(spellchecker.checkText("sign in sign up ranker home people entertainment sports culture channels videos create a list about us advertise press ranker insights actors celebrity facts historical figures musicians politicians anime gaming movies music tv athletes baseball basketball football soccer food politics & history relationships travel thought provoking weird history graveyard shift total nerd anime underground weird nature weirdly interesting 40 LISTS Jokes, Jokes, JokesGet your laughing lips ready. Chuck Norris Your Mom Yo Mama Knock Knock! Dad Jokes PG Jokes Ancient Comedy Mitch Hedberg Photo: Meetup.com jokes Dirty Adult Jokes That Will Get You a Laugh on Demand Evan Lambert 37.9k votes 7.7k voters 465.6k views 38 items Follow Embed List Rules Vote up the funniest joke! Looking for a quick and dirty joke to get you an easy laugh? Then these funny adult jokes are for you. We've compiled the funniest jokes about sex that you'll ever come across, so that you can go and tell your friends - hopefully without offending them. Vote on your favorite funny adult joke! 1 3,449 VOTES What do a penis and a Rubik's Cube have in common? The more you play with it, the harder it gets. 2,682 767 Agree or disagree? 14 Famous People Who Have PhDs 2 2,233 VOTES How is a push-up bra like a bag of chips? Once you open it, you realize it's half-empty. 1,692 541 Agree or disagree? 3 2,446 VOTES What do boobs and toys have in common? They were both originally made for kids, but daddies end up playing with them. 1,846 600 Agree or disagree? Adult Jokes Hidden In Dr. Seuss Movies That Went Right Over Your Head 4 1,814 VOTES What do the Mafia and pussies have in common? One slip of the tongue, and you're in deep sh*t. 1,384 430 Agree or disagree? LOAD MORE Filed Under: Polls funnyjokesadult jokesHumor love this list? Dirty Adult Jokes That Will Get You a Laugh on Demand share tweet pin email embed rank your version prev list more popular lists next list 47 Adult Jokes in Cartoons You Didn't Get As A Child Female Sports with the Hottest Athletes This Makeup Artist Creates Mesmerizing Lip Art Inspired By Nature The 20 Most Epic Wedding FAILs of All Time Stereotypes That Republicans Are Tired Of Hearing Anime Characters Ranked By How Tragically Their Parents Died The Best Starter Pokemon The Smartest Anime Characters of All Time The 25+ Greatest Anime Characters With Fire Powers Where Were You on September 11th? The Best Generation 1 Pokemon 25 Signs You Just Don't Care About Star Wars Anymore Funny Names to Give a Sugar Glider The Most Overrated Wrestlers of All Time The Best Tasting Whiskey The Most Powerful Anime Characters of All Time The 25+ Best Anime Water Users of All Time . Top 10 Current Queries: biathlon medals song laura bands from colorado versace celebrities chronicles of riddick cast tiger character michael alig sandlot characters famous teenagers teller young mobile site contact us we're hiring embed a list data blog listopedia like us on facebook follow us on pinterest subscribe to our top lists Information and media on this page and throughout Ranker is supplied by Wikipedia, Ranker users, and other sources. Freebase content is freely licensed under the CC-BY license and Wikipedia content is licensed under the GNU Free Documentation license. © Ranker 2018 terms privacy sitemap sign in sign up ranker home people entertainment sports culture channels videos create a list about us advertise press ranker insights actors celebrity facts historical figures musicians politicians anime gaming movies music tv athletes baseball basketball football soccer food politics & history relationships travel thought provoking weird history graveyard shift total nerd anime underground weird nature weirdly interesting 40 LISTS Jokes, Jokes, JokesGet your laughing lips ready. Chuck Norris Your Mom Yo Mama Knock Knock! Dad Jokes PG Jokes Ancient Comedy Mitch Hedberg Photo: Meetup.com jokes Dirty Adult Jokes That Will Get You a Laugh on Demand Evan Lambert 37.9k votes 7.7k voters 465.6k views 38 items Follow Embed List Rules Vote up the funniest joke! Looking for a quick and dirty joke to get you an easy laugh? Then these funny adult jokes are for you. We've compiled the funniest jokes about sex that you'll ever come across, so that you can go and tell your friends - hopefully without offending them. Vote on your favorite funny adult joke! 1 3,449 VOTES What do a penis and a Rubik's Cube have in common? The more you play with it, the harder it gets. 2,682 767 Agree or disagree? 14 Famous People Who Have PhDs 2 2,233 VOTES How is a push-up bra like a bag of chips? Once you open it, you realize it's half-empty. 1,692 541 Agree or disagree? 3 2,446 VOTES What do boobs and toys have in common? They were both originally made for kids, but daddies end up playing with them. 1,846 600 Agree or disagree? Adult Jokes Hidden In Dr. Seuss Movies That Went Right Over Your Head 4 1,814 VOTES What do the Mafia and pussies have in common? One slip of the tongue, and you're in deep sh*t. 1,384 430 Agree or disagree? LOAD MORE Filed Under: Polls funnyjokesadult jokesHumor love this list? Dirty Adult Jokes That Will Get You a Laugh on Demand share tweet pin email embed rank your version prev list more popular lists next list 47 Adult Jokes in Cartoons You Didn't Get As A Child Female Sports with the Hottest Athletes This Makeup Artist Creates Mesmerizing Lip Art Inspired By Nature The 20 Most Epic Wedding FAILs of All Time Stereotypes That Republicans Are Tired Of Hearing Anime Characters Ranked By How Tragically Their Parents Died The Best Starter Pokemon The Smartest Anime Characters of All Time The 25+ Greatest Anime Characters With Fire Powers Where Were You on September 11th? The Best Generation 1 Pokemon 25 Signs You Just Don't Care About Star Wars Anymore Funny Names to Give a Sugar Glider The Most Overrated Wrestlers of All Time The Best Tasting Whiskey The Most Powerful Anime Characters of All Time The 25+ Best Anime Water Users of All Time . Top 10 Current Queries: biathlon medals song laura bands from colorado versace celebrities chronicles of riddick cast tiger character michael alig sandlot characters famous teenagers teller young mobile site contact us we're hiring embed a list data blog listopedia like us on facebook follow us on pinterest subscribe to our top lists Information and media on this page and throughout Ranker is supplied by Wikipedia, Ranker users, and other sources. Freebase content is freely licensed under the CC-BY license and Wikipedia content is licensed under the GNU Free Documentation license. © Ranker 2018 terms privacy sitemap sign in sign up ranker home people entertainment sports culture channels videos create a list about us advertise press ranker insights actors celebrity facts historical figures musicians politicians anime gaming movies music tv athletes baseball basketball football soccer food politics & history relationships travel thought provoking weird history graveyard shift total nerd anime underground weird nature weirdly interesting 40 LISTS Jokes, Jokes, JokesGet your laughing lips ready. Chuck Norris Your Mom Yo Mama Knock Knock! Dad Jokes PG Jokes Ancient Comedy Mitch Hedberg Photo: Meetup.com jokes Dirty Adult Jokes That Will Get You a Laugh on Demand Evan Lambert 37.9k votes 7.7k voters 465.6k views 38 items Follow Embed List Rules Vote up the funniest joke! Looking for a quick and dirty joke to get you an easy laugh? Then these funny adult jokes are for you. We've compiled the funniest jokes about sex that you'll ever come across, so that you can go and tell your friends - hopefully without offending them. Vote on your favorite funny adult joke! 1 3,449 VOTES What do a penis and a Rubik's Cube have in common? The more you play with it, the harder it gets. 2,682 767 Agree or disagree? 14 Famous People Who Have PhDs 2 2,233 VOTES How is a push-up bra like a bag of chips? Once you open it, you realize it's half-empty. 1,692 541 Agree or disagree? 3 2,446 VOTES What do boobs and toys have in common? They were both originally made for kids, but daddies end up playing with them. 1,846 600 Agree or disagree? Adult Jokes Hidden In Dr. Seuss Movies That Went Right Over Your Head 4 1,814 VOTES What do the Mafia and pussies have in common? One slip of the tongue, and you're in deep sh*t. 1,384 430 Agree or disagree? LOAD MORE Filed Under: Polls funnyjokesadult jokesHumor love this list? Dirty Adult Jokes That Will Get You a Laugh on Demand share tweet pin email embed rank your version prev list more popular lists next list 47 Adult Jokes in Cartoons You Didn't Get As A Child Female Sports with the Hottest Athletes This Makeup Artist Creates Mesmerizing Lip Art Inspired By Nature The 20 Most Epic Wedding FAILs of All Time Stereotypes That Republicans Are Tired Of Hearing Anime Characters Ranked By How Tragically Their Parents Died The Best Starter Pokemon The Smartest Anime Characters of All Time The 25+ Greatest Anime Characters With Fire Powers Where Were You on September 11th? The Best Generation 1 Pokemon 25 Signs You Just Don't Care About Star Wars Anymore Funny Names to Give a Sugar Glider The Most Overrated Wrestlers of All Time The Best Tasting Whiskey The Most Powerful Anime Characters of All Time The 25+ Best Anime Water Users of All Time . Top 10 Current Queries: biathlon medals song laura bands from colorado versace celebrities chronicles of riddick cast tiger character michael alig sandlot characters famous teenagers teller young mobile site contact us we're hiring embed a list data blog listopedia like us on facebook follow us on pinterest subscribe to our top lists Information and media on this page and throughout Ranker is supplied by Wikipedia, Ranker users, and other sources. Freebase content is freely licensed under the CC-BY license and Wikipedia content is licensed under the GNU Free Documentation license. © Ranker 2018 terms privacy sitemap"));
        //System.out.println(spellchecker.checkCompound("hellowordl"));
        System.out.println(spellchecker.checkWord("cutn"));
        //System.out.println(spellchecker.checkCompound("hellolwordl"));

        long startProccessingTime = System.nanoTime();
        System.out.println(spellchecker.isValid("cunt"));
        long endProccessingTime = System.nanoTime();

        LOGGER.debug("isValid() took time: " + (endProccessingTime - startProccessingTime) / (double) 1000000 + " ms");
    }
}
