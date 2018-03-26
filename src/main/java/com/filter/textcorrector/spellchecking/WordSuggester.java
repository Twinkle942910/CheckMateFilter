package com.filter.textcorrector.spellchecking;

import com.filter.textcorrector.spellchecking.model.Suggestion;
import com.filter.textcorrector.spellchecking.util.DamerauLevenshteinDistance;
import com.filter.textcorrector.spellchecking.util.Soundex;
import it.unimi.dsi.fastutil.chars.Char2ObjectAVLTreeMap;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 * is an efficient information retrieval data structure that we can use to search a word in O(M) time, where
 * M is maximum string length. However the penalty is on trie storage requirements.
 */
public class WordSuggester {

    // Dummy node
    private Node root;
    // Current number of unique words in trie
    private int numOfwords;
    // If this is a case sensitive trie
    private boolean caseSensitive;
    private Charset charset;

    /**
     * Constructor.
     *
     * @param caseSensitive set if this is a case sensitive trie
     */
    public WordSuggester(boolean caseSensitive, Charset charset) {
        root = new Node();
        root.setRoot(true);
        setNumberOfWords(0);
        setCaseSensitive(caseSensitive);
        setCharset(charset);
    }

    /**
     * Inserts a word into the trie.
     *
     * @param word
     */
    public void add(String word) {

        word = preprocessWord(word);

        Char2ObjectAVLTreeMap<Node> children = root.children;

        // To avoid duplicates
        if (!search(word, false)) {

            Node currentParent;
            currentParent = root;

            for (int i = 0; i < word.length(); i++) {
                char c = word.charAt(i);
                Node node;
                if (children.containsKey(c)) {
                    node = children.get(c);
                } else {
                    node = new Node(c);
                    node.setRoot(false);
                    node.setParent(currentParent);
                    children.put(c, node);
                }

                children = node.children;
                currentParent = node;

                // set leaf node
                if (i == word.length() - 1) {
                    node.setLeaf(true);
                    this.numOfwords++;
                }
                // how many words starting with prefix
                node.setCount(node.getCount() + 1);
            }
        }
    }

