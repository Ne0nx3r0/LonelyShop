package com.ne0nx3r0.lonelyshop.inventory;

import com.ne0nx3r0.lonelyshop.LonelyShopPlugin;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class InventoryManager {
    private Connection con;
    private final Logger logger;
    
    private final String TBL_ACCOUNTS;
    private final String TBL_ITEMS;
    private LonelyShopPlugin plugin;

    public InventoryManager(LonelyShopPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        
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
            this.con = DriverManager.getConnection("jdbc:mysql://"+hostname+":"+port+"/"+database,username,password);
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

                PreparedStatement createAccountsTable = this.con.prepareStatement("CREATE TABLE IF NOT EXISTS \"+this.TBL_ACCOUNTS+\" (  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,  `uuid` VARCHAR(36) NOT NULL,  `username` VARCHAR(16) NOT NULL,  PRIMARY KEY (`id`),  UNIQUE INDEX `uuid_UNIQUE` (`uuid` ASC))ENGINE = InnoDB;");
                createAccountsTable.execute();
                
                PreparedStatement createItemsTable = this.con.prepareStatement("CREATE TABLE IF NOT EXISTS \"+this.TBL_ITEMS+\" (  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,  `player_account` INT UNSIGNED NOT NULL,  `material` INT UNSIGNED NOT NULL,  `data` INT UNSIGNED NOT NULL,  `amount` INT UNSIGNED NOT NULL,  `item_data` TEXT NOT NULL,  `posted` DATETIME NOT NULL,  `price` DECIMAL(13,2) UNSIGNED NOT NULL,  `price_per_item` DECIMAL(13,2) UNSIGNED NOT NULL,  PRIMARY KEY (`id`),  INDEX `fk_items_player_account_idx` (`player_account` ASC),  INDEX `material` (`material` ASC),  CONSTRAINT `fk_items_player_account`    FOREIGN KEY (`player_account`)    REFERENCES \"+this.TBL_ACCOUNTS+\" (`id`)    ON DELETE NO ACTION    ON UPDATE NO ACTION)ENGINE = InnoDB;");
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
    
    public PlayerInventoryAccount getPlayerAccount(Player player) {
        UUID uuid = player.getUniqueId();
        
        try {
            PreparedStatement getPlayerAccount = this.con.prepareStatement("SELECT id FROM "+this.TBL_ACCOUNTS+" WHERE uuid=?");
            
            getPlayerAccount.setString(1, uuid.toString());
            
            ResultSet result = getPlayerAccount.executeQuery();
            
            int dbID;
            
            if(result.next()) {
                dbID = result.getInt("id");
            }
            else {
                PreparedStatement createPlayerAccount = this.con.prepareStatement("INSERT INTO "+this.TBL_ACCOUNTS+"(uuid) VALUES(?);",
                    Statement.RETURN_GENERATED_KEYS);
                
                createPlayerAccount.setString(1, uuid.toString());
                
                dbID = createPlayerAccount.executeUpdate();
            }
            
            return new PlayerInventoryAccount(dbID,uuid);
        }
        catch (SQLException ex) {
            Logger.getLogger(InventoryManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
    
    public boolean putItemForSale(Player player, ItemStack inHand, double price) {
        PlayerInventoryAccount pia = this.getPlayerAccount(player);
        
        try {
            PreparedStatement createPlayerItem = this.con.prepareStatement("INSERT INTO "+this.TBL_ITEMS
                    +"(player_account,seller_uuid,material,amount,item_data,posted,price,price_per_item) "
                    +"VALUES(?,?,?,?,?,?,?,?);");
            
            createPlayerItem.setInt(1, pia.getdbId());
            createPlayerItem.setString(2, player.getUniqueId().toString());
            createPlayerItem.setInt(3, inHand.getTypeId());
            createPlayerItem.setInt(4, inHand.getAmount());
            createPlayerItem.setString(5, this.stringFromItemStack(inHand));
            createPlayerItem.setTimestamp(6, new java.sql.Timestamp(new java.util.Date().getTime()));
            createPlayerItem.setDouble(7, price);
            createPlayerItem.setDouble(8, price / inHand.getAmount());
            
            createPlayerItem.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(InventoryManager.class.getName()).log(Level.SEVERE, null, ex);
            
            return false;
        }

        return true;
    }

    public ArrayList<ItemForSale> getItemsForSale(Material material) {
        ArrayList<ItemForSale> items = new ArrayList<>();
        
        try {
            PreparedStatement getItemsForSale = this.con.prepareStatement("SELECT id,amount,item_data,posted,price,price_per_item FROM "+this.TBL_ITEMS+" WHERE material = ? ORDER BY price_per_item,posted LIMIT "+plugin.shopsManager.getMaxShopSlots());
            getItemsForSale.setInt(1, material.getId());
            
            ResultSet result = getItemsForSale.executeQuery();
            
            while(result.next()) {
                int id = result.getInt("id");
                double price = result.getDouble("price");
                double pricePerItem = result.getDouble("price_per_item");
                Date postedDate = result.getTimestamp("posted");
                
                String sItemData = result.getString("item_data");
                ItemStack is = this.itemStackfromString(sItemData);
                
                items.add(new ItemForSale(id,is,price,pricePerItem,postedDate));
            }
        } 
        catch (SQLException ex) {
            Logger.getLogger(InventoryManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return items;
    }

    public ItemForSale getItemForSale(int itemId) {
        try {
            PreparedStatement getItemsForSale = this.con.prepareStatement("SELECT amount,item_data,posted,price,price_per_item FROM "+this.TBL_ITEMS+" WHERE id = ?");
            getItemsForSale.setInt(1, itemId);
            
            ResultSet result = getItemsForSale.executeQuery();
            
            if(result.next()) {
                double price = result.getDouble("price");
                double pricePerItem = result.getDouble("price_per_item");
                Date postedDate = result.getTimestamp("posted");
                
                String sItemData = result.getString("item_data");
                ItemStack is = this.itemStackfromString(sItemData);
                
                return new ItemForSale(itemId,is,price,pricePerItem,postedDate);
            }
        } 
        catch (SQLException ex) {
            Logger.getLogger(InventoryManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
    
    public InventoryActionResponse attemptToBuy(Player player, int itemId) {
        ItemForSale ifs = this.getItemForSale(itemId);
        
        if(ifs == null) {
            return new InventoryActionResponse(null,false,"Invalid item id! (The item may have already been sold)");
        }
        
        double price = ifs.getPrice();
        
        if(!plugin.economy.has(player.getName(), price)) {
            return new InventoryActionResponse(null,false,"You don't have "+plugin.economy.format(price));
        }
        
        EconomyResponse buyerWithdrawlResponse = plugin.economy.withdrawPlayer(player.getName(), price);
        
        if(buyerWithdrawlResponse.type.equals(ResponseType.SUCCESS)){
            try {
                PreparedStatement removeItemStatement = this.con.prepareStatement("DELETE FROM "+this.TBL_ITEMS+" WHERE id = ? LIMIT 1;");
                removeItemStatement.setInt(1, itemId);

                removeItemStatement.executeUpdate();

                Player pSeller = Bukkit.getPlayer(ifs.getSellerUUID());

                if(pSeller == null) {
                    your face is a blocking operation that can't be run on the main thread so we need to either store the seller name or notify them some other way
                    pSeller = Bukkit.getOfflinePlayer(ifs.getSellerUUID()).getPlayer();
                }
                
                EconomyResponse sellerDepositResponse = plugin.economy.depositPlayer(pSeller.getName(), price);

                if(sellerDepositResponse.type.equals(ResponseType.SUCCESS)) {
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "mail "+pSeller.getName()+" You sold "+ifs.getItemStack().getType()+" for "+plugin.economy.format(price));
                }
                // really shouldn't ever happen... Even with LonelyEconomy the money was just given to the server to hand to the player
                else {
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "mail ne0nx3r0 that scary error happened at "+new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date())+" with "+ifs.getItemStack().getType()+" for "+plugin.economy.format(price));
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "mail "+pSeller.getName()+" an error occurred selling "+ifs.getItemStack().getType()+" for "+plugin.economy.format(price)+", sorry but you did not receive the money for it. Ne0nx3r0 should have been notified.");
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
    
    // helper
    // TODO: Put into util class
    public String stringFromItemStack(ItemStack is) {
        StringBuilder f = new StringBuilder();
        f.append("type=" + is.getType() + ";");
        if (is.getDurability() != 0)
           f.append("dura=" + is.getDurability() + ";");
        f.append("amount=" + is.getAmount() + ";");
        if (!is.getEnchantments().isEmpty()) {
           f.append("enchantments=");
           int in = 1;
           for (Entry<Enchantment, Integer> key : is.getEnchantments().entrySet()) {
              f.append(key.getKey().getName() + ":" + key.getValue());
              if (in != is.getEnchantments().size()) {
                 f.append("&");
              }
              in++;
           }
           f.append(";");
        }
        if (is.hasItemMeta()) {
           ItemMeta m = is.getItemMeta();
           if (m.hasDisplayName()) {
              f.append("name=").append(m.getDisplayName()).append(";");
           }
           if (m instanceof LeatherArmorMeta) {
              LeatherArmorMeta me = (LeatherArmorMeta) m;
              int r = me.getColor().getRed();
              int g = me.getColor().getGreen();
              int b = me.getColor().getBlue();
              f.append("rgb=").append(r).append(",").append(g).append(",").append(b);
           }
           if (m.hasLore()) {
              f.append("lore=");
              StringBuilder lore = new StringBuilder();
              for (String s : m.getLore()) {
                 lore.append("line:").append(s);
              }
              f.append(lore.toString().replaceFirst("line:", ""));
           }
           if (m instanceof SkullMeta) {
              SkullMeta me = (SkullMeta) m;
              if (me.hasOwner())
                 f.append("owner=").append(me.getOwner());
           }
        }
        return f.toString();
    }
    
    public ItemStack itemStackfromString(String s) {
      ItemStack i;
      Material type = Material.AIR;
      short dura = 0;
      int amount = 1;
      Map<Enchantment, Integer> enchants = new HashMap<Enchantment, Integer>();
      String cName = null;
      String[] rgb = null;
      List<String> lore = new ArrayList<String>();
      String owner = null;
      for (String d : s.split(";")) {
         String[] id = d.split("=");
         if (id[0].equalsIgnoreCase("type")) {
            type = Material.getMaterial(id[1]);
         } else if (id[0].equalsIgnoreCase("dura")) {
            dura = Short.parseShort(id[1]);
         } else if (id[0].equalsIgnoreCase("amount")) {
            amount = Integer.parseInt(id[1]);
         } else if (id[0].equalsIgnoreCase("enchantments")) {
            for (String en : id[1].split("&")) {
               String[] ench = en.split(":");
               enchants.put(Enchantment.getByName(ench[0]), Integer.parseInt(ench[1]));
            }
         } else if (id[0].equalsIgnoreCase("name")) {
            cName = id[1];
         } else if (id[0].equalsIgnoreCase("rgb")) {
            rgb = id[1].split(",");
         } else if (id[0].equalsIgnoreCase("lore")) {
            lore = Arrays.asList(id[1].split("line:"));
         } else if (id[0].equalsIgnoreCase("owner")) {
            owner = id[1];
         }
      }
      i = new ItemStack(type, amount);
      if (dura != 0) {
         i.setDurability(dura);
      }
      ItemMeta m = i.getItemMeta();
      if (cName != null)
         m.setDisplayName(cName);
      if (rgb != null)
         ((LeatherArmorMeta) m).setColor(Color.fromRGB(Integer.parseInt(rgb[0]), Integer.parseInt(rgb[1]), Integer.parseInt(rgb[2])));
      if (!lore.isEmpty())
         m.setLore(lore);
      if (owner != null)
         ((SkullMeta) m).setOwner(owner);
      i.setItemMeta(m);
      i.addUnsafeEnchantments(enchants);
      return i;
    }
}
