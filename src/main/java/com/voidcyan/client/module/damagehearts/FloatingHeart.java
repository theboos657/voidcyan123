package com.voidcyan.client.module.damagehearts;

import net.minecraft.util.math.Vec3d;

public class FloatingHeart {
   public Vec3d position;
   public Vec3d velocity;
   public float scale;
   public float alpha;
   public float rotation;
   public int color;
   public boolean isCrit;
   public long spawnTime;
   public float lifetime;
   public float damage;

   public FloatingHeart(Vec3d position, float hearts, int color, boolean isCrit) {
      this.position = position;
      this.damage = hearts * 2.0F;
      double vx = (Math.random() - 0.5) * 0.12;
      double vy = 0.1 + Math.random() * 0.08;
      double vz = (Math.random() - 0.5) * 0.12;
      this.velocity = new Vec3d(vx, vy, vz);
      this.scale = 0.35F + hearts * 0.08F;
      this.alpha = 1.0F;
      this.rotation = 0.0F;
      this.color = color;
      this.isCrit = isCrit;
      this.spawnTime = System.currentTimeMillis();
      this.lifetime = 1200.0F + hearts * 300.0F;
   }

   public void update(float deltaTime) {
      this.position = this.position.add(this.velocity.x * deltaTime, this.velocity.y * deltaTime, this.velocity.z * deltaTime);
      this.rotation = this.rotation + deltaTime * DamageHeartsModule.rotationSpeed * 0.1F;
      long currentTime = System.currentTimeMillis();
      float age = (float)(currentTime - this.spawnTime);
      if (age > this.lifetime * 0.6F) {
         this.alpha = Math.max(0.0F, 1.0F - (age - this.lifetime * 0.6F) / (this.lifetime * 0.4F));
      }

      double nextVx = this.velocity.x * 0.85;
      double nextVy = (this.velocity.y - 0.006) * 0.9;
      if (nextVy < 0.015) {
         nextVy = 0.015;
      }

      double nextVz = this.velocity.z * 0.85;
      this.velocity = new Vec3d(nextVx, nextVy, nextVz);
   }

   public boolean isExpired() {
      return (float)(System.currentTimeMillis() - this.spawnTime) > this.lifetime;
   }
}
