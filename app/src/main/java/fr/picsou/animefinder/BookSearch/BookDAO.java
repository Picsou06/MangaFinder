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

    @Query("SELECT * FROM books WHERE title LIKE '%' || :searchText || '%' AND language IN (:languages) ORDER BY title")
    List<BookClass> searchBooks(String searchText, List<String> languages);

    @Query("SELECT * FROM books WHERE language IN (:languages) ORDER BY title")
    List<BookClass> getAllBooks(List<String> languages);

    @Query("DELETE FROM books")
    void DeleteAllBook();

    @Query("SELECT count(id) AS NBOFBOOKS FROM books")
    long CountValue();

    @Query("SELECT * FROM books WHERE language IN (:languages) ORDER BY title LIMIT :limit OFFSET :offset")
    List<BookClass> getBooks(int limit, int offset, List<String> languages);
}
