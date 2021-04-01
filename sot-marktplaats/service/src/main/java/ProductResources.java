import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Path("/products")
public class ProductResources {
    private MockDataStore store;
    public ProductResources(){
        store = MockDataStore.singleton();
    }

    private int generateStudentNr() {
        return store.getAll().size();
    }

    @GET
    @Path("/hello")
    @PermitAll
    @Produces(MediaType.TEXT_PLAIN)
    public Response sayHello(){
        String message = "Hello world";
        return Response.status(Response.Status.OK).entity(message).build();
    }
    @GET
    @Path("/all")
    @PermitAll
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllProducts(){
        GenericEntity<List<Product>> entity = new GenericEntity<>(store.getAll()) {};
        return Response.ok(entity).build();
    }
    @GET //asd
    @RolesAllowed({"ADMIN"})
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProductWithID(@PathParam("id") int id){
        Product product = this.getProduct(id);
        if(product == null){
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(product).build();
    }

    private Product getProduct(int id) {
        return store.get(id);
    }

    @Context
    private UriInfo uriInfo;

    @GET //asd
    @PermitAll
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProductByName(@QueryParam("name") String name){
        if(!uriInfo.getQueryParameters().containsKey("name")){
            GenericEntity<List<Product>> entity = new GenericEntity<>(store.getAll()) {};
            return Response.ok(entity).build();
        }
        List<Product> filtered = getProduct(name);
        if(filtered.isEmpty()){
            return Response.status(Response.Status.BAD_REQUEST).entity("Please provide a valid name").build();
        }
        GenericEntity<List<Product>> entity = new GenericEntity<>(filtered) {};
        return Response.ok(entity).build();
    }

    private List<Product> getProduct(String name) {
        List<Product> temp = new ArrayList<>();
        for(Product p : store.getAll()){
            if(p.getName().equals(name)){
                temp.add(p);
            }
        }
        return temp;
    }

    @DELETE
    @Path("{id}")
    public Response deleteProduct(@PathParam("id") int id){
        Product product = this.getProduct(id);
        if(product == null){
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        store.delete(product);
        return Response.noContent().build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response createProduct(@FormParam("name") String name){
        int id = this.generateProductId();
        Product product = new Product(id, name);
        store.add(product);
        String url = uriInfo.getAbsolutePath() + "/" + product.getId();
        URI uri = URI.create(url);
        return Response.created(uri).build();

    }

    private int generateProductId() {
        return store.getAll().size() + 1;
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("{id}")
    public Response updateProductName(Product product){
        Product productExisting = store.get(product.getId());
        if(productExisting == null){
            return Response.status(Response.Status.NOT_FOUND).entity("Please provide a valid id").build();
        }
        productExisting.setName(product.getName());
        return Response.noContent().build();

    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/buy/{id}")
    public Response buyProduct(Product product){
        Product productExisting = store.get(product.getId());
        if(productExisting == null){
            return Response.status(Response.Status.NOT_FOUND).entity("Please provide a valid id").build();
        }
        productExisting.setSold(true);
        return Response.noContent().build();

    }
}
