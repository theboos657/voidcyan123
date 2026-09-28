package com.voidcyan.client.module.damagehearts;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HeartManager {
   private final List<FloatingHeart> hearts = new ArrayList<>();
   private final int maxHearts;

   public HeartManager(int maxHearts) {
      this.maxHearts = maxHearts;
   }

   public void addHeart(FloatingHeart heart) {
      this.hearts.add(heart);
   }

   public void update(float deltaTime) {
      Iterator<FloatingHeart> iterator = this.hearts.iterator();

      while (iterator.hasNext()) {
         FloatingHeart heart = iterator.next();
         heart.update(deltaTime);
         if (heart.isExpired()) {
            iterator.remove();
         }
      }

      while (this.hearts.size() > this.maxHearts) {
         this.hearts.remove(0);
      }
   }

   public void clear() {
      this.hearts.clear();
   }

   public List<FloatingHeart> getHearts() {
      return this.hearts;
   }

}
