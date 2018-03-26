package com.filter.textcorrector.spellchecking.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Suggestion that = (Suggestion) o;
        return Float.compare(that.soundexCodeDistance, soundexCodeDistance) == 0 &&
                editDistance == that.editDistance &&
                Float.compare(that.matchPercentage, matchPercentage) == 0 &&
                Objects.equals(word, that.word);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), word, soundexCodeDistance, editDistance, matchPercentage);
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
