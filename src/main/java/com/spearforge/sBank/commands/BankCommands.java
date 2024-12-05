package com.spearforge.sBank.commands;

import com.spearforge.sBank.SBank;
import com.spearforge.sBank.guis.BankGUI;
import com.spearforge.sBank.guis.DebtGui;
import com.spearforge.sBank.utils.TextUtils;
import lombok.SneakyThrows;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;


public class BankCommands implements CommandExecutor {

    @SneakyThrows
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender instanceof Player){
            Player player = (Player) sender;
            if (player.hasPermission("sbank.use") || player.hasPermission("sbank.admin")){
                if (args.length == 0){
                    if (SBank.getPlugin().getConfig().getBoolean("npc-bankers.enabled") && !player.hasPermission(SBank.getPlugin().getConfig().getString("npc-bankers.bypass-permission"))){
                        TextUtils.sendMessageWithPrefix(player, SBank.getPlugin().getConfig().getString("npc-bankers.command-message"));
                    } else {
                        Inventory inventory = BankGUI.getBankHomePage((Player) sender);
                        player.openInventory(inventory);
                    }
                } else if (args.length == 1){
                    if (args[0].equalsIgnoreCase("reload")){
                        if (player.hasPermission("sbank.admin")){
                            SBank.getPlugin().reloadConfig();
                            SBank.getGuiConfig().reloadConfig();
                            TextUtils.sendMessageWithPrefix(player, SBank.getPlugin().getConfig().getString("messages.plugin-reloaded"));
                        } else {
                            TextUtils.sendMessageWithPrefix(player, SBank.getPlugin().getConfig().getString("messages.no-permission"));
                        }
                    } else if (args[0].equalsIgnoreCase("debt")){
                        if (SBank.getPlugin().getConfig().getBoolean("npc-bankers.enabled") && !player.hasPermission(SBank.getPlugin().getConfig().getString("npc-bankers.bypass-permission"))){
                            TextUtils.sendMessageWithPrefix(player, SBank.getPlugin().getConfig().getString("npc-bankers.command-message"));
                        } else {
                            if (player.hasPermission("sbank.loan")) {
                                if (SBank.getPlugin().getConfig().getBoolean("loan.enabled")) {
                                    player.openInventory(DebtGui.openDebtPage(player));
                                } else {
                                    TextUtils.sendMessageWithPrefix(player, SBank.getPlugin().getConfig().getString("messages.loan-disabled"));
                                }
                            } else {
                                TextUtils.sendMessageWithPrefix(player, SBank.getPlugin().getConfig().getString("messages.no-permission"));
                            }
                        }
                    }
                }
            } else {
                TextUtils.sendMessageWithPrefix(player, SBank.getPlugin().getConfig().getString("messages.no-permission"));
            }
        }


        return true;
    }

}
