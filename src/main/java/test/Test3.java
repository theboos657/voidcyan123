package test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import net.minecraft.client.render.RenderLayer;

public class Test3 {
   public static void main(String[] args) {
      for (Method m : RenderLayer.class.getMethods()) {
         if (Modifier.isStatic(m.getModifiers())) {
            System.out.println(m.getName());
         }
      }
   }
}
