package com.spearforge.sBank.database;

import com.spearforge.sBank.SBank;
import com.spearforge.sBank.model.Bank;
import com.spearforge.sBank.model.Debt;
import com.spearforge.sBank.utils.MiscUtils;
import com.spearforge.sBank.utils.TextUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MySQLConnection extends DatabaseConnection{

    private Connection connection;

    @Override
    public Connection getConnection() throws SQLException {
        if (connection != null) {
            return connection;
        }
        FileConfiguration config = SBank.getPlugin().getConfig();

        String dbName = config.getString("database.name");
        String host = config.getString("database.host");
        String user = config.getString("database.username");
        String password = config.getString("database.password");
        String port = config.getString("database.port");
        String url = "jdbc:mysql://" + host + ":" + port + "/" + dbName;

        this.connection = DriverManager.getConnection(url, user, password);

        return this.connection;
    }
    @Override
    public void initializeDatabase() throws SQLException {
            Statement st = getConnection().createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS banks (id INT AUTO_INCREMENT PRIMARY KEY, username VARCHAR(255), uuid VARCHAR(255), balance DOUBLE, bankname VARCHAR(255))";
            String sql2 = "CREATE TABLE IF NOT EXISTS debts (id INT AUTO_INCREMENT PRIMARY KEY, username VARCHAR(255), uuid VARCHAR(255), total DOUBLE, remaining DOUBLE, daily DOUBLE, last_payment_date VARCHAR(255))";
            st.execute(sql);
            st.execute(sql2);
            st.close();
    }
    @Override
    public void loadOnlinePlayersBanks(){
        for (Player player : Bukkit.getOnlinePlayers()) {
            Bank bank = getBank(player.getName());
            SBank.getBanks().put(player.getName(), bank);
        }
    }
    @Override
    public Bank getBank(String username) {
        Bank bank = new Bank();
        String sql = "SELECT * FROM banks WHERE username = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    bank.setUsername(rs.getString("username"));
                    bank.setUuid(rs.getString("uuid"));
                    bank.setBalance(rs.getDouble("balance"));
                    bank.setBankname(rs.getString("bankname"));
                    return bank;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean hasBank(String username) {
        String sql = "SELECT * FROM banks WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void loadDebtsFromDatabase() throws SQLException {
        Map<String, Debt> debts = SBank.getDebts();
        Statement st = getConnection().createStatement();
        String sql = "SELECT * FROM debts";
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()){
            Debt debt = new Debt();
            debt.setUsername(rs.getString("username"));
            debt.setUuid(rs.getString("uuid"));
            debt.setTotal(rs.getDouble("total"));
            debt.setRemaining(rs.getDouble("remaining"));
            debt.setDaily(rs.getDouble("daily"));
            debt.setLastPaymentDate(rs.getString("last_payment_date"));
            debts.put(debt.getUsername(), debt);
        }
        st.close();
    }

    @Override
    public void applyInterest(double _interest) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (SBank.getBanks().get(player.getName()).getBalance() > 0){
                if (player.hasPermission("sbank.interest")){
                    double balance = SBank.getBanks().get(player.getName()).getBalance();

                    double interest = MiscUtils.getInterest(player, _interest);
                    double interestAmount = balance * (interest / 100);
                    SBank.getBanks().get(player.getName()).setBalance(balance + interestAmount);
                    TextUtils.sendMessageWithPrefix(player, SBank.getPlugin().getConfig().getString("interest.interest-message").replaceAll("%interest%", String.valueOf(MiscUtils.formatBalance(interestAmount))));
                }
            }
        }
    }

    // for first join
    @Override
    public void setBankInDatabase(Bank bank) {
        String query = "INSERT INTO banks (username, uuid, balance, bankname) VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, bank.getUsername());
            stmt.setString(2, bank.getUuid());
            stmt.setDouble(3, bank.getBalance());
            stmt.setString(4, bank.getBankname());
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // when player leave the server
    @Override
    public void updateBankInDatabase(Bank bank) throws SQLException {
        String query = "UPDATE banks SET bankname = ?, balance = ? WHERE username = ?";
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setString(1, bank.getBankname());
        stmt.setDouble(2, bank.getBalance());
        stmt.setString(3, bank.getUsername());
        stmt.executeUpdate();
        stmt.close();
    }

    @Override
    public void setDebtToDatabase(Debt debt) throws SQLException {
        String query = "INSERT INTO debts (username, uuid, total, remaining, daily, last_payment_date) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setString(1, debt.getUsername());
        stmt.setString(2, debt.getUuid());
        stmt.setDouble(3, debt.getTotal());
        stmt.setDouble(4, debt.getRemaining());
        stmt.setDouble(5, debt.getDaily());
        stmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
        stmt.executeUpdate();
        stmt.close();
    }
    @Override
    public void updateDebtInDatabase(Debt debt) throws SQLException {
        String query = "UPDATE debts SET total = ?, remaining = ?, daily = ?, last_payment_date = ? WHERE username = ?";
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setDouble(1, debt.getTotal());
        stmt.setDouble(2, debt.getRemaining());
        stmt.setDouble(3, debt.getDaily());
        stmt.setString(4, debt.getLastPaymentDate());
        stmt.setString(5, debt.getUsername());
        stmt.executeUpdate();
        stmt.close();
    }
    @Override
    public void removeDebt(String username) throws  SQLException {
        String query = "DELETE FROM debts WHERE username = ?";
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setString(1, username);
        stmt.executeUpdate();
        stmt.close();
    }
}
