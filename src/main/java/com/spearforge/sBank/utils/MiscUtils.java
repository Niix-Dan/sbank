package com.spearforge.sBank.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.spearforge.sBank.SBank;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MiscUtils {
    
    public static double getInterest(Player player, double _interest) {
        Pattern pattern = Pattern.compile("^sbank\\.interest\\.(\\d+)$"); // sbank.interest.<percent>

        return player.getEffectivePermissions().stream()
            .map(PermissionAttachmentInfo::getPermission)
            .map(pattern::matcher)
            .filter(Matcher::matches) 
            .mapToInt(matcher -> Integer.parseInt(matcher.group(1)))
            .max()
            .orElse((int) _interest);
    }


    public static String formatBalance(double balance) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator(',');
        symbols.setGroupingSeparator('.');

        DecimalFormat df = new DecimalFormat("#,##0.00", symbols);

        return df.format(balance) + SBank.getPlugin().getConfig().getString("currency-symbol");
    }



    public static boolean isInteger(String s, int radix) {
        if(s.isEmpty()) return false;
        for(int i = 0; i < s.length(); i++) {
            if(i == 0 && s.charAt(i) == '-') {
                if(s.length() == 1) return false;
                else continue;
            }
            if(Character.digit(s.charAt(i),radix) < 0) return false;
        }
        return true;
    }

    public static boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) return false;
        try {
            double value = Double.parseDouble(str);
            return value >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static ItemStack getCustomHead(String base64) {

        ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
        if (base64 == null || base64.isEmpty()) {
            return head; //
        }

        SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().put("textures", new Property("textures", base64));

        try {
            Field profileField = headMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(headMeta, profile);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        head.setItemMeta(headMeta);
        return head;
    }

    public static ItemStack getMaterialOrHead(String configPath){
        try {
            return new ItemStack(Material.valueOf(SBank.getGuiConfig().getString(configPath + ".material")));
        } catch (IllegalArgumentException e) {
            return getCustomHead(SBank.getGuiConfig().getString(configPath + ".material"));
        }
    }

}
