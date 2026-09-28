package com.voidcyan.client.util;

import com.voidcyan.client.mixin.accessor.SpriteContentsImageAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.util.Identifier;

/**
 * Dynamic Item Previewer core: publishes the texture editor's live canvas into the
 * item's atlas sprite (block or items atlas) so any render that samples it — held
 * items, GUI icons, dropped items — updates instantly, without saving or reloads.
 *
 * Why the atlas: in 1.21.11 baked item quads carry atlas UVs directly (the item
 * command record has no sprite field), so redirecting a "sprite" per draw is not
 * possible. The correct live-override point is therefore the sprite's stitched
 * image inside the atlas itself: copy the canvas into the sprite's base image,
 * regenerate that sprite's mipmaps, and re-upload it into the atlas GPU texture.
 * The vanilla sprite object, identity and UVs all stay valid — no remapping needed.
 */
public final class DynamicItemPreviewer {
   private static Identifier overrideSpriteId = null;
   private static Sprite overrideSprite = null;
   private static SpriteAtlasTexture overrideAtlas = null;
   private static NativeImage originalPixels = null;
   private static int canvasWidth;
   private static int canvasHeight;

   private DynamicItemPreviewer() {
   }

   /**
    * Prepares live override for the given item texture id. Call whenever the edited
    * target changes. Restores any previous override first. Returns true when the
    * sprite exists in the atlas and live preview is armed.
    */
   public static boolean beginOverride(Identifier itemTextureId, int width, int height) {
      endOverride();
      if (itemTextureId == null || width <= 0 || height <= 0) return false;

      MinecraftClient client = MinecraftClient.getInstance();
      if (client == null || client.getAtlasManager() == null) return false;

      // Item sprites may be stitched into the block atlas or the separate items
      // atlas depending on version/pack layout — probe both, block atlas first.
      Sprite sprite = null;
      SpriteAtlasTexture atlas = null;
      for (Identifier atlasId : new Identifier[]{
         SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, SpriteAtlasTexture.ITEMS_ATLAS_TEXTURE
      }) {
         SpriteAtlasTexture candidateAtlas = client.getAtlasManager().getAtlasTexture(atlasId);
         if (candidateAtlas == null) continue;

         Sprite candidate = resolveRealSprite(candidateAtlas, itemTextureId);
         if (candidate != null) {
            sprite = candidate;
            atlas = candidateAtlas;
            break;
         }
      }

      if (sprite == null || atlas == null) return false;

      SpriteContents contents = sprite.getContents();
      if (contents.getWidth() != width || contents.getHeight() != height) {
         // Atlas sprite dimensions differ (padding/anomaly) — publishing would
         // corrupt neighboring sprites, so live preview stays off for this target.
         return false;
      }

      NativeImage base = ((SpriteContentsImageAccessor) contents).voidcyan$getBaseImage();
      if (base == null) return false;

      overrideSpriteId = itemTextureId;
      overrideSprite = sprite;
      overrideAtlas = atlas;
      canvasWidth = width;
      canvasHeight = height;
      // Preserve pristine atlas pixels so endOverride can restore exactly.
      originalPixels = new NativeImage(width, height, true);
      for (int x = 0; x < width; x++) {
         for (int y = 0; y < height; y++) {
            originalPixels.setColorArgb(x, y, base.getColorArgb(x, y));
         }
      }

      return true;
   }

   /**
    * Looks up a sprite while refusing the shared "missing sprite" fallback:
    * {@link SpriteAtlasTexture#getSprite} returns the missing sprite (never null)
    * for unknown ids, and painting into it would corrupt every missing-texture
    * render game-wide. An id is considered real only when its contents carry the
    * requested id (the missing sprite reports its own id).
    */
   private static Sprite resolveRealSprite(SpriteAtlasTexture atlas, Identifier itemTextureId) {
      Sprite candidate;
      try {
         candidate = atlas.getSprite(itemTextureId);
      } catch (Exception e) {
         // Atlas not initialized or lookup threw — treat as absent.
         return null;
      }

      if (candidate == null) return null;
      return itemTextureId.equals(candidate.getContents().getId()) ? candidate : null;
   }

   /** Publishes the current canvas into the atlas sprite (mips + GPU upload). */
   public static void publishFrame(int[][] canvasPixels) {
      if (overrideSprite == null || canvasPixels == null) return;

      SpriteContents contents = overrideSprite.getContents();
      NativeImage base = ((SpriteContentsImageAccessor) contents).voidcyan$getBaseImage();
      if (base == null) return;

      try {
         for (int x = 0; x < canvasWidth; x++) {
            for (int y = 0; y < canvasHeight; y++) {
               base.setColorArgb(x, y, canvasPixels[x][y]);
            }
         }

         contents.generateMipmaps(overrideAtlas.getMaxTextureSize() > 0 ? atlasMipLevels() : 0);
         overrideSprite.upload(overrideAtlas.getGlTexture(), 0);
      } catch (Exception ignored) {
         // Never let preview publishing break the frame.
      }
   }

   private static int atlasMipLevels() {
      return MinecraftClient.getInstance().options.getMipmapLevels().getValue();
   }

   /** Restores the original atlas pixels and disarms the live override. */
   public static void endOverride() {
      if (overrideSprite != null && originalPixels != null) {
         try {
            SpriteContents contents = overrideSprite.getContents();
            NativeImage base = ((SpriteContentsImageAccessor) contents).voidcyan$getBaseImage();
            if (base != null) {
               for (int x = 0; x < canvasWidth; x++) {
                  for (int y = 0; y < canvasHeight; y++) {
                     base.setColorArgb(x, y, originalPixels.getColorArgb(x, y));
                  }
               }

               contents.generateMipmaps(atlasMipLevels());
               overrideSprite.upload(overrideAtlas.getGlTexture(), 0);
            }
         } catch (Exception ignored) {
         }
      }

      overrideSpriteId = null;
      overrideSprite = null;
      overrideAtlas = null;
      if (originalPixels != null) {
         originalPixels.close();
         originalPixels = null;
      }
      canvasWidth = 0;
      canvasHeight = 0;
   }

   public static boolean isActive() {
      return overrideSprite != null;
   }

   public static Identifier getActiveSpriteId() {
      return overrideSpriteId;
   }
}
