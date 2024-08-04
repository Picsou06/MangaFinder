package fr.picsou.mangafinder.reader;


public class BookReaderClass {
    private String title;
    private String imageCover;
    private String NumberOfPages;
    private String language;

    public BookReaderClass(String title, String imageCover, String NumberOfPages, String language) {
        this.title = title;
        this.imageCover = imageCover;
        this.NumberOfPages = NumberOfPages;
        this.language = language;
    }

    public String getTitle() {
        return title;
    }

    public String getImageCover() {
        if (imageCover == null) {
            return null;
        }
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

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

}