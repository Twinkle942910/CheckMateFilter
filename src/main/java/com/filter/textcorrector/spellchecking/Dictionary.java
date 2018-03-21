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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.lang.Math.max;

public class Dictionary {
    private static Logger LOGGER = LoggerFactory.getLogger(Dictionary.class);
    private static final String DICTIONARY_PATH = "/dictionaries/en_most_common_dictionary.txt";
    private BloomFilter<String> filter;
    private int count;

    public Dictionary() {
        long startTime = System.currentTimeMillis();

        loadDictionary();

        long endTime = System.currentTimeMillis();
        LOGGER.debug("Dictionary loading took time: " + (endTime - startTime) / (double) 1000000 + " ms");
    }

    public static class Node {
        final String name;
        final Map<Integer, Node> children = new HashMap<>();
        public Node(String name) {
            this.name = name;
        }

        protected Node childAtDistance(int pos) {
            return children.get(pos);
        }

        private void addChild(int pos, Node child) {
            children.put(pos, child);
        }

        public List<Suggestion> search(String node, int maxDistance) {
            int distance = (int) DamerauLevenshteinDistance.distance(this.name.toLowerCase(), node.toLowerCase());
            List<Suggestion> matches = new LinkedList<>();
            if (distance <= maxDistance)
                matches.add(new Suggestion(this.name, Soundex.difference(Soundex.translate(this.name), Soundex.translate(node)), distance));
            if (children.size() == 0)
                return matches;
            int i = max(1, distance - maxDistance);
            for (; i <= distance + maxDistance; i++) {
                Node child = children.get(i);
                if (child == null)
                    continue;
                matches.addAll(child.search(node, maxDistance));
            }
            return matches;
        }
    }

    private Node root;

    public List<Suggestion> search(String q, int maxDist) {
        return root.search(q, maxDist);
    }

    public Suggestion search(String q) {
        List<Suggestion> list = root.search(q, 1);
        return list.isEmpty() ? null : list.iterator().next();
    }


    public void add(String node) {
        if(node == null || node.isEmpty()) throw new IllegalArgumentException("word can't be null or empty.");
        Node newNode = new Node(node);
        if (root == null) {
            root = newNode;
        }
        addInternal(root, newNode);
    }

    public boolean contains(String word){
        return filter.mightContain(word);
    }

    public int size() {
        return count;
    }

    private void addInternal(Node src, Node newNode) {
        if (src.equals(newNode))
            return;
        int distance = (int) DamerauLevenshteinDistance.distanceCaseIgnore(src.name, newNode.name);
        Node node = src.childAtDistance(distance);
        if (node == null) {
            src.addChild(distance, newNode);
        } else
            addInternal(node, newNode);

        count++;
    }

    private void loadDictionary() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(Spellchecker.class.getResourceAsStream(DICTIONARY_PATH)));

            filter = BloomFilter.create(
                    Funnels.stringFunnel(Charset.defaultCharset()),
                    10500,
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
}