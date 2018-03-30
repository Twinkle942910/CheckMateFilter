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
        Dictionary  englishDictionary = new EnglishDictionary();
        Dictionary  russianDictionary = new RussianDictionary();

        map.put(Language.ENGLISH, () -> englishDictionary);
        map.put(Language.RUSSIAN, () -> russianDictionary);

      /*  map.put(Language.ENGLISH, EnglishDictionary::new);
        map.put(Language.ENGLISH, RussianDictionary::new);*/

        //TODO: load all dictionaries.
 /*       map.put(Language.SPANISH, SpanishDictionary::new);
        map.put(Language.UKRAINIAN, UkrainianDictionary::new);
        map.put(Language.RUSSIAN, RussianDictionary::new);*/
    }

    public static Dictionary create(final Language language) {
        Supplier<? extends Dictionary> supplier = map.get(language);

        if (supplier != null) {
            return supplier.get();
        }

        throw new IllegalArgumentException("No such dictionary " + language.toString());
    }
}
