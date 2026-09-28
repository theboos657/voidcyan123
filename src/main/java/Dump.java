import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class Dump {
   public static void main(String[] args) {
      try {
         Class<?> clazz = Class.forName("net.minecraft.client.render.RenderLayer");

         for (Method m : clazz.getMethods()) {
            if (m.getName().startsWith("get") && Modifier.isStatic(m.getModifiers())) {
               System.out.println(m.toString());
            }
         }
      } catch (Exception var6) {
         var6.printStackTrace();
      }
   }
}
