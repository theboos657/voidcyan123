package com.voidcyan.client.module;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public final class MmoArmorHudRenderer {

   private MmoArmorHudRenderer() {
   }

   public static void render(DrawContext context) {
      if (VoidCyanClient.isTargetHudEnabled) {
         LivingEntity target = TargetHudRenderer.getCurrentTarget();
         if (target != null) {
            drawHelmet(context, getArmorColor(target.getEquippedStack(EquipmentSlot.HEAD)));
            drawChestplate(context, getArmorColor(target.getEquippedStack(EquipmentSlot.CHEST)));
            drawLeggings(context, getArmorColor(target.getEquippedStack(EquipmentSlot.LEGS)));
            drawBoots(context, getArmorColor(target.getEquippedStack(EquipmentSlot.FEET)));
         }
      }
   }

   private static void drawHelmet(DrawContext context, int color) {
      fillOutlined(context, 19, 0, 43, 17, color);
      context.fill(23, 5, 39, 17, shade(color, 0.84F));
      context.fill(19, 16, 25, 21, color);
      context.fill(37, 16, 43, 21, color);
      context.fill(25, 13, 37, 21, color);
   }

   private static void drawChestplate(DrawContext context, int color) {
      fillOutlined(context, 5, 30, 57, 63, color);
      context.fill(5, 30, 13, 49, color);
      context.fill(49, 30, 57, 49, color);
      context.fill(13, 50, 49, 73, color);
      int shadow = shade(color, 0.84F);
      context.fill(16, 34, 46, 53, shadow);
      context.fill(19, 53, 43, 70, shadow);
      context.fill(25, 30, 37, 36, shade(color, 1.2F));
   }

   private static void drawLeggings(DrawContext context, int color) {
      fillOutlined(context, 16, 78, 30, 111, color);
      fillOutlined(context, 32, 78, 46, 111, color);
      context.fill(18, 81, 29, 108, shade(color, 0.84F));
      context.fill(34, 81, 45, 108, shade(color, 0.84F));
      context.fill(16, 78, 30, 82, shade(color, 1.2F));
      context.fill(32, 78, 46, 82, shade(color, 1.2F));
   }

   private static void drawBoots(DrawContext context, int color) {
      fillOutlined(context, 15, 115, 30, 126, color);
      fillOutlined(context, 32, 115, 47, 126, color);
      context.fill(15, 122, 30, 126, shade(color, 0.8F));
      context.fill(32, 122, 47, 126, shade(color, 0.8F));
      context.fill(18, 115, 30, 119, shade(color, 1.2F));
      context.fill(35, 115, 47, 119, shade(color, 1.2F));
   }

   private static void fillOutlined(DrawContext context, int x1, int y1, int x2, int y2, int color) {
      context.fill(x1 - 1, y1 - 1, x2 + 1, y2 + 1, -15724008);
      context.fill(x1, y1, x2, y2, color);
   }

   private static int getArmorColor(ItemStack stack) {
      if (stack.isEmpty()) {
         return -14342095;
      } else {
         float durability = stack.isDamageable() ? (float)(stack.getMaxDamage() - stack.getDamage()) / stack.getMaxDamage() : 1.0F;
         if (durability > 0.6F) {
            return -13244022;
         } else if (durability > 0.4F) {
            return -11442;
         } else {
            return durability > 0.2F ? -28339 : -54006;
         }
      }
   }

   private static int shade(int color, float multiplier) {
      int red = Math.min(255, Math.round((color >> 16 & 0xFF) * multiplier));
      int green = Math.min(255, Math.round((color >> 8 & 0xFF) * multiplier));
      int blue = Math.min(255, Math.round((color & 0xFF) * multiplier));
      return 0xFF000000 | red << 16 | green << 8 | blue;
   }
}
