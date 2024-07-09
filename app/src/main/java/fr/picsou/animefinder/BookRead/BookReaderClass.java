package fr.picsou.animefinder.BookRead;


public class BookReaderClass {
    private String title;
    private String imageCover;
    private String NumberOfPages;

    public BookReaderClass(String title, String imageCover, String NumberOfPages) {
        this.title = title;
        this.imageCover = imageCover;
        this.NumberOfPages = NumberOfPages;
    }

    public String getTitle() {
        return title;
    }

    public String getImageCover() {
        return imageCover;
    }

    public String getNumberOfPages() {
        return  NumberOfPages;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setImageCover(String imageCover) {
        this.imageCover = imageCover;
    }

    public void setNumberOfPages(String NumberOfPages) {
        this.NumberOfPages = NumberOfPages;
    }
}