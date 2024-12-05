package com.spearforge.sBank.guis;

import com.spearforge.sBank.SBank;
import com.spearforge.sBank.model.Bank;
import com.spearforge.sBank.utils.TextUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;


public class BankGUI {


    public static Inventory getBankHomePage(Player player){
        Bank pBank = SBank.getBanks().get(player.getName());
        Inventory bankGUI = Bukkit.createInventory(player, 27, ChatColor.translateAlternateColorCodes('&', SBank.getPlugin().getConfig().getString("gui.bank-home-title")));

        if (pBank != null){
            bankGUI.setItem(11, createButton("gui.deposit-button", pBank));
            bankGUI.setItem(15, createButton("gui.withdraw-button", pBank));
            bankGUI.setItem(8, createButton("gui.bank-set-name", pBank));
            bankGUI.setItem(13, createButton("gui.bank-details", pBank));
            if (SBank.getPlugin().getConfig().getBoolean("loan.enabled")){
                bankGUI.setItem(0, createButton("gui.loan.loan-gui", pBank));
            }
            bankGUI.setItem(26, createButton("gui.close-button", pBank));

        } else {
            player.sendMessage(SBank.getPlugin().getConfig().getString("messages.no-bank-account"));
        }
        return bankGUI;
    }


    public static ItemStack createButton(String configPath, Bank pBank) {
        ItemStack button = new ItemStack(Material.valueOf(SBank.getGuiConfig().getString(configPath + ".material")));
        ItemMeta meta = button.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', SBank.getGuiConfig().getString(configPath + ".name")));
        meta.setLore(TextUtils.replacePlaceholders(SBank.getGuiConfig().getStringList(configPath + ".lore"), pBank, null));
        button.setItemMeta(meta);
        return button;
    }



}
