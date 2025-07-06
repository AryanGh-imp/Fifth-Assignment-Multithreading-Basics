import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class TypingTest {
    private static volatile String lastInput = "";
    private static volatile boolean acceptingInput = true; // Control input thread
    private static int correctCount = 0;
    private static int incorrectCount = 0;
    private static long startTime = 0;
    private static final Scanner scanner = new Scanner(System.in);

    public static class InputRunnable implements Runnable {
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted() && acceptingInput) {
                try {
                    if (scanner.hasNextLine()) {
                        lastInput = scanner.nextLine().trim();
                    }
                } catch (Exception e) {
                    System.err.println("Error in input thread: " + e.getMessage());
                    e.printStackTrace();
                    break;
                }
            }
        }
    }

    public static void testWord(String wordToTest) {
        try {
            System.out.println(ANSI.CYAN + "\n=======================" + ANSI.RESET);
            System.out.println(wordToTest);
            System.out.println(ANSI.CYAN + "->" + ANSI.RESET);
            lastInput = "";
            long timeout = wordToTest.length() * 1000L;
            long start = System.currentTimeMillis();

            while (System.currentTimeMillis() - start < timeout) {
                if (!lastInput.isEmpty()) {
                    System.out.println();
                    System.out.println("You typed: " + lastInput);
                    if (lastInput.equals(wordToTest)) {
                        System.out.println(ANSI.GREEN + "Correct" + ANSI.RESET);
                        correctCount++;
                    } else {
                        System.out.println(ANSI.RED + "Incorrect" + ANSI.RESET);
                        incorrectCount++;
                    }
                    return;
                }
            }

            System.out.println();
            System.out.println("You typed: " + (lastInput.isEmpty() ? ANSI.RED + "(nothing)" + ANSI.RESET : lastInput));
            System.out.println(ANSI.RED + "Timeout" + ANSI.RESET);
            incorrectCount++;

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void typingTest(List<String> inputList, int n) throws InterruptedException {
        List<String> testWords = new ArrayList<>(inputList);
        Collections.shuffle(testWords);
        List<String> selectedTestWords = new ArrayList<>();
        for (int i = 0; i < Math.min(n, testWords.size()); i++) {
            selectedTestWords.add(testWords.get(i));
        }
        if (selectedTestWords.isEmpty()) {
            System.err.println("No words to test. Exiting.");
            return;
        }

        Thread inputThread = new Thread(new InputRunnable());
        inputThread.setDaemon(true);
        inputThread.start();

        long totalTestTime = 0;
        for (String wordToTest : selectedTestWords) {
            long wordStart = System.currentTimeMillis();
            testWord(wordToTest);
            long wordEnd = System.currentTimeMillis();
            totalTestTime += (wordEnd - wordStart);

            if (selectedTestWords.indexOf(wordToTest) < selectedTestWords.size() - 1) {
                Thread.sleep(2000);
            }
        }

        inputThread.interrupt();
        acceptingInput = false;
        Thread.sleep(200); // Add back for safety

        long totalPauseTime = (selectedTestWords.size() - 1) * 2000L; // Total pause time
        long totalTime = totalTestTime - totalPauseTime; // Subtract pauses
        double avgTimePerWord = !selectedTestWords.isEmpty() ? totalTime / (double) selectedTestWords.size() : 0.0;

        System.out.println(ANSI.CYAN + "\nTest Summary:" + ANSI.RESET);
        System.out.println("Correct words: " + ANSI.GREEN + correctCount + ANSI.RESET);
        System.out.println("Incorrect words: " + ANSI.RED + incorrectCount + ANSI.RESET);
        System.out.println("Total time taken: " + totalTime + " ms");
        System.out.println("Total pause time: " + totalPauseTime + " ms");
        System.out.println("Average time per word: " + String.format("%.2f", avgTimePerWord) + " ms");
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println(ANSI.CYAN + "=== Welcome to the Typing Test ===" + ANSI.RESET);
        System.out.println("How many words do you want to take this test with?");
        System.out.println("The test starts " + ANSI.RED + "immediately" + ANSI.RESET + " after selecting the number");

        int n = 0;
        while (true) {
            System.out.println();
            System.out.println(ANSI.CYAN + "Number of words: " + ANSI.RESET);
            try {
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) {
                    System.err.println("Input cannot be empty. Please try again.");
                    continue;
                }
                n = Integer.parseInt(input);
                break;
            } catch (NumberFormatException e) {
                System.err.println("Invalid input. Please enter a valid integer.");
            }
        }

        List<String> words = new ArrayList<>();
        try (InputStream is = TypingTest.class.getResourceAsStream("/Words.txt")) {
            if (is == null) throw new IllegalStateException("Words.txt not found in resources.");
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                String line;
                while ((line = br.readLine()) != null) {
                    words.add(line.trim());
                }
            }
        } catch (Exception e) {
            System.err.println("Could not load Words.txt. Using hardcoded list.");
            words.add("remember");
            words.add("my friend");
            words.add("boredom");
            words.add("is a");
            words.add("crime");
        }

        if (words.isEmpty()) {
            throw new IllegalStateException("No words available to test. Please ensure Words.txt is present and contains words.");
        }

        typingTest(words, n);
    }
}