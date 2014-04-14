package com.ne0nx3r0.lonelyshop.inventory;

import java.util.UUID;

public class PlayerInventoryAccount {
    private final int dbId;
    private final UUID uuid;
    private final String username;
    
    public PlayerInventoryAccount(int dbId,UUID uuid,String username){
        this.dbId = dbId;
        this.uuid = uuid;
        this.username = username;
    }
    
    public int getdbId() {
        return this.dbId;
    }
    
    public UUID getUUID() {
        return this.uuid;
    }

    public String getUsername() {
        return this.username;
    }
}
