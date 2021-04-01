import org.glassfish.jersey.server.ResourceConfig;

public class CustomApplicationConfig extends ResourceConfig{
    public CustomApplicationConfig() {
        register(AuthenticationFilter.class);
        register(ProductResources.class);
    }
}
