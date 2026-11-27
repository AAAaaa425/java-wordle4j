package ru.yandex.practicum;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class WordleGame {
    public static final int MAX_STEPS = 6;

    private final String answer;
    private final WordleDictionary dictionary;
    private final PrintWriter log;

    private final LinkedHashMap<String, String> history = new LinkedHashMap<>();

    private final LinkedHashMap<String, Integer> hints = new LinkedHashMap<>();

    private int steps = MAX_STEPS;
    private boolean won;
    private Random random;

    public WordleGame(WordleDictionary dictionary, PrintWriter log) {
        this(dictionary, log, new Random());
    }

    public WordleGame(WordleDictionary dictionary, PrintWriter log, Random random) {
        if (dictionary == null || dictionary.getWords().isEmpty()) {
            throw new IllegalArgumentException("Для игры нужен непустой словарь.");
        }

        if (random == null) {
            throw new IllegalArgumentException("Генератор случайных чисел не должен быть null.");
        }

        this.dictionary = dictionary;
        this.log = log;
        this.random = random;

        this.answer = dictionary.getWords().get(random.nextInt(dictionary.getWords().size()));

        writeLog("Игра началась. Загаданное слово выбрано, попыток: " + steps);
    }

    public String makeGuess(String input) throws InvalidWordException, WordNotFoundInDictionary {

        String guess = WordleDictionary.normalize(input);

        if (guess.isEmpty()) {
            return getHint();
        }

        if (!WordleDictionary.isValidFormat(guess)) {
            throw new InvalidWordException("Введите одно русское слово из пяти букв.");
        }

        if (!dictionary.contains(guess)) {
            throw new WordNotFoundInDictionary(guess);
        }

        if (isFinished()) {
            throw new IllegalStateException("Игра уже завершена.");
        }

        String result = WordleDictionary.compare(guess, answer);
        history.put(guess, result);
        steps--;

        if (guess.equals(answer)) {
            won = true;
        }

        writeLog("Ход: " + guess + ", результат: " + result + ", осталось попыток: " + steps + ", победа: " + won);

        return result;
    }

    public String getHint() {
        List<String> candidates = new ArrayList<>();

        // Перебираем все слова из словаря
        for (String candidate : dictionary.getWords()) {

            // Не предлагаем слова, которые уже вводили
            if (history.containsKey(candidate)) {
                continue;
            }

            // Не предлагаем ранее показанные подсказки
            if (hints.containsKey(candidate)) {
                continue;
            }

            boolean matchesHistory = true;

            // Проверяем слово по результатам предыдущих попыток
            for (Map.Entry<String, String> entry : history.entrySet()) {
                String previousGuess = entry.getKey();
                String previousResult = entry.getValue();

                String candidateResult =
                        WordleDictionary.compare(previousGuess, candidate);

                if (!candidateResult.equals(previousResult)) {
                    matchesHistory = false;
                    break;
                }
            }

            // Если слово подходит под все предыдущие результаты,
            // добавляем его в список кандидатов
            if (matchesHistory) {
                candidates.add(candidate);
            }
        }

        // Проверяем, остались ли доступные подсказки
        if (candidates.isEmpty()) {
            throw new IllegalStateException("Нет доступных подсказок.");
        }

        // Выбираем случайное слово
        String hint = candidates.get(random.nextInt(candidates.size()));

        // Сохраняем использованную подсказку
        hints.merge(hint, 1, Integer::sum);

        return hint;
    }

    public boolean isFinished() {
        return won || steps == 0;
    }

    public boolean isWon() {
        return won;
    }

    public int getStepsLeft() {
        return steps;
    }

    public String getAnswer() {
        return answer;
    }

    public Map<String, String> getHistory() {
        return new LinkedHashMap<>(history);
    }

    public Map<String, Integer> getHints() {
        return new LinkedHashMap<>(hints);
    }

    private void writeLog(String message) {
        if (log != null) {
            log.println(message);
            log.flush();
        }
    }
}
