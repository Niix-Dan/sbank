package com.spearforge.sBank.database;

import com.spearforge.sBank.SBank;
import com.spearforge.sBank.model.Bank;
import com.spearforge.sBank.model.Debt;
import com.spearforge.sBank.utils.MiscUtils;
import com.spearforge.sBank.utils.TextUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Map;

public class SQLiteConnection extends DatabaseConnection {

    @Override
    public void initializeDatabase() throws SQLException {
        Connection connection = getConnection();

        Statement st = connection.createStatement();

        String sql = "CREATE TABLE IF NOT EXISTS banks ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "username TEXT, "
                + "uuid TEXT, "
                + "balance REAL, "
                + "bankname TEXT)";

        String sql2 = "CREATE TABLE IF NOT EXISTS debts ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "username TEXT, "
                + "uuid TEXT, "
                + "total REAL, "
                + "remaining REAL, "
                + "daily REAL, "
                + "last_payment_date TEXT)";

        st.execute(sql);
        st.execute(sql2);
        st.close();
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = createNewConnection();
        }
        return connection;
    }

    private Connection createNewConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            SBank.getPlugin().getLogger().warning("SQLite JDBC driver not found");
        }

        File dataFolder = SBank.getPlugin().getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        String url = "jdbc:sqlite:" + new File(dataFolder, SBank.getPlugin().getConfig().getString("database.name") + ".db").getAbsolutePath();
        return DriverManager.getConnection(url);
    }

    @Override
    public void loadOnlinePlayersBanks() {
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
        Statement st = connection.createStatement();
        String sql = "SELECT * FROM debts";
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
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
    public void applyInterest(double interest) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (SBank.getBanks().get(player.getName()).getBalance() > 0) {
                if (player.hasPermission("sbank.interest")) {
                    double balance = SBank.getBanks().get(player.getName()).getBalance();
                    double interestAmount = balance * (interest / 100);
                    SBank.getBanks().get(player.getName()).setBalance(balance + interestAmount);
                    TextUtils.sendMessageWithPrefix(player, SBank.getPlugin().getConfig().getString("interest.interest-message").replaceAll("%interest%", String.valueOf(MiscUtils.formatBalance(interestAmount))));
                }
            }
        }
    }

    @Override
    public void setBankInDatabase(Bank bank) {
        String query = "INSERT INTO banks (username, uuid, balance, bankname) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, bank.getUsername());
            stmt.setString(2, bank.getUuid());
            stmt.setDouble(3, bank.getBalance());
            stmt.setString(4, bank.getBankname());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateBankInDatabase(Bank bank) throws SQLException {
        String query = "UPDATE banks SET bankname = ?, balance = ? WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, bank.getBankname());
            stmt.setDouble(2, bank.getBalance());
            stmt.setString(3, bank.getUsername());
            stmt.executeUpdate();
        }
    }

    @Override
    public Debt getDebt(String username) {
        Debt debt = new Debt();
        String sql = "SELECT * FROM debts WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    debt.setUsername(rs.getString("username"));
                    debt.setUuid(rs.getString("uuid"));
                    debt.setTotal(rs.getDouble("total"));
                    debt.setRemaining(rs.getDouble("remaining"));
                    debt.setDaily(rs.getDouble("daily"));
                    debt.setLastPaymentDate(rs.getString("last_payment_date"));
                    return debt;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void setDebtToDatabase(Debt debt) throws SQLException {
        String query = "INSERT INTO debts (username, uuid, total, remaining, daily, last_payment_date) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, debt.getUsername());
            stmt.setString(2, debt.getUuid());
            stmt.setDouble(3, debt.getTotal());
            stmt.setDouble(4, debt.getRemaining());
            stmt.setDouble(5, debt.getDaily());
            stmt.setString(6, LocalDateTime.now().toString());
            stmt.executeUpdate();
        }
    }

    @Override
    public void updateDebtInDatabase(Debt debt) throws SQLException {
        String query = "UPDATE debts SET total = ?, remaining = ?, daily = ?, last_payment_date = ? WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setDouble(1, debt.getTotal());
            stmt.setDouble(2, debt.getRemaining());
            stmt.setDouble(3, debt.getDaily());
            stmt.setString(4, debt.getLastPaymentDate());
            stmt.setString(5, debt.getUsername());
            stmt.executeUpdate();
        }
    }

    @Override
    public void removeDebt(String username) throws SQLException {
        String query = "DELETE FROM debts WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.executeUpdate();
        }
    }
}
