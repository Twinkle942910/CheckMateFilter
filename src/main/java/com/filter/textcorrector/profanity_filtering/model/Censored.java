package com.filter.textcorrector.profanity_filtering.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Censored {
    @JsonProperty("censored_text")
    private String censoredText;

    @JsonProperty("bad_words")
    private Set<String> badWordList;

    @JsonProperty("contains_profanity")
    private boolean containsProfanity;
}