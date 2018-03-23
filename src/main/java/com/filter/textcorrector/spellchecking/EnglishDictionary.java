package com.filter.textcorrector.spellchecking;


import com.filter.textcorrector.spellchecking.model.Suggestion;
import com.filter.textcorrector.spellchecking.util.DamerauLevenshteinDistance;
import com.filter.textcorrector.spellchecking.util.Soundex;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.max;

public class EnglishDictionary {
    private static Logger LOGGER = LoggerFactory.getLogger(EnglishDictionary.class);
    private static final String DICTIONARY_PATH = "/dictionaries/en_common_dictionary_80k.txt";

    private static DamerauLevenshteinDistance damerauLevenshteinDistance = new DamerauLevenshteinDistance();
    private Node root;
    private BloomFilter<String> filter;

    public EnglishDictionary() {
        long startTime = System.currentTimeMillis();
        loadDictionary();
        long endTime = System.currentTimeMillis();
        LOGGER.debug("Dictionary loading took time: " + (endTime - startTime) / (double) 1000000 + " ms");
    }

    private static class Node {
        private String word;
        private Map<Integer, Node> children;    // Children are keyed on edit distance

        Node(String word) {
            this.word = word;
        }

        Node getChild(int key) {
            if (containsChildWithDistance(key)) {
                return children.get(key);
            } else {
                return null;
            }
        }

        public List<Integer> getChildKeysWithinDistance(int minDistance, int maxDistance) {
            if (children == null) {
                return new ArrayList<>(0);
            } else {
                return children.keySet().stream().filter(n -> n >= minDistance && n <= maxDistance)
                        .collect(Collectors.toList());
            }
        }

        public boolean containsChildWithDistance(int key) {
            return children != null && children.containsKey(key);
        }

        public void addChild(int key, String word) {
            if (children == null) {
                children = new HashMap<>();
            }

            Node child = new Node(word);
            children.put(key, child);
        }
    }

    public void add(String word) {
        if (word == null) {
            throw new IllegalArgumentException("Word must not be null");
        }

        if (word.length() == 0) {
            throw new IllegalArgumentException("Word must not be empty");
        }

        if (root == null) {
            root = new Node(word);
        } else {
            // Traverse through the tree, adding the string as a leaf related by edit distance
            Node current = root;
            int editDistance = damerauLevenshteinDistance.distance(current.word, word);

            while (current.containsChildWithDistance(editDistance)) {
                current = current.getChild(editDistance);
                editDistance = damerauLevenshteinDistance.distance(current.word, word);

                if (editDistance == 0) {
                    return;    // Duplicate (string already exists in tree)
                }
            }
            current.addChild(editDistance, word);
        }
    }

    public List<Suggestion> search(String word, float matchPercentage) {
        int distanceThreshold = convertPercentageToEditDistance(word, matchPercentage);
        return searchTree(word, distanceThreshold);
    }

    private int convertPercentageToEditDistance(String keyword, float matchPercentage) {
        return keyword.length() - (Math.round((keyword.length() * matchPercentage) / 100.0f));
    }

    public List<Suggestion> search(String word, int distanceThreshold) {
        return searchTree(word, distanceThreshold);
    }

    private List<Suggestion> searchTree(String word, int distanceThreshold) {
        List<Suggestion> matches = new ArrayList<>();
        searchTree(root, word, distanceThreshold, matches);
        return matches;
    }

    private void searchTree(Node node, String word, int distanceThreshold, List<Suggestion> matches) {
        int currentDistance = damerauLevenshteinDistance.distance(node.word.toLowerCase(), word.toLowerCase());
        float soundexDistance = Soundex.difference(Soundex.translate(node.word), Soundex.translate(word));

        if (currentDistance <= distanceThreshold) {
            // Match found
            float percentageDifference = getPercentageDifference(node.word, word, currentDistance);
            Suggestion suggestion = new Suggestion(node.word, soundexDistance, currentDistance, percentageDifference);
            matches.add(suggestion);
        }

        // Get the children to search next
        int minDistance = currentDistance - distanceThreshold;
        int maxDistance = currentDistance + distanceThreshold;

        List<Integer> childKeysWithinDistanceThreshold = node.getChildKeysWithinDistance(minDistance, maxDistance);

        for (Integer childKey : childKeysWithinDistanceThreshold) {
            Node child = node.getChild(childKey);
            searchTree(child, word, distanceThreshold, matches);
        }
    }

    private float getPercentageDifference(String word, String wordToMatch, int editDistance) {
        int longestWordLength = Math.max(word.length(), wordToMatch.length());
        return 100.0f - (((float) editDistance / longestWordLength) * 100.0f);
    }

    public boolean contains(String word) {
        return filter.mightContain(word);
    }

    private void loadDictionary() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(Spellchecker.class.getResourceAsStream(DICTIONARY_PATH)));

            filter = BloomFilter.create(
                    Funnels.stringFunnel(Charset.defaultCharset()),
                    84110,
                    0.01);

            String line;

            while ((line = reader.readLine()) != null) {

                try {

                    this.add(line);
                    filter.put(line.toLowerCase());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        EnglishDictionary dictionary = new EnglishDictionary();

        long startTime1 = System.currentTimeMillis();
        List<Suggestion> suggestions = dictionary.search("fabi", 60f);
       // Collections.sort(suggestions, Spellchecker.suggestionDistanceComparator);
        long endTime1 = System.currentTimeMillis();
        System.out.println("took time: " + (endTime1 - startTime1) / (double) 1000000 + " ms");

        System.out.println(suggestions);
    }
}