
package com.ne0nx3r0.lonelyshop.shop;

import com.ne0nx3r0.lonelyshop.inventory.InventoryActionResponse;
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
    public static final String SHOPTITLE_SCREEN_YOUR_ITEMS = SHOPTITLE_PREFIX+" - Your Items";
    
    public static final String CLICK_TO_RETREIVE = ChatColor.GREEN+"Click to retreive";
    public static final String CLICK_TO_BUY = ChatColor.GREEN+"Click to Buy";
    public static final String CLICK_TO_CONFIRM = ChatColor.DARK_GREEN+"Click again"+ChatColor.GREEN+" to confirm";
    
    public static final String PREV_PAGE_TEXT = ChatColor.GREEN + "Prev Page";
    public static final String NEXT_PAGE_TEXT = ChatColor.GREEN + "Next Page";
    
    public static final int SHOP_ITEM_SLOTS = 5*9;
    
    private static ItemStack getPrevPageItem(int currentPage) {
        ItemStack is = new ItemStack(Material.BOOK,1);
        
        ItemMeta meta = is.getItemMeta();
        
        meta.setDisplayName(LonelyShop.PREV_PAGE_TEXT);
        
        is.setItemMeta(meta);
        
        return is;
    }
    
    private static ItemStack getNextPageItem(int currentPage) {
        ItemStack is = new ItemStack(Material.BOOK,1);
        
        ItemMeta meta = is.getItemMeta();
        
        meta.setDisplayName(LonelyShop.NEXT_PAGE_TEXT);
        
        is.setItemMeta(meta);
        
        return is;
    }

    // TODO: All this static was a terrible idea.
    static Inventory getShopInventory(Economy economy,Player player, ShopType shopType, ArrayList<ItemForSale> items, int pageNumber, Material shopMaterial, byte shopData) {    
        String sTitle;
        
        switch(shopType){
            case All:
            default:
                sTitle = LonelyShop.SHOPTITLE_PREFIX+LonelyShop.SHOPTITLE_SCREEN_ALL_ITEMS_PAGE+pageNumber;
                break;
            case MyForSaleItems:
                
                sTitle = LonelyShop.SHOPTITLE_PREFIX+LonelyShop.SHOPTITLE_SCREEN_YOUR_ITEMS+pageNumber;
                break;
            case Material:
                
                sTitle = LonelyShop.SHOPTITLE_PREFIX+String.format(LonelyShop.SHOPTITLE_SCREEN_SEARCH_PAGE+pageNumber,new Object[]{shopMaterial});
                break;
            case MaterialAndData:
                
                sTitle = LonelyShop.SHOPTITLE_PREFIX+String.format(LonelyShop.SHOPTITLE_SCREEN_SEARCH_PAGE+pageNumber,new Object[]{shopMaterial+" "+shopData});
                break;
        }
        
        Inventory inventory = Bukkit.getServer().createInventory(null, LonelyShop.SHOP_ITEM_SLOTS, sTitle);

        if(pageNumber > 1) {
            inventory.setItem(0, LonelyShop.getPrevPageItem(pageNumber-1));
        }
        
        //TODO:WHY AM I DOING THIS -9 all the time!!! FIX IT
        if(items.size() >= LonelyShop.SHOP_ITEM_SLOTS-9){
            inventory.setItem(8, LonelyShop.getNextPageItem(pageNumber+1));
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
            
            lore.add(LonelyShop.CLICK_TO_RETREIVE);
            
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

    static Inventory getShopInventory(Economy economy, Player player, ShopType shopType, ArrayList<ItemForSale> items, int pageNumber) {    
        return LonelyShop.getShopInventory(economy, player, shopType, items, pageNumber, null, (byte) 0);
    }
}
