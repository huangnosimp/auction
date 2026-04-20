package vn.io.huangnosimp.model;

public class Art extends Item {

    private final String artist;

    private final int creationYear;

    public Art(String ownerId, String name, String description, String artist, int creationYear) {
        super(ownerId, name, description);
        this.artist = artist;
        this.creationYear = creationYear;
    }

    public String getArtist() {
        return artist;
    }

    public int getCreationYear() {
        return creationYear;
    }

}
