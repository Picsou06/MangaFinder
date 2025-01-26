package fr.picsou.mangafinder.Read.Chapters;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "chapters")
public class ChapterClass {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String title;
    private String path;
    private int BookId;
    private Boolean readed;
    private int page;

    public ChapterClass(String title, String path, int BookId, Boolean readed, int page) {
        this.title = title;
        this.path = path;
        this.BookId = BookId;
        this.readed = readed;
        this.page = page;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getBookId() {
        return BookId;
    }

    public void setBookId(int BookId) {
        this.BookId = BookId;
    }

    public Boolean getReaded() {
        return readed;
    }

    public void setReaded(Boolean readed) {
        this.readed = readed;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }
}