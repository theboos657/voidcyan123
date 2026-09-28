package com.voidcyan.client.social;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.client.MinecraftClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FriendManager {
   private static final Logger LOGGER = LogManager.getLogger("VoidCyan-Social");
   public static final List<String> friends = new CopyOnWriteArrayList<>();

   public static void init() {
      loadFriends();
   }

   public static boolean isFriendOnline(String username) {
      MinecraftClient client = MinecraftClient.getInstance();
      return client != null && client.getNetworkHandler() != null
         ? client.getNetworkHandler().getPlayerList().stream().anyMatch(p -> p.getProfile().name().equalsIgnoreCase(username))
         : false;
   }

   private static File getFriendsFile() {
      return new File(MinecraftClient.getInstance().runDirectory, "voidcyan_friends.txt");
   }

   public static void loadFriends() {
      friends.clear();
      File file = getFriendsFile();
      if (file.exists()) {
         String line;
         try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            while ((line = reader.readLine()) != null) {
               String name = line.trim();
               if (!name.isEmpty()) {
                  friends.add(name);
               }
            }
         } catch (IOException var6) {
            LOGGER.error("Failed to load friends", var6);
         }
      }
   }

}
