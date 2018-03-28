package com.filter.textcorrector;

import com.filter.textcorrector.profanity_filtering.model.Censored;

import java.util.List;

public interface Filter {
    List<String> checkWord(String word);
    List<String> checkCompound(String compound);
    String checkText(String text);
    String censor(String text);
    Censored searchForProfanity(String text);
    String preproccess(String text, boolean removeRepeatedLetters);
}