    /**
     * Search a word in the trie.
     *
     * @param word
     * @param doPreprocess
     * @return the last word's node
     */
    private Node searchNode(String word, boolean doPreprocess) {
        if (doPreprocess) {
            word = preprocessWord(word);
        }
        Char2ObjectAVLTreeMap<Node> children = root.children;
        Node node = null;
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            if (children.containsKey(c)) {
                node = children.get(c);
                children = node.children;
            } else {
                return null;
            }
        }
        return node;
    }

    /**
     * Returns if the word is in the trie.
     *
     * @param word
     * @return true|false
     */
    public boolean search(String word) {
        return search(word, true);
    }

    /**
     * Returns if the word is in the trie.
     *
     * @param word
     * @param doPreprocess
     * @return true|false
     */
    public boolean search(String word, boolean doPreprocess) {
        if (doPreprocess) {
            word = preprocessWord(word);
        }
        Node t = searchNode(word, false);
        if (t != null && t.isLeaf())
            return true;
        else
            return false;
    }

    /**
     * The search function returns a list of all words that are less than the
     * given maximum distance from the target word, using Levenshtein distance
     * References: http://stevehanov.ca/blog/index.php?id=114
     * https://en.wikipedia.org/wiki/Levenshtein_distance
     *
     * @param word
     * @param maxDistance
     */
    public List<Suggestion> getSuggestions(String word, int maxDistance) {

        Set<Suggestion> results = new HashSet<>();
        word = preprocessWord(word);

        int size = word.length();

        // build first row
        Vector<Integer> previousRow = new Vector<>(size + 1);
        Vector<Integer> currentRow = new Vector<Integer>(size + 1);

        for (int i = 0; i <= size; ++i) {
            previousRow.insertElementAt(0, i);
            currentRow.insertElementAt(i, i);
        }

        // recursively search each branch of the trie
        for (Map.Entry<Character, Node> entry : root.children.entrySet()) {
            results.addAll(RecursiveDamerauLevenshteinDistance(entry.getValue(), '\0', entry.getValue().getC(), word, previousRow, currentRow,
                    results, maxDistance));
        }

        return new ArrayList<>(results);
    }

    public Set<Suggestion> RecursiveDamerauLevenshteinDistance(Node node, char prevLetter, char letter, String word,
                                                               Vector<Integer> previousRow2, Vector<Integer> previousRow,
                                                               Set<Suggestion> results, int maxDistance) {

        int columns = previousRow.size();
        Vector<Integer> currentRow = new Vector<>(previousRow.size());
        currentRow.add(0, previousRow.get(0) + 1);

        int insertCost, deleteCost, replaceCost;
        for (int i = 1; i < columns; i++) {
            insertCost = currentRow.get(i - 1) + 1;
            deleteCost = previousRow.get(i) + 1;

            int cost;

            if (word.charAt(i - 1) != letter) {
                replaceCost = previousRow.get(i - 1) + 1;
                cost = 1;
            } else {
                replaceCost = previousRow.get(i - 1);
                cost = 0;
            }

            currentRow.add(i, Math.min(insertCost, Math.min(deleteCost, replaceCost)));

            if ((i > 1) && (prevLetter != '\0') && (word.charAt(i - 1) == prevLetter) && (word.charAt(i - 2) == letter)) {
                currentRow.set(i, Math.min(currentRow.get(i), previousRow2.get(i - 2) + cost));
            }
        }

        // If the last entry in the row indicates the optimal cost is less than
        // the maximum distance, and there is a word in this trie node, then add
        // it.
        if (currentRow.lastElement() <= maxDistance && node.isLeaf()) {
            Node currentParent = node.getParent();
            StringBuilder wordBuilder = new StringBuilder();
            while (currentParent != null) {
                if (currentParent.getParent() != null) {
                    wordBuilder.append(currentParent.getC());
                }
                currentParent = currentParent.getParent();
            }

            String suggestedWord = wordBuilder.reverse().append(node.getC()).toString();
            int suggestedDistance = currentRow.lastElement();

            float soundexDistance = Soundex.difference(Soundex.translate(suggestedWord), Soundex.translate(word));
            float percentageDifference = DamerauLevenshteinDistance.getPercentageDifference(suggestedWord, word, suggestedDistance);

            results.add(new Suggestion(suggestedWord, soundexDistance, suggestedDistance, percentageDifference));
        }

        // If any entries in the row are less than the maximum distance, then
        // recursively search each branch of the trie.
        Object obj = Collections.min(currentRow);
        Integer i = new Integer((int) obj);
        if (i.intValue() <= maxDistance) {
            for (Map.Entry<Character, Node> entry : node.children.entrySet()) {
                results.addAll(RecursiveDamerauLevenshteinDistance(entry.getValue(), letter, entry.getValue().getC(), word, previousRow, currentRow,
                        results, maxDistance));
            }
        }
        return results;
    }

    /**
     * Encode the word and lowerCase if the word is case-sensitive
     *
     * @param word
     * @return
     */
    private String preprocessWord(String word) {
        // Encode String
        String w = encodeWord(word);
        // Case sensitive
        return caseSensitive(w);
    }

    /**
     * Encode String
     *
     * @param word
     * @return word encoded
     */
    private String encodeWord(String word) {
        byte wordBytes[] = word.getBytes(this.getCharset());
        return new String(wordBytes, this.getCharset());
    }

    /**
     * @param word
     * @return
     */
    private String caseSensitive(String word) {
        return this.caseSensitive ? word : word.toLowerCase();
    }

    /**
     * Set to unvisited all the Tries's node.
     *
     * @param node
     */
    public void initFalse(Node node) {
        node.setVisited(false);
        if (node.children != null) {
            for (Map.Entry<Character, Node> entry : node.children.entrySet()) {
                initFalse(entry.getValue());
            }
        }
    }

    /**
     * Show The WordSuggester.
     */
    public void show() {
        System.out.println("");
        if (this.root != null) {
            this.initFalse(this.root);
            this.dfs(this.root);
        }
    }

    /**
     * Recursive Depth-first search (DFS).
     *
     * @param node
     */
    private void dfs(Node node) {
        node.setVisited(true);
        for (Map.Entry<Character, Node> entry : node.children.entrySet()) {
            if (entry.getValue().isVisited() == false) {
                System.out.print("(" + entry.getValue().isRoot() + ":" + entry.getValue().getC() + ":"
                        + entry.getValue().getCount() + ":" + entry.getValue().getParent().getC() + ")->");
                if (entry.getValue().isLeaf()) {
                    System.out.println("*");
                }
                dfs(entry.getValue());
            }
        }
    }

    public int getNumberOfWords() {
        return numOfwords;
    }

    private void setNumberOfWords(int words) {
        this.numOfwords = words;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    private void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    private static class Node {

        private char c;
        // to count how many words starting with prefix
        private int count;
        private boolean isVisited;
        private boolean isLeaf;
        private boolean isRoot;
        private Node parent;
        //Map<Character, Node> children = new HashMap<Character, Node>();
        Char2ObjectAVLTreeMap<Node> children = new Char2ObjectAVLTreeMap<>();

        public Node() {
            setCount(0);
            setVisited(false);
        }

        public Node(char c) {
            this();
            setC(c);
        }

        public char getC() {
            return c;
        }

        public void setC(char c) {
            this.c = c;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public boolean isVisited() {
            return isVisited;
        }

        public void setVisited(boolean isVisited) {
            this.isVisited = isVisited;
        }

        public boolean isLeaf() {
            return isLeaf;
        }

        public void setLeaf(boolean isLeaf) {
            this.isLeaf = isLeaf;
        }

        public Node getParent() {
            return parent;
        }

        public void setParent(Node parent) {
            this.parent = parent;
        }

        public boolean isRoot() {
            return isRoot;
        }

        public void setRoot(boolean isRoot) {
            this.isRoot = isRoot;
        }

    }

}
