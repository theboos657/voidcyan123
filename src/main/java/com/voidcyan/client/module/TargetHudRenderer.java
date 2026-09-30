package com.voidcyan.client.module;

import com.voidcyan.client.VoidCyanClient;
import com.voidcyan.client.screen.EditHudScreen;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.StreamSupport;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;

public class TargetHudRenderer {
   private static final Map<UUID, Integer> totemPops = new HashMap<>();
   private static final Map<UUID, TargetHudRenderer.TrackedItems> trackedItems = new HashMap<>();
   private static final Map<UUID, Integer> totemOverrides = new HashMap<>();
   private static final Map<UUID, Integer> windChargesUsed = new HashMap<>();
   private static float animProgress = 0.0F;
   private static LivingEntity stickyTarget = null;
   private static long stickyLastSeen = 0L;

   public static void onPearlThrown(double x, double y, double z) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.world != null && client.player != null) {
         PlayerEntity closest = null;
         double closestDist = 16.0;

         for (PlayerEntity player : client.world.getPlayers()) {
            double dx = player.getX() - x;
            double dy = player.getEyeY() - y;
            double dz = player.getZ() - z;
            double distSq = dx * dx + dy * dy + dz * dz;
            if (distSq < closestDist) {
               closestDist = distSq;
               closest = player;
            }
         }

         if (closest != null && closest != client.player) {
            TargetHudRenderer.TrackedItems tracked = trackedItems.get(closest.getUuid());
            if (tracked != null) {
               int current = tracked.knownCounts.getOrDefault(Items.ENDER_PEARL, 0);
               if (current > 0) {
                  tracked.knownCounts.put(Items.ENDER_PEARL, current - 1);
               }
            }
         }
      }
   }

   public static void render(DrawContext context) {
      render(context, VoidCyanClient.targetHudX, VoidCyanClient.targetHudY, VoidCyanClient.targetHudScale);
   }

   public static void renderAboveHead(DrawContext context) {
      if (!VoidCyanClient.isTargetHudEnabled) {
         return;
      }
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player == null || client.world == null) {
         return;
      }

      LivingEntity target = getCurrentTarget();
      if (target == null) {
         return;
      }

      // If in edit/menu screen, don't project onto player if camera is first person
      if (target == client.player && client.options.getPerspective().isFirstPerson()) {
         return;
      }

      double ex = target.getX();
      double ey = target.getY() + target.getHeight() + 0.35;
      double ez = target.getZ();

      Vec3d projected = client.gameRenderer.project(new Vec3d(ex, ey, ez));
      if (projected == null || projected.z <= 0.0 || projected.z > 1.0) {
         return;
      }

      int screenW = client.getWindow().getScaledWidth();
      int screenH = client.getWindow().getScaledHeight();
      int screenX = (int)((projected.x * 0.5 + 0.5) * screenW);
      int screenY = (int)((1.0 - (projected.y * 0.5 + 0.5)) * screenH);

      if (screenX < -100 || screenX > screenW + 100 || screenY < -100 || screenY > screenH + 100) {
         return;
      }

      double dx = ex - client.gameRenderer.getCamera().getCameraPos().x;
      double dy = ey - client.gameRenderer.getCamera().getCameraPos().y;
      double dz = ez - client.gameRenderer.getCamera().getCameraPos().z;
      double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
      float baseScale = (float)Math.clamp(1.0 - (dist - 3.0) * 0.035, 0.45, 0.9);
      float scale = baseScale * VoidCyanClient.targetHudAboveHeadScale;

      String name = target.getName().getString();
      int nameW = client.textRenderer.getWidth(name);
      String distStr = (int)dist + "m";
      int distW = client.textRenderer.getWidth(" " + distStr);

      float health = target.getHealth();
      float maxHealth = target.getMaxHealth();

      int padX = 4;
      int padY = 3;
      int barH = 3;
      int totalW = nameW + distW + padX * 2;
      int totalH = padY + 9 + 2 + barH + padY;

      int drawX = screenX - (int)(totalW * scale / 2f);
      int drawY = screenY - (int)(totalH * scale);

      context.getMatrices().pushMatrix();
      context.getMatrices().translate(drawX + totalW * scale / 2f, drawY + totalH * scale / 2f);
      context.getMatrices().scale(scale, scale);
      context.getMatrices().translate(-(totalW / 2f), -(totalH / 2f));

      // Background: clean dark translucent pill matching screenshot
      int bg = 0xB0000000;
      context.fill(0, 0, totalW, totalH, bg);

      // Name: white with shadow
      context.drawTextWithShadow(client.textRenderer, name, padX, padY, 0xFFFFFFFF);

      // Distance: muted gray with shadow right next to name like "Kara0541 4m"
      context.drawTextWithShadow(client.textRenderer, " " + distStr, padX + nameW, padY, 0xFFAAAAAA);

      // Health bar track
      int barY = padY + 9 + 2;
      int barW = totalW - padX * 2;
      context.fill(padX, barY, padX + barW, barY + barH, 0x60000000);

      // Health bar fill (yellow/green based on HP, matching screenshot)
      float pct = Math.clamp(health / maxHealth, 0f, 1f);
      int barColor;
      if (pct > 0.65f) {
         barColor = 0xFF55FF55; // green
      } else if (pct > 0.25f) {
         barColor = 0xFFFFFF55; // yellow like reference image
      } else {
         barColor = 0xFFFF5555; // red
      }
      int fillW = (int)(barW * pct);
      if (fillW > 0) {
         context.fill(padX, barY, padX + fillW, barY + barH, barColor);
      }

      context.getMatrices().popMatrix();
   }



   public static void render(DrawContext context, int targetX, int targetY, float scale) {
      if (!VoidCyanClient.isTargetHudEnabled) {
         animProgress = 0.0F;
      } else {
         MinecraftClient client = MinecraftClient.getInstance();
         if (client.player != null && client.world != null) {
            LivingEntity target = getCurrentTarget();
            boolean shouldShow = target != null;
            if (shouldShow) {
               animProgress = animProgress + (1.0F - animProgress) * 0.18F;
               if (animProgress > 0.999F) {
                  animProgress = 1.0F;
               }
            } else {
               animProgress = animProgress - animProgress * 0.1F;
               if (animProgress < 0.001F) {
                  animProgress = 0.0F;
               }
            }

            if (!(animProgress <= 0.0F) && target != null) {
               int width = 160;
               int baseHeight = VoidCyanClient.targetHudShowArmor ? 55 : 45;
               float eased = easeOutBack(animProgress);
               float popScale = Math.max(0.001F, eased);
               int alpha = (int)(animProgress * 255.0F);
               alpha = Math.max(0, Math.min(255, alpha));
               float cx = width / 2.0F;
               float cy = baseHeight / 2.0F;
               context.getMatrices().pushMatrix();
               context.getMatrices().translate(cx, cy);
               context.getMatrices().scale(popScale, popScale);
               context.getMatrices().translate(-cx, -cy);
               int x = 0;
               int y = 0;
               int bg = withAlpha(0, (int)(alpha * 0.5F));
               int cyan = withAlpha(VoidCyanClient.getPrimaryColor(), alpha);
               context.fill(x, y, x + width, y + baseHeight, bg);
               context.fill(x, y, x + width, y + 1, cyan);
               context.fill(x, y + baseHeight - 1, x + width, y + baseHeight, cyan);
               context.fill(x, y + 1, x + 1, y + baseHeight - 1, cyan);
               context.fill(x + width - 1, y + 1, x + width, y + baseHeight - 1, cyan);
               float hudScale = scale * popScale;
               int screenBaseX = (int)(targetX + cx * scale - cx * hudScale);
               int screenBaseY = (int)(targetY + cy * scale - cy * hudScale);
               int localBoxX1 = x + 4;
               int localBoxY1 = y + 4;
               int localBoxX2 = x + 36;
               int localBoxY2 = y + baseHeight - 4;
               int screenBoxX1 = screenBaseX + (int)(localBoxX1 * hudScale);
               int screenBoxY1 = screenBaseY + (int)(localBoxY1 * hudScale);
               int screenBoxX2 = screenBaseX + (int)(localBoxX2 * hudScale);
               int screenBoxY2 = screenBaseY + (int)(localBoxY2 * hudScale);
               int modelSize = VoidCyanClient.targetHudShowArmor ? 20 : 16;
               InventoryScreen.drawEntity(context, screenBoxX1, screenBoxY1, screenBoxX2, screenBoxY2, modelSize, 0.0625F, 0.0F, 0.0F, target);
               int textOffsetX = 42;
               int nameColor = withAlpha(16777215, alpha);
               int healthColor = withAlpha(16733525, alpha);
               String name = target.getName().getString();
               context.drawTextWithShadow(client.textRenderer, name, x + textOffsetX, y + 5, nameColor);
               float health = target.getHealth();
               float maxHealth = target.getMaxHealth();
               String healthText = String.format("Health: %.1f / %.1f", health, maxHealth);
               context.drawTextWithShadow(client.textRenderer, healthText, x + textOffsetX, y + 15, healthColor);
               float myScore = client.player.getHealth() + client.player.getArmor() * 4;
               int myGapples = 0;

               for (int i = 0; i < client.player.getInventory().size(); i++) {
                  ItemStack stack = client.player.getInventory().getStack(i);
                  if (stack.getItem() == Items.GOLDEN_APPLE || stack.getItem() == Items.ENCHANTED_GOLDEN_APPLE) {
                     myGapples += stack.getCount();
                  }
               }

               myScore += myGapples * 4;
               float targetScore = target.getHealth() + target.getArmor() * 4;
               int targetGapples = 0;
               ItemStack mainHand = target.getMainHandStack();
               if (mainHand.getItem() == Items.GOLDEN_APPLE || mainHand.getItem() == Items.ENCHANTED_GOLDEN_APPLE) {
                  targetGapples += mainHand.getCount();
               }

               ItemStack offHand = target.getOffHandStack();
               if (offHand.getItem() == Items.GOLDEN_APPLE || offHand.getItem() == Items.ENCHANTED_GOLDEN_APPLE) {
                  targetGapples += offHand.getCount();
               }

               targetScore += targetGapples * 4;
               float winChance = 50.0F;
               if (myScore + targetScore > 0.0F) {
                  winChance = myScore / (myScore + targetScore) * 100.0F;
               }

               int baseChance = 5635925;
               if (winChance < 40.0F) {
                  baseChance = 16733525;
               } else if (winChance < 60.0F) {
                  baseChance = 16755200;
               }

               int chanceColor = withAlpha(baseChance, alpha);
               String wlText = String.format("Win Chance: %.1f%%", winChance);
               context.drawTextWithShadow(client.textRenderer, wlText, x + textOffsetX, y + 25, chanceColor);
               if (VoidCyanClient.targetHudShowArmor) {
                  int armorX = x + textOffsetX;
                  int armorY = y + 36;
                  EquipmentSlot[] slots = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

                  for (int ix = 0; ix < slots.length; ix++) {
                     ItemStack stack = target.getEquippedStack(slots[ix]);
                     if (!stack.isEmpty()) {
                        context.drawItem(stack, armorX, armorY);
                        context.drawStackOverlay(client.textRenderer, stack, armorX, armorY);
                        if (VoidCyanClient.targetHudShowArmorDurability && stack.isDamageable()) {
                           int maxDamage = stack.getMaxDamage();
                           int damage = stack.getDamage();
                           int durability = maxDamage - damage;
                           float pct = (float)durability / maxDamage;
                           int durColor = -16711936;
                           if (pct < 0.25F) {
                              durColor = -65536;
                           } else if (pct < 0.5F) {
                              durColor = -22016;
                           } else if (pct < 0.75F) {
                              durColor = -256;
                           }

                           String pctText = (int)(pct * 100.0F) + "%";
                           context.getMatrices().pushMatrix();
                           context.getMatrices().translate(armorX + 8, armorY - 6);
                           context.getMatrices().scale(0.5F, 0.5F);
                           int textWidth = client.textRenderer.getWidth(pctText);
                           context.drawTextWithShadow(client.textRenderer, pctText, -textWidth / 2, 0, durColor);
                           context.getMatrices().popMatrix();
                        }
                     }

                     armorX += 18;
                  }
               }

               context.getMatrices().popMatrix();
            }
         }
      }
   }

   private static float easeOutBack(float t) {
      float c1 = 1.70158F;
      float c3 = c1 + 1.0F;
      return 1.0F + c3 * (float)Math.pow(t - 1.0F, 3.0) + c1 * (float)Math.pow(t - 1.0F, 2.0);
   }

   private static int withAlpha(int rgb, int alpha) {
      alpha = Math.max(0, Math.min(255, alpha));
      return alpha << 24 | rgb & 16777215;
   }

   public static int countItem(PlayerEntity player, Item item) {
      int count = 0;

      for (int i = 0; i < player.getInventory().size(); i++) {
         ItemStack stack = player.getInventory().getStack(i);
         if (stack.isOf(item)) {
            count += stack.getCount();
         }
      }

      ItemStack offHand = player.getOffHandStack();
      if (offHand.isOf(item)) {
         count += offHand.getCount();
      }

      return count;
   }

   public static int countHealthPots(PlayerEntity player) {
      int count = 0;

      for (int i = 0; i < player.getInventory().size(); i++) {
         if (isInstantHealthPotion(player.getInventory().getStack(i))) {
            count++;
         }
      }

      if (isInstantHealthPotion(player.getOffHandStack())) {
         count++;
      }

      return count;
   }

   private static boolean isInstantHealthPotion(ItemStack stack) {
      if (stack.isEmpty()) {
         return false;
      } else if (!stack.isOf(Items.SPLASH_POTION) && !stack.isOf(Items.LINGERING_POTION) && !stack.isOf(Items.POTION)) {
         return false;
      } else {
         PotionContentsComponent contents = (PotionContentsComponent)stack.get(DataComponentTypes.POTION_CONTENTS);
         return contents == null
            ? false
            : StreamSupport.<StatusEffectInstance>stream(contents.getEffects().spliterator(), false)
               .anyMatch(e -> e.getEffectType().equals(StatusEffects.INSTANT_HEALTH));
      }
   }

   public static void trackNearbyPlayers() {
      for (PlayerEntity player : getPlayersInTrackingRange()) {
         UUID id = player.getUuid();
         TargetHudRenderer.TrackedItems tracked = trackedItems.get(id);
         if (tracked == null) {
            trackedItems.put(id, new TargetHudRenderer.TrackedItems(player));
         } else {
            int oldWindCharges = tracked.count(Items.WIND_CHARGE);
            tracked.update(player);
            int newWindCharges = tracked.count(Items.WIND_CHARGE);
            if (newWindCharges < oldWindCharges) {
               windChargesUsed.merge(id, oldWindCharges - newWindCharges, Integer::sum);
            }
         }
      }
   }

   public static List<PlayerEntity> getPlayersInTrackingRange() {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player != null && client.world != null) {
         if (!(client.currentScreen instanceof EditHudScreen)) {
            List<PlayerEntity> players = new ArrayList<>();

            for (PlayerEntity player : client.world.getPlayers()) {
               if (player != client.player && player.isAlive()) {
                  players.add(player);
               }
            }

            players.sort(Comparator.comparingDouble(client.player::distanceTo));
            return players;
         } else {
            return List.of(client.player);
         }
      } else {
         return List.of();
      }
   }

   public static int getTrackedItemCount(PlayerEntity player, Item item) {
      UUID id = player.getUuid();
      TargetHudRenderer.TrackedItems tracked = trackedItems.get(id);
      int currentTracked = tracked != null ? tracked.count(item) : countItem(player, item);
      if (item == Items.TOTEM_OF_UNDYING && totemOverrides.containsKey(id)) {
         int overrideCount = totemOverrides.get(id);
         if (currentTracked > overrideCount) {
            totemOverrides.remove(id);
            return currentTracked;
         } else {
            return overrideCount;
         }
      } else {
         return currentTracked;
      }
   }

   public static void recordTotemPop(PlayerEntity player) {
      if (player != null) {
         UUID id = player.getUuid();
         totemPops.merge(id, 1, Integer::sum);
         TargetHudRenderer.TrackedItems tracked = trackedItems.get(id);
         int current = totemOverrides.containsKey(id) ? totemOverrides.get(id) : (tracked != null ? tracked.count(Items.TOTEM_OF_UNDYING) : 0);
         totemOverrides.put(id, Math.max(0, current - 1));
         if (tracked != null) {
            int known = tracked.knownCounts.getOrDefault(Items.TOTEM_OF_UNDYING, 0);
            tracked.knownCounts.put(Items.TOTEM_OF_UNDYING, Math.max(0, known - 1));
         }
      }
   }

   public static int getTotemPops(PlayerEntity player) {
      return totemPops.getOrDefault(player.getUuid(), 0);
   }

   public static int getWindChargesUsed(PlayerEntity player) {
      return windChargesUsed.getOrDefault(player.getUuid(), 0);
   }

   public static void resetItemTracker() {
      trackedItems.clear();
      totemPops.clear();
      totemOverrides.clear();
      windChargesUsed.clear();
   }

   public static int getHudHeight() {
      return VoidCyanClient.targetHudShowArmor ? 55 : 45;
   }

   public static LivingEntity getCurrentTarget() {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player == null || client.world == null) {
         return null;
      } else if (!(client.currentScreen instanceof EditHudScreen)) {
         LivingEntity direct = null;
         if (client.crosshairTarget instanceof EntityHitResult hitResult
            && hitResult.getEntity() instanceof PlayerEntity player
            && player.isAlive()
            && player != client.player) {
            direct = player;
         } else if (VoidCyanClient.lastAttackedEntity instanceof PlayerEntity player
            && player.isAlive()
            && player != client.player
            && System.currentTimeMillis() - VoidCyanClient.lastAttackTime < 500L) {
            direct = player;
         }

         if (direct == null) {
            Vec3d look = client.player.getRotationVec(1.0f);
            Vec3d eye = client.player.getEyePos();
            double bestScore = Double.MAX_VALUE;
            for (PlayerEntity player : client.world.getPlayers()) {
               if (player == client.player || !player.isAlive()) continue;
               Vec3d toTarget = player.getEyePos().subtract(eye);
               double dist = toTarget.length();
               if (dist <= 16.0) {
                  double dot = look.dotProduct(toTarget.normalize());
                  if (dot > 0.85) {
                     double score = dist * (1.0 - dot);
                     if (score < bestScore) {
                        bestScore = score;
                        direct = player;
                     }
                  }
               }
            }
         }

         if (direct != null) {
            stickyTarget = direct;
            stickyLastSeen = System.currentTimeMillis();
            return direct;
         } else {
            if (VoidCyanClient.targetHudStickyEnabled && stickyTarget != null) {
               long elapsed = System.currentTimeMillis() - stickyLastSeen;
               long limit = VoidCyanClient.targetHudStickyDuration * 1000L;
               if (elapsed < limit && stickyTarget.isAlive()) {
                  return stickyTarget;
               }

               stickyTarget = null;
            }

            return null;
         }
      } else {
         return client.player;
      }
   }

   private static final class TrackedItems {
      public final Map<Item, Integer> knownCounts = new HashMap<>();
      private final Map<Item, Integer> lastSeenCounts = new HashMap<>();
      private int knownHealthPots = 0;
      private int lastSeenHealthPots = 0;

      private TrackedItems(PlayerEntity player) {
         this.update(player);
      }

      private static final Item[] ITEMS_TO_TRACK = {
         Items.ENDER_PEARL, Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE, Items.COBWEB, Items.TOTEM_OF_UNDYING,
         Items.WIND_CHARGE, Items.END_CRYSTAL, Items.RESPAWN_ANCHOR, Items.GLOWSTONE, Items.EXPERIENCE_BOTTLE
      };

      public void update(PlayerEntity player) {
         for (Item item : ITEMS_TO_TRACK) {
            int currentHandCount = TargetHudRenderer.countItem(player, item);
            int lastSeen = this.lastSeenCounts.getOrDefault(item, 0);
            int known = this.knownCounts.getOrDefault(item, 0);
            if (currentHandCount > known) {
               this.knownCounts.put(item, currentHandCount);
            } else if (currentHandCount < lastSeen && item != Items.TOTEM_OF_UNDYING && (currentHandCount != 0 || lastSeen <= 1)) {
               if (currentHandCount == 0 && lastSeen == 1) {
                  this.knownCounts.put(item, Math.max(0, known - 1));
               } else {
                  int consumed = lastSeen - currentHandCount;
                  this.knownCounts.put(item, Math.max(0, known - consumed));
               }
            }

            this.lastSeenCounts.put(item, currentHandCount);
         }

         int currentHealthPots = TargetHudRenderer.countHealthPots(player);
         if (currentHealthPots > this.knownHealthPots) {
            this.knownHealthPots = currentHealthPots;
         } else if (currentHealthPots < this.lastSeenHealthPots && (currentHealthPots != 0 || this.lastSeenHealthPots <= 1)) {
            if (currentHealthPots == 0 && this.lastSeenHealthPots == 1) {
               this.knownHealthPots = Math.max(0, this.knownHealthPots - 1);
            } else {
               int consumed = this.lastSeenHealthPots - currentHealthPots;
               this.knownHealthPots = Math.max(0, this.knownHealthPots - consumed);
            }
         }

         this.lastSeenHealthPots = currentHealthPots;
      }

      public int count(Item item) {
         return item == Items.SPLASH_POTION ? this.knownHealthPots : this.knownCounts.getOrDefault(item, 0);
      }
   }
}
