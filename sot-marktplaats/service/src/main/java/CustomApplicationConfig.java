import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

public class CustomApplicationConfig extends ResourceConfig{
    public CustomApplicationConfig() {
        register(AuthenticationFilter.class);
        register(ProductResources.class);
        register(UserResources.class);
        property(ServerProperties.RESPONSE_SET_STATUS_OVER_SEND_ERROR, "true");
    }
}
