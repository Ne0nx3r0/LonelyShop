package com.ne0nx3r0.lonelyshop.inventory;

import org.bukkit.inventory.ItemStack;

public class InventoryActionResponse {
    private final boolean successful;
    private final String message;
    private final ItemStack is;
    
    public InventoryActionResponse(ItemStack is,boolean successful,String message) {
        this.is = is;
        this.successful = successful;
        this.message = message;
    }

    public ItemStack getItemStack() {
        return this.is;
    }
    
    public String getMessage() {
        return this.message;
    }

    public boolean wasSuccessful() {
        return this.successful;
    }
}
