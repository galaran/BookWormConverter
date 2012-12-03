package com.galaran.plugins.bookwormconverter;

import me.galaran.bukkitutils.bwconverter.Book;
import me.galaran.bukkitutils.bwconverter.UnicodeBOMInputStream;
import me.galaran.bukkitutils.bwconverter.WordWrapper;
import org.apache.commons.lang.Validate;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class WormBook extends Book {

    private final short id;

    private static final int GUARANTEED_NO_WRAP_BOOK_PAGE_WIDTH = 16;
    private static final int MAX_CHARS_ON_PAGE = 192;

    public static WormBook load(File wormBookFile) throws IOException, IllegalArgumentException {
        BufferedReader bw = null;
        try {
            bw = BookWormLoader.openReader(wormBookFile);

            String idString = bw.readLine();
            Validate.notNull(idString, "Not a bookworm book file: " + wormBookFile.getName());
            short id;
            try {
                id = Short.parseShort(idString);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Not a bookworm book file: " + wormBookFile.getName());
            }
            String title = bw.readLine();
            String author = bw.readLine();
            String text = bw.readLine();

            if (title == null || author == null || text == null) {
                throw new IllegalArgumentException("Not a bookworm book file: " + wormBookFile.getName());
            }

            return new WormBook(id, title, author, textToPages(text));
        } finally {
            if (bw != null) bw.close();
        }
    }

    private WormBook(short id, String title, String author, String[] pages) {
        super(title, author, pages);
        this.id = id;
    }

    private static String[] textToPages(String bookWormText) {
        List<String> pages = new ArrayList<String>();

        String text = bookWormText.replaceAll("[ ]?::[ ]?", "\n").replaceAll("\\n{3,}", "\n\n");
        text = filterHorizontalLines(text);

        StringBuilder pageBuffer = new StringBuilder();
        boolean isPrevLineBreak = false;
        int pageLength = 0; // approximate
        for (String curWord : WordWrapper.splitToWordsWithLineBreaks(text)) {
            if (curWord.equals("\n")) {
                if (isPrevLineBreak) {
                    pageBuffer.append("§r"); // empty line
                    pageLength += GUARANTEED_NO_WRAP_BOOK_PAGE_WIDTH;
                } else {
                    pageLength += GUARANTEED_NO_WRAP_BOOK_PAGE_WIDTH / 3;
                }
                isPrevLineBreak = true;
            } else {
                if (!isPrevLineBreak && pageLength > 0) {
                    pageBuffer.append(' ');
                    pageLength += 1;
                }
                isPrevLineBreak = false;
            }
            pageBuffer.append(curWord);
            pageLength += curWord.length();

            if (pageLength > MAX_CHARS_ON_PAGE) {
                // next page
                pages.add(pageBuffer.toString());
                pageBuffer.setLength(0);
                pageLength = 0;
            }
        }
        if (pageBuffer.length() != 0) {
            pages.add(pageBuffer.toString());
        }

        return pages.toArray(new String[pages.size()]);
    }

    private static String filterHorizontalLines(String text) {
        char[] hlChars = new char[] { '~', '=', '-', ':', ';', '+', '!', '@', '#', '$', '%', '*', '_' };
        for (char hlChar : hlChars) {
            String pattern = Pattern.quote(String.valueOf(hlChar));

            char[] replacing = new char[GUARANTEED_NO_WRAP_BOOK_PAGE_WIDTH];
            Arrays.fill(replacing, hlChar);
            String replacingString = new String(replacing);

            text = text.replaceAll(pattern + "{" + GUARANTEED_NO_WRAP_BOOK_PAGE_WIDTH + ",}", replacingString);
        }
        return text;
    }

    public short getId() {
        return id;
    }
}
