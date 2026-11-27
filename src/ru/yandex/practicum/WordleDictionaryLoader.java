package ru.yandex.practicum;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class WordleDictionaryLoader {
    private final PrintWriter log;

    public WordleDictionaryLoader(PrintWriter log) {
        this.log = log;
    }

    public WordleDictionary load(String fileName) throws IOException {
        List<String> words = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName, StandardCharsets.UTF_8))) {
            String line;

            while ((line = reader.readLine()) != null) {
                String word = WordleDictionary.normalize(line);

                if (WordleDictionary.isValidFormat(word)) {
                    words.add(word);
                }
            }

        } catch (IOException e) {
            if (log != null) {
                log.println("Ошибка чтения словаря: " + e.getMessage());
                log.flush();
            }

            throw e;
        }

        return new WordleDictionary(words, log);
    }
}