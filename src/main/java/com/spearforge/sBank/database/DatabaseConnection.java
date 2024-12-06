package com.spearforge.sBank.database;

import com.spearforge.sBank.model.Bank;
import com.spearforge.sBank.model.Debt;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class DatabaseConnection {

    protected Connection connection;

    public abstract Connection getConnection() throws SQLException;

    public abstract void initializeDatabase() throws SQLException;

    public abstract void loadOnlinePlayersBanks() throws SQLException;

    public abstract Bank getBank(String username) throws SQLException;

    public abstract boolean hasBank(String username) throws SQLException;

    public abstract void loadDebtsFromDatabase() throws SQLException;

    public abstract void applyInterest(double default) throws SQLException;

    public abstract void setBankInDatabase(Bank bank) throws SQLException;

    public abstract void updateBankInDatabase(Bank bank) throws SQLException;

    public abstract Debt getDebt(String username) throws SQLException;

    public abstract void setDebtToDatabase(Debt debt) throws SQLException;

    public abstract void updateDebtInDatabase(Debt debt) throws SQLException;

    public abstract void removeDebt(String username) throws SQLException;

}
