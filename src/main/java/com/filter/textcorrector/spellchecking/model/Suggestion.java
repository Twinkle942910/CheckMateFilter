package com.filter.textcorrector.spellchecking.model;

public class Suggestion implements Comparable<Suggestion> {
    private String word;
    private double soundexCodeDistance;
    private int editDistance;

    public Suggestion() {
    }

    public Suggestion(String word, double soundexCodeDistance, int editDistance) {
        this.word = word;
        this.soundexCodeDistance = soundexCodeDistance;
        this.editDistance = editDistance;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public double getSoundexCodeDistance() {
        return soundexCodeDistance;
    }

    public void setSoundexCodeDistance(double soundexCodeDistance) {
        this.soundexCodeDistance = soundexCodeDistance;
    }

    public int getEditDistance() {
        return editDistance;
    }

    public void setEditDistance(int editDistance) {
        this.editDistance = editDistance;
    }

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
