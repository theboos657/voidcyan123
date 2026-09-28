package com.voidcyan.client.module;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NameTagItemsRenderer {

    private static final Map<UUID, Long> totemPopTimes = new HashMap<>();
    private static final long TOTEM_POP_FLASH_DURATION_MS = 3000L;

    public static void recordTotemPop(UUID uuid) {
        totemPopTimes.put(uuid, System.currentTimeMillis());
    }

    public static boolean hasRecentTotemPop(UUID uuid) {
        Long t = totemPopTimes.get(uuid);
        if (t == null) return false;
        return System.currentTimeMillis() - t < TOTEM_POP_FLASH_DURATION_MS;
    }

    public static void render(DrawContext context) {
        if (!VoidCyanClient.isNameTagItemsEnabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null || client.gameRenderer == null) return;

        int scaledW = client.getWindow().getScaledWidth();
        int scaledH = client.getWindow().getScaledHeight();

        Vec3d cameraPos = client.gameRenderer.getCamera().getCameraPos();

        for (PlayerEntity player : client.world.getPlayers()) {
            if (player == null || player.isSpectator() || player.isInvisible() || player.isRemoved()) continue;

            boolean isSelf = player.getUuid().equals(client.player.getUuid());
            if (isSelf && (!VoidCyanClient.nameTagItemsShowSelf || client.options.getPerspective().isFirstPerson())) {
                continue;
            }

            if (!isSelf && VoidCyanClient.nameTagItemsOnlyFriends) {
                boolean isFriend = false;
                for (String f : VoidCyanClient.friends) {
                    if (player.getName().getString().equalsIgnoreCase(f.trim())) {
                        isFriend = true;
                        break;
                    }
                }
                if (!isFriend) continue;
            }

            double dx = player.getX() - cameraPos.x;
            double dy = (player.getY() + player.getHeight()) - cameraPos.y;
            double dz = player.getZ() - cameraPos.z;
            double distSq = dx * dx + dy * dy + dz * dz;
            if (distSq > 64.0 * 64.0) continue;

            double nametagY = player.getY() + player.getHeight() + (player.isInSneakingPose() ? 0.3 : 0.5);
            Vec3d projected = client.gameRenderer.project(new Vec3d(player.getX(), nametagY, player.getZ()));
            if (projected == null || projected.z <= 0.0 || projected.z > 1.0) continue;

            int screenX = (int) ((projected.x * 0.5 + 0.5) * scaledW);
            int screenY = (int) ((1.0 - (projected.y * 0.5 + 0.5)) * scaledH);

            if (screenX < -120 || screenX > scaledW + 120 || screenY < -120 || screenY > scaledH + 120) continue;

            double dist = Math.sqrt(distSq);
            float distFactor = (float) Math.clamp(1.0 - (dist - 4.0) * 0.02, 0.45, 1.0);
            float scale = VoidCyanClient.nameTagItemsScale * distFactor;

            switch (VoidCyanClient.nameTagItemsDisplayMode) {
                case 1 -> render3DModel(context, client, player, screenX, screenY, scale);
                case 2 -> renderItemRow(context, client, player, screenX, screenY, scale);
                default -> renderPlayerDoll(context, client, player, screenX, screenY, scale);
            }
        }
    }

    /**
     * Renders a 2D player doll / silhouette holding items in their hands and wearing armor,
     * with active items (blocking shield, eating food, drawing bow) painted grey.
     */
    private static void renderPlayerDoll(DrawContext context, MinecraftClient client, PlayerEntity player,
                                         int screenX, int screenY, float scale) {
        ItemStack mainHand = VoidCyanClient.nameTagItemsShowMainHand ? player.getMainHandStack() : ItemStack.EMPTY;
        ItemStack offHand = VoidCyanClient.nameTagItemsShowOffHand ? player.getOffHandStack() : ItemStack.EMPTY;

        ItemStack helmet = VoidCyanClient.nameTagItemsShowArmor ? player.getEquippedStack(EquipmentSlot.HEAD) : ItemStack.EMPTY;
        ItemStack chest = VoidCyanClient.nameTagItemsShowArmor ? player.getEquippedStack(EquipmentSlot.CHEST) : ItemStack.EMPTY;
        ItemStack legs = VoidCyanClient.nameTagItemsShowArmor ? player.getEquippedStack(EquipmentSlot.LEGS) : ItemStack.EMPTY;
        ItemStack boots = VoidCyanClient.nameTagItemsShowArmor ? player.getEquippedStack(EquipmentSlot.FEET) : ItemStack.EMPTY;

        int pops = VoidCyanClient.nameTagItemsShowTotemPop ? TargetHudRenderer.getTotemPops(player) : 0;
        boolean recentPop = VoidCyanClient.nameTagItemsShowTotemPop && hasRecentTotemPop(player.getUuid());
        boolean hasTotem = pops > 0 || recentPop;

        boolean hasAny = !mainHand.isEmpty() || !offHand.isEmpty() || !helmet.isEmpty()
                || !chest.isEmpty() || !legs.isEmpty() || !boots.isEmpty() || hasTotem;
        if (!hasAny) return;

        boolean isUsing = player.isUsingItem();
        ItemStack activeStack = isUsing ? player.getActiveItem() : ItemStack.EMPTY;
        Hand activeHand = isUsing ? player.getActiveHand() : Hand.MAIN_HAND;

        boolean usingMain = isUsing && activeHand == Hand.MAIN_HAND;
        boolean usingOff = isUsing && activeHand == Hand.OFF_HAND;

        boolean isShield = isUsing && activeStack.isOf(Items.SHIELD);
        boolean isEating = isUsing && (activeStack.contains(DataComponentTypes.FOOD)
                || activeStack.isOf(Items.POTION) || activeStack.isOf(Items.MILK_BUCKET) || activeStack.isOf(Items.HONEY_BOTTLE));
        boolean isRanged = isUsing && (activeStack.isOf(Items.BOW) || activeStack.isOf(Items.CROSSBOW) || activeStack.isOf(Items.TRIDENT));

        boolean mainIsRight = player.getMainArm() == Arm.RIGHT;
        int mainSlotX = mainIsRight ? 34 : 0;
        int offSlotX = mainIsRight ? 0 : 34;
        int handY = 17;

        int dollW = hasTotem ? 70 : 52;
        int dollH = 68;

        float finalScale = scale * 0.65f;
        float scaledW = dollW * finalScale;
        float scaledH = dollH * finalScale;

        float startX = screenX - (scaledW / 2.0f);
        float startY = screenY - scaledH - 6.0f;

        context.getMatrices().pushMatrix();
        context.getMatrices().translate(startX, startY);
        context.getMatrices().scale(finalScale, finalScale);

        // --- 1. Background Player Body Silhouette ---
        // Head / Skin Face
        if (player instanceof AbstractClientPlayerEntity acp && helmet.isEmpty()) {
            PlayerSkinDrawer.draw(context, acp.getSkin(), 19, 2, 12);
        } else {
            context.fill(19, 2, 31, 14, 0x44222222);
        }

        // Torso silhouette
        context.fill(19, 17, 31, 33, 0x33333333);

        // Arms silhouettes
        context.fill(4, 18, 12, 32, 0x22333333);
        context.fill(38, 18, 46, 32, 0x22333333);

        // Legs & Boots silhouette
        context.fill(19, 34, 31, 50, 0x2A333333);
        context.fill(19, 51, 31, 65, 0x2A333333);

        // --- 2. Armor Slots ---
        if (!helmet.isEmpty()) drawItemSlot(context, client, helmet, 17, 0, false);
        if (!chest.isEmpty()) drawItemSlot(context, client, chest, 17, 17, false);
        if (!legs.isEmpty()) drawItemSlot(context, client, legs, 17, 34, false);
        if (!boots.isEmpty()) drawItemSlot(context, client, boots, 17, 51, false);

        // --- 3. Held Items with Action State ---
        // Main hand item handling
        if (!mainHand.isEmpty()) {
            if (usingMain) {
                boolean grey = VoidCyanClient.nameTagItemsGreyWhenUsing;
                if (isEating) {
                    // Raised to mouth
                    drawItemSlot(context, client, mainHand, 17, 2, grey);
                    drawUseProgressBar(context, player, activeStack, 17, 19);
                } else if (isRanged) {
                    // Aiming center
                    drawItemSlot(context, client, mainHand, 17, 12, grey);
                    drawUseProgressBar(context, player, activeStack, 17, 29);
                } else if (isShield) {
                    // Shield in blocking position
                    drawItemSlot(context, client, mainHand, 17, 15, grey);
                } else {
                    drawItemSlot(context, client, mainHand, mainSlotX, handY, grey);
                }
            } else {
                drawItemSlot(context, client, mainHand, mainSlotX, handY, false);
            }
        }

        // Off hand item handling
        if (!offHand.isEmpty()) {
            if (usingOff) {
                boolean grey = VoidCyanClient.nameTagItemsGreyWhenUsing;
                if (isShield) {
                    // Shield in front of body, blocking!
                    drawItemSlot(context, client, offHand, 17, 15, grey);
                } else if (isEating) {
                    // Food raised to mouth
                    drawItemSlot(context, client, offHand, 17, 2, grey);
                    drawUseProgressBar(context, player, activeStack, 17, 19);
                } else {
                    drawItemSlot(context, client, offHand, offSlotX, handY, grey);
                }
            } else {
                drawItemSlot(context, client, offHand, offSlotX, handY, false);
            }
        }

        // --- 4. Totem Pop Counter ---
        if (hasTotem) {
            drawTotemPopSlot(context, client, 52, 17, pops, recentPop);
        }

        context.getMatrices().popMatrix();
    }

    /**
     * Renders a 3D player entity model above the nametag with status indicators.
     */
    private static void render3DModel(DrawContext context, MinecraftClient client, PlayerEntity player,
                                      int screenX, int screenY, float scale) {
        int modelW = 34;
        int modelH = 52;
        float finalScale = scale * 0.85f;
        int scaledW = (int) (modelW * finalScale);
        int scaledH = (int) (modelH * finalScale);

        int boxX1 = screenX - scaledW / 2;
        int boxY1 = screenY - scaledH - 6;
        int boxX2 = screenX + scaledW / 2;
        int boxY2 = screenY - 6;

        // Subtle background behind model
        context.fill(boxX1 - 2, boxY1 - 2, boxX2 + 2, boxY2 + 2, 0x66000000);

        InventoryScreen.drawEntity(context, boxX1, boxY1, boxX2, boxY2, 22, 0.0625F, 0.0F, 0.0F, player);

        // Action badge next to model if using an item
        boolean isUsing = player.isUsingItem();
        if (isUsing && !player.getActiveItem().isEmpty()) {
            ItemStack active = player.getActiveItem();
            int badgeX = boxX2 + 4;
            int badgeY = boxY1 + 12;

            boolean paintGrey = VoidCyanClient.nameTagItemsGreyWhenUsing;
            drawItemSlot(context, client, active, badgeX, badgeY, paintGrey);

            if (active.isOf(Items.SHIELD)) {
                context.drawTextWithShadow(client.textRenderer, Text.literal("BLOCK"), badgeX, badgeY + 18, 0xFFAAAAAA);
            } else if (active.contains(DataComponentTypes.FOOD)) {
                drawUseProgressBar(context, player, active, badgeX, badgeY + 17);
            }
        }

        // Totem pop indicator
        int pops = VoidCyanClient.nameTagItemsShowTotemPop ? TargetHudRenderer.getTotemPops(player) : 0;
        boolean recent = VoidCyanClient.nameTagItemsShowTotemPop && hasRecentTotemPop(player.getUuid());
        if (pops > 0 || recent) {
            int totemX = boxX1 - 20;
            int totemY = boxY1 + 12;
            drawTotemPopSlot(context, client, totemX, totemY, pops, recent);
        }
    }

    /**
     * Renders items in a compact horizontal row.
     */
    private static void renderItemRow(DrawContext context, MinecraftClient client, PlayerEntity player,
                                      int screenX, int screenY, float scale) {
        java.util.List<ItemStack> items = new java.util.ArrayList<>();
        java.util.List<Boolean> isGrey = new java.util.ArrayList<>();

        boolean isUsing = player.isUsingItem();
        ItemStack active = isUsing ? player.getActiveItem() : ItemStack.EMPTY;

        if (VoidCyanClient.nameTagItemsShowMainHand) {
            ItemStack mh = player.getMainHandStack();
            if (!mh.isEmpty()) {
                items.add(mh);
                isGrey.add(VoidCyanClient.nameTagItemsGreyWhenUsing && isUsing && mh == active);
            }
        }

        if (VoidCyanClient.nameTagItemsShowArmor) {
            for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
                ItemStack arm = player.getEquippedStack(slot);
                if (!arm.isEmpty()) {
                    items.add(arm);
                    isGrey.add(false);
                }
            }
        }

        if (VoidCyanClient.nameTagItemsShowOffHand) {
            ItemStack oh = player.getOffHandStack();
            if (!oh.isEmpty()) {
                items.add(oh);
                isGrey.add(VoidCyanClient.nameTagItemsGreyWhenUsing && isUsing && oh == active);
            }
        }

        int pops = VoidCyanClient.nameTagItemsShowTotemPop ? TargetHudRenderer.getTotemPops(player) : 0;
        boolean recent = VoidCyanClient.nameTagItemsShowTotemPop && hasRecentTotemPop(player.getUuid());
        boolean hasTotem = pops > 0 || recent;

        if (items.isEmpty() && !hasTotem) return;

        int totalSlots = items.size() + (hasTotem ? 1 : 0);
        int slotW = 18;
        int totalW = totalSlots * slotW - 2;

        float scaledW = totalW * scale;
        float scaledH = 16 * scale;

        float startX = screenX - (scaledW / 2.0f);
        float startY = screenY - scaledH - 6.0f;

        context.getMatrices().pushMatrix();
        context.getMatrices().translate(startX, startY);
        context.getMatrices().scale(scale, scale);

        int curX = 0;
        for (int i = 0; i < items.size(); i++) {
            drawItemSlot(context, client, items.get(i), curX, 0, isGrey.get(i));
            curX += slotW;
        }

        if (hasTotem) {
            drawTotemPopSlot(context, client, curX, 0, pops, recent);
        }

        context.getMatrices().popMatrix();
    }

    /**
     * Draws an item in a slot, optionally painted grey when active (e.g. blocking or eating).
     */
    private static void drawItemSlot(DrawContext context, MinecraftClient client, ItemStack stack,
                                     int x, int y, boolean paintGrey) {
        if (stack.isEmpty()) return;

        // Dark background plate
        context.fill(x - 1, y - 1, x + 17, y + 17, 0x88000000);

        context.drawItem(stack, x, y);

        // Painted grey effect when being used
        if (paintGrey) {
            // Smooth grey overlay over the item
            context.fill(x, y, x + 16, y + 16, 0xBB505050);
            // Grey outline
            context.fill(x - 1, y - 1, x + 17, y, 0xFF888888);
            context.fill(x - 1, y + 16, x + 17, y + 17, 0xFF888888);
            context.fill(x - 1, y, x, y + 16, 0xFF888888);
            context.fill(x + 16, y, x + 17, y + 16, 0xFF888888);
        }

        if (VoidCyanClient.nameTagItemsShowDurability) {
            context.drawStackOverlay(client.textRenderer, stack, x, y);
        }
    }

    /**
     * Draws a tiny progress bar below an item currently being used (e.g. eating/drinking/drawing bow).
     */
    private static void drawUseProgressBar(DrawContext context, PlayerEntity player, ItemStack stack, int x, int y) {
        int max = stack.getMaxUseTime(player);
        if (max <= 0) return;
        int left = player.getItemUseTimeLeft();
        float progress = Math.clamp(1.0f - (left / (float) max), 0.0f, 1.0f);

        int barW = 16;
        int fillW = (int) (barW * progress);
        context.fill(x, y, x + barW, y + 2, 0xAA000000);
        context.fill(x, y, x + fillW, y + 2, 0xFF55FF55);
    }

    /**
     * Draws the Totem of Undying icon with pop counter and animated golden flash.
     */
    private static void drawTotemPopSlot(DrawContext context, MinecraftClient client,
                                         int x, int y, int pops, boolean recentPop) {
        ItemStack totem = new ItemStack(Items.TOTEM_OF_UNDYING);

        context.fill(x - 1, y - 1, x + 17, y + 17, 0x88000000);

        if (recentPop) {
            long time = System.currentTimeMillis();
            float pulse = (float) Math.sin((time % 1000) / 1000.0 * Math.PI * 2) * 0.5f + 0.5f;
            int alpha = (int) (140 + pulse * 115);
            int gold = (alpha << 24) | 0xFFD700;
            context.fill(x - 1, y - 1, x + 17, y, gold);
            context.fill(x - 1, y + 16, x + 17, y + 17, gold);
            context.fill(x - 1, y, x, y + 16, gold);
            context.fill(x + 16, y, x + 17, y + 16, gold);
        }

        context.drawItem(totem, x, y);

        if (pops > 0) {
            String popStr = "x" + pops;
            context.drawTextWithShadow(client.textRenderer, Text.literal(popStr),
                    x + 17 - client.textRenderer.getWidth(popStr), y + 9, 0xFFFF5555);
        }
    }
}
