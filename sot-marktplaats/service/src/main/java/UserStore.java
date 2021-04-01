import java.util.ArrayList;
import java.util.List;

public class UserStore {
    private static UserStore instance = new UserStore();

    private final List<User> users = new ArrayList<User>();
    private UserStore(){
        System.out.println("new UserStore()");
        users.add(new User(1, "admin", "admin", "ADMIN"));
    }
    public static UserStore singleton(){
        return instance;
    }
    public void delete(User user){
        users.remove(user);
    }
    public void add(User user){
        users.add(user);
    }
    public List<User> getAll(){
        return users;
    }
    public User get(int id){
        for (User user : users){
            if(user.getId() == id){
                return user;
            }
        }
        //TODO do not return null
        return null;
    }
    public void delete(int id){
        for (User user : users){
            if(user.getId() == id){
                users.remove(user);
            }
        }
    }

}
