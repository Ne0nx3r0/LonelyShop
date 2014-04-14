package com.ne0nx3r0.lonelyshop.inventory;

import java.util.UUID;

public class PlayerInventoryAccount {
    private final int dbId;
    private final UUID uuid;
    
    public PlayerInventoryAccount(int dbId,UUID uuid){
        this.dbId = dbId;
        this.uuid = uuid;
    }
    
    public int getdbId() {
        return this.dbId;
    }
    
    public UUID getUUID() {
        return this.uuid;
    }
}
