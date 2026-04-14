package vn.io.huangnosimp.model;

public class Art extends Item {

    private final String artist;

    private final int creationYear;

    public Art(String ownerId, String name, String description, String artist, int creationYear) {
        super(ownerId, name, description);
        this.artist = artist;
        this.creationYear = creationYear;
    }


    @Override
    public void printInfo() {
        System.out.println("=== Art Item ===");
        System.out.println("Name          : " + getName());
        System.out.println("Description   : " + getDescription());
        System.out.println("Artist        : " + artist);
        System.out.println("Creation Year : " + creationYear);
    }


    public String getArtist() {
        return artist;
    }

    public int getCreationYear() {
        return creationYear;
    }

    @Override
    public String toString() {
        return "Art{name='" + getName() + "', artist='" + artist + "', year=" + creationYear + "}";
    }
}
