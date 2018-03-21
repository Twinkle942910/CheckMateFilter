package com.filter.textcorrector.text_preproccessing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class SymbolMapper {
    private static final String PATH = "/dictionaries/UnicodeToASCIIAppearance.txt";

    private static Logger LOGGER = LoggerFactory.getLogger(SymbolMapper.class);

    private Map<Integer, Character> codes;
    private Map<Integer, Character> numberCodes;

    public SymbolMapper() {
        codes = new HashMap<>();
        numberCodes = new HashMap<>();
        loadCodes();
    }

    private void loadCodes() {
        Scanner scanner = null;

        long startProccessingTime = System.nanoTime();

        try {
            scanner = new Scanner(SymbolMapper.class.getResourceAsStream(PATH));

            char asciiSymbol = 33;

            while (scanner.hasNextLine()) {
                String code = scanner.nextLine();

                if (code.equals("")) {
                    asciiSymbol++;
                } else {
                    int decimalUnicode = Integer.parseInt(code);

                    if (asciiSymbol >= 48 && asciiSymbol <= 57) {
                        numberCodes.put(decimalUnicode, asciiSymbol);
                    } else codes.put(decimalUnicode, asciiSymbol);
                }
            }
        } finally {
            if (scanner != null) {
                scanner.close();
            }

            long endProccessingTime = System.nanoTime();

            LOGGER.debug("Symbol mapper loaded. Contains - " + codes.size() + numberCodes.size() + " codes.");
            LOGGER.debug("Symbol mapper loading took time: " + (endProccessingTime - startProccessingTime) / (double) 1000000 + " ms");
        }
    }

    public String mapNumbers(String text) {
        char[] characterArray = text.toCharArray();

        char previous = '\0';
        int startIndex = -1;
        int position = 0;

        int length = characterArray.length;

        while (position < length) {

            char current = characterArray[position];
            int code = (int) current;

            if (Character.isSurrogatePair(previous, current)) {
                code = Character.toCodePoint(previous, current);
                startIndex = position - 1;
            }

            char replacement = numberCodes.get(code) == null ? (char) code : numberCodes.get(code);

            if (Character.isDigit(replacement)) {
                characterArray[position] = replacement;

                if (startIndex != -1) {
                    move(characterArray, startIndex, characterArray.length);
                    characterArray = Arrays.copyOf(characterArray, length - 1);
                    startIndex = -1;
                    length--;
                } else {
                    position++;
                }
            } else {
                startIndex = -1;
                position++;
            }

            previous = current;
        }

        return new String(characterArray);
    }


    public String mapCharacters(String text) {
        if (textIsNumber(text)) {
            return text;
        }

        char[] textArray = text.toCharArray();
        int length = textArray.length;

        char previous = '\0';
        int startIndex = -1;

        int position = 0;

        while (position < length) {

            char current = textArray[position];
            int code = (int) current;

            if (Character.isSurrogatePair(previous, current)) {
                code = Character.toCodePoint(previous, current);
                startIndex = position - 1;
            }

            char replacement = codes.get(code) == null ? (char) code : codes.get(code);
            textArray[position] = replacement;

            if (startIndex != -1) {
                move(textArray, startIndex, textArray.length);
                textArray = Arrays.copyOf(textArray, length - 1);
                startIndex = -1;
                length--;
            } else position++;

            previous = current;
        }

        return new String(textArray);
    }

    private static boolean textIsNumber(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (!Character.isDigit(text.charAt(i)))
                return false;
        }
        return true;
    }

    private static void move(char[] array, int startIndex, int endIndex) {
        for (int i = startIndex; i < endIndex - 1; i++) {
            array[i] = array[i + 1];
        }
    }

    public static void main(String... args) {
        SymbolMapper symbolMapper = new SymbolMapper();

        if (args.length > 0) {
            System.out.println(symbolMapper.mapCharacters(symbolMapper.mapNumbers(args[0])));
        } else {
            System.out.println("Input text.");
        }

    }
}
