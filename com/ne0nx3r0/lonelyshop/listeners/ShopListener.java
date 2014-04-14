package com.ne0nx3r0.lonelyshop.listeners;

import com.ne0nx3r0.lonelyshop.LonelyShopPlugin;
import com.ne0nx3r0.lonelyshop.inventory.InventoryActionResponse;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class ShopListener implements Listener {
    private final LonelyShopPlugin plugin;
    private final String SHOP_TITLE;
    
    public ShopListener(LonelyShopPlugin plugin){
        this.plugin = plugin;
        this.SHOP_TITLE = plugin.shopsManager.getShopTitle();
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLonelyShopClick(InventoryClickEvent e) {        
        if(e.getRawSlot() < plugin.shopsManager.getMaxShopSlots() 
        &&(e.getWhoClicked() instanceof Player) 
        && e.getInventory().getTitle().equals(this.SHOP_TITLE)) 
        {
            if(e.getCurrentItem() == null){
                return;
            }
            
            e.setCancelled(true);
         
            Player player = (Player) e.getWhoClicked();
            
            
            if(!e.getCursor().getType().equals(Material.AIR)){
                player.sendMessage(ChatColor.RED+"You can only buy with an empty hand.");
                
                return;
            }
            
            if(!e.getCurrentItem().getType().equals(Material.AIR)){
                InventoryActionResponse iar = plugin.shopsManager.attemptToBuy(player,e.getCurrentItem());

                if(iar.wasSuccessful()) {
                    e.getClickedInventory().setItem(e.getRawSlot(), new ItemStack(Material.AIR));

                    ItemStack is = iar.getItemStack();

                    e.setCursor(is);

                    player.sendMessage(ChatColor.GREEN+iar.getMessage());
                }
                else {
                    player.sendMessage(ChatColor.RED+iar.getMessage());
                }
            }
        }
    }
}
