package com.filter.textcorrector.profanity_filtering.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Censored {
    private String censoredText;
    private Set<String> badWordList;
}