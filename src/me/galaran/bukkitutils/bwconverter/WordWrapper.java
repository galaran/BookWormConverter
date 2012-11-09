package me.galaran.bukkitutils.bwconverter;

import java.util.ArrayList;
import java.util.List;

public class WordWrapper {

    private final StringBuilder buffer;
    private final int bufferLimit;
    private final int longWordMinCharsForContinueLine;

    private boolean lastBreakExplicit = false;

    private final List<String> lines = new ArrayList<String>();

    /**
     * Breaks a raw string up into a series of lines. Words are wrapped using spaces as decimeters and the newline
     * character is respected. Special codes are not respected.
     */
    public static List<String> wrap(String text, int lineLimit, int longWordMinCharsForContinueLine) {
        WordWrapper ww = new WordWrapper(lineLimit, longWordMinCharsForContinueLine);

        for (String curWord : splitToWordsWithLineBreaks(text)) {
            if (curWord.equals("\n")) { // explicit line break
                ww.nextLine(true);
            } else if (ww.tryFitToCurrentLine(curWord)) { // continue current line
            } else if (ww.tryFitToNextLine(curWord)) { // wrap to next line
            } else { // word is too long, wrap it to multiple lines, but may be start on current
                ww.fitToMultipleLines(curWord);
            }
        }
        ww.flushTail();

        return ww.getLines();
    }

    /**
     * Split multiline text using spaces and line breaks as delimeters
     * Handles multiple spaces in row as one
     * Ignores leading and trailing spaces to line break
     * @return "\n" element is line break
     */
    public static List<String> splitToWordsWithLineBreaks(String text) {
        String processedText = text.replaceAll("[ ]{2,}", " ").replaceAll("[ ]?(\\r|\\r\\n|\\n)[ ]?", "\n");

        List<String> words = new ArrayList<String>();
        StringBuilder wordBuffer = new StringBuilder();
        for (char c : processedText.toCharArray()) {
            if (c == ' ') {
                words.add(wordBuffer.toString());
                wordBuffer.setLength(0);
            } else if (c == '\n') {
                if (wordBuffer.length() > 0) { // handle \n\n
                    words.add(wordBuffer.toString());
                    wordBuffer.setLength(0);
                }
                words.add("\n");
            } else {
                wordBuffer.append(c);
            }
        }
        if (wordBuffer.length() > 0) { // append tail (last word)
            words.add(wordBuffer.toString());
        }

        return words;
    }

    private WordWrapper(int bufferLimit, int longWordMinCharsForContinueLine) {
        if (longWordMinCharsForContinueLine >= bufferLimit) {
            throw new IllegalArgumentException("Continue limit must be lower, than buffer limit");
        }

        this.longWordMinCharsForContinueLine = longWordMinCharsForContinueLine;
        this.bufferLimit = bufferLimit;
        buffer = new StringBuilder(bufferLimit);
    }

    public boolean tryFitToCurrentLine(String word) {
        if (isEmpty()) {
            if (word.length() <= bufferLimit) { // new line, space not needed
                buffer.append(word);
            } else {
                return false;
            }
        } else {
            if (length() + 1 + word.length() <= bufferLimit) { // buffer + space + word
                buffer.append(' ');
                buffer.append(word);
            } else {
                return false;
            }
        }
        if (isFull()) nextLine(false);
        return true;
    }

    public boolean tryFitToNextLine(String word) {
        if (word.length() > bufferLimit) return false;

        nextLine(false);
        buffer.append(word);
        if (isFull()) nextLine(false);
        return true;
    }

    public void fitToMultipleLines(String word) {
        int from = 0;
        if (!isEmpty()) {
            if (bufferLimit - length() - 1 >= longWordMinCharsForContinueLine) {
                // append long word start to current line
                buffer.append(' ');
                from = bufferLimit - length();
                buffer.append(word.substring(0, from));
            }
            nextLine(false);
        }

        int to;
        while (from < word.length()) {
            to = Math.min(from + bufferLimit, word.length());
            buffer.append(word.substring(from, to));
            from = to;
            if (isFull()) nextLine(false);
        }
    }

    public void nextLine(boolean explicitLineBreak) {
        if (explicitLineBreak) {
            if (isEmpty()) {
                if (lastBreakExplicit) lines.add("");
            } else {
                lines.add(buffer.toString());
            }
            lastBreakExplicit = true;
        } else {
            lines.add(buffer.toString());
            lastBreakExplicit = false;
        }
        reset();
    }

    public void flushTail() {
        if (!isEmpty()) {
            lines.add(buffer.toString());
        }
    }

    private int length() {
        return buffer.length();
    }

    private boolean isFull() {
        return length() == bufferLimit;
    }

    private boolean isEmpty() {
        return length() == 0;
    }

    private void reset() {
        buffer.setLength(0);
    }

    private List<String> getLines() {
        return lines;
    }

//    public static void main(String[] args) {
//        String test = "Breaks a raw      string up      into a series of lines.\nWords are wrapped using spaces\nasdasdasdasdasdasas decimeters " +
//                "and the newline * character isgggf respectedxcoifjdvhdfjkghfjgh dfgjhfjg.";
//        for (String line : wrap(test, 20, 7)) {
//            System.out.println(line);
//        }
//    }
}
