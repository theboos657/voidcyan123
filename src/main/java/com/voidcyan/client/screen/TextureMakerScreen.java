package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import com.voidcyan.client.screen.ColorPickerModal;
import com.voidcyan.client.util.DynamicItemPreviewer;
import com.voidcyan.client.util.PlayerModelManager;
import com.voidcyan.client.util.TexturePackManager;
import com.voidcyan.client.util.TextureTarget;
import java.nio.file.Path;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.input.MouseInput;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.state.ArmedEntityRenderState;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.EntityPose;
import net.minecraft.util.SwingAnimationType;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EquippableComponent;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import com.voidcyan.client.util.ModelRaycaster;
import org.lwjgl.glfw.GLFW;


public class TextureMakerScreen extends Screen {
   private final Screen parent;
   private TextFieldWidget searchField;
   private String lastSearchQuery = "";
   private final List<TextureTarget> allTargets = new ArrayList<>();
   private final List<TextureTarget> filteredTargets = new ArrayList<>();

   // Categories & Filtering
   private TextureTarget.Category activeCategory = TextureTarget.Category.ALL;
   private boolean filterModifiedOnly = false;

   // View mode: null = catalog, non-null = editor
   private TextureTarget selectedTarget = null;

   // 3D Viewport State
   private boolean viewMode3D = true;
   private float cameraYaw = 0.0F;
   private float cameraPitch = 0.0F;
   private float zoomScale = 1.0F;
   private float panOffsetX = 0.0F;
   private float panOffsetY = 0.0F;
   private boolean isOrbiting = false;
   private boolean isPanning = false;

   // Dynamic Preview Texture for real-time 3D model reflection
   private NativeImageBackedTexture dynamicPreviewTexture = null;
   private Identifier dynamicPreviewTextureId = null;

   // Editor State
   private int canvasWidth = 16;
   private int canvasHeight = 16;
   private int[][] canvasPixels = new int[16][16];
   private boolean showGrid = true;
   private int activeTool = 0; // 0 = Pencil, 1 = Eraser, 2 = Bucket, 3 = Eyedropper, 4 = Orbit


   // Undo / Redo History
   private final Deque<int[][]> undoStack = new ArrayDeque<>();
   private final Deque<int[][]> redoStack = new ArrayDeque<>();

   // Color Studio State (HSV + Alpha)
   private float currentHue = 0.52F;       // Cyan default
   private float currentSaturation = 1.0F; // 0.0 - 1.0
   private float currentValue = 1.0F;      // 0.0 - 1.0
   private int currentAlpha = 255;         // 0 - 255
   private int activeColor = 0xFF00F5FF;   // ARGB
   private ColorPickerModal colorModal = null;

   // Drag states
   private boolean isDraggingCanvas = false;
   private int lastDragBtn = 0;

   // Catalog scroll
   private int catalogScroll = 0;
   private int maxCatalogScroll = 0;

   // Player Model panel state
   /** Set while the file-picker thread is running so we don't spawn duplicates. */
   private volatile boolean pmPickerRunning = false;
   /** Latest status string displayed in the Player Model panel. */
   private String pmStatus = "";

   // Notification banner
   private String statusMessage = "";
   private long statusMessageTime = 0L;

   // Studio Palette Presets (24 colors)
   private static final int[] PRESET_PALETTE = new int[]{
      0xFF000000, 0xFF222222, 0xFF555555, 0xFF888888, 0xFFBBBBBB, 0xFFFFFFFF,
      0xFFFF2222, 0xFFFF6600, 0xFFFFCC00, 0xFF88FF00, 0xFF00FF66, 0xFF00F5FF,
      0xFF0088FF, 0xFF3300FF, 0xFF8800FF, 0xFFFF00CC, 0xFFFF6699, 0xFFB37346,
      0xFF5C3826, 0xFF4A6B82, 0xFF46A082, 0xFFD4AF37, 0xFFC0C0C0, 0xFF1C1C24
   };

   public TextureMakerScreen(Screen parent) {
      super(Text.literal("Texture Studio"));
      this.parent = parent;
   }

   @Override
   protected void init() {
      super.init();
      if (this.allTargets.isEmpty()) {
         // Populate Worn Armor model targets (first, so they appear at top)
         // Category MOBS: armor cards live with the 3D-model targets, and the
         // "Items" tab stays for things you actually hold in hand/inventory.
         this.allTargets.addAll(TextureTarget.buildArmorTargets());
         // Populate Mobs
         this.allTargets.addAll(TextureTarget.buildMobTargets());
         // Populate Items & Blocks
         for (Item item : Registries.ITEM) {
            if (item != Items.AIR) {
               this.allTargets.add(TextureTarget.fromItem(item));
            }
         }
      }

      int sbW = Math.min(170, Math.max(120, this.width - 420));
      int sbX = 14;
      this.searchField = new TextFieldWidget(this.textRenderer, sbX + 22, 37, sbW - 38, 18, Text.literal("Search..."));
      this.searchField.setMaxLength(64);
      this.searchField.setText(this.lastSearchQuery);
      this.searchField.setChangedListener(this::updateFilter);
      this.searchField.setDrawsBackground(false);
      this.addDrawableChild(this.searchField);

      this.updateFilter(this.lastSearchQuery);
      updateActiveColor();
   }

   // Cached LivingEntity instances for 3D rendering
   private final Map<String, LivingEntity> cachedEntities = new HashMap<>();

   // Armor LAYER model (humanoid / humanoid_leggings layout) for raycasting worn-armor
   // targets; null for everything else (base entity model is used instead).
   private BipedEntityModel<?> armorLayerModel = null;

   /**
    * For armor:* targets, builds the vanilla armor-layer biped matching the edited
    * texture's layer (legs -> humanoid_leggings inner model, else humanoid outer).
    * Its UVs are exactly the humanoid 64x32 layout of the texture being painted.
    */
   private BipedEntityModel<?> resolveArmorLayerModel(TextureTarget target) {
      if (target == null || !target.getId().startsWith("armor:")) return null;
      EquipmentSlot slot = target.getId().endsWith("_legs") ? EquipmentSlot.LEGS : EquipmentSlot.CHEST;
      return ModelRaycaster.createArmorLayerModel(slot);
   }

   /**
    * Single raycast entry point used everywhere (frame hover + click paint).
    * Armor targets raycast their dedicated layer model (correct UV layout); all
    * other targets raycast the entity's base model as before.
    */
   private ModelRaycaster.RayHit raycastEntity(
      LivingEntity entity, float mx, float my,
      float centerX, float centerY, float scale,
      int canvasW, int canvasH
   ) {
      if (this.armorLayerModel != null) {
         return ModelRaycaster.raycastWithModel(
            entity, getOrCreateEntityRenderState(entity), this.armorLayerModel,
            mx, my, centerX, centerY, scale, this.cameraYaw, this.cameraPitch,
            canvasW, canvasH
         );
      }
      return ModelRaycaster.raycast(
         entity, mx, my, centerX, centerY, scale, this.cameraYaw, this.cameraPitch,
         canvasW, canvasH
      );
   }

   // Dynamic Item Previewer animation states (index into PREVIEW_MODE_LABELS).
   private int previewAnimationMode = 0;
   private long previewAnimStartTime = System.currentTimeMillis();
   private static final String[] PREVIEW_MODE_LABELS = {"Stand", "Run", "Swim", "Fly"};

   /**
    * Arms the Dynamic Item Previewer for the current selection: item targets get
    * a live atlas override of their sprite; armor/mob targets keep their own
    * dedicated 3D preview entity and need no atlas override.
    */
   private void setupLivePreview() {
      DynamicItemPreviewer.endOverride();
      TextureTarget target = this.selectedTarget;
      if (target == null || target.isMob() || target.getId().startsWith("armor:")) return;

      // Arm the atlas override by probing every candidate texture path: atlas sprite
      // ids drop the "textures/" prefix and ".png" suffix (textures/item/diamond.png
      // -> item/diamond), and block items stitch under block/ rather than item/.
      for (Identifier path : target.getCandidatePaths()) {
         String dir = path.getPath();
         String spritePath = dir.startsWith("textures/") ? dir.substring("textures/".length()) : dir;
         if (spritePath.endsWith(".png")) spritePath = spritePath.substring(0, spritePath.length() - 4);
         Identifier spriteId = Identifier.of(path.getNamespace(), spritePath);

         if (DynamicItemPreviewer.beginOverride(spriteId, this.canvasWidth, this.canvasHeight)) {
            break;
         }
      }
   }

   public boolean isArmorTarget(TextureTarget target) {
      if (target == null) return false;
      String id = target.getId().toLowerCase();
      if (id.startsWith("armor:")) return true;
      if (target.getIcon() != null && target.getIcon().contains(DataComponentTypes.EQUIPPABLE)) {
         EquippableComponent eq = target.getIcon().get(DataComponentTypes.EQUIPPABLE);
         if (eq != null && eq.slot().isArmorSlot()) return true;
      }
      return id.contains("helmet") || id.contains("chestplate") || id.contains("leggings") || id.contains("boots");
   }

   public boolean is3dTarget(TextureTarget target) {
      return target != null && (target.isMob() || isArmorTarget(target) || getPreviewEntity(target) != null);
   }

   public LivingEntity getPreviewEntity(TextureTarget target) {
      if (target == null) return null;
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.world == null) return null;

      if (this.cachedEntities.containsKey(target.getId())) {
         return this.cachedEntities.get(target.getId());
      }

      LivingEntity entity = null;
      if (target.isMob()) {
         String mobId = target.getId();
         if (mobId.equals("mob:steve") || mobId.equals("mob:alex")) {
            entity = client.player;
         } else {
            entity = target.createPreviewEntity();
         }
      } else if (isArmorTarget(target)) {
         entity = createArmorStandForTarget(target);
      }

