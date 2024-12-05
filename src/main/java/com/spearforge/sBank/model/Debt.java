package com.spearforge.sBank.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class Debt {

    private String username;
    private String uuid;
    private double total;
    private double remaining;
    private double daily;
    private String lastPaymentDate;

    @Override
    public String toString() {
        return "Debt{" +
                "username='" + username + '\'' +
                ", uuid='" + uuid + '\'' +
                ", total=" + total +
                ", remaining=" + remaining +
                ", daily=" + daily +
                ", lastPaymentDate=" + lastPaymentDate +
                '}';
    }
}
