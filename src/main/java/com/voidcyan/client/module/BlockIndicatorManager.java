package com.voidcyan.client.module;

import com.voidcyan.client.util.WaypointManager;
import java.util.Properties;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

public final class BlockIndicatorManager {
   private static final int GAP = 4;
   public static int maxWidth = 180;
   public static BlockIndicatorManager.IconMode iconMode = BlockIndicatorManager.IconMode.BLOCK;
   public static String customIconItem = "minecraft:grass_block";
   public static int fillColor = Integer.MIN_VALUE;
   public static boolean blurFill = false;
   public static int borderColor = -16711681;

   private BlockIndicatorManager() {
   }

   public static void load(Properties props) {
      try {
         maxWidth = Integer.parseInt(props.getProperty("blockIndicatorMaxWidth", "180"));
      } catch (Exception ignored) {}
      customIconItem = props.getProperty("blockIndicatorCustomIcon", "minecraft:grass_block");
      try {
         fillColor = (int)Long.parseLong(props.getProperty("blockIndicatorFillColor", "2147483648"), 10);
      } catch (Exception ignored) {}
      blurFill = Boolean.parseBoolean(props.getProperty("blockIndicatorBlurFill", "false"));
      try {
         borderColor = (int)Long.parseLong(props.getProperty("blockIndicatorBorderColor", "4278255615"), 10);
      } catch (Exception ignored) {}
      if (props.containsKey("blockIndicatorIconMode")) {
         iconMode = parseIconMode(props.getProperty("blockIndicatorIconMode", "block"));
      } else {
         boolean useBlock = Boolean.parseBoolean(props.getProperty("blockIndicatorUseLookedAtBlockIcon", "true"));
         iconMode = useBlock ? BlockIndicatorManager.IconMode.BLOCK : BlockIndicatorManager.IconMode.CUSTOM;
      }
   }

   public static void save(Properties props) {
      props.setProperty("blockIndicatorMaxWidth", String.valueOf(maxWidth));
      props.setProperty("blockIndicatorIconMode", iconMode.name().toLowerCase());
      props.setProperty("blockIndicatorCustomIcon", customIconItem);
      props.setProperty("blockIndicatorFillColor", String.valueOf(fillColor & 4294967295L));
      props.setProperty("blockIndicatorBlurFill", String.valueOf(blurFill));
      props.setProperty("blockIndicatorBorderColor", String.valueOf(borderColor & 4294967295L));
   }

   private static BlockIndicatorManager.IconMode parseIconMode(String raw) {
      if (raw == null) {
         return BlockIndicatorManager.IconMode.BLOCK;
      } else {
         String var1 = raw.toLowerCase();

         return switch (var1) {
            case "off", "none" -> BlockIndicatorManager.IconMode.OFF;
            case "custom", "item" -> BlockIndicatorManager.IconMode.CUSTOM;
            default -> BlockIndicatorManager.IconMode.BLOCK;
         };
      }
   }

   public static int getWidth(MinecraftClient client) {
      return computeLayout(client, false).boxWidth;
   }

   public static int getPreviewWidth(MinecraftClient client) {
      return computeLayout(client, true).boxWidth;
   }

   public static int getHeight() {
      return 20;
   }

   private static BlockIndicatorManager.Layout computeLayout(MinecraftClient client, boolean preview) {
      if (client != null && client.textRenderer != null) {
         BlockState state = getLookedAtState(client);
         String blockName;
         if (state != null && !state.isAir()) {
            blockName = state.getBlock().getName().getString();
         } else if (preview) {
            blockName = "Stone";
            state = Blocks.STONE.getDefaultState();
         } else {
            blockName = "Air";
         }

         boolean showIcon = iconMode != BlockIndicatorManager.IconMode.OFF;
         ItemStack icon = showIcon ? resolveIcon(client, state, preview) : ItemStack.EMPTY;
         int iconSpace = showIcon ? 20 : 0;
         int innerMax = Math.max(40, maxWidth - 8 - iconSpace);
         String displayName = fitText(client, blockName, innerMax);
         int textWidth = client.textRenderer.getWidth(displayName);
         int boxWidth = Math.min(maxWidth, 4 + iconSpace + textWidth + 4);
         return new BlockIndicatorManager.Layout(displayName, icon, showIcon, boxWidth);
      } else {
         return new BlockIndicatorManager.Layout("Stone", new ItemStack(Items.STONE), iconMode != BlockIndicatorManager.IconMode.OFF, maxWidth);
      }
   }

