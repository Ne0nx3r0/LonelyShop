package com.ne0nx3r0.lonelyshop.inventory;

import java.util.Date;
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
    
    public Date getPostedDate(){
	return this.postedDate;
    }

    public int getDbID() {
        return this.DbID;
    }
}
