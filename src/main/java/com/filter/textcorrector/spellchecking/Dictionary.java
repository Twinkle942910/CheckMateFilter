package com.filter.textcorrector.spellchecking;

import com.filter.textcorrector.spellchecking.model.Suggestion;

import java.util.List;

public interface Dictionary {
    boolean contains(String word);
    List<Suggestion> search(String word, float editDistancePercent);
    int getSize();
}
