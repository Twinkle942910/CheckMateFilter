package com.filter.textcorrector;

import com.filter.textcorrector.spellchecking.Language;

public class Demo {
    public static void main(String[] args) {
        TextFilter textFilter = new TextFilter(Language.ENGLISH);

        String word = textFilter.checkWord("cutn").get(0);
        System.out.println(textFilter.censor(word));
        textFilter.setProfanityReplacement("[****]");
        System.out.println(textFilter.searchForProfanity("bitch"));

        String text = textFilter.preproccess("p13ce of sh1t", false);
        System.out.println(textFilter.searchForProfanity(text));

        String compound = textFilter.checkCompound("holyshit").get(0);
        System.out.println(textFilter.searchForProfanity(compound));
    }
}
