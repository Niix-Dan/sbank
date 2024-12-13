package com.spearforge.sBank.listener;

import com.spearforge.sBank.SBank;
import com.spearforge.sBank.model.Bank;
import com.spearforge.sBank.model.MoneyPile;
import com.spearforge.sBank.utils.MiscUtils;
import com.spearforge.sBank.utils.TextUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class MoneyPileListener implements Listener {

    @EventHandler
    public void onMoneyPileClick(PlayerInteractEvent e){
        Player player = e.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item == null || !item.hasItemMeta()){
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null || meta.getDisplayName() == null || !meta.getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', SBank.getPlugin().getConfig().getString("physical-money.item.name")))){
            return;
        }

        List<String> lore = SBank.getPlugin().getConfig().getStringList("physical-money.item.lore");
        if (lore == null || lore.isEmpty()){
            return;
        }

        double amount = MiscUtils.extractMoney(meta.getLore());
        if (amount == -100.0){
            TextUtils.sendMessageWithPrefix(player, SBank.getPlugin().getConfig().getString("messages.invalid-money-pile"));
            return;
        }

        player.getInventory().remove(item);
        Bank pBank = SBank.getBanks().get(player.getName());
        pBank.setBalance(pBank.getBalance() + amount);
        TextUtils.sendMessageWithPrefix(player, SBank.getPlugin().getConfig().getString("messages.money-pile-redeemed").replaceAll("%money_pile%", MiscUtils.formatBalance(amount)));


    }

}
