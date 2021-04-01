import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;

@Path("/")
public class UserResources {

    private UserStore store;
    public UserResources(){
        store = UserStore.singleton();
    }

    private int generateUserId() {return store.getAll().size() + 1;}

    @Context
    private UriInfo uriInfo;

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response createProduct(@FormParam("username") String username, @FormParam("password") String password, @FormParam("role") String role){
        int id = this.generateUserId();
        User user = new User(id, username, password, role);
        store.add(user);
        String url = uriInfo.getAbsolutePath() + "" + user.getId();
        URI uri = URI.create(url);
        return Response.created(uri).build();
    }
}
