package com.voidcyan.client;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.network.packet.s2c.play.ScoreboardDisplayS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardObjectiveUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardScoreUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.TeamS2CPacket;

public class PacketTest {
   public static void main(String[] args) throws Exception {
      Class<?>[] classes = new Class[]{
         TeamS2CPacket.class, ScoreboardObjectiveUpdateS2CPacket.class, ScoreboardScoreUpdateS2CPacket.class, ScoreboardDisplayS2CPacket.class
      };

      for (Class<?> c : classes) {
         System.out.println("=== " + c.getName() + " ===");

         for (Field f : c.getDeclaredFields()) {
            System.out.println("F: " + f.getType().getSimpleName() + " " + f.getName());
         }

         for (Method m : c.getDeclaredMethods()) {
            System.out.println("M: " + m.getReturnType().getSimpleName() + " " + m.getName() + "(...)");
         }

         for (Constructor<?> cons : c.getDeclaredConstructors()) {
            System.out.println("C: " + cons.toString());
         }
      }
   }
}