      if (entity != null) {
         this.cachedEntities.put(target.getId(), entity);
      }
      return entity;
   }

   private ArmorStandEntity createArmorStandForTarget(TextureTarget target) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.world == null) return null;
      try {
         ArmorStandEntity stand = new ArmorStandEntity(EntityType.ARMOR_STAND, client.world);
         stand.setShowArms(true);
         stand.setHideBasePlate(true);
         // Invisible stand: vanilla skips the base model entirely (invisible-to-viewer
         // -> null layer) while feature renderers still draw the equipped armor layer.
         // The viewport then shows ONLY the armor piece whose UVs the editor paints.
         stand.setInvisible(true);
         stand.setNoGravity(true);
         String id = target.getId().toLowerCase();
         ItemStack iconStack = target.getIcon();

         if (iconStack != null && iconStack.contains(DataComponentTypes.EQUIPPABLE)) {
            EquippableComponent eq = iconStack.get(DataComponentTypes.EQUIPPABLE);
            if (eq != null && eq.slot().isArmorSlot()) {
               stand.equipStack(eq.slot(), iconStack);
               return stand;
            }
         }

         Item item = null;

         if (id.contains("diamond")) {
            if (id.contains("legs") || id.contains("leggings")) item = Items.DIAMOND_LEGGINGS;
            else if (id.contains("boots")) item = Items.DIAMOND_BOOTS;
            else if (id.contains("helmet")) item = Items.DIAMOND_HELMET;
            else item = Items.DIAMOND_CHESTPLATE;
         } else if (id.contains("netherite")) {
            if (id.contains("legs") || id.contains("leggings")) item = Items.NETHERITE_LEGGINGS;
            else if (id.contains("boots")) item = Items.NETHERITE_BOOTS;
            else if (id.contains("helmet")) item = Items.NETHERITE_HELMET;
            else item = Items.NETHERITE_CHESTPLATE;
         } else if (id.contains("iron")) {
            if (id.contains("legs") || id.contains("leggings")) item = Items.IRON_LEGGINGS;
            else if (id.contains("boots")) item = Items.IRON_BOOTS;
            else if (id.contains("helmet")) item = Items.IRON_HELMET;
            else item = Items.IRON_CHESTPLATE;
         } else if (id.contains("gold")) {
            if (id.contains("legs") || id.contains("leggings")) item = Items.GOLDEN_LEGGINGS;
            else if (id.contains("boots")) item = Items.GOLDEN_BOOTS;
            else if (id.contains("helmet")) item = Items.GOLDEN_HELMET;
            else item = Items.GOLDEN_CHESTPLATE;
         } else if (id.contains("chainmail")) {
            if (id.contains("legs") || id.contains("leggings")) item = Items.CHAINMAIL_LEGGINGS;
            else if (id.contains("boots")) item = Items.CHAINMAIL_BOOTS;
            else if (id.contains("helmet")) item = Items.CHAINMAIL_HELMET;
            else item = Items.CHAINMAIL_CHESTPLATE;
         } else if (id.contains("leather")) {
            if (id.contains("legs") || id.contains("leggings")) item = Items.LEATHER_LEGGINGS;
            else if (id.contains("boots")) item = Items.LEATHER_BOOTS;
            else if (id.contains("helmet")) item = Items.LEATHER_HELMET;
            else item = Items.LEATHER_CHESTPLATE;
         } else if (id.contains("copper")) {
            if (id.contains("legs") || id.contains("leggings")) item = Items.COPPER_LEGGINGS;
            else if (id.contains("boots")) item = Items.COPPER_BOOTS;
            else if (id.contains("helmet")) item = Items.COPPER_HELMET;
            else item = Items.COPPER_CHESTPLATE;
         } else if (id.contains("turtle")) {
            item = Items.TURTLE_HELMET;
         }

         if (item != null) {
            ItemStack armorStack = new ItemStack(item);
            String itemStr = item.toString().toLowerCase();
            if (itemStr.contains("helmet") || id.contains("helmet") || id.contains("turtle")) {
               stand.equipStack(EquipmentSlot.HEAD, armorStack);
            } else if (itemStr.contains("chestplate") || id.contains("body")) {
               stand.equipStack(EquipmentSlot.CHEST, armorStack);
            } else if (itemStr.contains("leggings") || id.contains("legs")) {
               stand.equipStack(EquipmentSlot.LEGS, armorStack);
            } else if (itemStr.contains("boots")) {
               stand.equipStack(EquipmentSlot.FEET, armorStack);
            }
         }
         return stand;
      } catch (Exception e) {
         return null;
      }
   }

   /**
    * Mutates a freshly-created render state into the selected preview animation.
    * Runs AFTER state creation (getOrCreateEntityRenderState) and BEFORE addEntity —
    * vanilla re-poses the biped model from these fields at GUI-render time, so
    * standing/running/swimming/flying all come from vanilla's own posing math.
    *
    * Limb amplitude drives the leg/arm swing; limbSwingAnimationProgress is advanced
    * by wall-clock time so Run keeps animating even while the game world is paused
    * behind the screen.
    */
   private void applyPreviewAnimationState(EntityRenderState state) {
      if (!(state instanceof LivingEntityRenderState ls)) return;

      // Swing fields live on the armed subclass; neutralize them for all modes.
      if (ls instanceof ArmedEntityRenderState armed) {
         armed.handSwingProgress = 0.0F;
         if (armed.swingAnimationType != null) armed.swingAnimationType = SwingAnimationType.NONE;
      }

      float elapsed = (System.currentTimeMillis() - this.previewAnimStartTime) / 1000.0F;
      switch (this.previewAnimationMode) {
         case 1 -> {
            // Run: fast, wide limb swing.
            ls.pose = EntityPose.STANDING;
            ls.limbSwingAmplitude = 1.35F;
            ls.limbSwingAnimationProgress = elapsed * 2.4F;
         }
         case 2 -> {
            // Swim: horizontal body (90° pitch), moderate crawl-style limb swing.
            ls.pose = EntityPose.SWIMMING;
            ls.limbSwingAmplitude = 0.9F;
            ls.limbSwingAnimationProgress = elapsed * 1.7F;
            ls.pitch = (float) Math.toRadians(85.0);
            if (ls instanceof ArmedEntityRenderState armed) {
               armed.rightArmPose = BipedEntityModel.ArmPose.EMPTY;
               armed.leftArmPose = BipedEntityModel.ArmPose.EMPTY;
            }
         }
         case 3 -> {
            // Fly (elytra glide): full horizontal body + arms back, no limb swing.
            ls.pose = EntityPose.GLIDING;
            ls.limbSwingAmplitude = 0.0F;
            ls.limbSwingAnimationProgress = 0.0F;
            ls.pitch = (float) Math.toRadians(90.0);
            if (ls instanceof ArmedEntityRenderState armed) {
               armed.rightArmPose = BipedEntityModel.ArmPose.EMPTY;
               armed.leftArmPose = BipedEntityModel.ArmPose.EMPTY;
            }
         }
         default -> {
            // Stand: neutral idle.
            ls.pose = EntityPose.STANDING;
            ls.limbSwingAmplitude = 0.0F;
            ls.limbSwingAnimationProgress = 0.0F;
            ls.pitch = 0.0F;
         }
      }
   }

   private int getPrevCardHeight() {
      return (this.selectedTarget != null && getPreviewEntity(this.selectedTarget) != null) ? 76 : 46;
   }

   /**
    * Extra sidebar height contributed by the Dynamic Item Previewer panel.
    * Zero for 3D targets (the dedicated armor/mob preview replaces it) or when
    * no player is available to drive the preview model.
    */
   private int itemPreviewPanelHeight() {
      MinecraftClient client = MinecraftClient.getInstance();
      boolean hasHolder = client.player != null
         && (this.selectedTarget == null || !is3dTarget(this.selectedTarget));
      return hasHolder ? 96 + 8 : 0;
   }

   private void updateFilter(String query) {
      this.lastSearchQuery = query;
      this.filteredTargets.clear();
      String q = query.trim().toLowerCase();
      String[] words = q.isEmpty() ? new String[0] : q.split("\\s+");

      for (TextureTarget target : this.allTargets) {
         if (this.filterModifiedOnly && !TexturePackManager.hasCustomTexture(target)) {
            continue;
         }
         if (this.activeCategory != TextureTarget.Category.ALL && target.getCategory() != this.activeCategory) {
            continue;
         }
         if (words.length > 0) {
            String name = target.getDisplayName().toLowerCase();
            String id = target.getId().toLowerCase().replace('_', ' ');
            boolean allMatch = true;
            for (String w : words) {
               if (w.isEmpty()) continue;
               boolean wordMatch = name.contains(w) || id.contains(w);
               if (!wordMatch && w.equals("armor")) {
                  wordMatch = name.contains("helmet") || name.contains("chestplate")
                           || name.contains("leggings") || name.contains("boots")
                           || id.startsWith("armor:")
                           || id.contains("helmet") || id.contains("chestplate")
                           || id.contains("leggings") || id.contains("boots");
               }
               if (!wordMatch) {
                  allMatch = false;
                  break;
               }
            }
            if (!allMatch) continue;
         }
         this.filteredTargets.add(target);
      }
      this.catalogScroll = 0;
   }


   /**
    * Resolves the target the editor should actually open.
    *
    * Plain armor items (e.g. "Diamond Chestplate") are 16x16 item sprites, which
    * cannot drive the worn 3D equipment model — selecting them previously gave a
    * flat sprite experience. Armor items are therefore redirected onto the matching
    * worn-armor target (armor:*) that shares the same equipment texture file, so the
    * editor opens the full humanoid texture (64x32) with a live 3D armor-stand preview.
    * Worn targets and mobs resolve to themselves; non-armor items return null.
    */
   private TextureTarget resolveWornArmorTarget(TextureTarget target) {
      if (target == null || target.isMob() || target.getId().startsWith("armor:")) return target;

      ItemStack icon = target.getIcon();
      if (icon == null || !icon.contains(DataComponentTypes.EQUIPPABLE)) return null;

      EquippableComponent eq = icon.get(DataComponentTypes.EQUIPPABLE);
      if (eq == null || !eq.slot().isArmorSlot()) return null;

      String assetPath = eq.assetId().map(key -> key.getValue().getPath()).orElse(null);
      if (assetPath == null) return null;

      // The worn texture lives under the layer directory matching the slot:
      // legs use the humanoid_leggings layer, everything else the humanoid layer.
      String layerDir = eq.slot() == EquipmentSlot.LEGS
         ? "textures/entity/equipment/humanoid_leggings/"
         : "textures/entity/equipment/humanoid/";
      String wanted = layerDir + assetPath + ".png";

      for (TextureTarget t : this.allTargets) {
         if (t.getId().startsWith("armor:")) {
            for (Identifier path : t.getCandidatePaths()) {
               if (path.getPath().equals(wanted)) {
                  return t;
               }
            }
         }
      }

      // Fallback: match the id suffix convention ("armor:<material>_body/_legs").
      String suffix = eq.slot() == EquipmentSlot.LEGS ? "_legs" : "_body";
      for (TextureTarget t : this.allTargets) {
         if (t.getId().equals("armor:" + assetPath + suffix)) {
            return t;
         }
      }

      return null;
   }

   private void selectTarget(TextureTarget target) {
      // Armor items redirect onto their worn-armor model target so 3D editing works.
      TextureTarget worn = resolveWornArmorTarget(target);
      if (worn != null && worn != target) {
         target = worn;
         this.statusMessage = "Editing worn model texture (3D)";
         this.statusMessageTime = System.currentTimeMillis();
      }

      this.selectedTarget = target;
      this.searchField.setVisible(false);
      // Release focus so the hidden field cannot swallow keys/chars in the editor
      // (tool shortcuts like V for the 3D/2D toggle run before super, but typed
      // characters would still land in the invisible field via super.charTyped).
      this.searchField.setFocused(false);
      this.setFocused(null);
      this.undoStack.clear();
      this.redoStack.clear();

      boolean is3d = is3dTarget(target);
      this.viewMode3D = is3d;
      this.cameraYaw = 0.0F;
      this.cameraPitch = 0.0F;
      this.zoomScale = 1.0F;
      this.panOffsetX = 0.0F;
      this.panOffsetY = 0.0F;
      this.isOrbiting = false;
      this.isPanning = false;

      BufferedImage img = TexturePackManager.loadCustomTexture(target);
      if (img == null) {
         img = TexturePackManager.loadVanillaTexture(target);
      }

      if (img != null) {
         this.canvasWidth = img.getWidth();
         this.canvasHeight = img.getHeight();
         this.canvasPixels = new int[this.canvasWidth][this.canvasHeight];
         for (int x = 0; x < this.canvasWidth; x++) {
            for (int y = 0; y < this.canvasHeight; y++) {
               this.canvasPixels[x][y] = img.getRGB(x, y);
            }
         }
      } else {
         this.canvasWidth = target.getDefaultWidth();
         this.canvasHeight = target.getDefaultHeight();
         this.canvasPixels = new int[this.canvasWidth][this.canvasHeight];
      }
      this.updateDynamicPreviewTexture();
      this.setupLivePreview();
      this.previewAnimStartTime = System.currentTimeMillis();
      this.armorLayerModel = resolveArmorLayerModel(target);
   }

   public void updateDynamicPreviewTexture() {
      if (this.canvasWidth <= 0 || this.canvasHeight <= 0) return;
      if (this.dynamicPreviewTexture == null ||
          this.dynamicPreviewTexture.getImage().getWidth() != this.canvasWidth ||
          this.dynamicPreviewTexture.getImage().getHeight() != this.canvasHeight) {
         if (this.dynamicPreviewTexture != null) {
            this.dynamicPreviewTexture.close();
         }
         NativeImage img = new NativeImage(this.canvasWidth, this.canvasHeight, true);
         this.dynamicPreviewTexture = new NativeImageBackedTexture(() -> "voidcyan_dyn_preview", img);
         this.dynamicPreviewTextureId = Identifier.of("voidcyan", "dyn_preview_" + System.currentTimeMillis());
         MinecraftClient.getInstance().getTextureManager().registerTexture(this.dynamicPreviewTextureId, this.dynamicPreviewTexture);
      }
      NativeImage img = this.dynamicPreviewTexture.getImage();
      for (int x = 0; x < this.canvasWidth; x++) {
         for (int y = 0; y < this.canvasHeight; y++) {
            img.setColorArgb(x, y, this.canvasPixels[x][y]);
         }
      }
      this.dynamicPreviewTexture.upload();
   }

   @SuppressWarnings({"rawtypes", "unchecked"})
   public static EntityRenderState getOrCreateEntityRenderState(LivingEntity entity) {
      MinecraftClient client = MinecraftClient.getInstance();
      EntityRenderManager dispatcher = client.getEntityRenderDispatcher();
      EntityRenderer renderer = dispatcher.getRenderer(entity);
      EntityRenderState state = renderer.getAndUpdateRenderState(entity, 1.0F);
      state.light = 15728880;
      state.shadowPieces.clear();
      state.outlineColor = 0;
      if (state instanceof LivingEntityRenderState ls) {
         ls.bodyYaw = 180.0F;
         ls.relativeHeadYaw = 0.0F;
         ls.pitch = 0.0F;
         ls.width = ls.width / ls.baseScale;
         ls.height = ls.height / ls.baseScale;
         ls.baseScale = 1.0F;
      }
      return state;
   }

   @Override
   public void close() {
      VoidCyanClient.customPreviewTexture = null;
      DynamicItemPreviewer.endOverride();
      if (this.dynamicPreviewTexture != null) {
         this.dynamicPreviewTexture.close();
         this.dynamicPreviewTexture = null;
      }
      this.cachedEntities.clear();
      super.close();
   }


   private void pushUndo() {
      int[][] copy = new int[this.canvasWidth][this.canvasHeight];
      for (int x = 0; x < this.canvasWidth; x++) {
         System.arraycopy(this.canvasPixels[x], 0, copy[x], 0, this.canvasHeight);
      }
      this.undoStack.push(copy);
      if (this.undoStack.size() > 30) {
         this.undoStack.removeLast();
      }
      this.redoStack.clear();
   }

   private void undo() {
      if (this.undoStack.isEmpty()) return;
      int[][] current = new int[this.canvasWidth][this.canvasHeight];
      for (int x = 0; x < this.canvasWidth; x++) {
         System.arraycopy(this.canvasPixels[x], 0, current[x], 0, this.canvasHeight);
      }
      this.redoStack.push(current);

      int[][] prev = this.undoStack.pop();
      if (prev.length == this.canvasWidth && prev[0].length == this.canvasHeight) {
         for (int x = 0; x < this.canvasWidth; x++) {
            System.arraycopy(prev[x], 0, this.canvasPixels[x], 0, this.canvasHeight);
         }
         this.statusMessage = "Undo applied";
         this.statusMessageTime = System.currentTimeMillis();
         this.updateDynamicPreviewTexture();
      }
   }

   private void redo() {
      if (this.redoStack.isEmpty()) return;
      int[][] current = new int[this.canvasWidth][this.canvasHeight];
      for (int x = 0; x < this.canvasWidth; x++) {
         System.arraycopy(this.canvasPixels[x], 0, current[x], 0, this.canvasHeight);
      }
      this.undoStack.push(current);

      int[][] next = this.redoStack.pop();
      if (next.length == this.canvasWidth && next[0].length == this.canvasHeight) {
         for (int x = 0; x < this.canvasWidth; x++) {
            System.arraycopy(next[x], 0, this.canvasPixels[x], 0, this.canvasHeight);
         }
         this.statusMessage = "Redo applied";
         this.statusMessageTime = System.currentTimeMillis();
         this.updateDynamicPreviewTexture();
      }
   }

   // ================= SHARED TOOL-ROW GEOMETRY =================
   // Single source of truth for the second toolbar row, used by BOTH the renderer
   // and the click handler. Duplicating this layout between render and input is
   // what previously let the 3D mode toggle's hit region drift out of sync.

   /** Canvas top edge in the editor view; mirrors canvasMarginTop in render/click. */
   private static final int CANVAS_TOP = 74;
   /** Y of the second toolbar row (mode toggle, tools, grid/reset). */
   private static final int TOOL_ROW2_Y = 52;
   /** Slot indexes into the tool row: 0 = mode toggle, 1..N = tools, 5 = grid/reset. */
   private static final int HIT_MODE = 0;
   private static final int HIT_GRID_RESET = 5;

   private int getToolX(int slot, int modeWidth, boolean targetIs3d) {
      int x = 12;
      if (targetIs3d) {
         if (slot == HIT_MODE) return x;
         x += modeWidth + 5;
      }

      String[] toolNames = targetIs3d
         ? new String[]{"Pencil (B)", "Eraser (E)", "Bucket (G)", "Pick (I)", "Orbit (R)"}
         : new String[]{"Pencil (B)", "Eraser (E)", "Bucket (G)", "Pick (I)"};

      for (int t = 0; t < toolNames.length; t++) {
         if (slot == t + 1) return x;
         x += this.textRenderer.getWidth(toolNames[t]) + 8 + 4;
      }

      return x;
   }

   /**
    * Which tool-row button is under the given point: HIT_MODE (0), tool slot
    * 1..toolNames.length, HIT_GRID_RESET (5), or -1 when nothing is hit.
    */
   private int getToolRowHitIndex(double px, double py, int modeWidth, boolean targetIs3d) {
      if (py < TOOL_ROW2_Y || py > TOOL_ROW2_Y + 16) return -1;

      if (targetIs3d) {
         if (px >= 12 && px <= 12 + modeWidth) return HIT_MODE;
      }

      String[] toolNames = targetIs3d
         ? new String[]{"Pencil (B)", "Eraser (E)", "Bucket (G)", "Pick (I)", "Orbit (R)"}
         : new String[]{"Pencil (B)", "Eraser (E)", "Bucket (G)", "Pick (I)"};

      for (int t = 0; t < toolNames.length; t++) {
         int tw = this.textRenderer.getWidth(toolNames[t]) + 8;
         int x = getToolX(t + 1, modeWidth, targetIs3d);
         if (px >= x && px <= x + tw) return t + 1;
      }

      int gw = this.textRenderer.getWidth(this.showGrid ? "Grid: ON" : "Grid: OFF") + 8;
      int rw = this.textRenderer.getWidth("↺ Reset View") + 8;
      int endX = getToolX(5, modeWidth, targetIs3d);
      int endW = !this.viewMode3D || !targetIs3d ? gw : rw;
      if (px >= endX && px <= endX + endW) return HIT_GRID_RESET;
      return -1;
   }

   private void setResolution(int targetW, int targetH) {
      if (this.canvasWidth == targetW && this.canvasHeight == targetH) return;
      pushUndo();
      BufferedImage current = new BufferedImage(this.canvasWidth, this.canvasHeight, BufferedImage.TYPE_INT_ARGB);
      for (int x = 0; x < this.canvasWidth; x++) {
         for (int y = 0; y < this.canvasHeight; y++) {
            current.setRGB(x, y, this.canvasPixels[x][y]);
         }
      }
      BufferedImage resized = TexturePackManager.resize(current, targetW, targetH);
      this.canvasWidth = targetW;
      this.canvasHeight = targetH;
      this.canvasPixels = new int[targetW][targetH];
      for (int x = 0; x < targetW; x++) {
         for (int y = 0; y < targetH; y++) {
            this.canvasPixels[x][y] = resized.getRGB(x, y);
         }
      }
      this.statusMessage = "Resized to " + targetW + "x" + targetH;
      this.statusMessageTime = System.currentTimeMillis();
      this.updateDynamicPreviewTexture();
      // Re-arm the atlas override against the new sprite dimensions.
      this.setupLivePreview();
   }

   private void updateActiveColor() {
      int rgb = Color.HSBtoRGB(this.currentHue, this.currentSaturation, this.currentValue);
      this.activeColor = ((this.currentAlpha & 0xFF) << 24) | (rgb & 0x00FFFFFF);
   }

   private void setColorFromRgb(int rgb) {
      int a = (rgb >> 24) & 0xFF;
      int r = (rgb >> 16) & 0xFF;
      int g = (rgb >> 8) & 0xFF;
      int b = rgb & 0xFF;
      float[] hsb = Color.RGBtoHSB(r, g, b, null);
      this.currentHue = hsb[0];
      this.currentSaturation = hsb[1];
      this.currentValue = hsb[2];
      this.currentAlpha = a != 0 ? a : 255;
      updateActiveColor();
   }

   private void saveAndApply() {
      if (this.selectedTarget == null) return;
      BufferedImage img = new BufferedImage(this.canvasWidth, this.canvasHeight, BufferedImage.TYPE_INT_ARGB);
      for (int x = 0; x < this.canvasWidth; x++) {
         for (int y = 0; y < this.canvasHeight; y++) {
            img.setRGB(x, y, this.canvasPixels[x][y]);
         }
      }
      TexturePackManager.saveCustomTexture(this.selectedTarget, img);
      TexturePackManager.applyAndReload();
      // The resource reload re-stitched the atlas; re-arm the live override
      // against the fresh sprite so previewing continues seamlessly.
      DynamicItemPreviewer.endOverride();
      this.setupLivePreview();
      this.statusMessage = "Applied texture for " + this.selectedTarget.getDisplayName() + "!";
      this.statusMessageTime = System.currentTimeMillis();
   }

   private void revertSelected() {
      if (this.selectedTarget == null) return;
      pushUndo();
      TexturePackManager.deleteCustomTexture(this.selectedTarget);
      TexturePackManager.applyAndReload();
      BufferedImage van = TexturePackManager.loadVanillaTexture(this.selectedTarget);
      if (van != null) {
         this.canvasWidth = van.getWidth();
         this.canvasHeight = van.getHeight();
         this.canvasPixels = new int[this.canvasWidth][this.canvasHeight];
         for (int x = 0; x < this.canvasWidth; x++) {
            for (int y = 0; y < this.canvasHeight; y++) {
               this.canvasPixels[x][y] = van.getRGB(x, y);
            }
         }
      } else {
         this.canvasPixels = new int[this.canvasWidth][this.canvasHeight];
      }
      this.statusMessage = "Reverted " + this.selectedTarget.getDisplayName() + " to vanilla!";
      this.statusMessageTime = System.currentTimeMillis();
   }

   @Override
   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      // Modern sleek gradient obsidian background
      context.fill(0, 0, this.width, this.height, 0xF20D0D14);

      if (this.selectedTarget == null) {
         this.renderCatalogView(context, mouseX, mouseY);
      } else {
         this.renderEditorView(context, mouseX, mouseY);
      }

      if (this.colorModal != null) {
         this.colorModal.render(context, mouseX, mouseY, this.width, this.height);
         if (!this.colorModal.isOpen()) this.colorModal = null;
      }

      // Status Notification Toast
      if (!this.statusMessage.isEmpty() && System.currentTimeMillis() - this.statusMessageTime < 3000L) {
         int msgW = this.textRenderer.getWidth(this.statusMessage) + 24;
         int msgX = (this.width - msgW) / 2;
         int msgY = this.height - 32;
         context.fill(msgX, msgY, msgX + msgW, msgY + 22, 0xE814141E);
         int accent = VoidCyanClient.getPrimaryColor() | 0xFF000000;
         context.fill(msgX, msgY, msgX + msgW, msgY + 1, accent);
         context.fill(msgX - 1, msgY, msgX, msgY + 22, 0x40FFFFFF);
         context.fill(msgX + msgW, msgY, msgX + msgW + 1, msgY + 22, 0x40FFFFFF);
         context.drawTextWithShadow(this.textRenderer, this.statusMessage, msgX + 12, msgY + 7, 0xFF55FFFF);
      }

      super.render(context, mouseX, mouseY, delta);
   }

   // ================= CATALOG VIEW =================

   private void renderCatalogView(DrawContext context, int mouseX, int mouseY) {
      int primary = VoidCyanClient.getPrimaryColor() | 0xFF000000;

      // Header Bar
      context.drawTextWithShadow(this.textRenderer, Text.literal("TEXTURE STUDIO"), 14, 12, primary);
      context.drawTextWithShadow(this.textRenderer, Text.literal("Customize Blocks, Items, and Mobs in Real-Time"), 115, 13, 0xFF888899);

      // Back / Close button
      int backBtnW = 46;
      int backBtnX = this.width - backBtnW - 14;
      boolean backHover = mouseX >= backBtnX && mouseX <= backBtnX + backBtnW && mouseY >= 10 && mouseY <= 26;
      context.fill(backBtnX, 10, backBtnX + backBtnW, 26, backHover ? 0x60FFFFFF : 0x25FFFFFF);
      context.drawTextWithShadow(this.textRenderer, "Close", backBtnX + 11, 14, 0xFFFFFFFF);

      // Revert All button
      int revAllW = 72;
      int revAllX = backBtnX - revAllW - 6;
      boolean revAllHover = mouseX >= revAllX && mouseX <= revAllX + revAllW && mouseY >= 10 && mouseY <= 26;
      context.fill(revAllX, 10, revAllX + revAllW, 26, revAllHover ? 0x90D32F2F : 0x45D32F2F);
      context.fill(revAllX, 10, revAllX + revAllW, 11, 0x80FF5555);
      context.drawTextWithShadow(this.textRenderer, "Revert All", revAllX + 8, 14, 0xFFFF9999);

      // Modified Only Filter Button
      int modW = 86;
      int modX = revAllX - modW - 6;
      boolean modHover = mouseX >= modX && mouseX <= modX + modW && mouseY >= 10 && mouseY <= 26;
      int modBg = this.filterModifiedOnly ? (primary & 0x00FFFFFF | 0xC0000000) : (modHover ? 0x45FFFFFF : 0x20FFFFFF);
      context.fill(modX, 10, modX + modW, 26, modBg);
      if (this.filterModifiedOnly) {
         context.fill(modX, 10, modX + modW, 11, primary);
      }
      context.drawTextWithShadow(this.textRenderer, "Modified Only", modX + 8, 14, this.filterModifiedOnly ? 0xFF000000 : 0xFFCCCCCC);

      // Custom Search Bar
      int searchBarX = 14;
      int searchBarY = 35;
      int searchBarW = Math.min(170, Math.max(120, this.width - 420));
      int searchBarH = 22;
      boolean isFocused = this.searchField.isFocused();
      int borderColor = isFocused ? primary : 0x35FFFFFF;
      drawModernRoundedRect(context, searchBarX - 1, searchBarY - 1, searchBarW + 2, searchBarH + 2, 6, borderColor);
      drawModernRoundedRect(context, searchBarX, searchBarY, searchBarW, searchBarH, 6, 0xE814141E);

      // Search Icon
      Identifier searchIcon = Identifier.of("voidcyan", "textures/gui/icons/search.png");
      context.drawTexturedQuad(searchIcon, searchBarX + 6, searchBarY + 5, searchBarX + 18, searchBarY + 17, 0.0F, 1.0F, 0.0F, 1.0F);

      // Search field placeholder
      if (this.searchField.getText().isEmpty() && !isFocused) {
         context.drawTextWithShadow(this.textRenderer, Text.literal("Search..."), searchBarX + 22, searchBarY + 7, 0xFF666677);
      }
      // Search clear button (x)
      if (!this.searchField.getText().isEmpty()) {
         int clearX = searchBarX + searchBarW - 14;
         int clearY = searchBarY + 6;
         boolean clrHov = mouseX >= clearX && mouseX <= clearX + 10 && mouseY >= clearY && mouseY <= clearY + 10;
         context.drawTextWithShadow(this.textRenderer, "✕", clearX, clearY, clrHov ? 0xFFFFFFFF : 0xFFAAAAAA);
      }

      // Target Count Indicator
      int totalFiltered = this.filteredTargets.size();
      String countStr = totalFiltered + " targets";
      int countW = this.textRenderer.getWidth(countStr);
      int countRightMargin = countW + 20;
      context.drawTextWithShadow(this.textRenderer, countStr, this.width - countW - 14, 42, 0xFF777788);

      // Category Chips Bar
      int catX = searchBarX + searchBarW + 10;
      int catY = 37;
      for (TextureTarget.Category cat : TextureTarget.Category.values()) {
         String label = cat.getLabel();
         int cw = this.textRenderer.getWidth(label) + 14;
         if (catX + cw > this.width - countRightMargin) break;
         boolean isCur = this.activeCategory == cat;
         boolean cHov = mouseX >= catX && mouseX <= catX + cw && mouseY >= catY && mouseY <= catY + 18;

         int catBg = isCur ? primary : (cHov ? 0x50FFFFFF : 0x22FFFFFF);
         drawModernRoundedRect(context, catX, catY, cw, 18, 5, catBg);
         context.drawTextWithShadow(this.textRenderer, label, catX + 7, catY + 5, isCur ? 0xFF000000 : 0xFFDDDDDD);
         catX += cw + 4;
      }

      // Catalog Grid Area — leave the right PM_PANEL_W px for the Player Model sidebar
      int pmPanelX = this.width - PM_PANEL_W - 6;
      int startY = 64;
      int bottomMargin = 16;
      int gridH = this.height - startY - bottomMargin;
      int cardSize = 38;
      int gap = 5;
      int availW = pmPanelX - 30; // stop before the Player Model panel
      int cols = Math.max(1, availW / (cardSize + gap));
      int startX = 14; // left-aligned instead of centred so nothing clips into PM panel

      int totalItems = this.filteredTargets.size();
      int totalRows = (int)Math.ceil((double)totalItems / cols);
      this.maxCatalogScroll = Math.max(0, totalRows * (cardSize + gap) - gridH);
      this.catalogScroll = Math.clamp(this.catalogScroll, 0, this.maxCatalogScroll);

      context.enableScissor(startX - 2, startY, startX + cols * (cardSize + gap) + 4, startY + gridH);

      TextureTarget hoveredTarget = null;
      int hoveredX = 0;
      int hoveredY = 0;

      for (int i = 0; i < totalItems; i++) {
         int row = i / cols;
         int col = i % cols;
         int cardX = startX + col * (cardSize + gap);
         int cardY = startY + row * (cardSize + gap) - this.catalogScroll;

         if (cardY + cardSize < startY || cardY > startY + gridH) continue;

         TextureTarget target = this.filteredTargets.get(i);
         boolean isHover = mouseX >= cardX && mouseX <= cardX + cardSize && mouseY >= cardY && mouseY <= cardY + cardSize && mouseY >= startY && mouseY <= startY + gridH;
         boolean hasCustom = TexturePackManager.hasCustomTexture(target);

         int cardBg = isHover ? 0x65252538 : (hasCustom ? 0x30152535 : 0x2214141E);
         context.fill(cardX, cardY, cardX + cardSize, cardY + cardSize, cardBg);

         if (hasCustom) {
            context.fill(cardX, cardY, cardX + cardSize, cardY + 1, primary);
            context.fill(cardX, cardY + cardSize - 1, cardX + cardSize, cardY + cardSize, primary);
            context.fill(cardX, cardY, cardX + 1, cardY + cardSize, primary);
            context.fill(cardX + cardSize - 1, cardY, cardX + cardSize, cardY + cardSize, primary);
            // Glowing cyan badge in top-right
            context.fill(cardX + cardSize - 5, cardY + 2, cardX + cardSize - 2, cardY + 5, primary);
         } else if (isHover) {
            context.fill(cardX, cardY, cardX + cardSize, cardY + 1, 0x90FFFFFF);
            context.fill(cardX, cardY + cardSize - 1, cardX + cardSize, cardY + cardSize, 0x90FFFFFF);
            context.fill(cardX, cardY, cardX + 1, cardY + cardSize, 0x90FFFFFF);
            context.fill(cardX + cardSize - 1, cardY, cardX + cardSize, cardY + cardSize, 0x90FFFFFF);
         } else {
            // Subtle border
            context.fill(cardX, cardY, cardX + cardSize, cardY + 1, 0x18FFFFFF);
            context.fill(cardX, cardY + cardSize - 1, cardX + cardSize, cardY + cardSize, 0x18FFFFFF);
            context.fill(cardX, cardY, cardX + 1, cardY + cardSize, 0x18FFFFFF);
            context.fill(cardX + cardSize - 1, cardY, cardX + cardSize, cardY + cardSize, 0x18FFFFFF);
         }

         // Draw the ITEM icon always — even for 3D targets. The card must show the
         // inventory/hand item form; the 3D entity preview lives in the editor.
         // Full 3D entity preview ONLY on the hovered card (perf: rendering N live
         // 3D entities every frame was making the catalog crawl)
         LivingEntity preview = getPreviewEntity(target);
         if (preview != null && isHover && !isArmorTarget(target)) {
            float entHeight = preview.getHeight();
            float entWidth = preview.getWidth();
            float maxDim = Math.max(entHeight, entWidth);
            int baseSize = 13;
            int size = Math.max(5, Math.min(16, (int)(baseSize * (1.8f / Math.max(0.8f, maxDim)))));
            InventoryScreen.drawEntity(context, cardX + 2, cardY + 2, cardX + cardSize - 2, cardY + cardSize - 2, size, 0.0625F, mouseX, mouseY, preview);
         } else {
            context.drawItem(target.getIcon(), cardX + (cardSize - 16) / 2, cardY + (cardSize - 16) / 2);
         }

         if (isHover) {
            hoveredTarget = target;
            hoveredX = mouseX;
            hoveredY = mouseY;
         }
      }

      context.disableScissor();

      // Empty State
      if (totalItems == 0) {
         String noRes = "No textures found matching criteria.";
         int noResW = this.textRenderer.getWidth(noRes);
         context.drawTextWithShadow(this.textRenderer, noRes, (this.width - noResW) / 2, startY + gridH / 2 - 10, 0xFF888899);
      }

      // Scrollbar — positioned just before the PM panel
      if (this.maxCatalogScroll > 0) {
         int sbX = pmPanelX - 8;
         int sbW = 4;
         int sbH = gridH;
         context.fill(sbX, startY, sbX + sbW, startY + sbH, 0x20FFFFFF);
         float scrollPct = (float)this.catalogScroll / this.maxCatalogScroll;
         int thumbH = Math.max(16, sbH * gridH / (gridH + this.maxCatalogScroll));
         int thumbY = startY + (int)((sbH - thumbH) * scrollPct);
         context.fill(sbX, thumbY, sbX + sbW, thumbY + thumbH, primary);
      }

      // Rich Multi-line Tooltip
      if (hoveredTarget != null) {
         boolean hasCustom = TexturePackManager.hasCustomTexture(hoveredTarget);
         boolean is3d = getPreviewEntity(hoveredTarget) != null;
         List<Text> tooltip = List.of(
            Text.literal("§f§l" + hoveredTarget.getDisplayName() + (is3d ? " §b[3D Model]" : "")),
            Text.literal("§7Category: §b" + hoveredTarget.getCategory().getLabel() + (hoveredTarget.isMob() ? " (Mob)" : "")),
            Text.literal(hasCustom ? "§a● Custom Texture Active" : "§8○ Vanilla Default"),
            Text.literal("§8" + hoveredTarget.getId())
         );
         context.drawTooltip(this.textRenderer, tooltip, hoveredX, hoveredY);
      }

      // Player Model sidebar panel
      renderPlayerModelPanel(context, mouseX, mouseY, pmPanelX, 34);
   }

   // ================= EDITOR VIEW =================

   private void renderEditorView(DrawContext context, int mouseX, int mouseY) {
      int primary = VoidCyanClient.getPrimaryColor() | 0xFF000000;

      // Top bar
      boolean backHover = mouseX >= 10 && mouseX <= 85 && mouseY >= 10 && mouseY <= 28;
      context.fill(10, 10, 85, 28, backHover ? 0x60FFFFFF : 0x25FFFFFF);
      context.drawTextWithShadow(this.textRenderer, "← Catalog", 18, 15, 0xFFFFFFFF);

      // Selected target icon & info
      context.drawItem(this.selectedTarget.getIcon(), 96, 11);
      String name = this.selectedTarget.getDisplayName();
      context.drawTextWithShadow(this.textRenderer, name, 118, 15, primary);

      // Status Pill
      boolean hasCustom = TexturePackManager.hasCustomTexture(this.selectedTarget);
      int pillX = 124 + this.textRenderer.getWidth(name);
      String statusPill = hasCustom ? "CUSTOM ACTIVE" : "VANILLA";
      int pillW = this.textRenderer.getWidth(statusPill) + 10;
      context.fill(pillX, 12, pillX + pillW, 26, hasCustom ? 0x4000F5FF : 0x25FFFFFF);
      if (hasCustom) {
         context.fill(pillX, 12, pillX + pillW, 13, primary);
      }
      context.drawTextWithShadow(this.textRenderer, statusPill, pillX + 5, 15, hasCustom ? 0xFF00F5FF : 0xFFAAAAAA);

      // Action buttons: Save & Apply, Revert, Load Default, Clear, Undo, Redo
      int btnY = 10;
      int btnH = 18;

      // Save & Apply Button
      int saveW = 86;
      int saveX = this.width - saveW - 10;
      boolean saveHover = mouseX >= saveX && mouseX <= saveX + saveW && mouseY >= btnY && mouseY <= btnY + btnH;
      context.fill(saveX, btnY, saveX + saveW, btnY + btnH, saveHover ? primary : (primary & 0x00FFFFFF | 0xA0000000));
      context.fill(saveX, btnY, saveX + saveW, btnY + 1, 0xFFFFFFFF);
      context.drawTextWithShadow(this.textRenderer, "Save & Apply", saveX + 8, btnY + 5, 0xFFFFFFFF);

      // Revert Button
      int revW = 54;
      int revX = saveX - revW - 6;
      boolean revHover = mouseX >= revX && mouseX <= revX + revW && mouseY >= btnY && mouseY <= btnY + btnH;
      int revBg = hasCustom ? (revHover ? 0xE0D32F2F : 0x90D32F2F) : (revHover ? 0x50FF5555 : 0x25FF5555);
      context.fill(revX, btnY, revX + revW, btnY + btnH, revBg);
      context.drawTextWithShadow(this.textRenderer, "Revert", revX + 9, btnY + 5, 0xFFFFFFFF);

      // Load Default Button
      int vanW = 76;
      int vanX = revX - vanW - 6;
      boolean vanHover = mouseX >= vanX && mouseX <= vanX + vanW && mouseY >= btnY && mouseY <= btnY + btnH;
      context.fill(vanX, btnY, vanX + vanW, btnY + btnH, vanHover ? 0x60FFFFFF : 0x25FFFFFF);
      context.drawTextWithShadow(this.textRenderer, "Load Default", vanX + 6, btnY + 5, 0xFFFFFFFF);

      // Clear Button
      int clrW = 44;
      int clrX = vanX - clrW - 6;
      boolean clrHover = mouseX >= clrX && mouseX <= clrX + clrW && mouseY >= btnY && mouseY <= btnY + btnH;
      context.fill(clrX, btnY, clrX + clrW, btnY + btnH, clrHover ? 0x60FF5555 : 0x25FF5555);
      context.drawTextWithShadow(this.textRenderer, "Clear", clrX + 8, btnY + 5, 0xFFFF7777);

      // Undo & Redo Buttons
      int redoW = 42;
      int redoX = clrX - redoW - 6;
      boolean redoHov = mouseX >= redoX && mouseX <= redoX + redoW && mouseY >= btnY && mouseY <= btnY + btnH;
      boolean canRedo = !this.redoStack.isEmpty();
      context.fill(redoX, btnY, redoX + redoW, btnY + btnH, canRedo ? (redoHov ? 0x50FFFFFF : 0x25FFFFFF) : 0x15FFFFFF);
      context.drawTextWithShadow(this.textRenderer, "Redo", redoX + 7, btnY + 5, canRedo ? 0xFFFFFFFF : 0xFF666666);

      int undoW = 42;
      int undoX = redoX - undoW - 4;
      boolean undoHov = mouseX >= undoX && mouseX <= undoX + undoW && mouseY >= btnY && mouseY <= btnY + btnH;
      boolean canUndo = !this.undoStack.isEmpty();
      context.fill(undoX, btnY, undoX + undoW, btnY + btnH, canUndo ? (undoHov ? 0x50FFFFFF : 0x25FFFFFF) : 0x15FFFFFF);
      context.drawTextWithShadow(this.textRenderer, "Undo", undoX + 7, btnY + 5, canUndo ? 0xFFFFFFFF : 0xFF666666);

      // Studio Toolbar - two left-aligned rows so buttons never overlap:
      // Row 1 (y=34): size label + resolution presets. Row 2 (y=52): mode + tools + grid/reset.
      int rightPanelW = 190;
      int toolBarY = 34;
      int toolRow2Y = TOOL_ROW2_Y;

      // Resolution Selectors
      context.drawTextWithShadow(this.textRenderer, "Size: " + this.canvasWidth + "x" + this.canvasHeight, 12, toolBarY + 4, 0xFFAAAAAA);
      int resBtnX = 90;
      int[][] presets;
      if (this.canvasWidth == this.canvasHeight) {
         presets = new int[][]{{1, 1}, {2, 2}, {4, 4}, {8, 8}, {16, 16}, {32, 32}, {64, 64}};
      } else {
         presets = new int[][]{{32, 16}, {64, 32}, {128, 64}};
      }
      for (int[] res : presets) {
         String label = res[0] + "x" + res[1];
         int rw = this.textRenderer.getWidth(label) + 8;
         boolean isCur = this.canvasWidth == res[0] && this.canvasHeight == res[1];
         boolean rHover = mouseX >= resBtnX && mouseX <= resBtnX + rw && mouseY >= toolBarY && mouseY <= toolBarY + 16;
         context.fill(resBtnX, toolBarY, resBtnX + rw, toolBarY + 16, isCur ? primary : (rHover ? 0x50FFFFFF : 0x20FFFFFF));
         context.drawTextWithShadow(this.textRenderer, label, resBtnX + 4, toolBarY + 4, isCur ? 0xFF2E2A38 : 0xFFFFFFFF);
         resBtnX += rw + 4;
      }

      // Tools Row: [3D Model / 2D Grid], Pencil (B), Eraser (E), Bucket (G), Pick (I), Orbit (R), Grid/Reset
      boolean targetIs3d = is3dTarget(this.selectedTarget);

      // Render the mode toggle first (its width feeds the layout), then draw every
      // button from the shared geometry helper so hit regions always match pixels.
      String viewModeLbl = this.viewMode3D ? "Mode: 3D Model" : "Mode: 2D Grid";
      int vmw = this.textRenderer.getWidth(viewModeLbl) + 10;
      int vmHoverIdx = getToolRowHitIndex(mouseX, mouseY, vmw, targetIs3d);
      if (targetIs3d) {
         boolean vmHover = vmHoverIdx == 0;
         context.fill(12, toolRow2Y, 12 + vmw, toolRow2Y + 16, this.viewMode3D ? 0xE000B4D8 : (vmHover ? 0x50FFFFFF : 0x25FFFFFF));
         context.drawTextWithShadow(this.textRenderer, viewModeLbl, 12 + 5, toolRow2Y + 4, this.viewMode3D ? 0xFF0E2833 : 0xFFFFFFFF);
      }

      String[] toolNames = targetIs3d 
         ? new String[]{"Pencil (B)", "Eraser (E)", "Bucket (G)", "Pick (I)", "Orbit (R)"}
         : new String[]{"Pencil (B)", "Eraser (E)", "Bucket (G)", "Pick (I)"};

      for (int t = 0; t < toolNames.length; t++) {
         int tw = this.textRenderer.getWidth(toolNames[t]) + 8;
         boolean isCur = this.activeTool == t;
         boolean tHover = vmHoverIdx == t + 1;
         context.fill(getToolX(t + 1, vmw, targetIs3d), toolRow2Y, getToolX(t + 1, vmw, targetIs3d) + tw, toolRow2Y + 16, isCur ? primary : (tHover ? 0x50FFFFFF : 0x20FFFFFF));
         context.drawTextWithShadow(this.textRenderer, toolNames[t], getToolX(t + 1, vmw, targetIs3d) + 4, toolRow2Y + 4, isCur ? 0xFF2E2A38 : 0xFFFFFFFF);
      }

      if (!this.viewMode3D || !targetIs3d) {
         // Grid toggle button (only in 2D mode)
         String gridLbl = this.showGrid ? "Grid: ON" : "Grid: OFF";
         int gw = this.textRenderer.getWidth(gridLbl) + 8;
         boolean gHov = vmHoverIdx == 5;
         context.fill(getToolX(5, vmw, targetIs3d), toolRow2Y, getToolX(5, vmw, targetIs3d) + gw, toolRow2Y + 16, this.showGrid ? 0x4000F5FF : (gHov ? 0x50FFFFFF : 0x20FFFFFF));
         context.drawTextWithShadow(this.textRenderer, gridLbl, getToolX(5, vmw, targetIs3d) + 4, toolRow2Y + 4, this.showGrid ? 0xFF00F5FF : 0xFFAAAAAA);
      } else {
         // Reset View button (in 3D mode)
         String rstLbl = "↺ Reset View";
         int rw = this.textRenderer.getWidth(rstLbl) + 8;
         boolean rHov = vmHoverIdx == 5;
         context.fill(getToolX(5, vmw, targetIs3d), toolRow2Y, getToolX(5, vmw, targetIs3d) + rw, toolRow2Y + 16, rHov ? 0x50FFFFFF : 0x20FFFFFF);
         context.drawTextWithShadow(this.textRenderer, rstLbl, getToolX(5, vmw, targetIs3d) + 4, toolRow2Y + 4, 0xFFCCCCCC);
      }

      // ================= MAIN CANVAS / 3D VIEWPORT AREA =================
      int canvasMarginTop = CANVAS_TOP;
      int canvasMarginBottom = 16;
      int maxCanvasH = this.height - canvasMarginTop - canvasMarginBottom;
      int maxCanvasW = this.width - rightPanelW - 30;
      int vpX = 14;
      int vpY = canvasMarginTop;

      if (this.viewMode3D && targetIs3d) {
         // --- 3D INTERACTIVE VIEWPORT ---
         context.fill(vpX, vpY, vpX + maxCanvasW, vpY + maxCanvasH, 0xEE12121A);
         drawModernRoundedRect(context, vpX - 1, vpY - 1, maxCanvasW + 2, maxCanvasH + 2, 6, 0x35FFFFFF);

         float centerX = vpX + maxCanvasW / 2.0F + this.panOffsetX;
         float centerY = vpY + maxCanvasH / 2.0F + this.panOffsetY;

         // Pedestal shadow
         int pedW = (int)(80 * this.zoomScale);
         int pedH = (int)(22 * this.zoomScale);
         int pedY = (int)(centerY + 55 * this.zoomScale);
         for (int r = pedH; r > 0; r -= 2) {
            int w = pedW * r / pedH;
            context.fill((int)centerX - w / 2, pedY - r / 2, (int)centerX + w / 2, pedY + r / 2, 0x15000000);
         }

         LivingEntity entity = getPreviewEntity(this.selectedTarget);
         if (entity == null) {
            // Entity build failed (e.g. EntityType.create threw) — explain instead of
            // showing a silent black viewport that makes 3D mode look broken.
            String failMsg = "Could not build 3D preview — using 2D grid";
            context.drawTextWithShadow(
               this.textRenderer, failMsg,
               vpX + (maxCanvasW - this.textRenderer.getWidth(failMsg)) / 2,
               vpY + maxCanvasH / 2, 0xFFFF8888
            );
            this.viewMode3D = false;
         }

         if (entity != null) {
            float entHeight = entity.getHeight();
            float entWidth = entity.getWidth();
            float maxDim = Math.max(entHeight, entWidth);
            float baseScale = Math.min(maxCanvasW, maxCanvasH) * 0.40F;
            float scale = baseScale * (1.8F / Math.max(0.8F, maxDim)) * this.zoomScale;

            context.enableScissor(vpX, vpY, vpX + maxCanvasW, vpY + maxCanvasH);

            if (this.dynamicPreviewTextureId != null) {
               VoidCyanClient.customPreviewTexture = this.dynamicPreviewTextureId;
            }

            Quaternionf rot = new Quaternionf().rotateZ((float) Math.PI);
            rot.rotateY((float) Math.toRadians(this.cameraYaw));
            rot.rotateX((float) Math.toRadians(this.cameraPitch));

            Vector3f pos = new Vector3f(
               this.panOffsetX / scale,
               (entity.getHeight() / 2.0F) + (this.panOffsetY / scale),
               0.0F
            );

            context.addEntity(
               getOrCreateEntityRenderState(entity),
               scale,
               pos,
               rot,
               new Quaternionf(),
               vpX, vpY, vpX + maxCanvasW, vpY + maxCanvasH
            );

            VoidCyanClient.customPreviewTexture = null;
            context.disableScissor();

            // Single raycast per frame, shared by drag-painting and hover display (perf:
            // raycasting the full model twice per frame burned CPU every frame)
            ModelRaycaster.RayHit frameHit = (this.isDraggingCanvas || mouseX >= vpX && mouseX <= vpX + maxCanvasW && mouseY >= vpY && mouseY <= vpY + maxCanvasH)
               ? raycastEntity(
                  entity, (float) mouseX, (float) mouseY, centerX, centerY, scale,
                  this.canvasWidth, this.canvasHeight
               )
               : null;

            // Continuous dragging painting in 3D
            if (this.isDraggingCanvas) {
               if (frameHit != null) {
                  applyToolAtPixel(frameHit.pixelX, frameHit.pixelY, this.lastDragBtn);
               }
            }

            // Hover indicator & 3D paint cursor
            if (frameHit != null) {
               int pCol = this.canvasPixels[frameHit.pixelX][frameHit.pixelY];
               String hoverText = String.format("3D Part: %s | UV: (%d, %d) | #%08X", frameHit.partName, frameHit.pixelX, frameHit.pixelY, pCol);
               context.drawTextWithShadow(this.textRenderer, hoverText, vpX + 8, vpY + maxCanvasH - 16, 0xFF00F5FF);

               // Reticle on hover pixel
               context.fill(mouseX - 3, mouseY, mouseX + 4, mouseY + 1, 0xFF00F5FF);
               context.fill(mouseX, mouseY - 3, mouseX + 1, mouseY + 4, 0xFF00F5FF);
            }
         }

         // Viewport overlay controls helper text
         String controlsHint = String.format("Right-Drag: Orbit 360° | Wheel: Zoom (%.0f%%) | Left-Click: %s",
            this.zoomScale * 100.0F,
            this.activeTool == 4 ? "Orbit" : "Paint"
         );
         context.drawTextWithShadow(this.textRenderer, controlsHint, vpX + 8, vpY + 8, 0xFF888899);

         // Mini 2D texture picture-in-picture in bottom-right corner of viewport
         int miniSize = 58;
         int miniX = vpX + maxCanvasW - miniSize - 8;
         int miniY = vpY + maxCanvasH - miniSize - 8;
         context.fill(miniX - 2, miniY - 2, miniX + miniSize + 2, miniY + miniSize + 2, 0x90000000);
         drawModernRoundedRect(context, miniX - 2, miniY - 2, miniSize + 4, miniSize + 4, 4, 0x40FFFFFF);
         this.updateDynamicPreviewTexture();
         if (this.dynamicPreviewTextureId != null) {
            context.drawTexturedQuad(this.dynamicPreviewTextureId, miniX, miniY, miniX + miniSize, miniY + miniSize, 0.0F, 1.0F, 0.0F, 1.0F);
         }
         context.drawTextWithShadow(this.textRenderer, "2D Unwrap", miniX, miniY - 10, 0xFF777788);

      } else {
         // --- 2D CANVAS RENDERING (Classic Pixel Grid) ---
         int pixelSize = Math.max(1, Math.min(maxCanvasW / this.canvasWidth, maxCanvasH / this.canvasHeight));
         int canvasW = pixelSize * this.canvasWidth;
         int canvasH = pixelSize * this.canvasHeight;
         int canvasX = 14 + (maxCanvasW - canvasW) / 2;
         int canvasY = canvasMarginTop + (maxCanvasH - canvasH) / 2;

         // Drop shadow around canvas
         context.fill(canvasX - 3, canvasY - 3, canvasX + canvasW + 3, canvasY + canvasH + 3, 0x30000000);

         // Checkerboard Background for transparency
         int checkSize = Math.max(4, pixelSize);
         for (int cx = 0; cx < canvasW; cx += checkSize) {
            for (int cy = 0; cy < canvasH; cy += checkSize) {
               boolean alt = ((cx / checkSize) + (cy / checkSize)) % 2 == 0;
               context.fill(canvasX + cx, canvasY + cy, canvasX + Math.min(canvasW, cx + checkSize), canvasY + Math.min(canvasH, cy + checkSize), alt ? 0xFF2A2A2A : 0xFF1C1C1C);
            }
         }

         // Draw Pixels — single textured quad via the dynamic preview texture (perf:
         // this loop previously issued thousands of fill() calls per frame)
         this.updateDynamicPreviewTexture();
         if (this.dynamicPreviewTextureId != null) {
            context.drawTexturedQuad(this.dynamicPreviewTextureId, canvasX, canvasY, canvasX + canvasW, canvasY + canvasH, 0.0F, 1.0F, 0.0F, 1.0F);
         }

         // Grid lines
         if (this.showGrid && pixelSize >= 4) {
            int gridCol = 0x22FFFFFF;
            for (int i = 0; i <= this.canvasWidth; i++) {
               context.fill(canvasX + i * pixelSize, canvasY, canvasX + i * pixelSize + 1, canvasY + canvasH, gridCol);
            }
            for (int j = 0; j <= this.canvasHeight; j++) {
               context.fill(canvasX, canvasY + j * pixelSize, canvasX + canvasW, canvasY + j * pixelSize + 1, gridCol);
            }
         }

         // Canvas Border (glowing obsidian & primary)
         context.fill(canvasX - 1, canvasY - 1, canvasX + canvasW + 1, canvasY, primary);
         context.fill(canvasX - 1, canvasY + canvasH, canvasX + canvasW + 1, canvasY + canvasH + 1, primary);
         context.fill(canvasX - 1, canvasY, canvasX, canvasY + canvasH, primary);
         context.fill(canvasX + canvasW, canvasY, canvasX + canvasW + 1, canvasY + canvasH, primary);

         // Handle continuous drag painting on canvas
         if (this.isDraggingCanvas) {
            this.paintPixelAt(mouseX, mouseY, canvasX, canvasY, pixelSize, this.lastDragBtn);
         }

         // Pixel Inspector Info (Cursor coordinates & color)
         int hoverPx = (int)((mouseX - canvasX) / pixelSize);
         int hoverPy = (int)((mouseY - canvasY) / pixelSize);
         if (hoverPx >= 0 && hoverPx < this.canvasWidth && hoverPy >= 0 && hoverPy < this.canvasHeight) {
            int pCol = this.canvasPixels[hoverPx][hoverPy];
            String coordInfo = String.format("Pixel: (%d, %d) | #%08X", hoverPx, hoverPy, pCol);
            context.drawTextWithShadow(this.textRenderer, coordInfo, canvasX, canvasY + canvasH + 4, 0xFFAAAAAA);
         }
      }

      // ================= STUDIO SIDEBAR =================
      int rightPanelX = this.width - rightPanelW - 10;

      // 1. LIVE PREVIEW CARD
      LivingEntity previewEnt = getPreviewEntity(this.selectedTarget);
      boolean is3d = previewEnt != null;
      int prevCardY = 34;
      int prevCardH = getPrevCardHeight();
      context.fill(rightPanelX, prevCardY, rightPanelX + rightPanelW, prevCardY + prevCardH, 0x2514141E);
      context.fill(rightPanelX, prevCardY, rightPanelX + rightPanelW, prevCardY + 1, 0x30FFFFFF);
      context.drawTextWithShadow(this.textRenderer, is3d ? "3D LIVE PREVIEW" : "LIVE PREVIEW", rightPanelX + 6, prevCardY + 4, 0xFF888899);

      // When painting a worn-armor texture, the live preview must reflect the canvas:
      // redirect the armor layer model onto the live canvas texture while the entity draws.
      if (is3d && this.armorLayerModel != null) {
         VoidCyanClient.customPreviewTexture = this.dynamicPreviewTextureId;
      }

      if (is3d) {
         // Render 3D entity interactive preview
         int entBoxW = 76;
         int entX1 = rightPanelX + 6;
         int entY1 = prevCardY + 14;
         int entX2 = entX1 + entBoxW;
         int entY2 = prevCardY + prevCardH - 4;

         context.fill(entX1, entY1, entX2, entY2, 0x30000000);
         drawModernRoundedRect(context, entX1 - 1, entY1 - 1, entBoxW + 2, (entY2 - entY1) + 2, 4, 0x25FFFFFF);

         float entHeight = previewEnt.getHeight();
         float entWidth = previewEnt.getWidth();
         float maxDim = Math.max(entHeight, entWidth);
         int baseSize = 25;
         int size = Math.max(8, Math.min(32, (int)(baseSize * (1.8f / Math.max(0.8f, maxDim)))));

         InventoryScreen.drawEntity(context, entX1, entY1, entX2, entY2, size, 0.0625F, -1, -1, previewEnt);

         // Alongside 3D preview, also show the 1x texture unwrap
         int p1xX = entX2 + 10;
         int p1xY = prevCardY + 18;
         int p1xScale = Math.max(1, 36 / Math.max(this.canvasWidth, this.canvasHeight));
         for (int px = 0; px < this.canvasWidth; px++) {
            for (int py = 0; py < this.canvasHeight; py++) {
               int col = this.canvasPixels[px][py];
               if ((col >> 24 & 0xFF) > 0) {
                  context.fill(p1xX + px * p1xScale, p1xY + py * p1xScale, p1xX + (px + 1) * p1xScale, p1xY + (py + 1) * p1xScale, col);
               }
            }
         }
         context.drawTextWithShadow(this.textRenderer, "Texture", p1xX, p1xY + this.canvasHeight * p1xScale + 3, 0xFF777788);
      }

      if (is3d && this.armorLayerModel != null) {
         VoidCyanClient.customPreviewTexture = null;
      } else {
         // Render 1x Preview
         int p1xX = rightPanelX + 6;
         int p1xY = prevCardY + 16;
         int p1xScale = Math.max(1, 24 / Math.max(this.canvasWidth, this.canvasHeight));
         for (int px = 0; px < this.canvasWidth; px++) {
            for (int py = 0; py < this.canvasHeight; py++) {
               int col = this.canvasPixels[px][py];
               if ((col >> 24 & 0xFF) > 0) {
                  context.fill(p1xX + px * p1xScale, p1xY + py * p1xScale, p1xX + (px + 1) * p1xScale, p1xY + (py + 1) * p1xScale, col);
               }
            }
         }
         context.drawTextWithShadow(this.textRenderer, "1x", p1xX + this.canvasWidth * p1xScale + 4, p1xY + 6, 0xFF777788);

         // Render 2x Preview
         int p2xX = rightPanelX + 70;
         int p2xScale = Math.max(1, p1xScale * 2);
         for (int px = 0; px < this.canvasWidth; px++) {
            for (int py = 0; py < this.canvasHeight; py++) {
               int col = this.canvasPixels[px][py];
               if ((col >> 24 & 0xFF) > 0) {
                  context.fill(p2xX + px * p2xScale, p1xY + py * p2xScale, p2xX + (px + 1) * p2xScale, p1xY + (py + 1) * p2xScale, col);
               }
            }
         }
         context.drawTextWithShadow(this.textRenderer, "2x", p2xX + this.canvasWidth * p2xScale + 4, p1xY + 6, 0xFF777788);
      }

      // 1b. DYNAMIC ITEM PREVIEWER — live player-model preview with toggleable
      // animation states (stand / run / swim / fly) + live atlas item override.
      LivingEntity holderEnt = MinecraftClient.getInstance().player;
      boolean itemPreviewActive = holderEnt != null && (this.selectedTarget == null || !is3dTarget(this.selectedTarget));
      if (itemPreviewActive) {
         // Compact panel: title, 4 small animation buttons, model box, status line.
         int dpH = 96;
         int dpY = prevCardY + prevCardH + 8;
         context.fill(rightPanelX, dpY, rightPanelX + rightPanelW, dpY + dpH, 0x2514141E);
         context.fill(rightPanelX, dpY, rightPanelX + rightPanelW, dpY + 1, 0x30FFFFFF);
         context.drawTextWithShadow(this.textRenderer, "DYNAMIC ITEM PREVIEW", rightPanelX + 6, dpY + 3, 0xFF00B4D8);

         // Publish the freshest canvas into the atlas before creating the state so
         // the state's baked item quads sample the newest pixels.
         DynamicItemPreviewer.publishFrame(this.canvasPixels);
         EntityRenderState holderState = getOrCreateEntityRenderState(holderEnt);
         this.applyPreviewAnimationState(holderState);

         int boxX1 = rightPanelX + 8;
         int boxY1 = dpY + 22;
         int boxX2 = rightPanelX + rightPanelW - 8;
         int boxY2 = dpY + dpH - 12;
         context.fill(boxX1, boxY1, boxX2, boxY2, 0x30000000);

         // Compact animation-state buttons on the title row (right-aligned).
         int stateBtnY = dpY + 2;
         int stateBtnX = rightPanelX + rightPanelW - 4;
         for (int m = PREVIEW_MODE_LABELS.length - 1; m >= 0; m--) {
            String lbl = PREVIEW_MODE_LABELS[m];
            int lw = this.textRenderer.getWidth(lbl) + 6;
            stateBtnX -= lw;
            boolean sel = this.previewAnimationMode == m;
            boolean hov = mouseX >= stateBtnX && mouseX <= stateBtnX + lw && mouseY >= stateBtnY && mouseY <= stateBtnY + 11;
            context.fill(stateBtnX, stateBtnY, stateBtnX + lw, stateBtnY + 11, sel ? 0xE000B4D8 : (hov ? 0x50FFFFFF : 0x25FFFFFF));
            context.drawTextWithShadow(this.textRenderer, lbl, stateBtnX + 3, stateBtnY + 2, sel ? 0xFF0E2833 : 0xFFFFFFFF);
            stateBtnX -= 2;
         }

         float hHeight = holderEnt.getHeight();
         float hWidth = holderEnt.getWidth();
         float hMaxDim = Math.max(hHeight, hWidth);
         // Fit the holder model into ~75% of the preview box height.
         float hScale = ((boxY2 - boxY1) * 0.75F) / hMaxDim;

         Quaternionf hRot = new Quaternionf().rotateZ((float) Math.PI);
         Vector3f hPos = new Vector3f(0.0F, hHeight / 2.0F, 0.0F);
         context.addEntity(
            holderState,
            hScale,
            hPos,
            hRot,
            new Quaternionf(),
            boxX1, boxY1, boxX2, boxY2
         );

         context.drawTextWithShadow(
            this.textRenderer,
            DynamicItemPreviewer.isActive() ? "Live in-hand preview" : "Live atlas preview unavailable",
            rightPanelX + 6, dpY + dpH - 10,
            DynamicItemPreviewer.isActive() ? 0xFF55FF99 : 0xFFFF8888
         );
      }

      // 2. COLOR STUDIO — compact swatch that opens the shared picker modal
      // (112 = 8px gap + 96px preview panel + 8px gap; must match mouseClicked)
      int studioY = prevCardY + prevCardH + (itemPreviewActive ? 112 : 14);
      boolean studioHov = mouseX >= rightPanelX && mouseX <= rightPanelX + rightPanelW && mouseY >= studioY && mouseY <= studioY + 26;
      GuiStyle.roundedRect(context, rightPanelX, studioY, rightPanelW, 26, 4, (studioHov ? 210 : 170) << 24 | 0x1A1622);
      GuiStyle.roundedOutline(context, rightPanelX, studioY, rightPanelW, 26, 4, (studioHov ? 160 : 70) << 24 | 0xFFFFFF);
      context.fill(rightPanelX + 4, studioY + 4, rightPanelX + 34, studioY + 22, this.activeColor);
      GuiStyle.roundedOutline(context, rightPanelX + 4, studioY + 4, 30, 18, 2, 0x60FFFFFF);
      String hexStr = String.format("#%08X", this.activeColor);
      GuiStyle.text(context, this.textRenderer, hexStr, rightPanelX + 42, studioY + 9, 0xFFE6E6F0);
      GuiStyle.text(context, this.textRenderer, "Edit", rightPanelX + rightPanelW - 28, studioY + 9, 0xFF9A93AC);

      // 3. EXPANDED PRESET PALETTES (24 colors in 3 rows)
      int palY = studioY + 38;
      int palCols = 8;
      int palSize = 18;
      int palGap = 3;
      for (int p = 0; p < PRESET_PALETTE.length; p++) {
         int pr = p / palCols;
         int pc = p % palCols;
         int px = rightPanelX + pc * (palSize + palGap);
         int py = palY + pr * (palSize + palGap);
         int pCol = PRESET_PALETTE[p];
         boolean pHover = mouseX >= px && mouseX <= px + palSize && mouseY >= py && mouseY <= py + palSize;
         context.fill(px, py, px + palSize, py + palSize, pCol);
         if (pHover || this.activeColor == pCol) {
            context.fill(px - 1, py - 1, px + palSize + 1, py, 0xFFFFFFFF);
            context.fill(px - 1, py + palSize, px + palSize + 1, py + palSize + 1, 0xFFFFFFFF);
            context.fill(px - 1, py, px, py + palSize, 0xFFFFFFFF);
            context.fill(px + palSize, py, px + palSize + 1, py + palSize, 0xFFFFFFFF);
         }
      }
   }


   // ================= MOUSE & INPUT HANDLING =================

   @Override
   public boolean mouseClicked(Click click, boolean doubled) {
      double mx = click.x();
      double my = click.y();
      int btn = click.button();

      if (this.selectedTarget == null) {
         // Back / Close button
         int backBtnW = 46;
         int backBtnX = this.width - backBtnW - 14;
         if (mx >= backBtnX && mx <= backBtnX + backBtnW && my >= 10 && my <= 26) {
            this.client.setScreen(this.parent);
            return true;
         }

         // Revert All button
         int revAllW = 72;
         int revAllX = backBtnX - revAllW - 6;
         if (mx >= revAllX && mx <= revAllX + revAllW && my >= 10 && my <= 26) {
            TexturePackManager.deleteAllCustomTextures();
            TexturePackManager.applyAndReload();
            this.statusMessage = "Reverted all custom textures to vanilla!";
            this.statusMessageTime = System.currentTimeMillis();
            return true;
         }

         // Modified Only Filter Button
         int modW = 86;
         int modX = revAllX - modW - 6;
         if (mx >= modX && mx <= modX + modW && my >= 10 && my <= 26) {
            this.filterModifiedOnly = !this.filterModifiedOnly;
            this.updateFilter(this.lastSearchQuery);
            return true;
         }

         // Custom Search Bar Click
         int searchBarX = 14;
         int searchBarY = 35;
         int searchBarW = Math.min(170, Math.max(120, this.width - 420));
         int searchBarH = 22;

         if (mx >= searchBarX && mx <= searchBarX + searchBarW && my >= searchBarY && my <= searchBarY + searchBarH) {
            // Check clear button (✕)
            if (!this.searchField.getText().isEmpty()) {
               int clearX = searchBarX + searchBarW - 14;
               int clearY = searchBarY + 6;
               if (mx >= clearX && mx <= clearX + 10 && my >= clearY && my <= clearY + 10) {
                  this.searchField.setText("");
                  this.updateFilter("");
                  return true;
               }
            }
            this.setFocused(this.searchField);
            this.searchField.setFocused(true);
            return this.searchField.mouseClicked(click, doubled);
         } else {
            this.setFocused(null);
            this.searchField.setFocused(false);
         }

         // Category Chips Bar
         int totalFiltered = this.filteredTargets.size();
         String countStr = totalFiltered + " targets";
         int countW = this.textRenderer.getWidth(countStr);
         int countRightMargin = countW + 20;

         int catX = searchBarX + searchBarW + 10;
         int catY = 37;
         for (TextureTarget.Category cat : TextureTarget.Category.values()) {
            String label = cat.getLabel();
            int cw = this.textRenderer.getWidth(label) + 14;
            if (catX + cw > this.width - countRightMargin) break;
            if (mx >= catX && mx <= catX + cw && my >= catY && my <= catY + 18) {
               this.activeCategory = cat;
               this.updateFilter(this.lastSearchQuery);
               return true;
            }
            catX += cw + 4;
         }

         // Player Model sidebar panel clicks
         int pmPanelX = this.width - PM_PANEL_W - 6;
         int pmHit = getPlayerModelPanelHit(mx, my, pmPanelX, 34);
         if (pmHit == -100) {
            // Toggle enable/disable
            VoidCyanClient.isPlayerModelEnabled = !VoidCyanClient.isPlayerModelEnabled;
            VoidCyanClient.saveConfig();
            return true;
         } else if (pmHit == 0) {
            openObjPicker();
            return true;
         } else if (pmHit == 1) {
            openTexturePicker();
            return true;
         } else if (pmHit == 2) {
            PlayerModelManager.clearModel();
            pmStatus = "Model removed";
            return true;
         }

         // Click target card
         int startY = 64;
         int bottomMargin = 16;
         int gridH = this.height - startY - bottomMargin;
         int cardSize = 38;
         int gap = 5;
         int availW = pmPanelX - 30;
         int cols = Math.max(1, availW / (cardSize + gap));
         int startX = 14;

         if (my >= startY && my <= startY + gridH) {
            int totalItems = this.filteredTargets.size();
            for (int i = 0; i < totalItems; i++) {
               int row = i / cols;
               int col = i % cols;
               int cardX = startX + col * (cardSize + gap);
               int cardY = startY + row * (cardSize + gap) - this.catalogScroll;

               if (mx >= cardX && mx <= cardX + cardSize && my >= cardY && my <= cardY + cardSize) {
                  this.selectTarget(this.filteredTargets.get(i));
                  return true;
               }
            }
         }

      } else {
         // Editor View clicks
         // Back to catalog
         if (mx >= 10 && mx <= 85 && my >= 10 && my <= 28) {
            this.selectedTarget = null;
            this.searchField.setVisible(true);
            // Stop publishing into the atlas — the edit session ended.
            DynamicItemPreviewer.endOverride();
            return true;
         }

         // Action buttons: Save & Apply, Revert, Load Default, Clear, Undo, Redo
         int btnY = 10;
         int btnH = 18;
         int saveW = 86;
         int saveX = this.width - saveW - 10;
         int revW = 54;
         int revX = saveX - revW - 6;
         int vanW = 76;
         int vanX = revX - vanW - 6;
         int clrW = 44;
         int clrX = vanX - clrW - 6;
         int redoW = 42;
         int redoX = clrX - redoW - 6;
         int undoW = 42;
         int undoX = redoX - undoW - 4;

         if (my >= btnY && my <= btnY + btnH) {
            if (mx >= saveX && mx <= saveX + saveW) {
               this.saveAndApply();
               return true;
            } else if (mx >= revX && mx <= revX + revW) {
               this.revertSelected();
               return true;
            } else if (mx >= vanX && mx <= vanX + vanW) {
               pushUndo();
               BufferedImage van = TexturePackManager.loadVanillaTexture(this.selectedTarget);
               if (van != null) {
                  this.canvasWidth = van.getWidth();
                  this.canvasHeight = van.getHeight();
                  this.canvasPixels = new int[this.canvasWidth][this.canvasHeight];
                  for (int x = 0; x < this.canvasWidth; x++) {
                     for (int y = 0; y < this.canvasHeight; y++) {
                        this.canvasPixels[x][y] = van.getRGB(x, y);
                     }
                  }
               }
               this.statusMessage = "Loaded vanilla default";
               this.statusMessageTime = System.currentTimeMillis();
               return true;
            } else if (mx >= clrX && mx <= clrX + clrW) {
               pushUndo();
               this.canvasPixels = new int[this.canvasWidth][this.canvasHeight];
               this.statusMessage = "Cleared canvas";
               this.statusMessageTime = System.currentTimeMillis();
               return true;
            } else if (mx >= redoX && mx <= redoX + redoW) {
               this.redo();
               return true;
            } else if (mx >= undoX && mx <= undoX + undoW) {
               this.undo();
               return true;
            }
         }

         // Resolution Selector
         int toolBarY = 34;
         int toolRow2Y = TOOL_ROW2_Y;
         int resBtnX = 90;
         int[][] presets;
         if (this.canvasWidth == this.canvasHeight) {
            presets = new int[][]{{1, 1}, {2, 2}, {4, 4}, {8, 8}, {16, 16}, {32, 32}, {64, 64}};
         } else {
            presets = new int[][]{{32, 16}, {64, 32}, {128, 64}};
         }
         for (int[] res : presets) {
            String label = res[0] + "x" + res[1];
            int rw = this.textRenderer.getWidth(label) + 8;
            if (mx >= resBtnX && mx <= resBtnX + rw && my >= toolBarY && my <= toolBarY + 16) {
               this.setResolution(res[0], res[1]);
               return true;
            }
            resBtnX += rw + 4;
         }

         // Tool Selector (Pencil, Eraser, Bucket, Pick, Orbit) & Mode / Grid / Reset - row 2
         boolean targetIs3d = is3dTarget(this.selectedTarget);
         String viewModeLbl = this.viewMode3D ? "Mode: 3D Model" : "Mode: 2D Grid";
         int vmw = this.textRenderer.getWidth(viewModeLbl) + 10;
         int hitIdx = getToolRowHitIndex(mx, my, vmw, targetIs3d);

         if (hitIdx == HIT_MODE && targetIs3d) {
            // 3D Model <-> 2D Grid toggle
            this.viewMode3D = !this.viewMode3D;
            // Any in-progress canvas gesture belongs to the old view — drop it.
            this.isDraggingCanvas = false;
            this.isOrbiting = false;
            this.isPanning = false;
            return true;
         }

         String[] toolNames = targetIs3d 
            ? new String[]{"Pencil (B)", "Eraser (E)", "Bucket (G)", "Pick (I)", "Orbit (R)"}
            : new String[]{"Pencil (B)", "Eraser (E)", "Bucket (G)", "Pick (I)"};

         if (hitIdx >= 1 && hitIdx <= toolNames.length) {
            this.activeTool = hitIdx - 1;
            return true;
         }

         if (hitIdx == HIT_GRID_RESET) {
            if (!this.viewMode3D || !targetIs3d) {
               // Grid Toggle
               this.showGrid = !this.showGrid;
            } else {
               // Reset View
               this.cameraYaw = 0;
               this.cameraPitch = 0;
               this.zoomScale = 1.0F;
               this.panOffsetX = 0;
               this.panOffsetY = 0;
            }

            return true;
         }

         // Viewport / Canvas bounds (rightPanelW=190 matches renderEditorView; marginTop=74 for two toolbar rows)
         int canvasMarginTop = CANVAS_TOP;
         int canvasMarginBottom = 16;
         int maxCanvasH = this.height - canvasMarginTop - canvasMarginBottom;
         int maxCanvasW = this.width - 190 - 30;

         if (this.viewMode3D && targetIs3d) {
            // 3D Viewport click
            int vpX = 14;
            int vpY = canvasMarginTop;
            if (mx >= vpX && mx <= vpX + maxCanvasW && my >= vpY && my <= vpY + maxCanvasH) {
               if (btn == 1) {
                  // Right click = orbit
                  this.isOrbiting = true;
                  this.lastDragBtn = btn;
                  return true;
               } else if (btn == 2) {
                  // Middle click = pan
                  this.isPanning = true;
                  this.lastDragBtn = btn;
                  return true;
               } else if (btn == 0) {
                  // Left click
                  if (this.activeTool == 4) {
                     this.isOrbiting = true;
                     this.lastDragBtn = btn;
                     return true;
                  }

                  // 3D Model Raycast Paint
                  LivingEntity entity = getPreviewEntity(this.selectedTarget);
                  if (entity != null) {
                     float centerX = vpX + maxCanvasW / 2.0F + this.panOffsetX;
                     float centerY = vpY + maxCanvasH / 2.0F + this.panOffsetY;
                     float entHeight = entity.getHeight();
                     float entWidth = entity.getWidth();
                     float maxDim = Math.max(entHeight, entWidth);
                     float baseScale = Math.min(maxCanvasW, maxCanvasH) * 0.40F;
                     float scale = baseScale * (1.8F / Math.max(0.8F, maxDim)) * this.zoomScale;

                     ModelRaycaster.RayHit hit = raycastEntity(
                        entity, (float) mx, (float) my, centerX, centerY, scale,
                        this.canvasWidth, this.canvasHeight
                     );
                     if (hit != null) {
                        pushUndo();
                        this.isDraggingCanvas = true;
                        this.lastDragBtn = btn;
                        this.applyToolAtPixel(hit.pixelX, hit.pixelY, btn);
                        return true;
                     }
                  }
                  // Clicking outside entity in 3D mode allows orbiting
                  this.isOrbiting = true;
                  this.lastDragBtn = btn;
                  return true;
               }
            }
         } else {
            // 2D Canvas click
            int pixelSize = Math.max(1, Math.min(maxCanvasW / this.canvasWidth, maxCanvasH / this.canvasHeight));
            int canvasW = pixelSize * this.canvasWidth;
            int canvasH = pixelSize * this.canvasHeight;
            int canvasX = 14 + (maxCanvasW - canvasW) / 2;
            int canvasY = canvasMarginTop + (maxCanvasH - canvasH) / 2;

            if (mx >= canvasX && mx < canvasX + canvasW && my >= canvasY && my < canvasY + canvasH) {
               pushUndo();
               this.isDraggingCanvas = true;
               this.lastDragBtn = btn;
               this.paintPixelAt(mx, my, canvasX, canvasY, pixelSize, btn);
               return true;
            }
         }

         // Color Studio (Right panel) — swatch opens the shared color picker modal
         // (112 = 8px gap + 96px preview panel + 8px gap; must match renderEditorView)
         int rightPanelX = this.width - 190 - 10;
         int studioY = 34 + getPrevCardHeight() + (itemPreviewPanelHeight() > 0 ? 112 : 14);

         // Animation-state buttons (Stand / Run / Swim / Fly) — compact, title row,
         // right-aligned (render order mirrored here). Only clickable while the
         // Dynamic Item Previewer panel is actually displayed.
         int dpY = 34 + getPrevCardHeight() + 8;
         int stateBtnY = dpY + 2;
         int stateBtnX = rightPanelX + 190 - 4;
         if (itemPreviewPanelHeight() > 0 && my >= stateBtnY && my <= stateBtnY + 11) {
            for (int m = PREVIEW_MODE_LABELS.length - 1; m >= 0; m--) {
               int lw = this.textRenderer.getWidth(PREVIEW_MODE_LABELS[m]) + 6;
               stateBtnX -= lw;
               if (mx >= stateBtnX && mx <= stateBtnX + lw) {
                  if (this.previewAnimationMode != m) {
                     this.previewAnimationMode = m;
                     this.previewAnimStartTime = System.currentTimeMillis();
                  }
                  return true;
               }
               stateBtnX -= 2;
            }
         }

         if (mx >= rightPanelX && mx <= rightPanelX + 190 && my >= studioY && my <= studioY + 26) {
            this.colorModal = new ColorPickerModal(this.width / 2, this.height / 2, this.activeColor, "Brush Color", "Texture paint", argb -> this.setColorFromRgb(argb));
            return true;
         }

         // Preset Palettes (24 swatches)
         int palY = studioY + 38;
         int palCols = 8;
         int palSize = 18;
         int palGap = 3;
         for (int p = 0; p < PRESET_PALETTE.length; p++) {
            int pr = p / palCols;
            int pc = p % palCols;
            int px = rightPanelX + pc * (palSize + palGap);
            int py = palY + pr * (palSize + palGap);
            if (mx >= px && mx <= px + palSize && my >= py && my <= py + palSize) {
               this.setColorFromRgb(PRESET_PALETTE[p]);
               return true;
            }
         }
      }

      return super.mouseClicked(click, doubled);
   }

   @Override
   public boolean mouseReleased(Click click) {
      this.isDraggingCanvas = false;
      this.isOrbiting = false;
      this.isPanning = false;
      if (this.colorModal != null) {
         this.colorModal.mouseReleased();
         if (!this.colorModal.isOpen()) this.colorModal = null;
      }
      return super.mouseReleased(click);
   }

   @Override
   public boolean mouseDragged(Click click, double offsetX, double offsetY) {
      double mx = click.x();
      double my = click.y();

      if (this.isOrbiting) {
         this.cameraYaw = (this.cameraYaw + (float) offsetX * 0.7F) % 360.0F;
         this.cameraPitch = Math.clamp(this.cameraPitch + (float) offsetY * 0.7F, -80.0F, 80.0F);
         return true;
      }

      if (this.isPanning) {
         this.panOffsetX += (float) offsetX;
         this.panOffsetY += (float) offsetY;
         return true;
      }

      int prevCardH = getPrevCardHeight();

      if (this.colorModal != null && this.colorModal.mouseDragged(new Click(mx, my, new MouseInput(click.button(), click.buttonInfo().modifiers())))) {
         return true;
      }

      return super.mouseDragged(click, offsetX, offsetY);
   }


   public void applyToolAtPixel(int px, int py, int btn) {
      if (px >= 0 && px < this.canvasWidth && py >= 0 && py < this.canvasHeight) {
         if (btn == 1 || this.activeTool == 1) {
            // Eraser / Right click
            this.canvasPixels[px][py] = 0;
         } else if (this.activeTool == 3) {
            // Eyedropper / Pick
            int col = this.canvasPixels[px][py];
            if ((col >> 24 & 0xFF) > 0) {
               this.setColorFromRgb(col);
               this.activeTool = 0; // return to pencil
               this.statusMessage = "Picked color";
               this.statusMessageTime = System.currentTimeMillis();
            }
         } else if (this.activeTool == 2) {
            // Bucket Fill
            this.floodFill(px, py, this.canvasPixels[px][py], this.activeColor);
         } else {
            // Pencil
            this.canvasPixels[px][py] = this.activeColor;
         }
         this.updateDynamicPreviewTexture();
      }
   }

   private void paintPixelAt(double mx, double my, int canvasX, int canvasY, int pixelSize, int btn) {
      int px = (int)((mx - canvasX) / pixelSize);
      int py = (int)((my - canvasY) / pixelSize);
      applyToolAtPixel(px, py, btn);
   }

   private void floodFill(int startX, int startY, int targetCol, int fillCol) {
      if (targetCol == fillCol) return;
      boolean[][] visited = new boolean[this.canvasWidth][this.canvasHeight];
      List<int[]> queue = new ArrayList<>();
      queue.add(new int[]{startX, startY});
      visited[startX][startY] = true;

      while (!queue.isEmpty()) {
         int[] cur = queue.remove(queue.size() - 1);
         int cx = cur[0];
         int cy = cur[1];
         if (this.canvasPixels[cx][cy] == targetCol) {
            this.canvasPixels[cx][cy] = fillCol;
            int[][] neighbors = new int[][]{{cx + 1, cy}, {cx - 1, cy}, {cx, cy + 1}, {cx, cy - 1}};
            for (int[] n : neighbors) {
               int nx = n[0];
               int ny = n[1];
               if (nx >= 0 && nx < this.canvasWidth && ny >= 0 && ny < this.canvasHeight && !visited[nx][ny]) {
                  visited[nx][ny] = true;
                  if (this.canvasPixels[nx][ny] == targetCol) {
                     queue.add(new int[]{nx, ny});
                  }
               }
            }
         }
      }
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      if (this.selectedTarget == null && this.maxCatalogScroll > 0) {
         this.catalogScroll = Math.clamp(this.catalogScroll - (int)(verticalAmount * 24.0), 0, this.maxCatalogScroll);
         return true;
      }
      if (this.selectedTarget != null && this.viewMode3D && is3dTarget(this.selectedTarget)) {
         int rightPanelW = 190;
         int maxCanvasW = this.width - rightPanelW - 30;
         if (mouseX >= 14 && mouseX <= 14 + maxCanvasW && mouseY >= CANVAS_TOP && mouseY <= this.height - 16) {
            this.zoomScale = Math.clamp(this.zoomScale * (1.0F + (float) verticalAmount * 0.12F), 0.35F, 4.0F);
            return true;
         }
      }
      return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
   }

   @Override
   public boolean charTyped(CharInput input) {
      if (this.colorModal != null && this.colorModal.charTyped(input)) {
         return true;
      }
      if (this.selectedTarget == null && this.searchField != null && this.searchField.isFocused()) {
         if (this.searchField.charTyped(input)) {
            return true;
         }
      }
      return super.charTyped(input);
   }

   @Override
   public boolean keyPressed(KeyInput input) {
      int key = input.key();
      int mods = input.modifiers();
      boolean ctrl = (mods & GLFW.GLFW_MOD_CONTROL) != 0;
      boolean shift = (mods & GLFW.GLFW_MOD_SHIFT) != 0;

      if (this.colorModal != null && this.colorModal.keyPressed(input)) {
         if (key == 256) {
            this.colorModal = null;
         }
         return true;
      }

      if (this.selectedTarget == null && this.searchField != null && this.searchField.isFocused()) {
         if (key == GLFW.GLFW_KEY_ESCAPE) {
            this.searchField.setFocused(false);
            this.setFocused(null);
            return true;
         }
         if (this.searchField.keyPressed(input)) {
            return true;
         }
      }

      if (this.selectedTarget != null) {
         if (ctrl && key == GLFW.GLFW_KEY_Z) {
            if (shift) {
               this.redo();
            } else {
               this.undo();
            }
            return true;
         }
         if (ctrl && key == GLFW.GLFW_KEY_Y) {
            this.redo();
            return true;
         }
         if (ctrl && key == GLFW.GLFW_KEY_S) {
            this.saveAndApply();
            return true;
         }
         if (key == GLFW.GLFW_KEY_B) {
            this.activeTool = 0; // Pencil
            return true;
         }
         if (key == GLFW.GLFW_KEY_E) {
            this.activeTool = 1; // Eraser
            return true;
         }
         if (key == GLFW.GLFW_KEY_G) {
            this.activeTool = 2; // Bucket
            return true;
         }
         if (key == GLFW.GLFW_KEY_I) {
            this.activeTool = 3; // Eyedropper
            return true;
         }
         if (key == GLFW.GLFW_KEY_R) {
            this.activeTool = 4; // Orbit
            return true;
         }
         if (key == GLFW.GLFW_KEY_V && is3dTarget(this.selectedTarget)) {
            this.viewMode3D = !this.viewMode3D;
            return true;
         }
         if (key == GLFW.GLFW_KEY_C) {
            this.cameraYaw = 0;
            this.cameraPitch = 0;
            this.zoomScale = 1.0F;
            this.panOffsetX = 0;
            this.panOffsetY = 0;
            return true;
         }
         if (key == GLFW.GLFW_KEY_ESCAPE) {
            this.selectedTarget = null;
            this.searchField.setVisible(true);
            return true;
         }
      } else {
         if (key == GLFW.GLFW_KEY_ESCAPE) {
            this.client.setScreen(this.parent);
            return true;
         }
      }
      return super.keyPressed(input);
   }

   private static void drawModernRoundedRect(DrawContext context, int x, int y, int width, int height, int radius, int color) {
      if (width <= 0 || height <= 0) return;
      if (radius <= 0) {
         context.fill(x, y, x + width, y + height, color);
         return;
      }
      int maxRadius = Math.min(width / 2, height / 2);
      radius = Math.min(radius, maxRadius);

      // Top curved cap
      for (int i = 0; i < radius; i++) {
         int distY = radius - 1 - i;
         int w = radius - (int)Math.sqrt(radius * radius - distY * distY);
         context.fill(x + w, y + i, x + width - w, y + i + 1, color);
      }

      // Center solid body
      if (height > radius * 2) {
         context.fill(x, y + radius, x + width, y + height - radius, color);
      }

      // Bottom curved cap
      for (int i = 0; i < radius; i++) {
         int distY = radius - 1 - i;
         int w = radius - (int)Math.sqrt(radius * radius - distY * distY);
         context.fill(x + w, y + height - 1 - i, x + width - w, y + height - i, color);
      }
   }

   // ================= PLAYER MODEL PANEL =================

   /**
    * Width of the Player Model sidebar panel.  Matches the editor right panel so
    * the catalog grid can subtract it from its available width consistently.
    */
   static final int PM_PANEL_W = 196;

   /**
    * Renders the Player Model panel in the right sidebar.
    *
    * @param context  draw context
    * @param mouseX   mouse X for hover detection
    * @param mouseY   mouse Y for hover detection
    * @param panelX   left edge of the panel
    * @param panelY   top edge of the panel (usually 34 — below the header bar)
    */
   private void renderPlayerModelPanel(DrawContext context, int mouseX, int mouseY, int panelX, int panelY) {
      int primary = VoidCyanClient.getPrimaryColor() | 0xFF000000;
      int panelW   = PM_PANEL_W;
      boolean hasModel   = PlayerModelManager.hasModel();
      boolean hasTexture = PlayerModelManager.hasTexture();
      boolean enabled    = VoidCyanClient.isPlayerModelEnabled;

      // --- Panel background ---
      context.fill(panelX, panelY, panelX + panelW, this.height - 10, 0x2014141E);
      context.fill(panelX, panelY, panelX + panelW, panelY + 1, 0x30FFFFFF);

      int y = panelY + 4;

      // --- Header row: "PLAYER MODEL" label + enable chip ---
      context.drawTextWithShadow(this.textRenderer, "PLAYER MODEL", panelX + 6, y, primary);
      // Enable toggle chip (right-aligned)
      String toggleLbl = enabled ? "ON" : "OFF";
      int toggleW = this.textRenderer.getWidth(toggleLbl) + 10;
      int toggleX = panelX + panelW - toggleW - 4;
      boolean toggleHov = mouseX >= toggleX && mouseX <= toggleX + toggleW
                       && mouseY >= y - 1  && mouseY <= y + 9;
      context.fill(toggleX, y - 1, toggleX + toggleW, y + 9,
            enabled ? (toggleHov ? primary : primary & 0x00FFFFFF | 0xC0000000)
                    : (toggleHov ? 0x60FFFFFF : 0x25FFFFFF));
      context.drawTextWithShadow(this.textRenderer, toggleLbl, toggleX + 5, y,
            enabled ? 0xFF000000 : 0xFFAAAAAA);

      y += 14;

      // --- 3D Preview box ---
      int previewH = 100;
      int previewX1 = panelX + 6;
      int previewY1 = y;
      int previewX2 = panelX + panelW - 6;
      int previewY2 = y + previewH;
      context.fill(previewX1, previewY1, previewX2, previewY2, 0x35000000);
      drawModernRoundedRect(context, previewX1 - 1, previewY1 - 1,
            (previewX2 - previewX1) + 2, previewH + 2, 4, 0x25FFFFFF);

      MinecraftClient mcClient = MinecraftClient.getInstance();
      if (hasModel && enabled && mcClient.player != null) {
         // drawEntity triggers the PlayerModelRenderMixin which renders the OBJ mesh
         InventoryScreen.drawEntity(context,
               previewX1, previewY1, previewX2, previewY2,
               30, 0.0625F, -1, -1, mcClient.player);
      } else if (hasModel) {
         // Model loaded but disabled — show a dim placeholder
         String msg = enabled ? "No player" : "Disabled";
         int mw = this.textRenderer.getWidth(msg);
         context.drawTextWithShadow(this.textRenderer, msg,
               previewX1 + (previewX2 - previewX1 - mw) / 2,
               previewY1 + previewH / 2 - 4, 0xFF666677);
      } else {
         // No model imported yet
         String noMsg = "No model imported";
         int nw = this.textRenderer.getWidth(noMsg);
         context.drawTextWithShadow(this.textRenderer, noMsg,
               previewX1 + (previewX2 - previewX1 - nw) / 2,
               previewY1 + previewH / 2 - 4, 0xFF555566);
      }

      y = previewY2 + 4;

      // --- Status line ---
      String statusLine = pmStatus.isEmpty()
            ? (hasModel ? (hasTexture ? "Ready" : "No texture") : "Import an .obj to start")
            : pmStatus;
      context.drawTextWithShadow(this.textRenderer, statusLine, panelX + 6, y,
            hasModel ? (hasTexture ? 0xFF55FF99 : 0xFFFFAA44) : 0xFF888899);
      y += 12;

      // --- Compact button row (auto-sized to fill panel width) ---
      // Three buttons: [Import .obj] [Import Tex] [Remove]
      // Available width minus padding on both sides
      int btnAreaW = panelW - 12;
      int btnGap   = 3;
      int numBtns  = 3;
      int btnW     = (btnAreaW - btnGap * (numBtns - 1)) / numBtns;
      int btnH     = 13;
      int bx       = panelX + 6;
      int by       = y;

      String[] btnLabels = {"Import .obj", "Texture", "Remove"};
      int[]    btnColors = {0x25FFFFFF,    0x25FFFFFF, hasModel ? 0x25FF5555 : 0x15FF5555};
      int[]    btnHovCol = {0x55FFFFFF,    0x55FFFFFF, hasModel ? 0x60FF5555 : 0x15FF5555};
      int[]    txtColors = {0xFFFFFFFF,    0xFFFFFFFF, hasModel ? 0xFFFF8888 : 0xFF555566};

      for (int b = 0; b < numBtns; b++) {
         int bxb = bx + b * (btnW + btnGap);
         boolean bHov = mouseX >= bxb && mouseX <= bxb + btnW
                     && mouseY >= by && mouseY <= by + btnH;
         context.fill(bxb, by, bxb + btnW, by + btnH,
               bHov ? btnHovCol[b] : btnColors[b]);
         // Centre-align text in button, scale text to fit
         String lbl = btnLabels[b];
         int lw = this.textRenderer.getWidth(lbl);
         // Truncate if necessary (very unlikely at these sizes but safe)
         int textX = bxb + Math.max(0, (btnW - lw) / 2);
         context.drawTextWithShadow(this.textRenderer, lbl, textX, by + 3, txtColors[b]);
      }
   }

   /**
    * Returns the hit button index [0..2] for the three Player Model panel buttons,
    * or -1 if none are hit.  Also returns -100 for the enable-toggle chip and
    * -101 for the toggle header area (enable/disable).
    *
    * <p>Button indices: 0 = Import .obj, 1 = Import Texture, 2 = Remove.
    */
   private int getPlayerModelPanelHit(double mx, double my, int panelX, int panelY) {
      int panelW = PM_PANEL_W;
      int primary = VoidCyanClient.getPrimaryColor() | 0xFF000000;

      // Toggle chip
      String toggleLbl = VoidCyanClient.isPlayerModelEnabled ? "ON" : "OFF";
      int toggleW = this.textRenderer.getWidth(toggleLbl) + 10;
      int toggleX = panelX + panelW - toggleW - 4;
      int toggleY = panelY + 4;
      if (mx >= toggleX && mx <= toggleX + toggleW && my >= toggleY - 1 && my <= toggleY + 9) {
         return -100;
      }

      // Button row — compute same geometry as renderPlayerModelPanel
      int previewH = 100;
      int previewY2 = panelY + 4 + 14 + previewH;
      int by = previewY2 + 4 + 12; // status line height = 12

      int btnAreaW = panelW - 12;
      int btnGap   = 3;
      int numBtns  = 3;
      int btnW     = (btnAreaW - btnGap * (numBtns - 1)) / numBtns;
      int btnH     = 13;
      int bx       = panelX + 6;

      for (int b = 0; b < numBtns; b++) {
         int bxb = bx + b * (btnW + btnGap);
         if (mx >= bxb && mx <= bxb + btnW && my >= by && my <= by + btnH) {
            return b;
         }
      }
      return -1;
   }

   /** Opens a file picker on a background thread to avoid freezing the render loop. */
   private void openObjPicker() {
      if (pmPickerRunning) return;
      pmPickerRunning = true;
      pmStatus = "Opening file dialog...";
      Thread t = new Thread(() -> {
         try {
            String result;
            try (org.lwjgl.system.MemoryStack stack = org.lwjgl.system.MemoryStack.stackPush()) {
               org.lwjgl.PointerBuffer filterPatterns = stack.pointers(stack.UTF8("*.obj"));
               result = TinyFileDialogs.tinyfd_openFileDialog(
                     "Import .obj Model", "", filterPatterns, "Wavefront OBJ (*.obj)", false);
            }
            if (result != null && !result.isBlank()) {
               try {
                  PlayerModelManager.importModel(Path.of(result));
                  pmStatus = "Imported: " + Path.of(result).getFileName();
               } catch (Exception e) {
                  pmStatus = "Error: " + e.getMessage();
               }
            } else {
               pmStatus = "";
            }
         } finally {
            pmPickerRunning = false;
         }
      }, "voidcyan-obj-picker");
      t.setDaemon(true);
      t.start();
   }

   /** Opens a file picker on a background thread to import a PNG texture. */
   private void openTexturePicker() {
      if (pmPickerRunning) return;
      pmPickerRunning = true;
      pmStatus = "Opening file dialog...";
      Thread t = new Thread(() -> {
         try {
            String result;
            try (org.lwjgl.system.MemoryStack stack = org.lwjgl.system.MemoryStack.stackPush()) {
               org.lwjgl.PointerBuffer filterPatterns = stack.pointers(stack.UTF8("*.png"));
               result = TinyFileDialogs.tinyfd_openFileDialog(
                     "Import Texture", "", filterPatterns, "PNG Image (*.png)", false);
            }
            if (result != null && !result.isBlank()) {
               try {
                  java.nio.file.Files.createDirectories(PlayerModelManager.MODEL_DIR);
                  java.nio.file.Files.copy(Path.of(result), PlayerModelManager.TEXTURE_FILE,
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                  // Re-register so the TextureManager picks up the new file.
                  // The existing texture ID will be replaced on next render.
                  MinecraftClient.getInstance().execute(PlayerModelManager::ensureTextureRegistered);
                  pmStatus = "Texture imported";
               } catch (Exception e) {
                  pmStatus = "Error: " + e.getMessage();
               }
            } else {
               pmStatus = "";
            }
         } finally {
            pmPickerRunning = false;
         }
      }, "voidcyan-tex-picker");
      t.setDaemon(true);
      t.start();
   }
}
