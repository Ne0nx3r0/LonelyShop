package com.ne0nx3r0.lonelyshop.inventory;

import com.ne0nx3r0.lonelyshop.LonelyShopPlugin;
import com.ne0nx3r0.lonelyshop.shop.LonelyShop;
import com.ne0nx3r0.util.ItemStackConvertor;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class InventoryManager {
    private LonelyShopPlugin plugin;
    
    private Connection con;
    private final Logger logger;
    
    private final String TBL_ACCOUNTS;
    private final String TBL_ITEMS;
    
    private final HashMap<String, Integer> PERMISSION_LIMIT_GROUPS;

    public InventoryManager(LonelyShopPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        
// setup seller limits
        this.PERMISSION_LIMIT_GROUPS = new HashMap<>();
        ConfigurationSection limitKeys = plugin.getConfig().getConfigurationSection("sell_limits");
        for(String sKey : limitKeys.getKeys(false)){
            this.PERMISSION_LIMIT_GROUPS.put(sKey, limitKeys.getInt(sKey));
        }

        ConfigurationSection dbConfig = plugin.getConfig().getConfigurationSection("database");
        
        String prefix = dbConfig.getString("prefix","");
        String hostname = dbConfig.getString("hostname","localhost");
        String port = dbConfig.getString("port","3306");
        String database = dbConfig.getString("database");
        String username = dbConfig.getString("username");
        String password = dbConfig.getString("password");
        
        this.TBL_ACCOUNTS = prefix+"player_accounts";
        this.TBL_ITEMS = prefix+"items";
                    
	try {
            Class.forName("com.mysql.jdbc.Driver");
	} 
        catch (ClassNotFoundException ex) {
            this.logger.log(Level.SEVERE, null, ex);
            
            this.logger.log(Level.SEVERE,"No MySQL JDBC driver found (that's bad)");
            
            return;
	}
        
	try {
            this.con = DriverManager.getConnection("jdbc:mysql://"+hostname+":"+port+"/"+database+"?autoReconnect=true",username,password);
	} 
        catch (SQLException ex) {
            this.logger.log(Level.SEVERE, null, ex);

            System.out.println("Database connection failed!");

            return;
	}
 
	if (this.con == null) {
            this.logger.log(Level.SEVERE,"Unable to connect to the database");
            
            return;
	}
        
        try {             
            ResultSet tableExistsResultSet = this.con.getMetaData().getTables(null, null, this.TBL_ITEMS, null);

            if(!tableExistsResultSet.next()) {
                this.con.setAutoCommit(false);

                // Generated from MySQL Workbench
                String createAccountsTableQuery = "CREATE TABLE IF NOT EXISTS ###TABLE_ACCOUNTS### (  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,  `uuid` VARCHAR(36) NOT NULL,  `username` VARCHAR(16) NOT NULL,  PRIMARY KEY (`id`),  UNIQUE INDEX `uuid_UNIQUE` (`uuid` ASC))ENGINE = InnoDB;";
                createAccountsTableQuery = createAccountsTableQuery.replaceAll("###TABLE_ACCOUNTS###", this.TBL_ACCOUNTS);

                // Generated from MySQL Workbench
                String createItemsTableQuery = "CREATE TABLE IF NOT EXISTS ###TABLE_ITEMS### (  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,  `seller_id` INT UNSIGNED NOT NULL,  `material` INT UNSIGNED NOT NULL,  `data` INT UNSIGNED NOT NULL,  `amount` INT UNSIGNED NOT NULL,  `item_data` TEXT NOT NULL,  `posted` DATETIME NOT NULL,  `price` DECIMAL(13,2) UNSIGNED NOT NULL,  `price_per_item` DECIMAL(13,2) UNSIGNED NOT NULL,  `buyer_id` INT UNSIGNED NULL,  `sold_at` DATETIME NULL,  `sold` BIT NOT NULL DEFAULT 0,  PRIMARY KEY (`id`),  INDEX `fk_items_player_account_idx` (`seller_id` ASC),  INDEX `material` (`material` ASC),  INDEX `fk_items_player_account1_idx` (`buyer_id` ASC),  CONSTRAINT `fk_items_player_account`    FOREIGN KEY (`seller_id`)    REFERENCES ###TABLE_ACCOUNTS### (`id`)    ON DELETE NO ACTION    ON UPDATE NO ACTION,  CONSTRAINT `fk_items_player_account1`    FOREIGN KEY (`buyer_id`)    REFERENCES ###TABLE_ACCOUNTS### (`id`)    ON DELETE NO ACTION    ON UPDATE NO ACTION)ENGINE = InnoDB;";
                createItemsTableQuery = createItemsTableQuery.replaceAll("###TABLE_ACCOUNTS###", this.TBL_ACCOUNTS);
                createItemsTableQuery = createItemsTableQuery.replaceAll("###TABLE_ITEMS###", this.TBL_ITEMS);
                
                PreparedStatement createAccountsTable = this.con.prepareStatement(createAccountsTableQuery);
                createAccountsTable.execute();
                
                PreparedStatement createItemsTable = this.con.prepareStatement(createItemsTableQuery);
                createItemsTable.execute();

                this.con.commit();
                
                this.con.setAutoCommit(true);
            }
        }
        catch (SQLException ex) {
            try {
                this.con.rollback();
            } 
            catch (SQLException ex1) {
                this.logger.log(Level.SEVERE, null, ex1);

                this.con = null;

                return;
            }

            this.logger.log(Level.SEVERE, null, ex);

            this.con = null;
        }
    }
    
    
    
    public synchronized PlayerInventoryAccount getPlayerAccount(Player player) {
        UUID uuid = player.getUniqueId();
        
        try {
            PreparedStatement getPlayerAccount = this.con.prepareStatement("SELECT id,username FROM "+this.TBL_ACCOUNTS+" WHERE uuid=? LIMIT 1;");
            
            getPlayerAccount.setString(1, uuid.toString());
            
            ResultSet result = getPlayerAccount.executeQuery();
            
            int dbID;
            String username;
            
            if(result.next()) {
                dbID = result.getInt("id");
                username = result.getString("username");
                
                if(!username.equals(player.getName())){
                    if(!this.updatePlayerAccountUsername(dbID,player.getName())){
                        player.sendMessage("Warning: was unable to update your username in the database. This may cause issues in delivering your funds.");
                    }
                }
                
                return new PlayerInventoryAccount(dbID,uuid,username);
            }
            else {
                PreparedStatement createPlayerAccount = this.con.prepareStatement("INSERT INTO "+this.TBL_ACCOUNTS+"(uuid,username) VALUES(?,?);");
                
                createPlayerAccount.setString(1, uuid.toString());
                createPlayerAccount.setString(2, player.getName());
                
                dbID = createPlayerAccount.executeUpdate();
                
                this.logger.log(Level.INFO, "account created for {0} uuid:{1} dbid:{2}", new Object[]{player.getName(), uuid.toString(), dbID});
                
                // I don't like the redundancy, but I can't seem to get the returned dbId to be correct using RETURN_GENERATED_KEYS...
                return this.getPlayerAccount(player);
            }
        }
        catch (SQLException ex) {
            Logger.getLogger(InventoryManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }

    public synchronized PlayerInventoryAccount getSellerAccount(ItemForSale ifs) {
        try {
            PreparedStatement getSellerAccount = this.con.prepareStatement("SELECT id,uuid,username FROM "+this.TBL_ACCOUNTS+" WHERE id=(SELECT seller_id FROM "+this.TBL_ITEMS+" WHERE id=? LIMIT 1) LIMIT 1;");
            getSellerAccount.setInt(1, ifs.getDbID());
            
            ResultSet result = getSellerAccount.executeQuery();
            
            if(result.next()){
                String username = result.getString("username");
                int dbID = result.getInt("id");
                UUID uuid = UUID.fromString(result.getString("uuid"));

                return new PlayerInventoryAccount(dbID,uuid,username);
            }
        }
        catch (SQLException ex) {
            Logger.getLogger(InventoryManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }

    private synchronized boolean updatePlayerAccountUsername(int dbID, String username) {
        try {
            PreparedStatement updateUsername = this.con.prepareStatement("UPDATE "+this.TBL_ACCOUNTS+" SET username=? WHERE id=? LIMIT 1;");
            updateUsername.setString(1, username);
            updateUsername.setInt(2, dbID);
            
            updateUsername.executeUpdate();
            
            this.logger.log(Level.INFO, "updated username for {0} dbid:{1}", new Object[]{username, dbID});
        } 
        catch (SQLException ex) {
            Logger.getLogger(InventoryManager.class.getName()).log(Level.SEVERE, null, ex);
            
            return false;
        }
        return true;
    }
    
    public synchronized InventoryActionResponse putItemForSale(Player player, ItemStack inHand, double price) {
        PlayerInventoryAccount sellerAccount = this.getPlayerAccount(player);
        
        Iterator iterator = this.PERMISSION_LIMIT_GROUPS.entrySet().iterator();
        int maxItems = 0;
        
        while(iterator.hasNext()) {
            Entry<String,Integer> next = (Entry<String,Integer>) iterator.next();
                    
            if(player.hasPermission("lonelyshop.sell.max."+next.getKey())) {
                if(next.getValue() > maxItems){
                    maxItems = next.getValue();
                }
            }
        }
        
        try {
            PreparedStatement getPlayerItemsCount = this.con.prepareStatement("SELECT COUNT(*) as playerItems FROM "+this.TBL_ITEMS+" WHERE sold = 0 AND seller_id = ?");
            
            getPlayerItemsCount.setInt(1, sellerAccount.getdbId());
            
            ResultSet result = getPlayerItemsCount.executeQuery();
            
            if(result.next()) {
                int playerItemsCount = result.getInt("playerItems");
                
                if(playerItemsCount >= maxItems) {
                    return new InventoryActionResponse(null,false,"You cannot put more than "+playerItemsCount+" items for sale at your rank!");
                }
            }
            
        } 
        catch (SQLException ex) {
            Logger.getLogger(InventoryManager.class.getName()).log(Level.SEVERE, null, ex);
            
            return new InventoryActionResponse(null,false,"A database error occurred!");
        }
        
        try {
            PreparedStatement createPlayerItem = this.con.prepareStatement("INSERT INTO "+this.TBL_ITEMS
                    +"(seller_id,material,data,amount,item_data,posted,price,price_per_item) "
                    +"VALUES(?,?,?,?,?,?,?,?);");
            
            createPlayerItem.setInt(1, sellerAccount.getdbId());
            createPlayerItem.setInt(2, inHand.getTypeId());
            createPlayerItem.setInt(3, inHand.getData().getData());
            createPlayerItem.setInt(4, inHand.getAmount());
            createPlayerItem.setString(5, ItemStackConvertor.fromItemStack(inHand));
            createPlayerItem.setTimestamp(6, new java.sql.Timestamp(new java.util.Date().getTime()));
            createPlayerItem.setDouble(7, price);
            createPlayerItem.setDouble(8, price / inHand.getAmount());
            
            createPlayerItem.executeUpdate();

            this.logger.log(Level.INFO, "is:{0} put for sale by {1}({2}) for {3}", 
                    new Object[]{inHand, sellerAccount.getUsername(),sellerAccount.getUUID(), price});
            
        } catch (SQLException ex) {
            Logger.getLogger(InventoryManager.class.getName()).log(Level.SEVERE, null, ex);
            
            return new InventoryActionResponse(null,false,"A database error occurred!");
        }

        return new InventoryActionResponse(inHand,true,"Item successfully put for sale!");
    }
    
    public ArrayList<ItemForSale> getItemsForSale(int page) {
        return this.getItemsForSale("",page);
    }
    
    public ArrayList<ItemForSale> getItemsForSale(Material material,int page) {
        return this.getItemsForSale("AND material = "+material.getId(),page);
    }
    
    public ArrayList<ItemForSale> getItemsForSale(Material material,byte data,int page) {
        return this.getItemsForSale("AND material = "+material.getId()+" AND data = "+data,page);
    }

    public synchronized ArrayList<ItemForSale> getSellerItems(Player player,int page) {
        PlayerInventoryAccount pia = this.getPlayerAccount(player);
        
        if(pia == null) {
            return new ArrayList<>();
        }
        
        return this.getItemsForSale("AND seller_id = "+pia.getdbId(),page);
    }
    
    private ArrayList<ItemForSale> getItemsForSale(String queryWhere,int page) {
        ArrayList<ItemForSale> items = new ArrayList<>();
        
        try {
            PreparedStatement getItemsForSale = this.con.prepareStatement("SELECT id,amount,item_data,posted,price,price_per_item FROM "+this.TBL_ITEMS+" WHERE sold = 0 "+queryWhere+" ORDER BY price_per_item,posted LIMIT "+(LonelyShop.SHOP_ITEM_SLOTS*page-9)+","+(LonelyShop.SHOP_ITEM_SLOTS-9));            
            ResultSet result = getItemsForSale.executeQuery();
            
            while(result.next()) {
                int id = result.getInt("id");
                double price = result.getDouble("price");
                double pricePerItem = result.getDouble("price_per_item");
                Date postedDate = result.getTimestamp("posted");
                
                String sItemData = result.getString("item_data");
                ItemStack is = ItemStackConvertor.fromString(sItemData);
                
                items.add(new ItemForSale(id,is,price,pricePerItem,postedDate));
            }
        } 
        catch (SQLException ex) {
            Logger.getLogger(InventoryManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return items;
    }

    public synchronized ItemForSale getItemForSale(int itemId) {
        try {
            PreparedStatement getItemsForSale = this.con.prepareStatement("SELECT amount,item_data,posted,price,price_per_item FROM "+this.TBL_ITEMS+" WHERE sold = 0 AND id = ?");
            getItemsForSale.setInt(1, itemId);
            
            ResultSet result = getItemsForSale.executeQuery();
            
            if(result.next()) {
                double price = result.getDouble("price");
                double pricePerItem = result.getDouble("price_per_item");
                Date postedDate = result.getTimestamp("posted");
                
                String sItemData = result.getString("item_data");
                ItemStack is = ItemStackConvertor.fromString(sItemData);
                
                return new ItemForSale(itemId,is,price,pricePerItem,postedDate);
            }
        } 
        catch (SQLException ex) {
            Logger.getLogger(InventoryManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }

    public synchronized InventoryActionResponse attemptToBuy(Player player, int itemId) {
        PlayerInventoryAccount buyerInventoryAccount = this.getPlayerAccount(player);
        
        if(buyerInventoryAccount == null) {
            return new InventoryActionResponse(null,false,"Unable to query database for your info!");
        }
        
        ItemForSale ifs = this.getItemForSale(itemId);
        
        if(ifs == null) {
            return new InventoryActionResponse(null,false,"Invalid item id! (The item may have already been sold)");
        }
        
        double price = ifs.getPrice();
        
        if(!plugin.economy.has(player.getName(), price)) {
            return new InventoryActionResponse(null,false,"You don't have "+plugin.economy.format(price));
        }
        
        PlayerInventoryAccount sellerInventoryAccount = this.getSellerAccount(ifs);
        
        if(sellerInventoryAccount == null) {
            return new InventoryActionResponse(null,false,"Unable to query database for seller info!");
        }
        
        EconomyResponse buyerWithdrawlResponse = plugin.economy.withdrawPlayer(player.getName(), price);
        
        if(buyerWithdrawlResponse.type.equals(ResponseType.SUCCESS)){
            try {
                PreparedStatement markAsSold = this.con.prepareStatement("UPDATE "+this.TBL_ITEMS+" SET sold=1,buyer_id=?,sold_at=? WHERE id = ? LIMIT 1;");
                markAsSold.setInt(1, buyerInventoryAccount.getdbId());
                markAsSold.setTimestamp(2, new java.sql.Timestamp(new java.util.Date().getTime()));
                markAsSold.setInt(3, itemId);

                markAsSold.executeUpdate();

                this.logger.log(Level.INFO, "set item id:{0} is:{1} sold to {2}({3}) for {4}", 
                        new Object[]{itemId, ifs.getItemStack(), buyerInventoryAccount.getUsername(), buyerInventoryAccount.getUUID(), price});
            
                EconomyResponse sellerDepositResponse = plugin.economy.depositPlayer(sellerInventoryAccount.getUsername(), price);

                if(sellerDepositResponse.type.equals(ResponseType.SUCCESS)) {
                    Player seller = Bukkit.getPlayer(sellerInventoryAccount.getUUID());
                    ItemStack itemSold = ifs.getItemStack();
                    
                    // message if online, mail otherwise
                    if(seller != null){
                        seller.sendMessage("LonelyShop: You sold "+itemSold.getAmount()+" "+itemSold.getType()+" for "+plugin.economy.format(price));
                    }
                    else {
                        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "mail "+sellerInventoryAccount.getUsername()+" You sold "+ifs.getItemStack().getType()+" for "+plugin.economy.format(price));
                    }
                }
                // really shouldn't ever happen... Even with LonelyEconomy the money was just given to the server to hand to the player
                else {
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "mail ne0nx3r0 that scary error happened at "+new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date())+" with "+ifs.getItemStack().getType()+" for "+plugin.economy.format(price));
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "mail "+sellerInventoryAccount.getUsername()+" an error occurred selling "+ifs.getItemStack().getType()+" for "+plugin.economy.format(price)+", sorry but you did not receive the money for it. Ne0nx3r0 should have been notified.");
                }
            } 
            catch (SQLException ex) {
                this.logger.log(Level.SEVERE, null, ex);

                player.sendMessage(ChatColor.RED+"Something bad happened and you really should mention it.");
            }
            
            return new InventoryActionResponse(ifs.getItemStack(),true,"You bought "+ifs.getItemStack().getType().name()+" for "+plugin.economy.format(price)+"!");
        }

        return new InventoryActionResponse(null,false,buyerWithdrawlResponse.errorMessage);
    }

    public synchronized InventoryActionResponse attemptToHandBackToSeller(Player player, int itemId) {
        ItemForSale ifs = this.getItemForSale(itemId);
        
        if(ifs == null) {
            return new InventoryActionResponse(null,false,"Invalid item id! (The item may have already been sold)");
        }
        
        // double check the player is this player
        PlayerInventoryAccount sellerAccount = this.getSellerAccount(ifs);
        
        if(sellerAccount == null){
            return new InventoryActionResponse(null,false,"Invalid seller account... Awhaaa?");
        }
        
        if(!sellerAccount.getUUID().equals(player.getUniqueId())){
            return new InventoryActionResponse(null,false,"It seems like you aren't the seller of this item.");
        }
        
        try {
            PreparedStatement markAsSold = this.con.prepareStatement("UPDATE "+this.TBL_ITEMS+" SET sold=1,buyer_id=?,sold_at=? WHERE id = ? LIMIT 1;");
            markAsSold.setInt(1, sellerAccount.getdbId());
            markAsSold.setTimestamp(2, new java.sql.Timestamp(new java.util.Date().getTime()));
            markAsSold.setInt(3, itemId);

            markAsSold.executeUpdate();

            this.logger.log(Level.INFO, "set item id:{0} is:{1} given back to seller {3} {4}", 
                    new Object[]{itemId, ifs.getItemStack(), sellerAccount.getUsername(), sellerAccount.getUUID()});
        }
        catch(Exception ex){
            this.logger.log(Level.SEVERE, null, ex);
                
            return new InventoryActionResponse(null,false,"Unable to remove the for sale item");
        }
        
        return new InventoryActionResponse(ifs.getItemStack(),true,"You retrieved your "+ifs.getItemStack().getType().name()+" from your shop!");
    }
}
