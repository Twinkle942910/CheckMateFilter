package com.filter.textcorrector.spellchecking;


import com.filter.textcorrector.spellchecking.model.Suggestion;
import com.filter.textcorrector.spellchecking.util.DamerauLevenshteinDistance;
import com.filter.textcorrector.spellchecking.util.Soundex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SuggestionSearcher {
    private Node root;
    private int count;

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
            int editDistance = DamerauLevenshteinDistance.distance(current.word, word);

            while (current.containsChildWithDistance(editDistance)) {
                current = current.getChild(editDistance);
                editDistance = DamerauLevenshteinDistance.distance(current.word, word);

                if (editDistance == 0) {
                    return;    // Duplicate (string already exists in tree)
                }
            }
            current.addChild(editDistance, word);
        }
        count++;
    }

    public List<Suggestion> search(String word, float matchPercentage) {
        int distanceThreshold = DamerauLevenshteinDistance.convertPercentageToEditDistance(word, matchPercentage);
        return searchTree(word, distanceThreshold);
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
        int currentDistance = DamerauLevenshteinDistance.distance(node.word.toLowerCase(), word.toLowerCase());
        float soundexDistance = Soundex.difference(Soundex.translate(node.word), Soundex.translate(word));

        if (currentDistance <= distanceThreshold) {
            // Match found
            float percentageDifference = DamerauLevenshteinDistance.getPercentageDifference(node.word, word, currentDistance);
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

    public int getSize() {
        return count;
    }

    private static class Node {
        private String word;
        private Map<Integer, Node> children;

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

        List<Integer> getChildKeysWithinDistance(int minDistance, int maxDistance) {
            if (children == null) {
                return new ArrayList<>(0);
            } else {
                return children.keySet().stream().filter(n -> n >= minDistance && n <= maxDistance)
                        .collect(Collectors.toList());
            }
        }

        boolean containsChildWithDistance(int key) {
            return children != null && children.containsKey(key);
        }

        void addChild(int key, String word) {
            if (children == null) {
                children = new HashMap<>();
            }

            Node child = new Node(word);
            children.put(key, child);
        }
    }
}