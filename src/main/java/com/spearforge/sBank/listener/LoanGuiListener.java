package com.spearforge.sBank.listener;

import com.spearforge.sBank.SBank;
import com.spearforge.sBank.model.Debt;
import com.spearforge.sBank.utils.MiscUtils;
import com.spearforge.sBank.utils.TextUtils;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;

public class LoanGuiListener implements Listener {

    @Getter
    private static final HashMap<String, Double> loanAmount = new HashMap<>();
    @Getter
    private static final HashMap<String, Debt> loanAgree = new HashMap<>();
    @Getter
    private static final HashMap<String, Boolean> manuallyClosed = new HashMap<>();


    @EventHandler
    public void onClickLoanGui(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        String loanTitle = ChatColor.translateAlternateColorCodes('&', SBank.getPlugin().getConfig().getString("gui.loan-page-title"));

        if (!e.getView().getTitle().equalsIgnoreCase(loanTitle)) return;

        e.setCancelled(true);
        ItemStack currentItem = e.getCurrentItem();

        if (currentItem == null) return;
        if (!player.hasPermission("sbank.loan")) {
            TextUtils.sendMessageWithPrefix(player, SBank.getPlugin().getConfig().getString("messages.no-permission"));
            player.closeInventory();
            return;
        }

        String availableLoanName = ChatColor.translateAlternateColorCodes('&', SBank.getGuiConfig().getString("gui.loan.available-loan.name"));

        if (e.getCurrentItem() != null && e.getCurrentItem().getItemMeta() != null && e.getCurrentItem().getItemMeta().getDisplayName() != null) {
            String itemDisplayName = currentItem.getItemMeta().getDisplayName();
            if (itemDisplayName.equalsIgnoreCase(availableLoanName) && !loanAmount.containsKey(player.getName())) {
                loanAmount.put(player.getName(), null);
                player.closeInventory();
                int minLoan = SBank.getPlugin().getConfig().getInt("loan.min-loan");
                int maxLoan = SBank.getPlugin().getConfig().getInt("loan.max-loan");
                TextUtils.sendMessageWithPrefix(player, SBank.getPlugin().getConfig().getString("messages.loan-amount")
                        .replace("%minloan%", MiscUtils.formatBalance(minLoan))
                        .replace("%maxloan%", MiscUtils.formatBalance(maxLoan))
                );
            }
        }
    }

    @SneakyThrows
    @EventHandler
    public void onAgreeLoanGui(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        String agreePageTitle = ChatColor.translateAlternateColorCodes('&', SBank.getPlugin().getConfig().getString("gui.agree-page-title"));

        if (!e.getView().getTitle().equals(agreePageTitle)) return;

        e.setCancelled(true);
        ItemStack currentItem = e.getCurrentItem();

        if (currentItem == null) return;

        String agreeName = ChatColor.translateAlternateColorCodes('&', SBank.getGuiConfig().getString("gui.loan.agree.name"));
        String disagreeName = ChatColor.translateAlternateColorCodes('&', SBank.getGuiConfig().getString("gui.loan.disagree.name"));

        if (e.getCurrentItem() != null && e.getCurrentItem().getItemMeta() != null && e.getCurrentItem().getItemMeta().getDisplayName() != null) {
            String displayName = currentItem.getItemMeta().getDisplayName();
            if (displayName.equals(agreeName)) {
                agreeLoan(player);
            } else if (displayName.equals(disagreeName)) {
                disagreeLoan(player);
            }
        }
    }

    private void agreeLoan(Player player) {
        Debt debt = loanAgree.get(player.getName());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime dateTime = LocalDateTime.now();
        String dateFormatted = dateTime.format(formatter);
        debt.setLastPaymentDate(dateFormatted);

        SBank.getDebts().put(player.getName(), debt);
        List<String> messages = TextUtils.replacePlaceholders(SBank.getPlugin().getConfig().getStringList("messages.loan-agree"), null, debt);

        for (String message : messages) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }

        try {
            SBank.getDb().setDebtToDatabase(debt);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        double loan = loanAmount.get(player.getName());
        SBank.getBanks().get(player.getName()).setBalance(SBank.getBanks().get(player.getName()).getBalance() + loan);

        loanAgree.remove(player.getName());
        loanAmount.remove(player.getName());

        manuallyClosed.put(player.getName(), true);

        player.closeInventory();
    }

    private void disagreeLoan(Player player) {
        TextUtils.sendMessageWithPrefix(player, SBank.getPlugin().getConfig().getString("messages.loan-failed"));
        loanAgree.remove(player.getName());
        loanAmount.remove(player.getName());

        manuallyClosed.put(player.getName(), true);

        player.closeInventory();
    }


    @EventHandler
    public void onGuiCloseEvent(InventoryCloseEvent e) {
        Player player = (Player) e.getPlayer();
        String agreeTitle = ChatColor.translateAlternateColorCodes('&', SBank.getPlugin().getConfig().getString("gui.agree-page-title"));

        if (e.getView().getTitle().equals(agreeTitle)) {

            if (manuallyClosed.containsKey(player.getName())) {
                manuallyClosed.remove(player.getName());
                return;
            }

            loanAgree.remove(player.getName());
            loanAmount.remove(player.getName());

            TextUtils.sendMessageWithPrefix(player, SBank.getPlugin().getConfig().getString("messages.loan-cancelled"));
        }
    }


}
