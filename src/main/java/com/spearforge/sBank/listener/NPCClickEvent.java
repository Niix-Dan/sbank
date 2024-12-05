package com.spearforge.sBank.listener;

import com.spearforge.sBank.SBank;
import com.spearforge.sBank.guis.BankGUI;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;

public class NPCClickEvent implements Listener {

    @EventHandler
    public void onClickBanker(NPCRightClickEvent e){
        NPC banker = e.getNPC();
        Player player = e.getClicker();
        if (banker.getId() == SBank.getPlugin().getConfig().getInt("npc-bankers.npc-id") && player.hasPermission("sbank.use")){
            Inventory inventory = BankGUI.getBankHomePage(player);
            player.openInventory(inventory);
        }
    }

}
