package com.filter.textcorrector.spellchecking.dictionary;

import com.filter.textcorrector.spellchecking.Language;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class DictionaryFactory {
    private static Map<Language, Supplier<? extends Dictionary>> map = new HashMap<>();

    private DictionaryFactory() {
        throw new AssertionError("This class is not meant to be instantiated.");
    }

    static {
        map.put(Language.ENGLISH, EnglishDictionary::new);
    }

    public static Dictionary create(final Language language) {
        Supplier<? extends Dictionary> supplier = map.get(language);

        if (supplier != null) {
            return supplier.get();
        }

        throw new IllegalArgumentException("No such dictionary " + language.toString());
    }
}
