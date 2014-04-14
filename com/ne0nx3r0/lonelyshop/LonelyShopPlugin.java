package com.ne0nx3r0.lonelyshop;

import com.ne0nx3r0.lonelyshop.commands.LonelyShopCommandExecutor;
import com.ne0nx3r0.lonelyshop.shop.ShopsManager;
import com.ne0nx3r0.lonelyshop.inventory.InventoryManager;
import com.ne0nx3r0.lonelyshop.listeners.ShopListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class LonelyShopPlugin extends JavaPlugin {
    public InventoryManager inventoryManager;
    public Economy economy;
    public ShopsManager shopsManager;
    
    @Override
    public void onEnable() {
        try {
            getDataFolder().mkdirs();

            File configFile = new File(getDataFolder(),"config.yml");

            if(!configFile.exists())
            {
                copy(getResource("config.yml"), configFile);
            }
        } 
        catch (IOException ex) {
            this.getLogger().log(Level.INFO, "Unable to load config! Disabling...");
            
            return;
        }
        
        if(!this.setupEconomy()) {
            this.getLogger().log(Level.SEVERE, "Unable to hook into Vault/Economy! Disabling...");
            
            return;
        }
                
        this.inventoryManager = new InventoryManager(this);
        
        this.shopsManager = new ShopsManager(this);
        
        this.getCommand("ls").setExecutor(new LonelyShopCommandExecutor(this));
 
        this.getServer().getPluginManager().registerEvents(new ShopListener(this), this);
    }
    
    @Override
    public void onDisable() {
        this.shopsManager.closeAllShops();
    }
    
    private boolean setupEconomy()
    {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            this.economy = economyProvider.getProvider();
        }

        return (this.economy != null);
    }

    public void copy(InputStream in, File file) throws IOException
    {
        OutputStream out = new FileOutputStream(file);
        byte[] buf = new byte[1024];
        int len;
        while((len=in.read(buf))>0)
        {
            out.write(buf,0,len);
        }
        out.close();
        in.close();
    }
}
