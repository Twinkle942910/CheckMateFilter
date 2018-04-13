package com.filter.textcorrector.spellchecking;

import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

public enum Language {
    ENGLISH("en"),
    SPANISH("es"),
    GERMAN("ge"),
    FRENCH("fr"),
    RUSSIAN("ru"),
    UKRAINIAN("ua"),
    POLISH("pl");

    private String name;

    Language(String name) {
        this.name = name;
    }

    public boolean contains(String name) {
        for (Language language : values()) {
            if (language.name.equals(name)) {
                return true;
            }
        }
        return false;
    }

    private static final Map<String, Language> stringToEnum =
            Stream.of(values()).collect(toMap(e -> e.name, e -> e));

    public static Language fromString(String symbol) {
        return stringToEnum.get(symbol);
    }

    public static void main(String[] args) {
        Language language = Language.fromString("en");
        System.out.println(language);
    }
}
