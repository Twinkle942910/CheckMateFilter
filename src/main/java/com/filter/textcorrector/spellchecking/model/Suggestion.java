package com.filter.textcorrector.spellchecking.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Suggestion implements Comparable<Suggestion> {
    private String word;
    private double soundexCodeDistance;
    private int editDistance;

    @Override
    public String toString() {
        return "\n Suggestion{" +
                "word='" + word + '\'' +
                ", soundexCodeDistance=" + soundexCodeDistance +
                ", editDistance=" + editDistance +
                '}';
    }

    @Override
    public int compareTo(Suggestion suggestion) {
        if(this.getEditDistance() < suggestion.getEditDistance())
        {
            return -1;
        }
        else if(this.getEditDistance() > suggestion.getEditDistance()){
            return +1;
        }
        return 0;
    }
}
