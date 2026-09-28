package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

/**
 * Drop Prevention settings — a searchable picker of EVERY item in the game.
 * Left-click an item to protect/unprotect it (green border = protected).
 * No more typing raw item IDs.
 */
public class DropPreventionSettingsScreen extends Screen {
   private static final int CELL = 26;
   private static final int GAP = 6;

   /** One pickable entry: the item, its id and display name. */
   private record Entry(ItemStack stack, String fullId, String path, String name) {
   }

   private static List<Entry> ALL_ITEMS = null;

   private final Screen parent;
   private String query = "";
   private float scrollOffset = 0.0F;
   private float scrollTarget = 0.0F;
   private int maxScroll = 0;
   private boolean dragging = false;
   private List<Entry> filtered = new ArrayList<>();
   private String statusMsg = "";
   private long statusUntil = 0L;
   private int primaryColor = 0xFF00F5FF;
   private long openTime = 0L;
   private int gridTop = 64;
   private int gridBottom = 0;
   private int gridX = 0;
   private int cols = 8;
   /** Names of protected items (parsed from the saved comma list). */
   private final Set<String> protectedIds = new HashSet<>();

   public DropPreventionSettingsScreen(Screen parent) {
      super(Text.literal("Drop Prevention"));
      this.parent = parent;
   }

   private void status(String msg) {
      this.statusMsg = msg;
      this.statusUntil = System.currentTimeMillis() + 2400L;
   }

   /** Rebuilds the protected set from the saved comma-separated config string. */
   private void loadProtected() {
      this.protectedIds.clear();
      String raw = VoidCyanClient.dropPreventionItems == null ? "" : VoidCyanClient.dropPreventionItems;
      for (String part : raw.split("[,;]+")) {
         String t = part.trim().toLowerCase(Locale.ROOT);
         if (!t.isEmpty()) this.protectedIds.add(t);
      }
   }

   /** Writes the protected set back to config and saves. */
   private void saveProtected() {
      VoidCyanClient.dropPreventionItems = String.join(",", this.protectedIds);
      VoidCyanClient.saveConfig();
   }

   private boolean isProtected(Entry e) {
      return this.protectedIds.contains(e.fullId()) || this.protectedIds.contains(e.path());
   }

   private void toggle(Entry e) {
      String key = e.fullId();
      // drop the stored "minecraft:" prefix to keep the list compact/compatible
      key = key.startsWith("minecraft:") ? key.substring("minecraft:".length()) : key;
      if (this.protectedIds.contains(key) || this.protectedIds.contains(e.fullId())) {
         this.protectedIds.remove(key);
         this.protectedIds.remove(e.fullId());
         this.status("Allowed to drop: " + e.name());
      } else {
         this.protectedIds.add(key);
         this.status("Protected: " + e.name());
      }
      this.saveProtected();
   }

   private static void buildCatalog() {
      if (ALL_ITEMS != null) return;
      List<Entry> list = new ArrayList<>();
      for (Item item : Registries.ITEM) {
         if (item == net.minecraft.item.Items.AIR) continue;
         Identifier id = Registries.ITEM.getId(item);
         list.add(new Entry(new ItemStack(item), id.toString(), id.getPath(), item.getName().getString()));
      }
      list.sort((a, b) -> a.name().compareToIgnoreCase(b.name()));
      ALL_ITEMS = list;
   }

   private void updateFilter() {
      String q = this.query.trim().toLowerCase(Locale.ROOT);
      this.filtered.clear();
      if (ALL_ITEMS == null) return;
      if (q.isEmpty()) {
         this.filtered.addAll(ALL_ITEMS);
      } else {
         for (Entry e : ALL_ITEMS) {
            if (e.name().toLowerCase(Locale.ROOT).contains(q)
               || e.path().contains(q)
               || e.fullId().contains(q)) {
               this.filtered.add(e);
            }
         }
      }
   }

   @Override
   protected void init() {
      this.openTime = System.currentTimeMillis();
      buildCatalog();
      this.loadProtected();
      this.updateFilter();
   }

   private int cellRows() {
      return (this.filtered.size() + this.cols - 1) / this.cols;
   }

   private int contentHeight() {
      return this.cellRows() * (CELL + GAP) + 8;
   }

   @Override
   public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
      // NOTE: no renderBackground() — vanilla blurs once per frame on 1.21.11,
      // a second call throws IllegalStateException.
      context.fillGradient(0, 0, this.width, this.height, 0xE0100A18, 0xC0150F20);
      this.primaryColor = VoidCyanClient.getPrimaryColor();
      float anim = Math.min(1.0F, (float) (System.currentTimeMillis() - this.openTime) / 240.0F);
      anim = 1.0F - (float) Math.pow(1.0F - anim, 3.0);

      // Header
      context.drawTextWithShadow(this.textRenderer, Text.literal("Drop Prevention"), 16, 14, this.primaryColor & 0xFFFFFF | 0xFF000000);
      context.drawTextWithShadow(this.textRenderer,
         Text.literal(this.protectedIds.size() + " item(s) protected \u00b7 click to toggle \u00b7 type to search"),
         16, 28, 0xFF8A93A6);

