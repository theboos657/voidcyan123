package com.voidcyan.client.module;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.voidcyan.client.VoidCyanClient;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;

public class StreakManager {
   private static final Path STREAKS_FILE = Path.of("config/voidcyan_streaks.json");
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(LocalDate.class, new JsonSerializer<LocalDate>() {
      public JsonElement serialize(LocalDate src, Type typeOfSrc, JsonSerializationContext context) {
         return new JsonPrimitive(src.toString());
      }
   }).registerTypeAdapter(LocalDate.class, new JsonDeserializer<LocalDate>() {
      public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
         return LocalDate.parse(json.getAsString());
      }
   }).create();
   public static List<StreakManager.Streak> streaks = new ArrayList<>();
   public static StreakManager.Streak currentStreak = null;
   private static LocalDate lastLoginDate = null;
   private static long lastRecordedTimeMs = 0L;

   public static void init() {
      loadStreaks();
      long currentTimeMs = System.currentTimeMillis();
      if (lastRecordedTimeMs > 0L && currentTimeMs < lastRecordedTimeMs) {
         System.err.println("[StreakManager] Time anomaly detected! System time moved backward.");
         System.err.println("[StreakManager] Previous: " + lastRecordedTimeMs + ", Current: " + currentTimeMs);
         if (currentStreak != null && currentStreak.isActive) {
            System.err.println("[StreakManager] Breaking active streak due to time manipulation.");
            endCurrentStreak();
         }

         lastLoginDate = null;
      }

      checkDailyLogin();
      lastRecordedTimeMs = System.currentTimeMillis();
   }

   public static void tick() {
      if (currentStreak != null && currentStreak.isActive) {
         MinecraftClient client = MinecraftClient.getInstance();
         if (client.world != null && client.player != null) {
            currentStreak.totalPlaytimeMs += 50L;
         }
         updateCurrentStreakStats();
         long currentTimeMs = System.currentTimeMillis();
         if (currentTimeMs > lastRecordedTimeMs) {
            lastRecordedTimeMs = currentTimeMs;
         }
      }
   }

   public static void onSessionEnd() {
      if (currentStreak != null && currentStreak.isActive) {
         updateCurrentStreakStats();
         lastRecordedTimeMs = System.currentTimeMillis();
         saveStreaks();
      }
   }

   private static void checkDailyLogin() {
      LocalDate today = LocalDate.now();
      if (lastLoginDate != null) {
         long daysDiff = ChronoUnit.DAYS.between(lastLoginDate, today);
         if (daysDiff > 1L) {
            System.err.println("[StreakManager] Suspicious date jump detected: " + daysDiff + " days");
            if (currentStreak != null && currentStreak.isActive) {
               endCurrentStreak();
            }

            lastLoginDate = today;
            startNewStreak(today);
            return;
         }
      }

      if (lastLoginDate == null || !lastLoginDate.equals(today)) {
         lastLoginDate = today;
         if (currentStreak != null && currentStreak.isActive) {
            LocalDate yesterday = today.minusDays(1L);
            if (currentStreak.endDate.equals(yesterday)) {
               currentStreak.endDate = today;
               currentStreak.daysCount++;
            } else if (currentStreak.endDate.isBefore(yesterday)) {
               endCurrentStreak();
               startNewStreak(today);
            }
         } else {
            startNewStreak(today);
         }

         saveStreaks();
      }
   }

   private static void startNewStreak(LocalDate date) {
      StreakManager.Streak newStreak = new StreakManager.Streak();
      newStreak.streakNumber = streaks.size() + 1;
      newStreak.startDate = date;
      newStreak.endDate = date;
      newStreak.daysCount = 1;
      newStreak.totalPlaytimeMs = 0L;
      newStreak.isActive = true;
      captureCurrentStats(newStreak.startStats);
      captureCurrentStats(newStreak.endStats);
      currentStreak = newStreak;
      streaks.add(newStreak);
   }

   private static void endCurrentStreak() {
      if (currentStreak != null) {
         currentStreak.isActive = false;
         updateCurrentStreakStats();
      }
   }

   private static void updateCurrentStreakStats() {
      if (currentStreak != null) {
         captureCurrentStats(currentStreak.endStats);
         currentStreak.gainedStats.subtract(currentStreak.startStats, currentStreak.endStats);
      }
   }

   private static void captureCurrentStats(StreakManager.StreakStats stats) {
      stats.kills = VoidCyanClient.allTimeKills;
      stats.deaths = VoidCyanClient.allTimeDeaths;
      stats.hits = VoidCyanClient.allTimeHits;
      stats.clicks = VoidCyanClient.allTimeClicks;
      stats.blocksBroken = VoidCyanClient.allTimeBlocksBroken;
      stats.blocksPlaced = VoidCyanClient.allTimeBlocksPlaced;
      stats.crystalsPlaced = VoidCyanClient.allTimeCrystalsPlaced;
      stats.crystalsBroken = VoidCyanClient.allTimeCrystalsBroken;
   }

   public static void loadStreaks() {
      try {
         if (Files.exists(STREAKS_FILE)) {
            String json = Files.readString(STREAKS_FILE);
            StreakManager.StreakData data = (StreakManager.StreakData)GSON.fromJson(json, StreakManager.StreakData.class);
            if (data != null) {
               streaks = data.streaks;
               lastLoginDate = data.lastLoginDate;
               lastRecordedTimeMs = data.lastRecordedTimeMs;

               long allTimeMs = VoidCyanClient.allTimePlayTimeTicks * 50L;
               for (StreakManager.Streak streak : streaks) {
                  if (streak.isActive) {
                     currentStreak = streak;
                  }
                  if (allTimeMs > 0L && streak.totalPlaytimeMs > allTimeMs) {
                     // Sanitize inflated playtime that exceeds all-time playtime
                     streak.totalPlaytimeMs = Math.max(0L, allTimeMs / Math.max(1, streaks.size()));
                  }
               }
            }
         }
      } catch (Exception var4) {
         System.err.println("Failed to load streaks: " + var4.getMessage());
      }
   }

   public static void saveStreaks() {
      try {
         Files.createDirectories(STREAKS_FILE.getParent());
         StreakManager.StreakData data = new StreakManager.StreakData();
         data.streaks = streaks;
         data.lastLoginDate = lastLoginDate;
         data.lastRecordedTimeMs = lastRecordedTimeMs;
         String json = GSON.toJson(data);
         Files.writeString(STREAKS_FILE, json);
      } catch (Exception var2) {
         System.err.println("Failed to save streaks: " + var2.getMessage());
      }
   }

   public static String formatPlaytime(long ms) {
      long seconds = ms / 1000L;
      long minutes = seconds / 60L;
      long hours = minutes / 60L;
      minutes %= 60L;
      return hours > 0L ? String.format("%dh %dm", hours, minutes) : String.format("%dm", minutes);
   }

   public static class Streak {
      public int streakNumber;
      public LocalDate startDate;
      public LocalDate endDate;
      public int daysCount;
      public long totalPlaytimeMs;
      public StreakManager.StreakStats startStats = new StreakManager.StreakStats();
      public StreakManager.StreakStats endStats = new StreakManager.StreakStats();
      public StreakManager.StreakStats gainedStats = new StreakManager.StreakStats();
      public boolean isActive = false;
   }

   private static class StreakData {
      List<StreakManager.Streak> streaks = new ArrayList<>();
      LocalDate lastLoginDate;
      long lastRecordedTimeMs = 0L;
   }

   public static class StreakStats {
      public int kills = 0;
      public int deaths = 0;
      public int hits = 0;
      public int clicks = 0;
      public int blocksBroken = 0;
      public int blocksPlaced = 0;
      public int crystalsPlaced = 0;
      public int crystalsBroken = 0;

      public void subtract(StreakManager.StreakStats start, StreakManager.StreakStats end) {
         this.kills = end.kills - start.kills;
         this.deaths = end.deaths - start.deaths;
         this.hits = end.hits - start.hits;
         this.clicks = end.clicks - start.clicks;
         this.blocksBroken = end.blocksBroken - start.blocksBroken;
         this.blocksPlaced = end.blocksPlaced - start.blocksPlaced;
         this.crystalsPlaced = end.crystalsPlaced - start.crystalsPlaced;
         this.crystalsBroken = end.crystalsBroken - start.crystalsBroken;
      }
   }
}
