package com.voidcyan.client.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

/**
 * Recipe lookup that reads vanilla recipe JSONs straight out of the game jar (works on any server,
 * unlike the recipe book which only knows unlocked recipes).
 */
public final class RecipeIndex {
   public record Recipe(String type, String result, int count, List<List<String>> slots) {
   }

   private static List<Recipe> all;

   private RecipeIndex() {
   }

   private static synchronized List<Recipe> all() {
      if (all != null) return all;
      List<Recipe> out = new ArrayList<>();
      try {
         Path root = FabricLoader.getInstance().getModContainer("minecraft").orElseThrow().getRootPaths().get(0);
         Path dir = root.resolve("data/minecraft/recipe");
         if (!Files.isDirectory(dir)) dir = root.resolve("data/minecraft/recipes");
         try (Stream<Path> files = Files.list(dir)) {
            for (Path f : (Iterable<Path>) files::iterator) {
               if (!f.toString().endsWith(".json")) continue;
               try {
                  Recipe r = parse(JsonParser.parseString(Files.readString(f, StandardCharsets.UTF_8)).getAsJsonObject());
                  if (r != null) out.add(r);
               } catch (Exception ignored) {
               }
            }
         }
      } catch (Exception e) {
         System.err.println("[VoidCyan] Could not read recipes: " + e);
      }

      all = out;
      return out;
   }

   private static String resultId(JsonElement e) {
      if (e == null) return null;
      if (e.isJsonPrimitive()) return e.getAsString();
      JsonObject o = e.getAsJsonObject();
      if (o.has("id")) return o.get("id").getAsString();
      if (o.has("item")) return o.get("item").getAsString();
      return null;
   }

   private static List<String> alternatives(JsonElement e) {
      List<String> out = new ArrayList<>();
      if (e == null || e.isJsonNull()) return out;
      if (e.isJsonPrimitive()) {
         out.add(e.getAsString());
      } else if (e.isJsonArray()) {
         for (JsonElement x : e.getAsJsonArray()) out.addAll(alternatives(x));
      } else if (e.isJsonObject()) {
         JsonObject o = e.getAsJsonObject();
         if (o.has("item")) out.add(o.get("item").getAsString());
         else if (o.has("tag")) out.add("#" + o.get("tag").getAsString());
      }

      return out;
   }

   private static Recipe parse(JsonObject o) {
      String type = o.has("type") ? o.get("type").getAsString().replace("minecraft:", "") : "";
      List<List<String>> slots = new ArrayList<>();
      String res;
      int count = 1;
      switch (type) {
         case "crafting_shaped" -> {
            JsonObject key = o.getAsJsonObject("key");
            Map<String, List<String>> used = new LinkedHashMap<>();
            for (JsonElement row : o.getAsJsonArray("pattern")) {
               for (char ch : row.getAsString().toCharArray()) {
                  if (ch != ' ' && key.has(String.valueOf(ch))) used.putIfAbsent(String.valueOf(ch), alternatives(key.get(String.valueOf(ch))));
               }
            }

            slots.addAll(used.values());
         }
         case "crafting_shapeless" -> {
            for (JsonElement e : o.getAsJsonArray("ingredients")) slots.add(alternatives(e));
         }
         case "smelting", "blasting", "smoking", "campfire_cooking", "stonecutting" -> slots.add(alternatives(o.get("ingredient")));
         case "smithing_transform" -> {
            slots.add(alternatives(o.get("template")));
            slots.add(alternatives(o.get("base")));
            slots.add(alternatives(o.get("addition")));
         }
         default -> {
            return null;
         }
      }

      res = resultId(o.get("result"));
      if (o.has("result") && o.get("result").isJsonObject() && o.getAsJsonObject("result").has("count")) count = o.getAsJsonObject("result").get("count").getAsInt();
      if (res == null || slots.isEmpty()) return null;
      return new Recipe(type, res.replace("minecraft:", ""), count, slots);
   }

   /** Resolves one ingredient slot to a displayable item (first alternative; tags use their first member). */
   public static Item firstItem(List<String> alts) {
      for (String a : alts) {
         if (a.startsWith("#")) {
            var tag = Registries.ITEM.getOptional(TagKey.of(RegistryKeys.ITEM, Identifier.of(a.substring(1))));
            if (tag.isPresent() && tag.get().size() > 0) return tag.get().get(0).value();
         } else {
            Item it = Registries.ITEM.get(Identifier.of(a));
            if (it != null && it != net.minecraft.item.Items.AIR) return it;
         }
      }

      return net.minecraft.item.Items.AIR;
   }

   private static boolean slotHas(List<String> alts, Item item) {
      Identifier id = Registries.ITEM.getId(item);
      for (String a : alts) {
         if (a.startsWith("#")) {
            if (new ItemStack(item).isIn(TagKey.of(RegistryKeys.ITEM, Identifier.of(a.substring(1))))) return true;
         } else if (Identifier.of(a).equals(id)) {
            return true;
         }
      }

      return false;
   }

   public static List<Recipe> making(Item item) {
      String path = Registries.ITEM.getId(item).getPath();
      List<Recipe> out = new ArrayList<>();
      for (Recipe r : all()) if (r.result().equals(path)) out.add(r);
      return out;
   }

   public static List<Recipe> using(Item item) {
      List<Recipe> out = new ArrayList<>();
      for (Recipe r : all()) {
         for (List<String> s : r.slots()) {
            if (slotHas(s, item)) {
               out.add(r);
               break;
            }
         }
      }

      return out;
   }

   public static String typeLabel(String type) {
      return switch (type) {
         case "crafting_shaped", "crafting_shapeless" -> "Crafting";
         case "smelting" -> "Furnace";
         case "blasting" -> "Blast";
         case "smoking" -> "Smoker";
         case "campfire_cooking" -> "Campfire";
         case "stonecutting" -> "Stonecut";
         case "smithing_transform" -> "Smithing";
         default -> type;
      };
   }
}