      // Search box
      int sbX = this.width - 236;
      int sbY = 12;
      int sbW = 220;
      boolean searchHov = mouseX >= sbX && mouseX <= sbX + sbW && mouseY >= sbY && mouseY <= sbY + 22;
      GuiStyle.roundedRect(context, sbX, sbY, sbW, 22, 5,
         (searchHov ? 0x50 : 0x38) << 24 | 0xFFFFFF);
      GuiStyle.roundedOutline(context, sbX, sbY, sbW, 22, 5,
         (this.query.isEmpty() ? 0x50FFFFFF : this.primaryColor & 0xFFFFFF | 0xC0000000));
      boolean caret = this.queryFocused() && System.currentTimeMillis() / 500 % 2 == 0;
      String shown = this.query + (caret ? "_" : "");
      if (this.query.isEmpty() && !caret) {
         context.drawTextWithShadow(this.textRenderer, Text.literal("Search items..."), sbX + 9, sbY + 7, 0xFF6A7386);
      } else {
         context.drawTextWithShadow(this.textRenderer, Text.literal(shown), sbX + 9, sbY + 7, 0xFFF0F3F8);
      }

      // Grid area
      this.gridTop = 48;
      this.gridBottom = this.height - 40;
      this.cols = Math.max(4, (this.width - 36) / (CELL + GAP));
      this.gridX = (this.width - (this.cols * (CELL + GAP) - GAP)) / 2;

      int contentH = this.contentHeight();
      this.maxScroll = Math.max(0, contentH - (this.gridBottom - this.gridTop));
      if (this.maxScroll == 0) {
         this.scrollOffset = 0.0F;
         this.scrollTarget = 0.0F;
      } else {
         this.scrollTarget = Math.max(0, Math.min(this.maxScroll, this.scrollTarget));
         this.scrollOffset += (this.scrollTarget - this.scrollOffset) * 0.3F;
      }

      context.enableScissor(0, this.gridTop, this.width, this.gridBottom);
      int hoveredIdx = -1;
      for (int i = 0; i < this.filtered.size(); i++) {
         int row = i / this.cols;
         int col = i % this.cols;
         int x = this.gridX + col * (CELL + GAP);
         int y = this.gridTop + 4 + row * (CELL + GAP) - (int) this.scrollOffset;
         if (y + CELL < this.gridTop || y > this.gridBottom) continue;
         Entry e = this.filtered.get(i);
         boolean hov = mouseX >= x && mouseX <= x + CELL && mouseY >= y && mouseY <= y + CELL;
         if (hov) hoveredIdx = i;
         boolean prot = this.isProtected(e);
         int border = prot ? 0xFF2FBF71 : hov ? this.primaryColor & 0xFFFFFF | 0xFF000000 : 0x50FFFFFF;
         GuiStyle.roundedRect(context, x, y, CELL, CELL, 5, (hov ? 0x58 : 0x34) << 24 | 0xFFFFFF);
         GuiStyle.roundedOutline(context, x, y, CELL, CELL, 5, (int) (anim * 255.0F) << 24 | border & 0xFFFFFF);
         context.drawItem(e.stack(), x + 5, y + 5);
         if (prot) {
            // small green check dot in the corner
            context.fill(x + CELL - 7, y + CELL - 7, x + CELL - 2, y + CELL - 2, 0xFF2FBF71);
            context.fill(x + CELL - 6, y + CELL - 5, x + CELL - 5, y + CELL - 3, 0xFF0E2A1A);
            context.fill(x + CELL - 5, y + CELL - 4, x + CELL - 3, y + CELL - 3, 0xFF0E2A1A);
         }
      }
      context.disableScissor();

      // Hover tooltip: name + id under cursor
      if (hoveredIdx >= 0) {
         Entry e = this.filtered.get(hoveredIdx);
         String label = e.name() + " \u00b7 " + e.path();
         int tw = this.textRenderer.getWidth(label);
         int tx = Math.min(this.width - tw - 10, Math.max(6, (int) mouseX - tw / 2));
         int ty = mouseY + 14;
         if (ty + 14 > this.height) ty = (int) mouseY - 18;
         GuiStyle.roundedRect(context, tx - 5, ty - 3, tw + 10, 14, 4, 0xE8140E1E);
         context.drawTextWithShadow(this.textRenderer, Text.literal(label), tx, ty, 0xFFF0F3F8);
      }

      // Scrollbar
      if (this.maxScroll > 0) {
         int trackX = this.width - 9;
         context.fill(trackX, this.gridTop, trackX + 3, this.gridBottom, 0x30FFFFFF);
         float ratio = (float) (this.gridBottom - this.gridTop) / contentH;
         int barH = Math.max(24, (int) ((this.gridBottom - this.gridTop) * ratio));
         int barY = this.gridTop + (int) (((this.gridBottom - this.gridTop) - barH) * (this.scrollOffset / (float) this.maxScroll));
         context.fill(trackX, barY, trackX + 3, barY + barH, 0x90FFFFFF);
      }

