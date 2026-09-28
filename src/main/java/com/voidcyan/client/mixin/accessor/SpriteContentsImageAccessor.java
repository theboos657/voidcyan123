package com.voidcyan.client.mixin.accessor;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.SpriteContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Exposes the base (mip level 0) image of a stitched atlas sprite so the texture
 * editor can publish live canvas edits into the block atlas. This is the only
 * reliable live-override point for item sprites: baked item quads carry atlas UVs,
 * so swapping the sprite's source pixels (and re-uploading) instantly changes how
 * the item renders everywhere — held in hand, in GUIs, on the ground.
 */
@Mixin(SpriteContents.class)
public interface SpriteContentsImageAccessor {
   @Accessor("image")
   NativeImage voidcyan$getBaseImage();
}
