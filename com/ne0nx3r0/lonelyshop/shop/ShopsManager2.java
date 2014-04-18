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

public class ShopsManager2 {
    
    private final LonelyShopPlugin plugin;

    public ShopsManager2(LonelyShopPlugin plugin) {
        this.plugin = plugin;
    }

    public void onShopAction(InventoryClickEvent e) {
        // if there's no item in the slot then we don't care
        if(e.getCurrentItem() == null || e.getCurrentItem().getType().equals(Material.AIR)){
            return;
        }
        
        Player player = (Player) e.getWhoClicked();
        ItemStack is = e.getCurrentItem();
        String shopTitle = e.getInventory().getTitle();
        ItemMeta meta = is.getItemMeta();

        // no metadata means it's not an action or item for sale
        if(!is.hasItemMeta()) { 
            player.sendMessage("Invalid LonelyShop item!");
            
            return;
        }
        
        String sMaterialAndData[] = shopTitle
                .substring(shopTitle.indexOf("-")+2,shopTitle.lastIndexOf("-")-2)
                .split(" ");

        Material shopMaterial = Material.matchMaterial(sMaterialAndData[0]);

        byte shopData = -1;

        if(sMaterialAndData[1] != null){
            try {
                shopData = Byte.parseByte(sMaterialAndData[1]);
            }catch(NumberFormatException ex){}
        }

        ShopType shopType;
        
        if(shopTitle.startsWith(LonelyShop.SHOPTITLE_PREFIX+LonelyShop.SHOPTITLE_SCREEN_YOUR_ITEMS)) {
            shopType = ShopType.MyForSaleItems;
        }
        else if(shopTitle.startsWith(LonelyShop.SHOPTITLE_PREFIX+LonelyShop.SHOPTITLE_SCREEN_ALL_ITEMS_PAGE)){
            shopType = ShopType.All;
        }
        else if(shopData != -1){
            shopType = ShopType.MaterialAndData;
        }
        else{
            shopType = ShopType.Material;
        }
        
        // pagination
        String displayName = meta.getDisplayName();
        
        if(displayName != null 
        && (displayName.equals(LonelyShop.NEXT_PAGE_TEXT) 
        ||  displayName.equals(LonelyShop.PREV_PAGE_TEXT))) {
            boolean nextPage = false;
            
            if(displayName.equals(LonelyShop.NEXT_PAGE_TEXT)){
                nextPage = true;
            }
            
            int newPageNumber = Integer.parseInt(shopTitle.substring(shopTitle.lastIndexOf(" ")+(nextPage?1:-1)));
            
            ArrayList<ItemForSale> items;
            
            if(shopType == ShopType.All) {
                items = plugin.inventoryManager.getItemsForSale(newPageNumber);
            }
            else if(shopType == ShopType.MyForSaleItems){
                items = plugin.inventoryManager.getSellerItems(player,newPageNumber);
            }
            else{
                if(shopData == -1) {// ShopType.Material
                    items = plugin.inventoryManager.getItemsForSale(shopMaterial,newPageNumber);
                }
                else {// ShopType.MaterialAndData
                    items = plugin.inventoryManager.getItemsForSale(shopMaterial,shopData,newPageNumber);
                }
            }
            
            if(items.isEmpty()){
                if(shopType.equals(ShopType.MyForSaleItems)) {
                    player.sendMessage("You don't have any items for sale currently!");
                }
                else if(!shopType.equals(ShopType.All)) {
                    player.sendMessage("No "+shopMaterial.name()+" found!");
                }
                else {
                    player.sendMessage("No items found!");
                }
                
                return;
            }
            
            Inventory inventory;
            
            if(shopType.equals(ShopType.All) || shopType.equals(ShopType.MyForSaleItems)) {
                inventory = LonelyShop.getShopInventory(plugin.economy,player,shopType,items,newPageNumber);
            }
            else {
                inventory = LonelyShop.getShopInventory(plugin.economy,player,shopType,items,newPageNumber,shopMaterial,shopData);
            }
           
            e.getInventory().setContents(inventory.getContents());
            
            return;
        }        
        
        // tried to buy an item
        List<String> lore = meta.getLore();

        String sId = lore.get(lore.size()-1);

        int itemId = 1;

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
        
        if(shopType.equals(ShopType.MyForSaleItems)) {
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
        else {//size - 5
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
            HumanEntity hePlayer = player;
            
            if(hePlayer.getOpenInventory().getTitle().startsWith(LonelyShop.SHOPTITLE_PREFIX)) {
                player.closeInventory();
                
                player.sendMessage(ChatColor.RED+"LonelyShops was disabled or reloaded.");
            }
        }
    }

    public InventoryActionResponse openPlayerShop(Player player) {
        ArrayList<ItemForSale> items = plugin.inventoryManager.getSellerItems(player, 1);
        
        if(items.isEmpty()) {
            return new InventoryActionResponse(null,false,"You don't have any items for sale!");
        }
        Inventory inventory = LonelyShop.getShopInventory(plugin.economy, player, ShopType.MyForSaleItems, items, 1);
        
        player.openInventory(inventory);
        
        return new InventoryActionResponse(null,true,"Opening your for sale items...");
    }

    public void openShopInventory(Player player, ArrayList<ItemForSale> items,ShopType shopType) {
        Inventory inventory = LonelyShop.getShopInventory(plugin.economy, player, ShopType.All, items, 1);
           
        player.openInventory(inventory);
    }
    
    public void openShopInventory(Player player, ArrayList<ItemForSale> items,ShopType shopType, Material material) {
        Inventory inventory = LonelyShop.getShopInventory(plugin.economy, player, ShopType.Material, items, 1, material, (byte) -1);
           
        player.openInventory(inventory);
    }
    
    public void openShopInventory(Player player, ArrayList<ItemForSale> items,ShopType shopType, Material material, byte data) {
        Inventory inventory = LonelyShop.getShopInventory(plugin.economy, player, ShopType.MaterialAndData, items, 1, material, data);
           
        player.openInventory(inventory);
    }
}
