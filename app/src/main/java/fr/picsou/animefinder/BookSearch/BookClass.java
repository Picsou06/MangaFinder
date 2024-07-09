package fr.picsou.animefinder.BookSearch;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "books")
public class BookClass {
    @PrimaryKey(autoGenerate = true)
    private int place;
    private String website;
    private String id;
    private String title;
    private String imageUrl;
    private String language;

    public BookClass(String id, String title, String imageUrl, String website, String language) {
        this.id = id;
        this.title = title;
        this.imageUrl = imageUrl;
        this.website = website;
        this.language = language;
    }

    public int getPlace() {
        return place;
    }

    public void setPlace(int place) {
        this.place = place;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language){
        this.language=language;
    }
}