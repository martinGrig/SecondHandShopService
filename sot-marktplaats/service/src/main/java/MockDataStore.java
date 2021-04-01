import java.util.ArrayList;
import java.util.List;

public class MockDataStore {
    private static MockDataStore instance = new MockDataStore();

    private final List<Product> products = new ArrayList<Product>();
    private MockDataStore(){
        System.out.println("new MockDataStore()");
        products.add(new Product(1, "Shelf"));
        products.add(new Product(2, "Table"));
        products.add(new Product(3, "TV"));
        products.add(new Product(4, "Laptop"));
    }
    public static MockDataStore singleton(){
        return instance;
    }
    public void add(Product product){
        products.add(product);
    }
    public void delete(Product product){
        products.remove(product);
    }
    public List<Product> getAll(){
        return products;
    }
    public Product get(int id){
        for (Product product : products){
            if(product.getId() == id){
                return product;
            }
        }
        //TODO do not return null
        return null;
    }
    public void delete(int id){
        for (Product product : products){
            if(product.getId() == id){
                products.remove(product);
            }
        }
    }
}
