package vn.io.huangnosimp.model;

import vn.io.huangnosimp.enums.ItemCondition;

public class Art extends Item {

    private final String artist;

    private final int creationYear;

    public Art(String ownerId, String name, String description, String artist, int creationYear, ItemCondition condition) {
        super(ownerId, name, description, condition);
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
