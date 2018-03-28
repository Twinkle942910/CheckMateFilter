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

    public TextFilter(Language language) {
        spellchecker = new Spellchecker.Builder(language)
                .withSuggestionLimit(7)
                .build();

        profanityFilter = new ProfanityFilter.Builder()
                .withWordReplacement("[profanity]")
                .build();

        textPreproccessor = TextPreproccessor.getInstance();
    }

    public TextFilter(Language language, String dictionaryPath){
        spellchecker = new Spellchecker.Builder(language)
                .withSuggestionLimit(7)
                .build();

        profanityFilter = new ProfanityFilter.Builder()
                .withWordReplacement("[profanity]")
                .withDictionary(dictionaryPath)
                .build();

        textPreproccessor = TextPreproccessor.getInstance();
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
    public boolean isProfane(String word){
        return profanityFilter.isProfane(word);
    }

    public void doPreproccessing(boolean doPreproccessing){
        spellchecker.doPreproccessing(doPreproccessing);
    }

    public void doRemoveRepeatedLetters(boolean doRemoveRepeatedLetters){
        spellchecker.doRemoveRepeatedLetters(doRemoveRepeatedLetters);
    }

    public void doCheckCompounds(boolean doCheckCompounds){
        spellchecker.doCheckCompounds(doCheckCompounds);
    }

    public void setSuggestionLimit(int suggestionLimit){
        spellchecker.setSuggestionLimit(suggestionLimit);
    }

    public void keepUnrecognized(boolean keepUnrecognized){
        spellchecker.keepUnrecognized(keepUnrecognized);
    }

    public void changeLanguage(Language language){
        profanityFilter.changeLanguage(language);
        spellchecker.changeLanguage(language);
    }

    public void setProfanityReplacement(String replacement){
        profanityFilter.setProfanityReplacement(replacement);
    }

    public void doRemoveProfaneWord(boolean removeProfaneWord){
        profanityFilter.removeProfaneWord(removeProfaneWord);
    }

    public void setMaxMatchPercentage(float maxMatchPercentage){
        spellchecker.setMaxMatchPercentage(maxMatchPercentage);
    }

    public static void main(String[] args) {
        Filter filter = new TextFilter(Language.ENGLISH);

        System.out.println(filter.preproccess("helllo", true));
    }
}
