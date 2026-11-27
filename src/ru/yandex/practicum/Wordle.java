package ru.yandex.practicum;

import java.io.PrintWriter;
import java.util.Scanner;

public class Wordle {
    private static final String DICTIONARY_FILE = "words_ru.txt";
    private static final String LOG_FILE = "wordle.log";

    public static void main(String[] args) {
        try (PrintWriter log = new PrintWriter(LOG_FILE, "UTF-8");
             Scanner scanner = new Scanner(System.in)) {

            try {
                WordleDictionaryLoader loader = new WordleDictionaryLoader(log);
                WordleDictionary dictionary = loader.load(DICTIONARY_FILE);
                WordleGame game = new WordleGame(dictionary, log);

                System.out.println("Добро пожаловать в Wordle!");
                System.out.println("Угадайте существительное из пяти букв. У вас 6 попыток.");
                System.out.println("Нажмите Enter, чтобы получить подсказку.");

                while (!game.isFinished()) {
                    System.out.print("> ");

                    if (!scanner.hasNextLine()) {
                        break;
                    }

                    String input = scanner.nextLine();

                    try {
                        String result = game.makeGuess(input);

                        if (input.trim().isEmpty()) {
                            System.out.println("Подсказка: " + result);
                        } else {
                            System.out.println(result);

                            if (game.isWon()) {
                                System.out.println("Вы угадали!");
                            } else if (!game.isFinished()) {
                                System.out.println("Осталось попыток: " + game.getStepsLeft());
                            }
                        }
                    } catch (InvalidWordException | WordNotFoundInDictionary e) {
                        System.out.println(e.getMessage());
                    }
                }

                if (game.isWon()) {
                    System.out.println("Победа! Загаданное слово: " + game.getAnswer());
                } else {
                    System.out.println("Игра окончена. Загаданное слово: " + game.getAnswer());
                }

            } catch (Exception e) {
                log.println("Критическая ошибка: " + e);
                e.printStackTrace(log);
                log.flush();

                System.out.println("Не удалось запустить игру. " + "Подробности записаны в " + LOG_FILE);
            }

        } catch (Exception e) {
            System.out.println("Не удалось создать или закрыть лог-файл: " + e.getMessage());
        }
    }
}