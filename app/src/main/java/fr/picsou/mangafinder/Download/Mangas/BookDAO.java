package fr.picsou.mangafinder.Download.Mangas;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import fr.picsou.mangafinder.Read.Mangas.BookReaderClass;

@Dao
public interface BookDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertBooks(List<BookClass> books);

    @Query("SELECT * FROM books " +
            "WHERE title LIKE :searchText || '%' " +
            "AND language IN (:languages) " +
            "ORDER BY LENGTH(title) - LENGTH(REPLACE(title, :searchText, '')) ASC, title " +
            "LIMIT 50")
    List<BookClass> searchBooks(String searchText, List<String> languages);


    @Query("SELECT * FROM books ORDER BY title")
    List<BookClass> getAllBooks();

    @Query("SELECT * FROM books WHERE id = :id")
    BookReaderClass getBookById(String id);

    @Query("DELETE FROM books")
    void DeleteAllBook();

    @Query("SELECT count(id) AS NBOFBOOKS FROM books")
    long CountValue();

    @Query("SELECT * FROM books WHERE language IN (:languages) ORDER BY title LIMIT :limit OFFSET :offset")
    List<BookClass> getBooks(int limit, int offset, List<String> languages);

    @Query("SELECT imageUrl FROM books WHERE title LIKE :searchText || '%' " + "AND LENGTH(:searchText) > (LENGTH(title) / 2) ORDER BY title LIMIT 1")
    String getPicture(String searchText);
}
