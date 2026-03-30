package vn.io.huangnosimp.model;

public class Art extends Item {

    private String artist;

    private int creationYear;

    public Art() {
        super();
    }

    public Art(String name, String description, double startingPrice,
               String artist, int creationYear) {
        super(name, description, startingPrice);
        this.artist = artist;
        this.creationYear = creationYear;
    }


    @Override
    public void printInfo() {
        System.out.println("=== Art Item ===");
        System.out.println("Name          : " + getName());
        System.out.println("Description   : " + getDescription());
        System.out.println("Starting Price: $" + getStartingPrice());
        System.out.println("Artist        : " + artist);
        System.out.println("Creation Year : " + creationYear);
    }


    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public int getCreationYear() {
        return creationYear;
    }

    public void setCreationYear(int creationYear) {
        this.creationYear = creationYear;
    }

    @Override
    public String toString() {
        return "Art{name='" + getName() + "', artist='" + artist + "', year=" + creationYear + "}";
    }
}
