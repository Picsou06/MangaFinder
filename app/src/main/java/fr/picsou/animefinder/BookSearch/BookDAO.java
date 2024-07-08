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

    @Query("SELECT * FROM books WHERE title LIKE '%'+:searchText+'%'")
    List<BookClass> searchBooks(String searchText);

    @Query("SELECT * FROM books")
    List<BookClass> getAllBooks();

    @Query("DELETE FROM books;")
    void DeleteAllBook();

    @Query("SELECT count(id) AS NBOFBOOKS FROM books")
    long CountValue();
}
