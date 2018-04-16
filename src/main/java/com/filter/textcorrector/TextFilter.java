package com.filter.textcorrector;

import com.filter.textcorrector.profanity_filtering.ProfanityFilter;
import com.filter.textcorrector.profanity_filtering.model.Censored;
import com.filter.textcorrector.spellchecking.Language;
import com.filter.textcorrector.spellchecking.Spellchecker;
import com.filter.textcorrector.text_preproccessing.TextPreproccessor;

import java.util.List;

public class TextFilter implements Filter {
    private Spellchecker spellchecker;
    private ProfanityFilter profanityFilter;
    private TextPreproccessor textPreproccessor;

    private Language language;

    public TextFilter(Language language, String dictionaryPath) {
        this.language = language;

        spellchecker = new Spellchecker.Builder(language)
                .withSuggestionLimit(3)
                .build();

        profanityFilter = new ProfanityFilter.Builder()
                .withWordReplacement("[profanity]")
                .withDictionary(dictionaryPath)
                .build();

        textPreproccessor = TextPreproccessor.getInstance();
    }

    public TextFilter(Language language) {
        this(language, "");
    }

    @Override
    public List<String> checkWord(String word) {
        return spellchecker.checkWord(word);
    }

    @Override
    public List<String> checkCompound(String compound) {
        return spellchecker.checkCompound(compound);
    }

    @Override
    public String checkText(String text) {
        return spellchecker.checkText(text);
    }

    //TODO: makes all words lower case. Fix.
    @Override
    public String censor(String text) {
        return profanityFilter.censor(text);
    }

    @Override
    public Censored searchForProfanity(String text) {
        return profanityFilter.searchForProfanity(text);
    }

    @Override
    public String preproccess(String text, boolean removeRepeatedLetters) {
        return textPreproccessor.preproccess(text, removeRepeatedLetters);
    }

    @Override
    public boolean isValid(String word) {
        return spellchecker.isValid(word);
    }

    @Override
    public boolean isProfane(String word) {
        return profanityFilter.isProfane(word);
    }

    @Override
    public Language checkLanguage() {
        return language;
    }

    @Override
    public void doPreproccessing(boolean doPreproccessing) {
        spellchecker.doPreproccessing(doPreproccessing);
    }

    @Override
    public void doRemoveRepeatedLetters(boolean doRemoveRepeatedLetters) {
        spellchecker.doRemoveRepeatedLetters(doRemoveRepeatedLetters);
    }

    @Override
    public void doCheckCompounds(boolean doCheckCompounds) {
        spellchecker.doCheckCompounds(doCheckCompounds);
    }

    @Override
    public void setSuggestionLimit(int suggestionLimit) {
        spellchecker.setSuggestionLimit(suggestionLimit);
    }

    @Override
    public void keepUnrecognized(boolean keepUnrecognized) {
        spellchecker.keepUnrecognized(keepUnrecognized);
    }

    @Override
    public void changeLanguage(Language language) {
        this.language = language;
        profanityFilter.changeLanguage(language);
        spellchecker.changeLanguage(language);
    }

    @Override
    public void setProfanityReplacement(String replacement) {
        profanityFilter.setProfanityReplacement(replacement);
    }

    @Override
    public void doRemoveProfaneWord(boolean removeProfaneWord) {
        profanityFilter.removeProfaneWord(removeProfaneWord);
    }

    @Override
    public void setMaxMatchPercentage(float maxMatchPercentage) {
        spellchecker.setMaxMatchPercentage(maxMatchPercentage);
    }
}
