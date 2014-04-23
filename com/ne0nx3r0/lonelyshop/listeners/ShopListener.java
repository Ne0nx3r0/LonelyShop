package com.ne0nx3r0.lonelyshop.listeners;

import com.ne0nx3r0.lonelyshop.LonelyShopPlugin;
import com.ne0nx3r0.lonelyshop.shop.LonelyShop;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ShopListener implements Listener {
    private final LonelyShopPlugin plugin;
    
    public ShopListener(LonelyShopPlugin plugin){
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLonelyShopClick(InventoryClickEvent e) {        
        if(e.getWhoClicked() instanceof Player
        && e.getInventory().getTitle().startsWith(LonelyShop.SHOPTITLE_PREFIX)
        && e.getRawSlot() < LonelyShop.SHOP_ITEM_SLOTS && e.getRawSlot() != -999) 
        {            
            e.setCancelled(true);  
                
            if(e.getCursor() != null && !e.getCursor().getType().equals(Material.AIR)){
                ((Player) e.getWhoClicked()).sendMessage(ChatColor.RED+"Put the item down first.");

                return;
            }
            
            if(e.getCurrentItem() != null && !e.getCurrentItem().getType().equals(Material.AIR)){
                plugin.shopsManager.onShopAction(e);
            }

        }
    }
}
