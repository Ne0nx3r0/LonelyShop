// This snippet is borrowed, but I couldn't find the source to credit when I googled bits of the code, so I am sorry anonymous donor.

package com.ne0nx3r0.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class ItemStackConvertor {
    public static String fromItemStack(ItemStack is) {
        StringBuilder f = new StringBuilder();
        f.append("type=" + is.getType() + ";");
        if (is.getDurability() != 0)
           f.append("dura=" + is.getDurability() + ";");
        f.append("amount=" + is.getAmount() + ";");
        
        if (!is.getEnchantments().isEmpty()) {
           f.append("enchantments=");
           int in = 1;
           for (Map.Entry<Enchantment, Integer> key : is.getEnchantments().entrySet()) {
              f.append(key.getKey().getName() + ":" + key.getValue());
              if (in != is.getEnchantments().size()) {
                 f.append("&");
              }
              in++;
           }
           f.append(";");
        }
        else if(is.getType().equals(Material.ENCHANTED_BOOK)) {
            EnchantmentStorageMeta bookmeta = (EnchantmentStorageMeta) is.getItemMeta();
            
            int in = 1;
            
            for (Entry<Enchantment, Integer> enchantment : bookmeta.getStoredEnchants().entrySet()) {
                if(in > 1){
                    f.append("&");
                }
                
                f.append(enchantment.getKey().getName()+":"+enchantment.getValue());
                
                in++;
            }
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
    
    public static ItemStack fromString(String s) {
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
