package fr.picsou.animefinder.BookSearch;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface BookDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertBooks(List<BookClass> books);

    @Query("SELECT * FROM books WHERE title LIKE '%' || :searchText || '%' AND language=:language")
    List<BookClass> searchBooks(String searchText, String language);

    @Query("SELECT * FROM books WHERE language=:language")
    List<BookClass> getAllBooks(String language);

    @Query("DELETE FROM books")
    void DeleteAllBook();

    @Query("SELECT count(id) AS NBOFBOOKS FROM books")
    long CountValue();

    @Query("SELECT * FROM books WHERE language=:language LIMIT :limit OFFSET :offset")
    List<BookClass> getBooks(int limit, int offset, String language);

    @Query("SELECT * FROM books ORDER BY place DESC LIMIT 1")
    BookClass getLastInsertedBook();
}
