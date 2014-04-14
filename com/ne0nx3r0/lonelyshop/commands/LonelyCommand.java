package com.ne0nx3r0.lonelyshop.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class LonelyCommand {
    private final String name;
    private final String usageArguments;
    private final String action;
    private final String permissionNode;
    
    public LonelyCommand(String name,String usageArguments,String action,String permissionNode) {
        this.name = name;
        this.usageArguments = usageArguments;
        this.action = action;
        this.permissionNode = permissionNode;
    }
    
    public String getPermissionNode() {
        return this.permissionNode;
    }
    
    public String getName() {
        return this.name;
    }

    boolean execute(CommandSender cs, String[] args) {
        return false;
    }

    public String getUsage() {
        return "/ls "+this.name+" "+this.usageArguments;
    }

    void send(CommandSender cs, String...lines) {
        cs.sendMessage(ChatColor.GRAY+"--- "+ChatColor.GOLD+this.action+ChatColor.GRAY+" ---");
        
        for(String line : lines) {
            cs.sendMessage(line);
        }
        
        cs.sendMessage("");
    }

    void sendError(CommandSender cs, String msg) {
        this.send(cs,ChatColor.RED+msg);
    }

    String getAction() {
        return this.action;
    }
}