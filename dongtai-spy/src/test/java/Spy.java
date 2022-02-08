import java.lang.dongtai.SpyDispatcherHandler;

public class Spy {

    public static void main(String[] args) {
        set("1");
    }

    public static Object set(Object a) {
        Object b = a;
        SpyDispatcherHandler.getDispatcher().collectMethodPool(
                b,
                new Object[]{new Object()},
                new Object(),
                new String(),
                new String(),
                new String(),
                new String(),
                new String(),
                true,
                1
        );
        return b;
    }
}
