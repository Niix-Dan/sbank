package com.spearforge.sBank.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MoneyPile {

    private double amount;
    private String player_name;

}
