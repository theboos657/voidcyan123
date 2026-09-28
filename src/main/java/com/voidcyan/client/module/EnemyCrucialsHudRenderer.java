package com.voidcyan.client.module;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public final class EnemyCrucialsHudRenderer {
   public static final int WIDTH = 126;
   private static final int ROW_HEIGHT = 18;

   private EnemyCrucialsHudRenderer() {
   }

   public static void render(DrawContext context) {
      if (VoidCyanClient.isTargetHudEnabled && VoidCyanClient.targetHudShowEnemyCrucials) {
         TargetHudRenderer.trackNearbyPlayers();
         MinecraftClient client = MinecraftClient.getInstance();
         if (TargetHudRenderer.getCurrentTarget() instanceof PlayerEntity player) {
            int cyan = VoidCyanClient.getPrimaryColor();
            drawPlayer(context, client, player, 0, cyan);
         }
      }
   }

   private static void drawPlayer(DrawContext context, MinecraftClient client, PlayerEntity player, int y, int cyan) {
      int height = getPlayerHeight();
      context.fill(0, y, 126, y + height, -1610612736);
      context.fill(0, y, 126, y + 1, cyan);
      context.fill(0, y + height - 1, 126, y + height, cyan);
      context.fill(0, y + 1, 1, y + height - 1, cyan);
      context.fill(125, y + 1, 126, y + height - 1, cyan);
      String name = player.getName().getString();

      while (client.textRenderer.getWidth(name) > 118 && name.length() > 1) {
         name = name.substring(0, name.length() - 1);
      }

      context.drawTextWithShadow(client.textRenderer, name, 5, y + 4, -1);
      int rowY = y + 17;
      if (VoidCyanClient.targetHudShowEnemyPearls) {
         drawRow(context, client, Items.ENDER_PEARL, "Pearls", TargetHudRenderer.getTrackedItemCount(player, Items.ENDER_PEARL), rowY);
         rowY += 18;
      }

      if (VoidCyanClient.targetHudShowEnemyGapples) {
         int gapples = TargetHudRenderer.getTrackedItemCount(player, Items.GOLDEN_APPLE)
            + TargetHudRenderer.getTrackedItemCount(player, Items.ENCHANTED_GOLDEN_APPLE);
         drawRow(context, client, Items.GOLDEN_APPLE, "Gaps", gapples, rowY);
         rowY += 18;
      }

      if (VoidCyanClient.targetHudShowEnemyCobwebs) {
         drawRow(context, client, Items.COBWEB, "Cobwebs", TargetHudRenderer.getTrackedItemCount(player, Items.COBWEB), rowY);
         rowY += 18;
      }

      if (VoidCyanClient.targetHudShowEnemyTotems) {
         drawRow(context, client, Items.TOTEM_OF_UNDYING, "Totems", TargetHudRenderer.getTrackedItemCount(player, Items.TOTEM_OF_UNDYING), rowY);
         rowY += 18;
      }

      if (VoidCyanClient.targetHudShowEnemyPops) {
         drawRow(context, client, Items.TOTEM_OF_UNDYING, "Totem Pop", TargetHudRenderer.getTotemPops(player), rowY);
         rowY += 18;
      }

      if (VoidCyanClient.targetHudShowEnemyWindCharges) {
         drawRow(context, client, Items.WIND_CHARGE, "Wind Charges", TargetHudRenderer.getTrackedItemCount(player, Items.WIND_CHARGE), rowY);
         rowY += 18;
      }

      if (VoidCyanClient.targetHudShowEnemyWindChargesUsed) {
         drawRow(context, client, Items.WIND_CHARGE, "Winds Used", TargetHudRenderer.getWindChargesUsed(player), rowY);
         rowY += 18;
      }

      if (VoidCyanClient.targetHudShowEnemyHealthPots) {
         drawRow(context, client, Items.SPLASH_POTION, "Health Pots", TargetHudRenderer.countHealthPots(player), rowY);
      }
   }

   public static int getHudHeight() {
      return getPlayerHeight();
   }

   private static int getPlayerHeight() {
      int rows = 0;
      if (VoidCyanClient.targetHudShowEnemyPearls) {
         rows++;
      }

      if (VoidCyanClient.targetHudShowEnemyGapples) {
         rows++;
      }

      if (VoidCyanClient.targetHudShowEnemyCobwebs) {
         rows++;
      }

      if (VoidCyanClient.targetHudShowEnemyTotems) {
         rows++;
      }

      if (VoidCyanClient.targetHudShowEnemyPops) {
         rows++;
      }

      if (VoidCyanClient.targetHudShowEnemyWindCharges) {
         rows++;
      }

      if (VoidCyanClient.targetHudShowEnemyWindChargesUsed) {
         rows++;
      }

      if (VoidCyanClient.targetHudShowEnemyHealthPots) {
         rows++;
      }

      return Math.max(1, rows) * 18 + 20;
   }

   private static void drawRow(DrawContext context, MinecraftClient client, Item item, String label, int count, int y) {
      context.drawItem(new ItemStack(item), 5, y);
      context.drawTextWithShadow(client.textRenderer, label + ": " + count, 25, y + 4, -1);
   }
}
