package com.filter.textcorrector.profanity_filtering.dictionary;

public class EnglishDictionary extends PlainDictionary {
    private static final String DICTIONARY_PATH = "dictionaries/en_profanity_list.txt";

    public EnglishDictionary() {
        super(DICTIONARY_PATH);
    }
}
