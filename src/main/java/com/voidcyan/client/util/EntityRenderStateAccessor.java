package com.voidcyan.client.util;

public interface EntityRenderStateAccessor {
   int voidcyan$getEntityId();

   void voidcyan$setEntityId(int var1);

   /** Extra fall-tumble rotation (radians) captured from the entity at state-update time. */
   float voidcyan$getFallTumble();

   void voidcyan$setFallTumble(float var1);
}
