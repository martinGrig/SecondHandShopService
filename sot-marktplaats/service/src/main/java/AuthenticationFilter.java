import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.lang.reflect.Method;
import java.util.*;

public class AuthenticationFilter implements ContainerRequestFilter {
    /**
     - resourceInfo contains information about the requested operation (GET,
     PUT, POST …).
     - resourceInfo will be assigned/set automatically by the Jersey
     framework, you do not need to assign/set it.
     */
    @Context
    private ResourceInfo resourceInfo;
    private UserStore store;


    // requestContext contains information about the HTTP request message
    @Override
    public void filter(ContainerRequestContext requestContext) {
        // here you will perform AUTHENTICATION and AUTHORIZATION
        /* if you want to abort this HTTP request, you do this:

      Response response = Response.status(Response.Status.UNAUTHORIZED).build();
      requestContext.abortWith(response);

        */


        final String AUTHORIZATION_PROPERTY = "Authorization";
        final String AUTHENTICATION_SCHEME = "Basic";
        //Get request headers
        final MultivaluedMap<String, String> headers = requestContext.getHeaders();

        //Fetch authorization header
        final List<String> authorization = headers.get(AUTHORIZATION_PROPERTY);

        //If no authorization information present: abort with UNAUTHORIZED and stop
        if (authorization == null || authorization.isEmpty()) {
            Response response = Response.status(Response.Status.UNAUTHORIZED).
                    entity("Missing username and/or password.").build();
            requestContext.abortWith(response);
            return;
        }

        //Get encoded username and password
        final String encodedCredentials = authorization.get(0).replaceFirst(AUTHENTICATION_SCHEME + " ", "");

        //Decode username and password into one string
        String credentials = new String(Base64.getDecoder().decode(encodedCredentials.getBytes()));

        //Split username and password tokens in credentials
        final StringTokenizer tokenizer = new StringTokenizer(credentials, ":");
        final String username = tokenizer.nextToken();
        final String password = tokenizer.nextToken();

        //Check if username and password are valid (e.g., database)
        //If not valid: abort with UNAUTHORIED and stop
        if (!isValidUser(username, password)) {
            Response response = Response.status(Response.Status.UNAUTHORIZED).
                    entity("Invalid username and/or password.").build();
            requestContext.abortWith(response);
            return;
        }
        /* Get information about the service method which is being called. This information includes the annotated/permitted roles. */
        Method method = resourceInfo.getResourceMethod();
        // if access is allowed for all -> do not check anything further : access is approved for all
        if (method.isAnnotationPresent(PermitAll.class)) {
            return;
        }

        // if access is denied for all: deny access
        if (method.isAnnotationPresent(DenyAll.class)) {
            Response response = Response.status(Response.Status.FORBIDDEN).build();
            requestContext.abortWith(response);
            return;
        }
        if (method.isAnnotationPresent(RolesAllowed.class)) {
            // get allowed roles for this method
            RolesAllowed rolesAnnotation = method.getAnnotation(RolesAllowed.class);
            Set<String> rolesSet = new HashSet<String>(Arrays.asList(rolesAnnotation.value()));

            /* isUserAllowed : implement this method to check if this user has any of
                       the roles in the rolesSet
            if not isUserAllowed abort the requestContext with FORBIDDEN response*/
            if (!isUserAllowed(username, password, rolesSet)) {
                Response response = Response.status(Response.Status.FORBIDDEN).build();
                requestContext.abortWith(response);
                return;
            }
        }
    }

    private boolean isUserAllowed(String username, String password, Set<String> rolesSet) {
        if(username != null && password != null && rolesSet.contains("ADMIN")){
            return true;
        }
        else{
            return false;
        }
//        for(User user : store.getAll()){
//            if(username.equals(user.getUsername()) && password.equals(user.getPassword()) && rolesSet.contains("ADMIN")){
//                return true;
//            }
//        }
//        return false;
    }

    private boolean isValidUser(String username, String password) {
        if(username != null && password != null){
            return true;
        }
        else{
            return false;
        }
    }
}
