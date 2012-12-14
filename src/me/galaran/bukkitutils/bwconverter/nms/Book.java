package me.galaran.bukkitutils.bwconverter.nms;

import net.minecraft.server.NBTTagCompound;
import net.minecraft.server.NBTTagList;
import net.minecraft.server.NBTTagString;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Book implements ConfigurationSerializable {

    private String author;
    private String title;
    private String[] pages;

    public Book(ItemStack bookItem) {
        NBTTagCompound bookData = ((CraftItemStack) bookItem).getHandle().tag;

        this.author = bookData.getString("author");
        this.title = bookData.getString("title");

        NBTTagList nPages = bookData.getList("pages");

        String[] sPages = new String[nPages.size()];
        for (int i = 0; i < nPages.size(); i++) {
            sPages[i] = nPages.get(i).toString();
        }

        this.pages = sPages;
    }

    public Book(String title, String author, String... pages) {
        this.title = title;
        this.author = author;
        this.pages = pages;
    }

    public String getAuthor() {
        return this.author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String[] getPages() {
        return this.pages;
    }

    public void setPages(String[] pages) {
        this.pages = pages;
    }

    public ItemStack toSignedBook(short data, int amount) {
        CraftItemStack resultStack = new CraftItemStack(Material.WRITTEN_BOOK);
        resultStack.setDurability(data);
        resultStack.setAmount(amount);

        NBTTagCompound newBookNBT = new NBTTagCompound();

        newBookNBT.setString("author", this.author);
        newBookNBT.setString("title", this.title);

        NBTTagList nPages = new NBTTagList();
        for (String page : pages) {
            nPages.add(new NBTTagString(page, page));
        }
        newBookNBT.set("pages", nPages);

        resultStack.getHandle().tag = newBookNBT;
        return resultStack;
    }

    public ItemStack toSignedBook() {
        return toSignedBook((short) 0, 1);
    }

    public ItemStack toUnsignedBook(short data, int amount) {
        CraftItemStack resultStack = new CraftItemStack(Material.BOOK_AND_QUILL);
        resultStack.setDurability(data);
        resultStack.setAmount(amount);

        NBTTagCompound newBookNBT = new NBTTagCompound();

        NBTTagList nPages = new NBTTagList();
        for (String page : pages) {
            nPages.add(new NBTTagString(page, page));
        }
        newBookNBT.set("pages", nPages);

        resultStack.getHandle().tag = newBookNBT;
        return resultStack;
    }

    public ItemStack toUnsignedBook() {
        return toUnsignedBook((short) 0, 1);
    }

    public Book(ConfigurationSection section) {
        title = section.getString("title");
        author = section.getString("author");
        List<String> pagesList = section.getStringList("pages");
        pages = pagesList.toArray(new String[pagesList.size()]);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("title", title);
        result.put("author", author);
        result.put("pages", Arrays.asList(pages));
        return result;
    }
}