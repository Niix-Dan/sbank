package com.spearforge.sBank.utils;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

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

        return df.format(balance);
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


}
