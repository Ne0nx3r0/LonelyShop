package com.ne0nx3r0.lonelyshop.inventory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import org.bukkit.inventory.ItemStack;

public class ItemForSale {
    private final ItemStack is;
    private final double price;
    private final double pricePerItem;
    private final Date postedDate;
    private final int DbID;
    
    ItemForSale(int DbID,ItemStack is, double price, double pricePerItem, Date postedDate) {
        this.DbID = DbID;
        this.is = is;
        this.price = price;
        this.pricePerItem = pricePerItem;
        this.postedDate = postedDate;
    }
 
    public ItemStack getItemStack() {
        return this.is;
    }
    
    public double getPrice(){
        return this.price;
    }
    
    public double getPricePerItem(){
        return this.pricePerItem;
    }
    
    public String getDisplayPostedDate(){
	return new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(this.postedDate);
    }

    public int getDbID() {
        return this.DbID;
    }
}
