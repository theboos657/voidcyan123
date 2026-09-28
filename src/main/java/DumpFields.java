import java.lang.reflect.Field;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;

public class DumpFields {
   public static void main(String[] args) {
      System.out.println("Fields of LivingEntityRenderState:");

      for (Class<?> clazz = LivingEntityRenderState.class; clazz != null; clazz = clazz.getSuperclass()) {
         System.out.println("--- " + clazz.getName() + " ---");

         for (Field f : clazz.getDeclaredFields()) {
            System.out.println(f.getType().getName() + " " + f.getName());
         }
      }
   }
}