   private static String fitText(MinecraftClient client, String text, int maxTextWidth) {
      if (text != null && !text.isEmpty()) {
         if (client.textRenderer.getWidth(text) <= maxTextWidth) {
            return text;
         } else {
            String ellipsis = "...";
            int ellipsisW = client.textRenderer.getWidth(ellipsis);
            int trimWidth = Math.max(0, maxTextWidth - ellipsisW);
            return trimWidth <= 0 ? ellipsis : client.textRenderer.trimToWidth(text, trimWidth) + ellipsis;
         }
      } else {
         return "";
      }
   }

   private static BlockState getLookedAtState(MinecraftClient client) {
      return client.crosshairTarget instanceof BlockHitResult bhr && client.world != null ? client.world.getBlockState(bhr.getBlockPos()) : null;
   }

   private static ItemStack resolveIcon(MinecraftClient client, BlockState state, boolean preview) {
      return switch (iconMode) {
         case OFF -> ItemStack.EMPTY;
         case BLOCK -> getBlockStack(client, state, preview);
         case CUSTOM -> WaypointManager.getIconStack(customIconItem);
      };
   }

   private static ItemStack getBlockStack(MinecraftClient client, BlockState state, boolean preview) {
      if (!preview || state != null && !state.isAir()) {
         if (state != null && !state.isAir() && client.world != null) {
            BlockHitResult bhr = client.crosshairTarget instanceof BlockHitResult hit ? hit : null;
            BlockPos pos = bhr != null ? bhr.getBlockPos() : BlockPos.ORIGIN;

            try {
               ItemStack pick = state.getPickStack(client.world, pos, false);
               if (pick != null && !pick.isEmpty()) {
                  return pick;
               }
            } catch (Throwable var6) {
            }

            Item item = state.getBlock().asItem();
            return item != null && item != Items.AIR ? new ItemStack(item) : new ItemStack(Items.GRASS_BLOCK);
         } else {
            return new ItemStack(Items.BARRIER);
         }
      } else {
         return new ItemStack(Items.STONE);
      }
   }

   public static void render(DrawContext context) {
      renderInternal(context, false);
   }

   public static void renderPreview(DrawContext context) {
      renderInternal(context, true);
   }

   private static void renderInternal(DrawContext context, boolean preview) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (!preview) {
         if (client.player == null) {
            return;
         }

         if (client.currentScreen != null) {
            return;
         }
      }

      BlockIndicatorManager.Layout layout = computeLayout(client, preview);
      drawLayout(context, client, layout);
   }

   private static void drawLayout(DrawContext context, MinecraftClient client, BlockIndicatorManager.Layout layout) {
      int boxW = layout.boxWidth;
      int x = 0;
      int y = 0;
      int bg = blurFill ? fillColor & 16777215 | -1157627904 : fillColor;
      context.fill(x, y, x + boxW, y + 20, bg);
      if (blurFill) {
         context.fill(x + 1, y + 1, x + boxW - 1, y + 20 - 1, fillColor & 16777215 | 1426063360);
      }

      drawBorder(context, x, y, boxW, 20, borderColor);
      int textX = x + 4;
      if (layout.showIcon && !layout.iconStack.isEmpty()) {
         int iconX = x + 4;
         int iconY = y + 2;
         context.drawItemWithoutEntity(layout.iconStack, iconX, iconY);
         textX = iconX + 16 + 4;
      }

      int textY = y + (20 - 9) / 2 + 1;
      context.drawTextWithShadow(client.textRenderer, layout.displayName, textX, textY, -1);
   }

   private static void drawBorder(DrawContext context, int x, int y, int w, int h, int color) {
      context.fill(x, y, x + w, y + 1, color);
      context.fill(x, y + h - 1, x + w, y + h, color);
      context.fill(x, y, x + 1, y + h, color);
      context.fill(x + w - 1, y, x + w, y + h, color);
   }

   public static enum IconMode {
      OFF,
      BLOCK,
      CUSTOM;
   }

   private record Layout(String displayName, ItemStack iconStack, boolean showIcon, int boxWidth) {
   }
}
