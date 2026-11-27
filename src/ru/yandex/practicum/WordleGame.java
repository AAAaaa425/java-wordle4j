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

    public WordleGame(WordleDictionary dictionary, PrintWriter log) {
        this(dictionary, log, new Random());
    }

    public WordleGame(WordleDictionary dictionary, PrintWriter log, Random random) {
        if (dictionary == null || dictionary.getWords().isEmpty()) {
            throw new IllegalArgumentException("Для игры нужен непустой словарь.");
        }

        this.dictionary = dictionary;
        this.log = log;
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

        outer:
        for (String candidate : dictionary.getWords()) {

            if (history.containsKey(candidate)) {
                continue;
            }

            if (hints.containsKey(candidate) && !candidate.equals(answer)) {
                continue;
            }

            for (Map.Entry<String, String> entry : history.entrySet()) {

                if (!WordleDictionary.compare(entry.getKey(), candidate).equals(entry.getValue())) {
                    continue outer;
                }
            }

            candidates.add(candidate);
        }

        if (!won && !isFinished() && !candidates.contains(answer)) {
            throw new IllegalStateException("Фильтрация подсказок потеряла правильный ответ.");
        }

        if (candidates.isEmpty()) {
            throw new IllegalStateException("Не осталось новых подходящих подсказок.");
        }

        String hint = candidates.get(0);
        hints.merge(hint, 1, Integer::sum);
        writeLog("Подсказка: " + hint + ", доступных вариантов: " + candidates.size() + ", подсказка выдана раз: " + hints.get(hint));
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
