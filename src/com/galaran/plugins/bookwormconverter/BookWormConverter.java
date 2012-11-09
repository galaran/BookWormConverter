package com.galaran.plugins.bookwormconverter;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class BookWormConverter extends JavaPlugin implements Listener {

    private final Map<Short, WormBook> wormBooks = new HashMap<Short, WormBook>();
    private boolean convertOnSneaking;
    private boolean toUnsignedBook;

    @Override
    public void onEnable() {
        File configFile = new File(getDataFolder(), "config.yml");
        saveDefaultConfig();
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        convertOnSneaking = config.getBoolean("convert-on-sneaking", false);
        toUnsignedBook = config.getBoolean("to-unsigned-book", false);

        File booksDir = new File(getDataFolder(), "books");
        if (!booksDir.exists()) {
            booksDir.mkdir();
        }

        File[] bookFiles = booksDir.listFiles();
        if (bookFiles == null) {
            getLogger().log(Level.SEVERE, "Error loading BookWorm books");
            return;
        } else if (bookFiles.length == 0) {
            getLogger().log(Level.SEVERE, "Books directory is empty. Put BookWorm books (1.txt, 2.txt...) to BookWormConverter/books dir");
            return;
        } else {
            for (File bookFile : bookFiles) {
                try {
                    WormBook book = WormBook.load(bookFile);
                    wormBooks.put(book.getId(), book);
                } catch (IOException ex) {
                    getLogger().log(Level.WARNING, "Failed to read book " + bookFile.getName() + ". Skipping it");
                } catch (IllegalArgumentException ex) {
                    getLogger().log(Level.WARNING, bookFile.getName() + " is not a BookWorm book file. Skipping it");
                }
            }
        }
        getLogger().log(Level.INFO, wormBooks.size() + " BookWorm books loaded");
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        if (convertOnSneaking && !player.isSneaking()) return;

        ItemStack handStack = player.getItemInHand();
        if (handStack == null || handStack.getType() != Material.BOOK) return;

        short bookData = handStack.getDurability();
        if (bookData == 0) return; // not a wormbook

        WormBook book = wormBooks.get(bookData);
        if (book != null) {
            if (toUnsignedBook) {
                player.setItemInHand(book.toUnsignedBook(handStack.getAmount()));
            } else {
                player.setItemInHand(book.toSignedBook(handStack.getAmount()));
            }
        }
    }
}
