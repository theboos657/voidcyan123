package com.voidcyan.client.module;

import com.voidcyan.client.VoidCyanClient;
import com.voidcyan.client.screen.GuiStyle;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.item.ItemStack;
import org.lwjgl.glfw.GLFW;

public class ShulkerPreviewManager {
   private static ItemStack lockedStack = null;
   private static int lockedX;
   private static int lockedY;
   private static boolean lockKeyWasDown = false;

   private static int[] previewPos(MinecraftClient client, ItemStack stack, int mouseX, int mouseY, int w, int h) {
      int sw = client.getWindow().getScaledWidth();
      int sh = client.getWindow().getScaledHeight();
      List<net.minecraft.text.Text> lines = net.minecraft.client.gui.screen.Screen.getTooltipFromItem(client, stack);
      int tw = 0;
      for (net.minecraft.text.Text t : lines) tw = Math.max(tw, client.textRenderer.getWidth(t));
      int th = lines.size() * 10 + (lines.size() > 1 ? 2 : 0);
      int tipX = mouseX + 12;
      int tipY = mouseY - 12;
      if (tipX + tw + 4 > sw) tipX = Math.max(4, tipX - 28 - tw);
      if (tipY + th + 3 > sh) tipY = sh - th - 3;
      int x = Math.max(4, Math.min(tipX, sw - w - 4));
      int y = tipY + th + 11;
      if (y + h + 4 > sh) y = Math.max(4, tipY - 11 - h);
      return new int[]{x, y};
   }

   public static void render(DrawContext context, ItemStack hovered, int mouseX, int mouseY) {
      if (!VoidCyanClient.isShulkerPreviewEnabled) return;
      MinecraftClient client = MinecraftClient.getInstance();
      long window = client.getWindow().getHandle();
      boolean previewDown = GLFW.glfwGetKey(window, VoidCyanClient.shulkerPreviewKey) == 1;
      boolean lockDown = GLFW.glfwGetKey(window, VoidCyanClient.shulkerPreviewLockKey) == 1;
      boolean hoveredIsContainer = hovered != null && hovered.get(DataComponentTypes.CONTAINER) != null;

      int cols = 9;
      int slotSize = 18;
      int w = cols * slotSize;
      int h = 3 * slotSize;
      if (lockDown && !lockKeyWasDown) {
         lockedStack = lockedStack != null ? null : (hoveredIsContainer ? hovered : null);
         if (lockedStack != null) {
            int[] pos = previewPos(client, hovered, mouseX, mouseY, w, h);
            lockedX = pos[0];
            lockedY = pos[1];
         }
      }

      lockKeyWasDown = lockDown;

      ItemStack stack = lockedStack != null ? lockedStack : (previewDown && hoveredIsContainer ? hovered : null);
      if (stack == null) return;
      ContainerComponent cc = stack.get(DataComponentTypes.CONTAINER);
      if (cc == null) return;
      List<ItemStack> items = cc.stream().toList();

      int rows = (items.size() + cols - 1) / cols;
      h = rows * slotSize;
      int[] pos = lockedStack != null ? new int[]{lockedX, lockedY} : previewPos(client, stack, mouseX, mouseY, w, h);
      int x = pos[0];
      int y = pos[1];
      GuiStyle.roundedRect(context, x - 4, y - 4, w + 8, h + 8, 4, 0xF0100010);
      GuiStyle.roundedOutline(context, x - 4, y - 4, w + 8, h + 8, 4, 0xFFFFFFFF);

      ItemStack tooltipStack = null;
      for (int i = 0; i < items.size(); i++) {
         ItemStack item = items.get(i);
         if (item.isEmpty()) continue;
         int sx = x + i % cols * slotSize;
         int sy = y + i / cols * slotSize;
         context.drawItem(item, sx + 1, sy + 1);
         context.drawStackOverlay(client.textRenderer, item, sx + 1, sy + 1);
         if (mouseX >= sx && mouseX < sx + slotSize && mouseY >= sy && mouseY < sy + slotSize) {
            tooltipStack = item;
         }
      }

      if (tooltipStack != null) {
         context.drawItemTooltip(client.textRenderer, tooltipStack, mouseX, mouseY);
      }
   }
}
