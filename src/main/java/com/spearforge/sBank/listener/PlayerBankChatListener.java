package com.spearforge.sBank.listener;

import com.spearforge.sBank.SBank;
import com.spearforge.sBank.model.Bank;
import com.spearforge.sBank.modules.DebtModule;
import com.spearforge.sBank.utils.MiscUtils;
import com.spearforge.sBank.utils.TextUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayerBankChatListener implements Listener {

    @EventHandler
    public void onGivingCustomAmount(AsyncPlayerChatEvent e) {
        String playerName = e.getPlayer().getName();

        if (isPlayerInCustomTransaction(playerName)) {
            e.setCancelled(true);
            String message = e.getMessage();
            double balance = SBank.getEcon().getBalance(e.getPlayer());

            if (message.equalsIgnoreCase("close")) {
                cancelTransaction(playerName);
                TextUtils.sendMessageWithPrefix(e.getPlayer(), SBank.getPlugin().getConfig().getString("messages.custom-amount-not-set"));
                return;
            }

            if (BankGuiListener.getSetName().containsKey(playerName)) {
                handleSetBankName(e, message);
            }
            else if (BankGuiListener.getCustomDepAmount().containsKey(playerName)) {
                handleDeposit(e, balance, message);
            }
            else if (BankGuiListener.getCustomWithAmount().containsKey(playerName)) {
                handleWithdraw(e, message);
            }
            else if (DebtGuiListener.getDebtPayment().containsKey(playerName)) {
                handleDebtPayment(e, balance, message);
            }
            else {
                TextUtils.sendMessageWithPrefix(e.getPlayer(), SBank.getPlugin().getConfig().getString("messages.invalid-amount"));
            }
        }
    }

    private boolean isPlayerInCustomTransaction(String playerName) {
        return BankGuiListener.getCustomDepAmount().containsKey(playerName) ||
                BankGuiListener.getCustomWithAmount().containsKey(playerName) ||
                DebtGuiListener.getDebtPayment().containsKey(playerName) ||
                BankGuiListener.getSetName().containsKey(playerName);
    }

    private void cancelTransaction(String playerName) {
        BankGuiListener.getCustomDepAmount().remove(playerName);
        BankGuiListener.getCustomWithAmount().remove(playerName);
        DebtGuiListener.getDebtPayment().remove(playerName);
        BankGuiListener.getSetName().remove(playerName);
    }

    private void handleSetBankName(AsyncPlayerChatEvent e, String message) {
        String playerName = e.getPlayer().getName();
        StringBuilder bankName = new StringBuilder();
        String[] args = message.split(" ");

        for (String arg : args) {
            bankName.append(arg).append(" ");
        }

        if (!args[0].equalsIgnoreCase("close")) {
            int maxLength = SBank.getPlugin().getConfig().getInt("bank-name-length");

            if (bankName.length() <= maxLength) {
                Pattern p = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
                Matcher m = p.matcher(bankName.toString());

                if (!m.find()) {
                    SBank.getBanks().get(playerName).setBankname(bankName.toString());
                    TextUtils.sendMessageWithPrefix(e.getPlayer(), SBank.getPlugin().getConfig().getString("messages.bank-name-set")
                            .replaceAll("%bankname%", bankName.toString()));
                    BankGuiListener.getSetName().remove(playerName);
                } else {
                    TextUtils.sendMessageWithPrefix(e.getPlayer(), SBank.getPlugin().getConfig().getString("messages.bank-name-invalid"));
                }
            } else {
                TextUtils.sendMessageWithPrefix(e.getPlayer(), "&cBank name can only be %max% characters long. " +
                        "(include spaces)".replaceAll("%max%", String.valueOf(maxLength)));
            }
        } else {
            TextUtils.sendMessageWithPrefix(e.getPlayer(), SBank.getPlugin().getConfig().getString("messages.bank-name-not-set"));
            BankGuiListener.getSetName().remove(playerName);
        }
    }

    private void handleDeposit(AsyncPlayerChatEvent e, double balance, String message) {
        String playerName = e.getPlayer().getName();

        // Ondalıklı sayıları ve tam sayıları kontrol eden bir yöntem
        if (MiscUtils.isNumeric(message)) {
            double amount = Double.parseDouble(message);
            if (balance >= amount) {
                Bank bank = SBank.getBanks().get(playerName);
                BankGuiListener.getCustomDepAmount().remove(playerName);
                bank.setBalance(bank.getBalance() + amount);
                SBank.getEcon().withdrawPlayer(e.getPlayer(), amount);
                TextUtils.sendMessageWithPrefix(e.getPlayer(), SBank.getPlugin().getConfig().getString("messages.deposit-success")
                        .replaceAll("%money%", MiscUtils.formatBalance(amount)));
            } else {
                BankGuiListener.getCustomDepAmount().remove(playerName);
                TextUtils.sendMessageWithPrefix(e.getPlayer(), SBank.getPlugin().getConfig().getString("messages.not-enough-money"));
            }
        } else {
            TextUtils.sendMessageWithPrefix(e.getPlayer(), SBank.getPlugin().getConfig().getString("messages.invalid-amount"));
        }
    }


    private void handleWithdraw(AsyncPlayerChatEvent e, String message) {
        String playerName = e.getPlayer().getName();
        Bank bank = SBank.getBanks().get(playerName);

        if (MiscUtils.isNumeric(message)) {
            double amount = Double.parseDouble(message);
            if (bank.getBalance() >= amount) {
                bank.setBalance(bank.getBalance() - amount);
                SBank.getEcon().depositPlayer(e.getPlayer(), amount);
                BankGuiListener.getCustomWithAmount().remove(playerName);
                TextUtils.sendMessageWithPrefix(e.getPlayer(), SBank.getPlugin().getConfig().getString("messages.withdraw-success")
                        .replaceAll("%money%", MiscUtils.formatBalance(amount)));
            } else {
                BankGuiListener.getCustomWithAmount().remove(playerName);
                TextUtils.sendMessageWithPrefix(e.getPlayer(), SBank.getPlugin().getConfig().getString("messages.not-enough-money"));
            }
        } else {
            TextUtils.sendMessageWithPrefix(e.getPlayer(), SBank.getPlugin().getConfig().getString("messages.invalid-amount"));
        }
    }


    private void handleDebtPayment(AsyncPlayerChatEvent e, double balance, String message) {
        String playerName = e.getPlayer().getName();

        if (MiscUtils.isNumeric(message)) {
            double amount = Double.parseDouble(message);

            if (SBank.getDebts().get(playerName).getDaily() < amount && amount > 0) {
                if (balance >= amount) {
                    DebtModule.payDebtFromBalance(e.getPlayer(), amount);
                } else {
                    DebtGuiListener.getDebtPayment().remove(playerName);
                    TextUtils.sendMessageWithPrefix(e.getPlayer(), SBank.getPlugin().getConfig().getString("messages.not-enough-money"));
                }
            } else {
                DebtGuiListener.getDebtPayment().remove(playerName);
                TextUtils.sendMessageWithPrefix(e.getPlayer(), SBank.getPlugin().getConfig().getString("messages.less-then-daily")
                        .replaceAll("%daily%", MiscUtils.formatBalance(SBank.getDebts().get(playerName).getDaily())));
            }
        } else {
            TextUtils.sendMessageWithPrefix(e.getPlayer(), SBank.getPlugin().getConfig().getString("messages.invalid-amount"));
        }
    }

}
