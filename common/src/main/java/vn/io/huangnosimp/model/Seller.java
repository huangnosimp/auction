package vn.io.huangnosimp.model;

import java.util.ArrayList;
import java.util.List;

public class Seller extends User {

    private List<Item> postedItems;

    public Seller() {
        super();
        this.postedItems = new ArrayList<>();
    }

    public Seller(String username, String password, String email) {
        super(username, password, email);
        this.postedItems = new ArrayList<>();
    }


    public List<Item> getPostedItems() {
        return postedItems;
    }

    public void setPostedItems(List<Item> postedItems) {
        this.postedItems = postedItems;
    }

    public void addItem(Item item) {
        this.postedItems.add(item);
    }

    public void removeItem(Item item) {
        this.postedItems.remove(item);
    }

    @Override
    public String toString() {
        return "Seller{username='" + getUsername() + "', postedItems=" + postedItems.size() + "}";
    }
}
