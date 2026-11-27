package ru.yandex.practicum;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class WordleDictionary {
    private final List<String> words;
    private final Set<String> wordSet;

    public WordleDictionary(List<String> words, PrintWriter log) {
        if (words == null || words.isEmpty()) {
            throw new IllegalArgumentException("Игровой словарь пуст.");
        }

        Set<String> normalized = new LinkedHashSet<>();

        for (String word : words) {
            String value = normalize(word);

            if (isValidFormat(value)) {
                normalized.add(value);
            }
        }

        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("В словаре нет подходящих пятибуквенных слов.");
        }

        this.words = Collections.unmodifiableList(new ArrayList<>(normalized));

        this.wordSet = Collections.unmodifiableSet(normalized);

        if (log != null) {
            log.println("Загружено игровых слов: " + this.words.size());
            log.flush();
        }
    }

    public List<String> getWords() {
        return words;
    }

    public boolean contains(String word) {
        return wordSet.contains(normalize(word));
    }

    public static String normalize(String word) {
        return word == null ? "" : word.trim().toLowerCase().replace('ё', 'е');
    }

    public static boolean isValidFormat(String word) {
        if (word == null || word.length() != 5) {
            return false;
        }

        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);

            if (c < 'а' || c > 'я') {
                return false;
            }
        }

        return true;
    }

    public static String compare(String guess, String answer) {
        StringBuilder result = new StringBuilder("-----");

        int[] remaining = new int[Character.MAX_VALUE + 1];

        for (int i = 0; i < 5; i++) {
            if (guess.charAt(i) == answer.charAt(i)) {
                result.setCharAt(i, '+');
            } else {
                remaining[answer.charAt(i)]++;
            }
        }

        for (int i = 0; i < 5; i++) {
            if (result.charAt(i) == '+') {
                continue;
            }

            char c = guess.charAt(i);

            if (remaining[c] > 0) {
                result.setCharAt(i, '^');
                remaining[c]--;
            }
        }

        return result.toString();
    }
}
