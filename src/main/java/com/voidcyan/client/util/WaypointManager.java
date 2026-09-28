package com.voidcyan.client.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.Identifier;

public class WaypointManager {
   public static List<WaypointManager.Waypoint> waypoints = new ArrayList<>();
   private static final File LEGACY_WAYPOINTS_FILE = new File("config/voidcyan_waypoints.json");
   private static final File SERVERS_DIR = new File("config/voidcyan/servers");
   private static final File LEGACY_MIGRATED_MARKER = new File(SERVERS_DIR, "_legacy_migrated.marker");
   private static String currentServerId = "default";
   private static boolean loadedForCurrentServer = false;

   public static String getCurrentServerId() {
      return currentServerId;
   }

   public static String getCurrentServerLabel(MinecraftClient client) {
      if (client == null) {
         return currentServerId;
      } else if (client.isInSingleplayer()) {
         IntegratedServer server = client.getServer();
         return server != null ? server.getSaveProperties().getLevelName() : "Singleplayer";
      } else {
         ServerInfo info = client.getCurrentServerEntry();
         if (info != null && info.name != null && !info.name.isBlank()) {
            return info.name;
         } else {
            return info != null && info.address != null && !info.address.isBlank() ? info.address : currentServerId;
         }
      }
   }

   public static void onServerJoin(MinecraftClient client) {
      onServerJoin(client, client != null ? client.getNetworkHandler() : null);
   }

   public static void onServerJoin(MinecraftClient client, ClientPlayNetworkHandler handler) {
      switchToServer(resolveServerId(client, handler));
   }

   public static void tickServerCheck(MinecraftClient client) {
      if (client != null && client.world != null) {
         String activeId = resolveServerId(client, client.getNetworkHandler());
         if (!loadedForCurrentServer || !activeId.equals(currentServerId)) {
            switchToServer(activeId);
         }
      }
   }

   private static void switchToServer(String nextServerId) {
      if (nextServerId == null || nextServerId.isBlank()) {
         nextServerId = "unknown";
      }

      if (loadedForCurrentServer && !nextServerId.equals(currentServerId)) {
         saveWaypoints();
         waypoints.clear();
      } else {
         if (loadedForCurrentServer) {
            return;
         }

         waypoints.clear();
      }

      currentServerId = nextServerId;
      loadWaypoints();
      loadedForCurrentServer = true;
   }

   public static void onServerDisconnect() {
      if (loadedForCurrentServer) {
         saveWaypoints();
      }

      waypoints.clear();
      currentServerId = "default";
      loadedForCurrentServer = false;
   }

   public static String resolveServerId(MinecraftClient client) {
      return resolveServerId(client, client != null ? client.getNetworkHandler() : null);
   }

   public static String resolveServerId(MinecraftClient client, ClientPlayNetworkHandler handler) {
      if (client == null) {
         return "default";
      } else if (client.isInSingleplayer()) {
         IntegratedServer server = client.getServer();
         return server != null ? sanitizeServerId("singleplayer/" + server.getSaveProperties().getLevelName()) : "singleplayer";
      } else {
         ServerInfo info = client.getCurrentServerEntry();
         if (info != null && info.address != null && !info.address.isBlank()) {
            return sanitizeServerId(normalizeServerAddress(info.address));
         } else if (handler != null && handler.getConnection() != null && handler.getConnection().getAddress() != null) {
            SocketAddress address = handler.getConnection().getAddress();
            if (address instanceof InetSocketAddress inet) {
               String host = inet.getHostString();
               int port = inet.getPort();
               if (host != null && !host.isBlank()) {
                  if (port > 0 && port != 25565) {
                     return sanitizeServerId(host + ":" + port);
                  }

                  return sanitizeServerId(host);
               }
            }

            return sanitizeServerId(address.toString());
         } else {
            return "multiplayer_unknown";
         }
      }
   }

   private static String normalizeServerAddress(String address) {
      String cleaned = address.trim().toLowerCase(Locale.ROOT);
      if (cleaned.startsWith("mc://")) {
         cleaned = cleaned.substring(5);
      }

      int slash = cleaned.indexOf(47);
      if (slash >= 0) {
         cleaned = cleaned.substring(0, slash);
      }

      return cleaned;
   }

