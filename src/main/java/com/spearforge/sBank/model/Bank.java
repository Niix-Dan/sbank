package com.spearforge.sBank.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Bank {

    private String username;
    private String uuid;
    private String bankname;
    private double balance;

    public double getBalance() {
        DecimalFormatSymbols dfs = new DecimalFormatSymbols(Locale.GERMANY);
        dfs.setDecimalSeparator('.');
        DecimalFormat df = new DecimalFormat("#.00", dfs);
        return Double.parseDouble(df.format(balance));
    }

}
