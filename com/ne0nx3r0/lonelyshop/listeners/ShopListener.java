package com.ne0nx3r0.lonelyshop.listeners;

import com.ne0nx3r0.lonelyshop.LonelyShopPlugin;
import com.ne0nx3r0.lonelyshop.shop.LonelyShop;
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
        && e.getInventory().getTitle().startsWith(LonelyShop.SHOPTITLE_PREFIX)) 
        {            
            e.setCancelled(true);
            
            if(!e.getCurrentItem().getType().equals(Material.AIR)){
                plugin.shopsManager.onShopAction(e);
            }
        }
    }
}