      // Bottom bar: Clear all + Done
      int by = this.height - 30;
      int bw = 110;
      int bx1 = this.width / 2 - bw - 6;
      int bx2 = this.width / 2 + 6;
      boolean hov1 = mouseX >= bx1 && mouseX <= bx1 + bw && mouseY >= by && mouseY <= by + 20;
      boolean hov2 = mouseX >= bx2 && mouseX <= bx2 + bw && mouseY >= by && mouseY <= by + 20;
      GuiStyle.roundedRect(context, bx1, by, bw, 20, 5, (hov1 ? 0x60 : 0x38) << 24 | 0xFFFFFF);
      GuiStyle.textCentered(context, this.textRenderer, "Clear All", bx1 + bw / 2, by + 6, hov1 ? 0xFFFFB4B4 : 0xFFFFFFFF);
      int accent = hov2 ? this.primaryColor : 0xFF2A2333;
      GuiStyle.roundedRect(context, bx2, by, bw, 20, 5, 0x66000000 | accent & 0xFFFFFF);
      GuiStyle.textCentered(context, this.textRenderer, "Done", bx2 + bw / 2, by + 6, 0xFFE8E4F0);

      if (System.currentTimeMillis() < this.statusUntil && !this.statusMsg.isEmpty()) {
         GuiStyle.textCentered(context, this.textRenderer, this.statusMsg, this.width / 2, this.height - 40, 0xFF9FE8FF);
      }
   }

   private boolean queryFocused() {
      // the search box has focus unless the cursor is deep in the grid area with no click yet —
      // keep it simple: the search owns all typing
      return true;
   }

   private Entry entryAt(double mouseX, double mouseY) {
      if (mouseY < this.gridTop || mouseY > this.gridBottom) return null;
      for (int i = 0; i < this.filtered.size(); i++) {
         int row = i / this.cols;
         int col = i % this.cols;
         int x = this.gridX + col * (CELL + GAP);
         int y = this.gridTop + 4 + row * (CELL + GAP) - (int) this.scrollOffset;
         if (mouseX >= x && mouseX <= x + CELL && mouseY >= y && mouseY <= y + CELL) {
            return this.filtered.get(i);
         }
      }
      return null;
   }

   @Override
   public boolean mouseClicked(Click click, boolean doubled) {
      double mouseX = click.x();
      double mouseY = click.y();
      int button = click.button();

      // Bottom bar buttons
      int by = this.height - 30;
      int bw = 110;
      int bx1 = this.width / 2 - bw - 6;
      int bx2 = this.width / 2 + 6;
      if (button == 0 && mouseY >= by && mouseY <= by + 20) {
         if (mouseX >= bx1 && mouseX <= bx1 + bw) {
            this.protectedIds.clear();
            this.saveProtected();
            this.status("Cleared all protected items");
            return true;
         }
         if (mouseX >= bx2 && mouseX <= bx2 + bw) {
            if (this.parent != null) {
               this.client.setScreen(this.parent);
            } else {
               this.client.setScreen(null);
            }
            return true;
         }
      }

      Entry e = this.entryAt(mouseX, mouseY);
      if (e != null && button == 0) {
         this.toggle(e);
         return true;
      }

      return super.mouseClicked(click, doubled);
   }

   @Override
   public boolean keyPressed(KeyInput input) {
      int key = input.key();
      if (key == GLFW.GLFW_KEY_ESCAPE) {
         if (this.parent != null) {
            this.client.setScreen(this.parent);
         } else {
            this.client.setScreen(null);
         }
         return true;
      }
      // search typing: printable chars handled in charTyped, backspace here
      if (key == GLFW.GLFW_KEY_BACKSPACE && !this.query.isEmpty()) {
         this.query = this.query.substring(0, this.query.length() - 1);
         this.updateFilter();
         return true;
      }
      return super.keyPressed(input);
   }

   @Override
   public boolean charTyped(net.minecraft.client.input.CharInput input) {
      if (input.isValidChar() && this.query.length() < 48) {
         this.query += input.asString();
         this.updateFilter();
         return true;
      }
      return super.charTyped(input);
   }

   @Override
   public boolean mouseDragged(Click click, double dx, double dy) {
      if (this.dragging && this.maxScroll > 0) {
         this.scrollTarget = Math.max(0, Math.min(this.maxScroll, this.scrollTarget - (int) dy));
         return true;
      }
      return super.mouseDragged(click, dx, dy);
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
      if (this.maxScroll > 0) {
         this.scrollTarget = Math.max(0, Math.min(this.maxScroll, this.scrollTarget - (int) (vertical * 40.0)));
         return true;
      }
      return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
   }

   @Override
   public void removed() {
      super.removed();
   }
}
