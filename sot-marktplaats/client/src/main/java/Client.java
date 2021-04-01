import org.glassfish.jersey.SslConfigurator;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.*;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Client {
    private WebTarget serviceTarget;

    private static File getFile(String fileName) throws URISyntaxException {
        ClassLoader classLoader = Client.class.getClassLoader();
        URL url = classLoader.getResource(fileName);
        if (url == null) {
            throw new RuntimeException("Cannot open file " + fileName);
        }
        return new File(url.toURI());
    }
    public Client(String username, String password, Set<String> roles){

        System.out.println(String.format("You are logged in as %s", username));

        System.out.println((String.format("%s", roles)));
        try{
            File keyStore = getFile("keystore_client");
            File trustStore = getFile("truststore_client");

            System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");

            SslConfigurator sslConfig = SslConfigurator.newInstance()
                    .keyStoreFile(keyStore.getAbsolutePath())
                    .keyStorePassword("asdfgh")
                    .trustStoreFile(trustStore.getAbsolutePath())
                    .trustStorePassword("asdfgh")
                    .keyPassword("asdfgh");

            final SSLContext sslContext = sslConfig.createSSLContext();

            ClientConfig config = new ClientConfig();
            config.register(HttpAuthenticationFeature.basic(username, password));
            javax.ws.rs.client.Client client = ClientBuilder.newBuilder().withConfig(config)
                    .sslContext(sslContext).build();
            URI baseURI = UriBuilder.fromUri("https://localhost:9090/store/products").build();
            serviceTarget = client.target(baseURI);
        }catch(URISyntaxException ex){
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public Client() {
        try {
            File keyStore = getFile("keystore_client");
            File trustStore = getFile("truststore_client");

            System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");

            SslConfigurator sslConfig = SslConfigurator.newInstance()
                    .keyStoreFile(keyStore.getAbsolutePath())
                    .keyStorePassword("asdfgh")
                    .trustStoreFile(trustStore.getAbsolutePath())
                    .trustStorePassword("asdfgh")
                    .keyPassword("asdfgh");

            final SSLContext sslContext = sslConfig.createSSLContext();

            ClientConfig config = new ClientConfig();
            config.register(HttpAuthenticationFeature.basic("admin", "admin"));
            javax.ws.rs.client.Client client = ClientBuilder.newBuilder().withConfig(config)
                    .sslContext(sslContext).build();
            URI baseURI = UriBuilder.fromUri("https://localhost:9090/store/").build();
            serviceTarget = client.target(baseURI);
        } catch (URISyntaxException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void hello(){
        Invocation.Builder requestBuilder = serviceTarget.path("hello").request().accept(MediaType.TEXT_PLAIN);
        Response response = requestBuilder.get();
        //Hello
        if (response.getStatus() == 200){
            String entity = response.readEntity(String.class);
            System.out.println("The resource responce is: " + entity);
        }
        else{
            System.err.println("ERROR: Cannot get hello! " + response);
            String entity = response.readEntity(String.class);
            System.err.println(entity);
        }
    }

    public void getAllProducts(){
        Invocation.Builder request= serviceTarget.path("all").request().accept(MediaType.APPLICATION_JSON);
        Response response = request.get();

        if (response.getStatus() == Response.Status.OK.getStatusCode()){
            GenericType<ArrayList<Product>> genericType = new GenericType<>(){};
            ArrayList<Product> entity = response.readEntity(genericType);
            for (Product product : entity) {
                System.out.println("\t" + product);
            }
        }
        else{
            System.err.println(request);
        }
    }

    public void getProductWithId(int id){
        //One student with path parameter
        Invocation.Builder requestBuilderPath = serviceTarget.path(String.format("%d", id)).request().accept(MediaType.APPLICATION_JSON);
        Response responsePath = requestBuilderPath.get();

        if (responsePath.getStatus() == Response.Status.OK.getStatusCode()){
            Product entity = responsePath.readEntity(Product.class);
            System.out.println("The resource responce is: " + entity);
        }
        else{
            System.err.println("ERROR: Cannot get one product with parameter! " + responsePath);
            String entity = responsePath.readEntity(String.class);
            System.err.println(entity);
        }
    }

    public void getProductByName(String name){
        //All student with query param name
        Invocation.Builder requestBuilderQuery= serviceTarget.queryParam("name", String.format("%s", name)).request().accept(MediaType.APPLICATION_JSON);
        Response responseAllQuery = requestBuilderQuery.get();

        if (responseAllQuery.getStatus() == Response.Status.OK.getStatusCode()){
            GenericType<ArrayList<Product>> genericType = new GenericType<>(){};
            ArrayList<Product> entity = responseAllQuery.readEntity(genericType);
            for (Product product : entity) {
                System.out.println("\t" + product);
            }
        }
        else{
            System.err.println(responseAllQuery);
        }
    }

    public void delete(int id){
        WebTarget resourceTarget = serviceTarget.path(String.format("%d", id));
        Invocation.Builder requestBuilderDelete = resourceTarget.request().accept(MediaType.TEXT_PLAIN);
        Response responseDelete = requestBuilderDelete.delete();
        if(responseDelete.getStatus() == Response.Status.NO_CONTENT.getStatusCode()){
            System.out.println(String.format("Deleted product with id %d successfully", id));
        } else{
            System.err.println(responseDelete);
        }
    }

    public void createProduct(String name){
        Form form = new Form();
        form.param("name", String.format("%s", name));
        Entity<Form> entity = Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED);
        Response response = serviceTarget.request().accept(MediaType.TEXT_PLAIN).post(entity);
        if(response.getStatus() == Response.Status.CREATED.getStatusCode()){
            String productURL = response.getHeaderString("Location");
            System.out.println(("POST product is created and it can be accessed at " + productURL));
        }
        else{
            System.err.println(response);
        }
    }

    public void updateProductName(int id, String name){
        WebTarget resourceTarget = serviceTarget.path(String.format("%d", id));
        Product product = new Product(id, name);
        Entity<Product> entity = Entity.entity(product, MediaType.APPLICATION_JSON);
        Response response = resourceTarget.request().accept(MediaType.TEXT_PLAIN).put(entity);
        if(response.getStatus() == Response.Status.NO_CONTENT.getStatusCode()){
            System.out.println(String.format("Product with id: %d was updated!", id));
        }
        else{
            System.err.println(response);
        }
    }

    public void buyProduct(int id){
        Invocation.Builder requestBuilderPath = serviceTarget.path(String.format("/%d", id)).request().accept(MediaType.APPLICATION_JSON);
        Response response = requestBuilderPath.get();
        Product oldProduct;
        if (response.getStatus() == Response.Status.OK.getStatusCode()){
            oldProduct = response.readEntity(Product.class);
            WebTarget resourceTarget = serviceTarget.path(String.format("/buy/%d", id));
            Entity<Product> entity = Entity.entity(oldProduct, MediaType.APPLICATION_JSON);
            response = resourceTarget.request().accept(MediaType.TEXT_PLAIN).put(entity);
            if(response.getStatus() == Response.Status.NO_CONTENT.getStatusCode()){
                System.out.println(String.format("Product with id: %d was sold!", id));
            }
            else{
                System.err.println(response);
            }
        }
        else{
            System.err.println("ERROR: Cannot get product with parameter! " + response);
            String entity = response.readEntity(String.class);
            System.err.println(entity);
        }
    }

    public static void showInitialMenu(){
        System.out.println("1: Login");
        System.out.println("2: Register a new user");
    }
    public static Client login(Scanner input){
        String username;
        String password;
        Set<String> roles = new HashSet<>();
        String answer;

        System.out.println("Log in:");
        System.out.println("username: ");
        username = input.next();

        System.out.println("password: ");
        password = input.next();
        Client client = null;
        if(username != null && password != null) {
            System.out.println(String.format("You are logged in as %s", username));
            client = new Client(username, password, roles);
            return client;
        }
        return client;
    }
    public void register(Scanner input){
        String username;
        String password;
        Set<String> roles = new HashSet<>();
        String answer;

        System.out.println("Register:");
        System.out.println("username: ");
        username = input.next();

        System.out.println("password: ");
        password = input.next();

        System.out.println("Do you want to be a seller: ");
        answer = input.next();
        String pattern = "^Yes|y|yes|YES|Y";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(answer);
        if (m.find( )) {
            roles.add("SELLER");
        }
        Form form = new Form();
        form.param("username", String.format("%s", username));
        form.param("password", String.format("%s", password));
        form.param("role", String.format("%s", roles));
        Entity<Form> entity = Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED);
        Response response = serviceTarget.request().accept(MediaType.TEXT_PLAIN).post(entity);
        if(response.getStatus() == Response.Status.CREATED.getStatusCode()){
            String productURL = response.getHeaderString("Location");
            System.out.println((String.format("User %s is created and it can be accessed at ", username) + productURL));
        }
        else{
            System.err.println(response);
        }
    }
    public void showMenu(){
        System.out.println("0: Exit the program");
        System.out.println("1: Show all products");
        System.out.println("2: Select a product(type its name)");
        System.out.println("3: Select a product(type its ID)");
        System.out.println("4: Delete Product with id(type its id)");
        System.out.println("5: Post a new product for sale");
        System.out.println("6: Update the name of a product. First type the id of the product you want to update, press enter and type the new name.");
        System.out.println("7: Buy product with id");
    }
    public void showItemMenu(int id){
        System.out.println("What would you like to do with this item");
        System.out.println("0: Go back");
        System.out.println("1: Delete");
        System.out.println("2: Update");
        System.out.println("2: Buy");
    }
    public void useMenu(Client client, Scanner input){
        int option;
        int id;
        String name;
        client.hello();
        client.showMenu();
        option = input.nextInt();
        while(option != 0){
            switch(option){
                case 1:
                    client.getAllProducts();
                    client.showMenu();
                    option = input.nextInt();
                    break;
                case 2:
                    name = input.next();
                    client.getProductByName(name);
                    client.showMenu();
                    option = input.nextInt();
                    break;
                case 3:
                    id = input.nextInt();
                    client.getProductWithId(id);
                    client.showMenu();
                    option = input.nextInt();
                    break;
                case 4:
                    id = input.nextInt();
                    client.delete(id);
                    client.showMenu();
                    option = input.nextInt();
                    break;
                case 5:
                    name = input.next();
                    client.createProduct(name);
                    client.showMenu();
                    option = input.nextInt();
                    break;
                case 6:
                    id = input.nextInt();
                    name = input.next();
                    client.updateProductName(id, name);
                    client.showMenu();
                    option = input.nextInt();
                    break;
                case 7:
                    id = input.nextInt();
                    client.buyProduct(id);
                    client.showMenu();
                    option = input.nextInt();
                    break;
                default:
                    break;
            }
        }
    }



    public static void main(String[] args){
        Scanner input = new Scanner(System.in);
        showInitialMenu();
        int option;
        option = input.nextInt();
        while(option != 0)
        switch(option){
            case 1:
                Client client = login(input);
                client.useMenu(client, input);
                break;
            case 2:
                Client emptyClient = new Client();
                emptyClient.register(input);
                showInitialMenu();
                option = input.nextInt();
            default:
                break;
        }

    }

}
