package com.filter.textcorrector.spellchecking.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Suggestion implements Comparable<Suggestion> {
    private String word;
    private float soundexCodeDistance;
    private int editDistance;
    private float matchPercentage;

    @Override
    public String toString() {
        return "Suggestion{" +
                "word='" + word + '\'' +
                ", soundexCodeDistance=" + soundexCodeDistance +
                ", editDistance=" + editDistance +
                ", matchPercentage=" + matchPercentage +
                '}' + '\n';
    }

    @Override
    public int compareTo(Suggestion suggestion) {
        if(this.getMatchPercentage() > suggestion.getMatchPercentage())
        {
            return -1;
        }
        else if(this.getMatchPercentage() < suggestion.getMatchPercentage()){
            return +1;
        }
        return 0;
    }
}
