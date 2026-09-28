package com.voidcyan.client.module;

public class Notification {
   public String message;
   public long startTime;
   public long duration;

   public Notification(String message, long duration) {
      this.message = message;
      this.startTime = System.currentTimeMillis();
      this.duration = duration;
   }

   public boolean isExpired() {
      return System.currentTimeMillis() - this.startTime > this.duration;
   }
}
