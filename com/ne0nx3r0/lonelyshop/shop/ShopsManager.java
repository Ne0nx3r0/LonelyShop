
package com.ne0nx3r0.lonelyshop.shop;

import com.ne0nx3r0.lonelyshop.LonelyShopPlugin;
import com.ne0nx3r0.lonelyshop.inventory.InventoryActionResponse;
import com.ne0nx3r0.lonelyshop.inventory.ItemForSale;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ShopsManager {
    private final LonelyShopPlugin plugin;
    
    public ShopsManager(LonelyShopPlugin plugin) {
        this.plugin = plugin;
    }
    public void openShopInventory(Player player, ArrayList<ItemForSale> items, ShopType shopType) {
         this.openShopInventory(player, items, shopType, null, (byte) -1);
    }

    public void openShopInventory(Player player, ArrayList<ItemForSale> items, ShopType shopType, Material material) {
        this.openShopInventory(player, items, shopType, material, (byte) -1);
    }

    public void openShopInventory(Player player, ArrayList<ItemForSale> items, ShopType shopType, Material material, byte data) {
        Inventory inventory = LonelyShop.getShopInventory(plugin.economy, player, shopType, items, material, data, 1);
           
        player.openInventory(inventory);
    }
    
    public void onShopAction(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        ItemStack is = e.getCurrentItem();

        // no metadata means it's not an action or item for sale
        if(!is.hasItemMeta() || !is.getItemMeta().hasLore()) { 
            player.sendMessage(ChatColor.RED+"Invalid LonelyShop item!");
            
            return;
        }
        
        ItemMeta meta = is.getItemMeta();
        
        List<String> lore = meta.getLore();

        // next or previous page
        if(meta.hasDisplayName()
        && (meta.getDisplayName().equals(LonelyShop.PREV_PAGE_TEXT) 
        || meta.getDisplayName().equals(LonelyShop.NEXT_PAGE_TEXT))) {
            ShopType shopType = ShopType.valueOf(lore.get(0).substring(2));
            int newPageNumber = Integer.parseInt(lore.get(1).substring(2));
            Material material = null;
            byte data = -1;

            if(shopType.equals(ShopType.Material) || shopType.equals(ShopType.MaterialAndData)){
                material = Material.valueOf(lore.get(2).substring(2));
            }
            
            if(shopType.equals(ShopType.MaterialAndData)){
                data = Byte.valueOf(lore.get(3).substring(2));
            }
            
            ArrayList<ItemForSale> items;

            switch(shopType) {
                default:
                case All:
                    items = plugin.inventoryManager.getItemsForSale(newPageNumber);
                    break;
                case Material:
                    items = plugin.inventoryManager.getItemsForSale(material, newPageNumber);
                    break;
                case MaterialAndData:
                    items = plugin.inventoryManager.getItemsForSale(material, data, newPageNumber);
                    break;
                case MyForSaleItems:
                    items = plugin.inventoryManager.getSellerItems(player, newPageNumber);
                    break;
            }
            
            if(items.isEmpty()) {
                player.sendMessage(ChatColor.RED+"No items found on that page");
                
                return;
            }

            Inventory inventory = LonelyShop.getShopInventory(plugin.economy, player, shopType, items, material, data, newPageNumber);
            
            e.getInventory().setContents(inventory.getContents());
            
            return;
        }
        
        // check if buying/confirming an item
        String sId = lore.get(lore.size()-1);

        int itemId = 0;

        try {
            //TODO: Something a little more secure? (check the rest of the string too)
            // strictly speaking it's not necessary because all lonelyshop items
            // get custom metadata, but I would feel a nice sense of calm if the 
            // rest of the string was verified.
            itemId = Integer.parseInt(sId.substring(sId.lastIndexOf(ChatColor.COLOR_CHAR)+2)); 
        }
        catch(NumberFormatException ex){
            player.sendMessage("Invalid LonelyShop item!");

            return;
        }
        
        String shopTitle = e.getInventory().getTitle();
        
        if(shopTitle.equals(LonelyShop.SHOPTITLE_SCREEN_MY_ITEMS)) {
            InventoryActionResponse response = plugin.inventoryManager.attemptToHandBackToSeller(player, itemId);

            if(response.wasSuccessful()) {
                e.getClickedInventory().setItem(e.getRawSlot(), new ItemStack(Material.AIR));

                // the original itemstack without additional shop metadata
                ItemStack isGiveToPlayer = response.getItemStack();

                e.setCursor(isGiveToPlayer);

                player.sendMessage(ChatColor.GREEN+response.getMessage());
            }
            else {
                player.sendMessage(ChatColor.RED+response.getMessage());
            }
        }
        else {
            if(lore.get(lore.size() - 4).equals(LonelyShop.CLICK_TO_BUY)) {
                player.sendMessage(LonelyShop.CLICK_TO_CONFIRM);
                
                lore.set(lore.size() - 4,LonelyShop.CLICK_TO_CONFIRM);
                
                meta.setLore(lore);
                
                is.setItemMeta(meta);
                
                return;
            }
            
            InventoryActionResponse response = plugin.inventoryManager.attemptToBuy(player, itemId);

            if(response.wasSuccessful()) {
                e.getClickedInventory().setItem(e.getRawSlot(), new ItemStack(Material.AIR));

                // the original itemstack without additional shop metadata
                ItemStack isGiveToPlayer = response.getItemStack();

                e.setCursor(isGiveToPlayer);

                player.sendMessage(ChatColor.GREEN+response.getMessage());
            }
            else {
                player.sendMessage(ChatColor.RED+response.getMessage());
            }
        }
    }

    public void closeAllShops() {
        for(Player player : plugin.getServer().getOnlinePlayers()){
            HumanEntity hent = player;
            
            if(hent.getOpenInventory().getTitle().equals(LonelyShop.SHOPTITLE_PREFIX)) {
                player.closeInventory();
                
                player.sendMessage(ChatColor.RED+"LonelyShops was disabled or reloaded.");
            }
        }
    }
}
