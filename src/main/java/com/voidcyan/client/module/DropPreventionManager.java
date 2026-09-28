package com.voidcyan.client.module;

import com.voidcyan.client.VoidCyanClient;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class DropPreventionManager {
   private static final Set<String> itemsToPrevent = new HashSet<>();
   private static String lastLoadedItems = "";

   public static void render(DrawContext context) {
      if (VoidCyanClient.isDropPreventionEnabled) {
         MinecraftClient client = MinecraftClient.getInstance();
         if (client.player != null) {
            loadPreventItems();
         }
      }
   }

   public static void loadPreventItems() {
      if (VoidCyanClient.dropPreventionItems != null && !VoidCyanClient.dropPreventionItems.equals(lastLoadedItems)) {
         lastLoadedItems = VoidCyanClient.dropPreventionItems;
         itemsToPrevent.clear();
         if (!VoidCyanClient.dropPreventionItems.isEmpty()) {
            String[] itemIds = VoidCyanClient.dropPreventionItems.split("[,\\s;]+");

            for (String itemId : itemIds) {
               itemId = itemId.trim();
               if (!itemId.isEmpty()) {
                  itemsToPrevent.add(itemId);
               }
            }
         }
      }
   }

   public static boolean shouldPreventDrop(ItemStack stack) {
      if (!VoidCyanClient.isDropPreventionEnabled) {
         return false;
      } else {
         loadPreventItems();
         if (itemsToPrevent.isEmpty()) {
            return false;
         } else if (stack != null && !stack.isEmpty()) {
            Identifier id = Registries.ITEM.getId(stack.getItem());
            String fullId = id.toString();
            String pathId = id.getPath();
            boolean prevent = itemsToPrevent.contains(fullId) || itemsToPrevent.contains(pathId);
            if (prevent) {
               MinecraftClient.getInstance().inGameHud.setOverlayMessage(Text.literal("§c[Drop Prevention] §fPrevented dropping §e" + pathId), false);
            }

            return prevent;
         } else {
            return false;
         }
      }
   }

   public static boolean shouldPreventDrop(String itemId) {
      if (!VoidCyanClient.isDropPreventionEnabled) {
         return false;
      } else {
         loadPreventItems();
         return itemsToPrevent.isEmpty() ? false : itemsToPrevent.contains(itemId) || itemsToPrevent.contains(itemId.replace("minecraft:", ""));
      }
   }
}
