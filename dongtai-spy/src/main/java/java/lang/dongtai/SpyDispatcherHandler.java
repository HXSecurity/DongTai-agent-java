package java.lang.dongtai;

/**
 * @author owefsad
 * @since 1.3.1
 */
public class SpyDispatcherHandler {

    private static SpyDispatcher dispatcher;

    public static void setDispatcher(SpyDispatcher dispatcher) {
        SpyDispatcherHandler.dispatcher = dispatcher;
    }

    public static SpyDispatcher getDispatcher() {
        return dispatcher;
    }
}
