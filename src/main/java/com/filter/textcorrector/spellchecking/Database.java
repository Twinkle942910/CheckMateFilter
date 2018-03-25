package com.filter.textcorrector.spellchecking;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Holds dictionary from a file.
 */
public final class Database {
    private Map<String, Set<String>> dictionary;

    private Database(){
        dictionary = new HashMap<>();
    }

    /**
     * Get a database.
     * @return
     */
    public static Database getInstance(){
        return new Database();
    }

    /**
     * get set of matches for the word.
     * @param key Soundex code of the word.
     * @return Set of suggested words.
     */
    public Set<String> getSetOfMatches(String key){
        return dictionary.get(key);
    }

    public Map<String, Set<String>> getDictionary() {
        return dictionary;
    }

    /**
     * Add code-word pair from file to the database.
     * @param key Soundex code for database.
     * @param setOfMatches list of the matches.
     */
    public void addMatchingPair(String key, Set<String> setOfMatches){
        dictionary.put(key, setOfMatches);
    }
}
