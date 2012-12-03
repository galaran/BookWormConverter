package com.galaran.plugins.bookwormconverter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Map;

public class BookWormConverter extends JavaPlugin implements Listener {

    private Map<Short, WormBook> books;
    private Map<Location, Short> shelves;

    private boolean convertOnSneaking;
    private boolean convertToUnsigned;
    private boolean copyBookshelvesToUnsigned;

    @Override
    public void onEnable() {
        File configFile = new File(getDataFolder(), "config.yml");
        saveDefaultConfig();
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        convertOnSneaking = config.getBoolean("convert.on-sneaking", false);
        convertToUnsigned = config.getBoolean("convert.to-unsigned", false);
        copyBookshelvesToUnsigned = config.getBoolean("copy-bookshelves.to-unsigned", false);

        final BookWormLoader loader = new BookWormLoader(getLogger(), getDataFolder());

        // load data when all worlds are loaded (Location requires World)
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                books = loader.loadBooks();
                shelves = loader.loadShelves();
                Bukkit.getPluginManager().registerEvents(BookWormConverter.this, BookWormConverter.this);
            }
        }, 1);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack handStack = player.getItemInHand();
        if (handStack != null) {
            Action act = event.getAction();
            Block clickedBlock = event.getClickedBlock();
            if (handStack.getType() == Material.BOOK &&
                    (act == Action.RIGHT_CLICK_AIR || act == Action.RIGHT_CLICK_BLOCK)) {
                tryConvertBook(player, handStack);
            } else if (handStack.getType() == Material.BOOK_AND_QUILL &&
                    act == Action.LEFT_CLICK_BLOCK && clickedBlock.getType() == Material.BOOKSHELF) {
                tryCopyBookshelf(player, handStack, clickedBlock);
                event.setUseInteractedBlock(Event.Result.DENY); // prevent bookshelf breaking in creative
            }
        }
    }

    private void tryConvertBook(Player player, ItemStack bookStack) {
        if (convertOnSneaking && !player.isSneaking()) return;

        short bookData = bookStack.getDurability();
        if (bookData == 0) return; // not a BookWorm book

        WormBook book = books.get(bookData);
        if (book == null) return;

        ItemStack converted;
        if (convertToUnsigned) {
            converted = book.toUnsignedBook(bookStack.getAmount());
        } else {
            converted = book.toSignedBook(bookStack.getAmount());
        }
        player.sendMessage(ChatColor.GREEN + "Book converted");
        player.setItemInHand(converted);
    }

    private void tryCopyBookshelf(Player player, ItemStack bookAndQuillStack, Block clickedBlock) {
        Short shelfData = shelves.get(clickedBlock.getLocation());
        if (shelfData == null) {
            player.sendMessage(ChatColor.RED + "Not a BookWorm bookshelf");
            return;
        }

        WormBook shelfBook = books.get(shelfData);
        if (shelfBook == null) {
            player.sendMessage(ChatColor.RED + "Invalid book!");
            return;
        }

        ItemStack copy;
        if (copyBookshelvesToUnsigned) {
            copy = shelfBook.toUnsignedBook(bookAndQuillStack.getAmount());
        } else {
            copy = shelfBook.toSignedBook(bookAndQuillStack.getAmount());
        }
        player.setItemInHand(copy);
        player.sendMessage(ChatColor.GREEN + "Book copied");
    }
}
