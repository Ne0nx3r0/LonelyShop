package com.ne0nx3r0.lonelyshop.shop;

import com.ne0nx3r0.lonelyshop.inventory.ItemForSale;
import com.ne0nx3r0.util.TimeSince;
import java.util.ArrayList;
import java.util.List;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class LonelyShop {
    public static final String SHOPTITLE_PREFIX = "LonelyShop";
    public static final String SHOPTITLE_SCREEN_ALL_ITEMS_PAGE = SHOPTITLE_PREFIX+" - All Items";
    public static final String SHOPTITLE_SCREEN_SEARCH_PAGE = SHOPTITLE_PREFIX+" - Search";
    public static final String SHOPTITLE_SCREEN_MY_ITEMS = SHOPTITLE_PREFIX+" - Your Items";
    
    public static final String CLICK_TO_RETREIVE = ChatColor.GREEN+"Click to retreive";
    public static final String CLICK_TO_BUY = ChatColor.GREEN+"Click to Buy";
    public static final String CLICK_TO_CONFIRM = ChatColor.DARK_GREEN+"Click again"+ChatColor.GREEN+" to confirm";
    
    public static final String PREV_PAGE_TEXT = ChatColor.GREEN + "Prev Page";
    public static final String NEXT_PAGE_TEXT = ChatColor.GREEN + "Next Page";
    
    public static final int SHOP_ITEM_SLOTS = 9*5;

    public static Inventory getShopInventory(Economy economy,Player player, ShopType shopType, ArrayList<ItemForSale> items, Material material, byte data, int pageNumber) {
        String sTitle;
        
        switch(shopType){
            case All:
            default:
                sTitle = LonelyShop.SHOPTITLE_SCREEN_ALL_ITEMS_PAGE;
                break;
            case MyForSaleItems:
                sTitle = LonelyShop.SHOPTITLE_SCREEN_MY_ITEMS;
                break;
            case Material:
            case MaterialAndData:
                sTitle = LonelyShop.SHOPTITLE_SCREEN_SEARCH_PAGE;
                break;
        }
        
        Inventory inventory = Bukkit.getServer().createInventory(null, LonelyShop.SHOP_ITEM_SLOTS, sTitle);

        if(pageNumber > 1) {
            ItemStack prevItem = new ItemStack(Material.BOOK,1);

            ItemMeta meta = prevItem.getItemMeta();

            meta.setDisplayName(LonelyShop.PREV_PAGE_TEXT);

            List<String> lore = new ArrayList<>();

            lore.add(ChatColor.DARK_GRAY+shopType.name());
            
            lore.add(ChatColor.DARK_GRAY.toString()+(pageNumber-1));
            
            if(shopType.equals(ShopType.Material) || shopType.equals(ShopType.MaterialAndData)) {
                lore.add(ChatColor.DARK_GRAY+material.name());
            }
            
            if(shopType.equals(ShopType.MaterialAndData)) {
                lore.add(ChatColor.DARK_GRAY.toString()+data);
            }
            
            meta.setLore(lore);

            prevItem.setItemMeta(meta);
            
            inventory.setItem(0, prevItem);
        }

        if(items.size() >= LonelyShop.SHOP_ITEM_SLOTS){
            ItemStack nextItem = new ItemStack(Material.BOOK,1);

            ItemMeta meta = nextItem.getItemMeta();

            meta.setDisplayName(LonelyShop.NEXT_PAGE_TEXT);

            List<String> lore = new ArrayList<>();

            lore.add(ChatColor.DARK_GRAY+shopType.name());
            
            lore.add(ChatColor.DARK_GRAY.toString()+(pageNumber+1));
            
            if(shopType.equals(ShopType.Material) || shopType.equals(ShopType.MaterialAndData)) {
                lore.add(ChatColor.DARK_GRAY+material.name());
            }
            
            if(shopType.equals(ShopType.MaterialAndData)) {
                lore.add(ChatColor.DARK_GRAY.toString()+data);
            }
            
            meta.setLore(lore);

            nextItem.setItemMeta(meta);
            
            inventory.setItem(8, nextItem);
        }

        int currentSlot = 9;
        
        for(ItemForSale item : items) {
            ItemStack is = item.getItemStack();
            
            ItemMeta meta = is.getItemMeta();
            
            List<String> lore;
            
            if(meta.hasLore()){
                lore = meta.getLore();
            }
            else{
                lore = new ArrayList<>();
            }
            
            if(shopType.equals(ShopType.MyForSaleItems)) {
                lore.add(LonelyShop.CLICK_TO_RETREIVE);
            }
            else {
                lore.add(LonelyShop.CLICK_TO_BUY);
            }
            
            String perItem = "";
            
            if(is.getAmount() > 1){
                lore.add(ChatColor.GRAY+"Amount: "+ChatColor.DARK_PURPLE+is.getAmount());
                
                perItem = ChatColor.GRAY+" ("+economy.format(item.getPricePerItem())+ChatColor.GRAY+" each)";
            }
            
            lore.add(ChatColor.GRAY+"Price: "+ChatColor.DARK_PURPLE+economy.format(item.getPrice()) + perItem);
            lore.add(ChatColor.GRAY+"Posted: "+ChatColor.DARK_PURPLE+TimeSince.getTimeSinceString(item.getPostedDate()));
            lore.add(ChatColor.GRAY+"ID: "+ChatColor.DARK_PURPLE+item.getDbID());
            
            meta.setLore(lore);
            
            is.setItemMeta(meta);
            
            inventory.setItem(currentSlot, is);
            
            currentSlot++;
            
            if(currentSlot == LonelyShop.SHOP_ITEM_SLOTS) {
                break;
            }
        }
        
        return inventory;
    }
    
}
