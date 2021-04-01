public class Product {
    private int id;
    private String name;
    private boolean sold = false;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSold() {
        return sold;
    }

    public Product() {
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", sold=" + sold +
                '}';
    }

    public void setSold(boolean sold) {
        this.sold = sold;
    }

    public Product(int id, String name) {
        this.id = id;
        this.name = name;
        sold = false;
    }
}
