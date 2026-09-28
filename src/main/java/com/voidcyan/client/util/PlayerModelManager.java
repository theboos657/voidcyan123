package com.voidcyan.client.util;

import com.voidcyan.client.VoidCyanClient;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.ReloadableTexture;
import net.minecraft.client.texture.TextureContents;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

/**
 * Owns the user's custom player model: the imported .obj file, its skin texture
 * and the parsed triangle cache used by the player render mixin.
 *
 * Files live in {@code .minecraft/config/voidcyan/playermodel/}:
 *   model.obj     — the imported geometry
 *   texture.png   — the UV texture for the model (any resolution)
 *
 * The texture is registered into the vanilla TextureManager as a reloadable
 * texture under "voidcyan:playermodel" so it can be referenced by normal
 * RenderLayers both in-world and in GUI entity previews.
 */
public final class PlayerModelManager {

   public static final Identifier MODEL_TEXTURE_ID = Identifier.of("voidcyan", "playermodel");
   public static final Identifier FALLBACK_TEXTURE_ID = Identifier.of("voidcyan", "playermodel_white");
   public static final Path MODEL_DIR = FabricLoader.getInstance().getConfigDir()
      .resolve("voidcyan").resolve("playermodel");
   public static final Path MODEL_FILE = MODEL_DIR.resolve("model.obj");
   public static final Path TEXTURE_FILE = MODEL_DIR.resolve("texture.png");

   /** Cached parse of the imported model; null when no model is imported/valid. */
   private static volatile ObjModelParser.Result cachedResult;
   /** True when the texture file exists and was registered successfully. */
   private static volatile boolean textureRegistered;
   /** Nano-timestamp of the last import, used for status messages. */
   private static volatile long lastImportTime;

   private PlayerModelManager() {}

   public static boolean hasModel() {
      return cachedResult != null;
   }

   public static ObjModelParser.Result getModel() {
      return cachedResult;
   }

   public static boolean hasTexture() {
      return textureRegistered;
   }

   public static long getLastImportTime() {
      return lastImportTime;
   }

   /** Copies the chosen .obj into the config dir, parses it and caches the result. */
   public static ObjModelParser.Result importModel(Path sourceObj) throws IOException {
      Files.createDirectories(MODEL_DIR);
      Files.copy(sourceObj, MODEL_FILE, StandardCopyOption.REPLACE_EXISTING);
      ObjModelParser.Result result = ObjModelParser.parse(MODEL_FILE);
      cachedResult = result;
      lastImportTime = System.nanoTime();
      ensureTextureRegistered();
      return result;
   }

   /** Registers the skin texture for the custom model if texture.png exists. */
   public static void ensureTextureRegistered() {
      if (!Files.isRegularFile(TEXTURE_FILE)) {
         textureRegistered = false;
         return;
      }
      try {
         TextureManager tm = MinecraftClient.getInstance().getTextureManager();
         AbstractTexture existing = tm.getTexture(MODEL_TEXTURE_ID);
         if (existing != null && existing instanceof PlayerModelTexture) {
            textureRegistered = true;
            return;
         }
         tm.registerTexture(MODEL_TEXTURE_ID, new PlayerModelTexture(MODEL_TEXTURE_ID));
         textureRegistered = true;
      } catch (Exception e) {
         textureRegistered = false;
      }
   }

   /** Removes the imported model files and clears the cache (back to vanilla skin). */
   public static void clearModel() {
      try {
         Files.deleteIfExists(MODEL_FILE);
      } catch (IOException ignored) {}
      cachedResult = null;
   }

   /** Re-reads model.obj from disk (after manual edits). Returns null on failure. */
   public static ObjModelParser.Result reloadFromDisk() {
      try {
         if (!Files.isRegularFile(MODEL_FILE)) {
            cachedResult = null;
            return null;
         }
         ObjModelParser.Result result = ObjModelParser.parse(MODEL_FILE);
         cachedResult = result;
         ensureTextureRegistered();
         return result;
      } catch (IOException e) {
         cachedResult = null;
         return null;
      }
   }

   /**
    * Loads the config-dir texture.png through the normal resource-pipeline upload
    * path: loadContents returns a TextureContents built from the file's NativeImage,
    * and ReloadableTexture.upload handles GPU upload + mipmaps for us.
    */
   private static final class PlayerModelTexture extends ReloadableTexture {
      private PlayerModelTexture(Identifier id) {
         super(id);
      }

      @Override
      public TextureContents loadContents(ResourceManager manager) throws IOException {
         try (var in = Files.newInputStream(TEXTURE_FILE)) {
            NativeImage image = NativeImage.read(in);
            return new TextureContents(image, null);
         }
      }
   }

   /** Convenience: the entity-cutout layer bound to the custom model texture. */
   public static RenderLayer modelLayer() {
      return net.minecraft.client.render.RenderLayers.entityCutoutNoCull(MODEL_TEXTURE_ID);
   }

   /** 1x1 white texture used until the user imports a texture.png. */
   public static RenderLayer fallbackLayer() {
      try {
         TextureManager tm = MinecraftClient.getInstance().getTextureManager();
         if (tm.getTexture(FALLBACK_TEXTURE_ID) == null) {
            NativeImage white = new NativeImage(1, 1, false);
            white.setColorArgb(0, 0, 0xFFFFFFFF);
            NativeImageBackedTexture tex = new NativeImageBackedTexture(() -> "voidcyan_playermodel_white", white);
            tm.registerTexture(FALLBACK_TEXTURE_ID, tex);
         }
         return net.minecraft.client.render.RenderLayers.entityCutoutNoCull(FALLBACK_TEXTURE_ID);
      } catch (Exception e) {
         return null;
      }
   }

   /** True when Item Physics-style debug logging is on and imports should log. */
   public static boolean isFeatureEnabled() {
      return VoidCyanClient.isPlayerModelEnabled;
   }
}
