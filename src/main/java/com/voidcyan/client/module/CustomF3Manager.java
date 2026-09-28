package com.voidcyan.client.module;

import com.voidcyan.client.VoidCyanClient;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.debug.DebugHudEntries;
import net.minecraft.client.gui.hud.debug.DebugHudEntry;
import net.minecraft.client.gui.hud.debug.DebugHudEntryCategory;
import net.minecraft.client.gui.hud.debug.DebugHudEntryVisibility;
import net.minecraft.client.gui.hud.debug.DebugProfileType;
import net.minecraft.util.Identifier;

public final class CustomF3Manager {
   private static final Map<String, Boolean> ENTRY_TOGGLES = new HashMap<>();
   private static final List<Identifier> SORTED_ENTRIES = new ArrayList<>();
   private static boolean entriesInitialized = false;
   private static final File F3_CONFIG_FILE = new File("config/voidcyan_f3.json");

   private CustomF3Manager() {
   }

   private static void ensureEntriesInitialized() {
      if (!entriesInitialized) {
         entriesInitialized = true;
         SORTED_ENTRIES.clear();

         for (Identifier id : DebugHudEntries.getEntries().keySet()) {
            DebugHudEntry entry = DebugHudEntries.get(id);
            if (entry != null && entry.getCategory() == DebugHudEntryCategory.TEXT) {
               SORTED_ENTRIES.add(id);
            }
         }

         SORTED_ENTRIES.sort(Comparator.comparing(idx -> formatEntryName(idx).toLowerCase()));
      }
   }

   private static String configKey(Identifier id) {
      return id.getNamespace() + "." + id.getPath();
   }

   private static String propsKey(Identifier id) {
      return "customF3Entry." + configKey(id);
   }

   public static boolean isEntryEnabled(Identifier id) {
      ensureEntriesInitialized();
      return ENTRY_TOGGLES.getOrDefault(configKey(id), getDefaultEnabled(id));
   }

   public static void syncAllToVanillaProfile(MinecraftClient client) {
      if (VoidCyanClient.isCustomF3Enabled && client != null && client.debugHudEntryList != null) {
         ensureEntriesInitialized();

         for (Identifier id : SORTED_ENTRIES) {
            boolean enabled = isEntryEnabled(id);
            client.debugHudEntryList.setEntryVisibility(id, enabled ? DebugHudEntryVisibility.IN_OVERLAY : DebugHudEntryVisibility.NEVER);
         }
      }
   }

   private static boolean getDefaultEnabled(Identifier id) {
      Map<Identifier, DebugHudEntryVisibility> defaults = (Map<Identifier, DebugHudEntryVisibility>)DebugHudEntries.PROFILES.get(DebugProfileType.DEFAULT);
      DebugHudEntryVisibility visibility = defaults.get(id);
      return visibility == DebugHudEntryVisibility.ALWAYS_ON || visibility == DebugHudEntryVisibility.IN_OVERLAY;
   }

   public static void load(Properties props) {
      ensureEntriesInitialized();
      ENTRY_TOGGLES.clear();
      loadFromJsonFile();

      for (Identifier id : SORTED_ENTRIES) {
         String key = propsKey(id);
         String legacyKey = "customF3Entry." + id;
         if (props.containsKey(key)) {
            ENTRY_TOGGLES.put(configKey(id), Boolean.parseBoolean(props.getProperty(key)));
         } else if (props.containsKey(legacyKey)) {
            ENTRY_TOGGLES.put(configKey(id), Boolean.parseBoolean(props.getProperty(legacyKey)));
         }
      }
   }

   public static void save(Properties props) {
      ensureEntriesInitialized();

      for (Identifier id : SORTED_ENTRIES) {
         props.setProperty(propsKey(id), String.valueOf(isEntryEnabled(id)));
      }

      persist();
   }

   private static void loadFromJsonFile() {
      if (F3_CONFIG_FILE.exists()) {
         try {
            try (BufferedReader reader = new BufferedReader(new FileReader(F3_CONFIG_FILE))) {
               StringBuilder sb = new StringBuilder();

               String line;
               while ((line = reader.readLine()) != null) {
                  sb.append(line.trim());
               }

               String content = sb.toString().trim();
               if (content.length() >= 2) {
                  for (Identifier id : SORTED_ENTRIES) {
                     String entryKey = configKey(id);
                     String search = "\"" + entryKey + "\":";
                     int idx = content.indexOf(search);
                     if (idx != -1) {
                        int valueStart = idx + search.length();

                        while (valueStart < content.length() && Character.isWhitespace(content.charAt(valueStart))) {
                           valueStart++;
                        }

                        if (valueStart < content.length()) {
                           if (content.startsWith("true", valueStart)) {
                              ENTRY_TOGGLES.put(entryKey, true);
                           } else if (content.startsWith("false", valueStart)) {
                              ENTRY_TOGGLES.put(entryKey, false);
                           }
                        }
                     }
                  }

                  return;
               }
            }
         } catch (IOException var12) {
         }
      }
   }

   private static void persist() {
      ensureEntriesInitialized();
      File parent = F3_CONFIG_FILE.getParentFile();
      if (parent != null && !parent.exists()) {
         parent.mkdirs();
      }

      StringBuilder sb = new StringBuilder();
      sb.append("{\n");
      sb.append("  \"entries\": {\n");

      for (int i = 0; i < SORTED_ENTRIES.size(); i++) {
         Identifier id = SORTED_ENTRIES.get(i);
         String key = configKey(id);
         sb.append("    \"").append(key).append("\": ").append(isEntryEnabled(id));
         if (i < SORTED_ENTRIES.size() - 1) {
            sb.append(",");
         }

         sb.append("\n");
      }

      sb.append("  }\n");
      sb.append("}\n");

      try (BufferedWriter writer = new BufferedWriter(new FileWriter(F3_CONFIG_FILE))) {
         writer.write(sb.toString());
      } catch (IOException var7) {
      }
   }

   public static String formatEntryName(Identifier id) {
      String path = id.getPath();
      StringBuilder sb = new StringBuilder();

      for (String part : path.split("_")) {
         if (!part.isEmpty()) {
            if (!sb.isEmpty()) {
               sb.append(' ');
            }

            sb.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
               sb.append(part.substring(1));
            }
         }
      }

      return sb.toString();
   }
}
