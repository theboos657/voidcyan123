package com.voidcyan.client.mixin;

import com.voidcyan.client.util.EntityRenderStateAccessor;
import net.minecraft.client.render.entity.state.EntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin({EntityRenderState.class})
public class EntityRenderStateMixin implements EntityRenderStateAccessor {
   @Unique
   private int voidcyan$entityId = -1;

   @Unique
   private float voidcyan$fallTumble = 0.0F;

   @Override
   public int voidcyan$getEntityId() {
      return this.voidcyan$entityId;
   }

   @Override
   public void voidcyan$setEntityId(int id) {
      this.voidcyan$entityId = id;
   }

   @Override
   public float voidcyan$getFallTumble() {
      return this.voidcyan$fallTumble;
   }

   @Override
   public void voidcyan$setFallTumble(float tumble) {
      this.voidcyan$fallTumble = tumble;
   }
}