   private static String sanitizeServerId(String raw) {
      String cleaned = raw.toLowerCase(Locale.ROOT).replace('\\', '/').replaceAll("[^a-z0-9._/-]", "_").replaceAll("_+", "_").replaceAll("/+", "/");
      if (cleaned.length() > 120) {
         cleaned = cleaned.substring(0, 120);
      }

      return cleaned.isBlank() ? "unknown" : cleaned;
   }

   private static File getWaypointsFile() {
      return new File(SERVERS_DIR, currentServerId + "/waypoints.json");
   }

   private static void ensureLoadedForActiveWorld() {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client != null && client.world != null) {
         if (!loadedForCurrentServer) {
            onServerJoin(client, client.getNetworkHandler());
         }
      }
   }

   public static void addWaypoint(String name, int x, int y, int z, String dimension, int color) {
      ensureLoadedForActiveWorld();
      waypoints.add(new WaypointManager.Waypoint(name, x, y, z, dimension, color, true, true, 10, 100, true, true, "minecraft:compass"));
      saveWaypoints();
   }

   public static void addWaypoint(
      String name,
      int x,
      int y,
      int z,
      String dimension,
      int color,
      boolean renderThroughWalls,
      int fillOpacity,
      int outlineOpacity,
      boolean showLabel,
      boolean showBlockDisplay,
      String iconItem
   ) {
      ensureLoadedForActiveWorld();
      waypoints.add(
         new WaypointManager.Waypoint(
            name, x, y, z, dimension, color, true, renderThroughWalls, fillOpacity, outlineOpacity, showLabel, showBlockDisplay, iconItem
         )
      );
      saveWaypoints();
   }

   public static ItemStack getIconStack(String iconItem) {
      if (iconItem != null && !iconItem.isBlank()) {
         try {
            Identifier id = iconItem.contains(":") ? Identifier.of(iconItem) : Identifier.of("minecraft", iconItem);
            Item item = (Item)Registries.ITEM.get(id);
            return item != null && item != Items.AIR ? new ItemStack(item) : new ItemStack(Items.COMPASS);
         } catch (Throwable var3) {
            return new ItemStack(Items.COMPASS);
         }
      } else {
         return new ItemStack(Items.COMPASS);
      }
   }

   public static void removeWaypoint(int index) {
      if (index >= 0 && index < waypoints.size()) {
         waypoints.remove(index);
         saveWaypoints();
      } else {
         System.err.println("[VoidCyan] WaypointManager: removeWaypoint index out of bounds: " + index);
      }
   }

   public static void saveWaypoints() {
      if (loadedForCurrentServer) {
         File waypointsFile = getWaypointsFile();
         File parentDir = waypointsFile.getParentFile();
         if (parentDir != null && !parentDir.exists() && !parentDir.mkdirs()) {
            System.err.println("[VoidCyan] WaypointManager: could not create server config directory.");
         }

         StringBuilder sb = new StringBuilder();
         sb.append("[\n");

         for (int i = 0; i < waypoints.size(); i++) {
            WaypointManager.Waypoint wp = waypoints.get(i);
            sb.append("  ");
            sb.append(waypointToJson(wp));
            if (i < waypoints.size() - 1) {
               sb.append(",");
            }

            sb.append("\n");
         }

         sb.append("]");

         try (BufferedWriter writer = new BufferedWriter(new FileWriter(waypointsFile))) {
            writer.write(sb.toString());
         } catch (IOException var8) {
            System.err.println("[VoidCyan] WaypointManager: failed to save waypoints - " + var8.getMessage());
            var8.printStackTrace();
         }
      }
   }

   public static void loadWaypoints() {
      waypoints.clear();
      File waypointsFile = getWaypointsFile();
      if (waypointsFile.exists()) {
         loadWaypointsFromFile(waypointsFile);
      } else {
         maybeImportLegacyWaypointsOnce();
      }
   }

   private static void maybeImportLegacyWaypointsOnce() {
      if (LEGACY_WAYPOINTS_FILE.exists() && !LEGACY_MIGRATED_MARKER.exists()) {
         loadWaypointsFromFile(LEGACY_WAYPOINTS_FILE);
         if (!waypoints.isEmpty()) {
            saveWaypoints();
         }

         File parent = SERVERS_DIR;
         if (!parent.exists()) {
            parent.mkdirs();
         }

         try {
            LEGACY_MIGRATED_MARKER.createNewFile();
         } catch (IOException var2) {
         }
      }
   }

   private static void loadWaypointsFromFile(File file) {
      try {
         try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
               sb.append(line.trim());
            }

            String content = sb.toString().trim();
            if (content.startsWith("[")) {
               content = content.substring(1);
            }

            if (content.endsWith("]")) {
               content = content.substring(0, content.length() - 1);
            }

            content = content.trim();
            if (!content.isEmpty()) {
               for (String obj : splitJsonObjects(content)) {
                  try {
                     WaypointManager.Waypoint wp = parseWaypoint(obj.trim());
                     if (wp != null) {
                        waypoints.add(wp);
                     }
                  } catch (Exception var10) {
                     System.err.println("[VoidCyan] WaypointManager: skipping malformed waypoint entry - " + var10.getMessage());
                  }
               }

               return;
            }
         }
      } catch (IOException var12) {
         System.err.println("[VoidCyan] WaypointManager: failed to load waypoints from " + file.getPath() + " - " + var12.getMessage());
         var12.printStackTrace();
      }
   }

   private static String waypointToJson(WaypointManager.Waypoint wp) {
      return "{\"name\":"
         + jsonString(wp.name)
         + ",\"x\":"
         + wp.x
         + ",\"y\":"
         + wp.y
         + ",\"z\":"
         + wp.z
         + ",\"dimension\":"
         + jsonString(wp.dimension)
         + ",\"color\":"
         + wp.color
         + ",\"enabled\":"
         + wp.enabled
         + ",\"renderThroughWalls\":"
         + wp.renderThroughWalls
         + ",\"fillOpacity\":"
         + wp.fillOpacity
         + ",\"outlineOpacity\":"
         + wp.outlineOpacity
         + ",\"showLabel\":"
         + wp.showLabel
         + ",\"showBlockDisplay\":"
         + wp.showBlockDisplay
         + ",\"iconItem\":"
         + jsonString(wp.iconItem)
         + "}";
   }

   private static String jsonString(String value) {
      if (value == null) {
         return "\"\"";
      } else {
         String escaped = value.replace("\\", "\\\\").replace("\"", "\\\"");
         return "\"" + escaped + "\"";
      }
   }

   private static List<String> splitJsonObjects(String body) {
      List<String> result = new ArrayList<>();
      int depth = 0;
      int start = -1;

      for (int i = 0; i < body.length(); i++) {
         char c = body.charAt(i);
         if (c == '{') {
            if (depth == 0) {
               start = i;
            }

            depth++;
         } else if (c == '}') {
            if (--depth == 0 && start != -1) {
               result.add(body.substring(start, i + 1));
               start = -1;
            }
         }
      }

      return result;
   }

   private static WaypointManager.Waypoint parseWaypoint(String json) {
      if (json.startsWith("{")) {
         json = json.substring(1);
      }

      if (json.endsWith("}")) {
         json = json.substring(0, json.length() - 1);
      }

      String name = parseStringField(json, "name");
      String dimension = parseStringField(json, "dimension");
      int x = parseIntField(json, "x", 0);
      int y = parseIntField(json, "y", 64);
      int z = parseIntField(json, "z", 0);
      int color = parseIntField(json, "color", -1);
      boolean enabled = parseBooleanField(json, "enabled", true);
      boolean rtw = parseBooleanField(json, "renderThroughWalls", true);
      int fillOp = parseIntField(json, "fillOpacity", 10);
      int outOp = parseIntField(json, "outlineOpacity", 100);
      boolean slbl = parseBooleanField(json, "showLabel", true);
      boolean sbk = parseBooleanField(json, "showBlockDisplay", true);
      String iconItem = parseStringField(json, "iconItem");
      if (iconItem.isEmpty()) {
         iconItem = "minecraft:compass";
      }

      return new WaypointManager.Waypoint(name, x, y, z, dimension, color, enabled, rtw, fillOp, outOp, slbl, sbk, iconItem);
   }

   private static String parseStringField(String json, String key) {
      String searchKey = "\"" + key + "\":\"";
      int keyStart = json.indexOf(searchKey);
      if (keyStart == -1) {
         return "";
      } else {
         int valueStart = keyStart + searchKey.length();
         StringBuilder sb = new StringBuilder();

         for (int i = valueStart; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '\\' && i + 1 < json.length()) {
               char next = json.charAt(i + 1);
               if (next == '"') {
                  sb.append('"');
                  i++;
               } else if (next == '\\') {
                  sb.append('\\');
                  i++;
               } else {
                  sb.append(next);
                  i++;
               }
            } else {
               if (c == '"') {
                  break;
               }

               sb.append(c);
            }
         }

         return sb.toString();
      }
   }

   private static int parseIntField(String json, String key, int defaultValue) {
      String searchKey = "\"" + key + "\":";
      int keyStart = json.indexOf(searchKey);
      if (keyStart == -1) {
         return defaultValue;
      } else {
         int valueStart = keyStart + searchKey.length();
         StringBuilder sb = new StringBuilder();

         for (int i = valueStart; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '-' && sb.length() == 0) {
               sb.append(c);
            } else {
               if (!Character.isDigit(c)) {
                  break;
               }

               sb.append(c);
            }
         }

         if (sb.length() == 0) {
            return defaultValue;
         } else {
            try {
               return Integer.parseInt(sb.toString());
            } catch (NumberFormatException var9) {
               return defaultValue;
            }
         }
      }
   }

   private static boolean parseBooleanField(String json, String key, boolean defaultValue) {
      String searchKey = "\"" + key + "\":";
      int keyStart = json.indexOf(searchKey);
      if (keyStart == -1) {
         return defaultValue;
      } else {
         int valueStart = keyStart + searchKey.length();
         if (json.startsWith("true", valueStart)) {
            return true;
         } else {
            return json.startsWith("false", valueStart) ? false : defaultValue;
         }
      }
   }

   public static class Waypoint {
      public String name;
      public int x;
      public int y;
      public int z;
      public String dimension;
      public int color;
      public boolean enabled;
      public boolean renderThroughWalls;
      public int fillOpacity;
      public int outlineOpacity;
      public boolean showLabel;
      public boolean showBlockDisplay;
      public String iconItem;

      public Waypoint(
         String name,
         int x,
         int y,
         int z,
         String dimension,
         int color,
         boolean enabled,
         boolean renderThroughWalls,
         int fillOpacity,
         int outlineOpacity,
         boolean showLabel,
         boolean showBlockDisplay,
         String iconItem
      ) {
         this.name = name;
         this.x = x;
         this.y = y;
         this.z = z;
         this.dimension = dimension;
         this.color = color;
         this.enabled = enabled;
         this.renderThroughWalls = renderThroughWalls;
         this.fillOpacity = fillOpacity;
         this.outlineOpacity = outlineOpacity;
         this.showLabel = showLabel;
         this.showBlockDisplay = showBlockDisplay;
         this.iconItem = iconItem != null ? iconItem : "minecraft:compass";
      }

      @Override
      public String toString() {
         return "Waypoint{name='"
            + this.name
            + "', x="
            + this.x
            + ", y="
            + this.y
            + ", z="
            + this.z
            + ", dimension='"
            + this.dimension
            + "', color="
            + this.color
            + ", enabled="
            + this.enabled
            + ", renderThroughWalls="
            + this.renderThroughWalls
            + ", fillOpacity="
            + this.fillOpacity
            + ", outlineOpacity="
            + this.outlineOpacity
            + ", showLabel="
            + this.showLabel
            + ", showBlockDisplay="
            + this.showBlockDisplay
            + ", iconItem='"
            + this.iconItem
            + "'}";
      }
   }
}
