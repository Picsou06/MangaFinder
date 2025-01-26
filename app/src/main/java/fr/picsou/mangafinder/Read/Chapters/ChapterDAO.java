package fr.picsou.mangafinder.Read.Chapters;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ChapterDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertChapters(List<ChapterClass> chapters);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertChapter(ChapterClass chapter);

    @Query("SELECT * FROM chapters")
    List<ChapterClass> getAllChapters();

    @Query("SELECT * FROM chapters WHERE id = :id")
    ChapterClass getChapterById(int id);

    @Query("SELECT * FROM chapters WHERE BookId = :bookId")
    List<ChapterClass> getAllChaptersOfManga(String bookId);

    @Query("Delete FROM chapters WHERE BookId = :bookId")
    void deleteChapters(String bookId);

    @Query("Delete FROM chapters WHERE id = :id")
    void deleteChapter(int id);
}
