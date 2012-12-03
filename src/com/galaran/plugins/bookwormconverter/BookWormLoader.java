package com.galaran.plugins.bookwormconverter;

import com.google.common.base.Splitter;
import me.galaran.bukkitutils.bwconverter.UnicodeBOMInputStream;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

public class BookWormLoader {

    private final Logger log;
    private final File dataFolder;

    public BookWormLoader(Logger logger, File dataFolder) {
        log = logger;
        this.dataFolder = dataFolder;
    }

    public Map<Short, WormBook> loadBooks() {
        File booksDir = new File(dataFolder, "books");
        if (!booksDir.exists()) {
            booksDir.mkdir();
        }

        File[] bookFiles = booksDir.listFiles();
        if (bookFiles == null) {
            log.warning("Error loading BookWorm books");
            return Collections.emptyMap();
        } else if (bookFiles.length == 0) {
            log.warning("Books directory is empty. Put BookWorm book files to BookWormConverter/books dir and reload the server");
            return Collections.emptyMap();
        } else {
            Map<Short, WormBook> books = new HashMap<Short, WormBook>();
            log.info("Loading books...");
            for (File bookFile : bookFiles) {
                try {
                    WormBook book = WormBook.load(bookFile);
                    books.put(book.getId(), book);
                } catch (IOException ex) {
                    log.warning("Failed to read book " + bookFile.getName() + ". Skipping it");
                    ex.printStackTrace();
                } catch (IllegalArgumentException ex) {
                    log.warning(bookFile.getName() + " is not a BookWorm book file. Skipping it");
                }
            }
            log.info(books.size() + " books loaded");
            return books;
        }
    }

    public Map<Location, Short> loadShelves() {
        File shelvesFile = new File(dataFolder, "bookshelves.txt");
        if (!shelvesFile.exists()) {
            log.warning("There is no bookshelves.txt file. Copy it from BookWorm to BookWormConverter directory and reload the server");
            log.warning("Without this file player will not be able to make bookshelves copies");
            return Collections.emptyMap();
        }

        Map<Location, Short> shelves = new HashMap<Location, Short>();
        log.info("Loading bookshelves...");
        BufferedReader bw = null;
        try {
            bw = openReader(shelvesFile);

            String line;
            while ((line = bw.readLine()) != null) {
                Iterator<String> locDataItr = Splitter.on(':').split(line).iterator();

                String locString = locDataItr.next();
                Location loc = parseLocation(locString);
                if (loc == null) {
                    log.warning("Wrong location: " + locString + ". Skipping this bookshelf");
                    continue;
                }

                short bookData = Short.parseShort(locDataItr.next());
                shelves.put(loc, bookData);
            }
        } catch (IOException ex) {
            log.warning("Error loading bookshelves data");
            ex.printStackTrace();
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        log.info(shelves.size() + " shelves loaded");
        return shelves;
    }

    private Location parseLocation(String locString) {
        Iterator<String> locItr = Splitter.on(',').split(locString).iterator();
        World world = Bukkit.getWorld(locItr.next());
        if (world == null) return null;

        try {
            int x = Integer.parseInt(locItr.next());
            int y = Integer.parseInt(locItr.next());
            int z = Integer.parseInt(locItr.next());
            return new Location(world, x, y, z);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public static BufferedReader openReader(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        UnicodeBOMInputStream bomStream = new UnicodeBOMInputStream(fis);
        BufferedReader bw = new BufferedReader(new InputStreamReader(bomStream, "utf-8"));
        bomStream.skipBOM();
        return bw;
    }
}
