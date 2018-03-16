package com.filter.textcorrector.text_preproccessing.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProccessedText {
    private String[] originalWords;
    private String[] cleanWords;
}
