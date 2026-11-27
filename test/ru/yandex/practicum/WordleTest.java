package ru.yandex.practicum;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class WordleTest {

    private WordleDictionary dictionary;
    private PrintWriter log;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        log = new PrintWriter(new StringWriter());
        dictionary = new WordleDictionary(List.of("герой", "гонец", "гений", "город", "берег", "ветер"), log);
    }

    // Тесты словаря

    @Test
    void normalizesYoAndCase() {
        assertEquals("ежик", WordleDictionary.normalize("ЁЖИК"));
        assertEquals("елка", WordleDictionary.normalize(" ЁлКа "));
    }

    @Test
    void validatesWordFormat() {
        assertTrue(WordleDictionary.isValidFormat("герой"));
        assertFalse(WordleDictionary.isValidFormat("дом"));
        assertFalse(WordleDictionary.isValidFormat("hello"));
    }

    @Test
    void comparesLettersAndPositions() {
        assertEquals("+^-^-", WordleDictionary.compare("гонец", "герой"));
    }

    @Test
    void duplicateLettersAreCountedOnlyOnce() {
        assertEquals("+-^--", WordleDictionary.compare("аабвг", "абдеж"));
    }

    // Тесты игры

    @Test
    void validGuessConsumesStepAndCanWin() throws Exception {
        WordleGame game = new WordleGame(dictionary, log, new Random(0));
        String answer = game.getAnswer();
        assertEquals("+++++", game.makeGuess(answer));
        assertTrue(game.isWon());
        assertTrue(game.isFinished());
        assertEquals(5, game.getStepsLeft());
    }

    @Test
    void invalidInputDoesNotConsumeStep() {
        WordleGame game = new WordleGame(dictionary, log, new Random(0));
        assertThrows(InvalidWordException.class, () -> game.makeGuess("abc"));
        assertThrows(WordNotFoundInDictionary.class, () -> game.makeGuess("абвгд"));
        assertEquals(6, game.getStepsLeft());
    }

    @Test
    void hintIsAValidUnusedCandidate() throws Exception {
        WordleGame game = new WordleGame(dictionary, log, new Random(0));

        String hint = game.getHint();
        assertTrue(dictionary.contains(hint));

        String secondHint = game.getHint();
        assertTrue(dictionary.contains(secondHint));

        if (!hint.equals(game.getAnswer())) {
            assertNotEquals(hint, secondHint);
        }
        game.makeGuess("гонец");
        String nextHint = game.getHint();
        assertNotEquals(hint, nextHint);
    }

    @Test
    void emptyInputDoesNotConsumeStep() throws Exception {
        WordleGame game = new WordleGame(dictionary, log, new Random(0));
        String hint = game.makeGuess("");
        assertTrue(dictionary.contains(hint));
        assertEquals(6, game.getStepsLeft());
    }

    // Тесты загрузчика словаря

    @Test
    void loadsOnlyFiveLetterRussianWordsAndNormalizesThem() throws Exception {
        Path file = tempDir.resolve("words.txt");
        Files.writeString(file, "ГЕРОЙ\nдом\nЁЖИК\nhello\n", StandardCharsets.UTF_8);
        WordleDictionary loadedDictionary = new WordleDictionaryLoader(new PrintWriter(new StringWriter())).load(file.toString());
        assertTrue(loadedDictionary.contains("герой"));
        assertTrue(loadedDictionary.contains("ежик"));
        assertFalse(loadedDictionary.contains("дом"));
        assertEquals(2, loadedDictionary.getWords().size());
    }

    @Test
    void missingFileProducesIOException() {WordleDictionaryLoader loader = new WordleDictionaryLoader(new PrintWriter(new StringWriter()));
        assertThrows(java.io.IOException.class, () -> loader.load(tempDir.resolve("missing.txt").toString()));
    }

    @Test
    void emptySuitableDictionaryIsRejected() throws Exception {
        Path file = tempDir.resolve("empty.txt");
        Files.writeString(file, "дом\nhello\n", StandardCharsets.UTF_8);
        WordleDictionaryLoader loader = new WordleDictionaryLoader(new PrintWriter(new StringWriter()));
        assertThrows(IllegalArgumentException.class, () -> loader.load(file.toString()));
    }
}