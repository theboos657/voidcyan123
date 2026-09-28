package com.voidcyan.client.mixin.accessor;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({BipedEntityModel.class})
public interface PlayerEntityModelAccessor {
   @Accessor("head")
   ModelPart voidcyan$getHead();

   @Accessor("hat")
   ModelPart voidcyan$getHat();
}
