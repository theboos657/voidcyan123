package com.voidcyan.client.mixin;

import com.voidcyan.client.accessor.BigItemRenderStateAccess;
import net.minecraft.client.render.entity.state.ItemEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin({ItemEntityRenderState.class})
public class BigItemRenderStateMixin implements BigItemRenderStateAccess {
   @Unique
   private boolean voidcyan$bigItem;

   @Override
   public void voidcyan$setBigItem(boolean bigItem) {
      this.voidcyan$bigItem = bigItem;
   }

   @Override
   public boolean voidcyan$isBigItem() {
      return this.voidcyan$bigItem;
   }
}
