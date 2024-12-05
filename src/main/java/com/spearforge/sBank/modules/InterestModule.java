package com.spearforge.sBank.modules;

import com.spearforge.sBank.SBank;
import org.bukkit.configuration.file.FileConfiguration;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class InterestModule {

    public static boolean hasDefinedHoursPassed() {
        try {
            String lastInterest = SBank.getPlugin().getConfig().getString("interest.last-interest-date");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime lastInterestDate = LocalDateTime.parse(lastInterest, formatter);

            LocalDateTime currentTime = LocalDateTime.now();

            Duration duration = Duration.between(lastInterestDate, currentTime);

            long hoursPassed = duration.toHours();
            return hoursPassed >= Long.parseLong(SBank.getPlugin().getConfig().getString("interest.interest-time"));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void updateLastInterestDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentDate = sdf.format(new Date());

        FileConfiguration config = SBank.getPlugin().getConfig();
        config.set("interest.last-interest-date", currentDate);
        SBank.getPlugin().saveConfig();
    }

    public static String getTimeUntilNextInterest() {
        try {
            String lastInterest = SBank.getPlugin().getConfig().getString("interest.last-interest-date");
            long interestIntervalHours = Long.parseLong(SBank.getPlugin().getConfig().getString("interest.interest-time"));

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime lastInterestDate = LocalDateTime.parse(lastInterest, formatter);

            LocalDateTime nextInterestDate = lastInterestDate.plusHours(interestIntervalHours);
            LocalDateTime currentTime = LocalDateTime.now();

            Duration duration = Duration.between(currentTime, nextInterestDate);

            long hours = duration.toHours();
            long minutes = duration.toMinutes() % 60;
            long seconds = duration.getSeconds() % 60;

            return String.format("%02d hours, %02d minutes, %02d seconds", hours, minutes, seconds);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error calculating time until next interest";
        }
    }

}
