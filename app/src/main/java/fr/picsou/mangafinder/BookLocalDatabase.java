package fr.picsou.mangafinder;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import fr.picsou.mangafinder.Download.Mangas.BookClass;
import fr.picsou.mangafinder.Download.Mangas.BookDAO;
import fr.picsou.mangafinder.Read.Chapters.ChapterClass;
import fr.picsou.mangafinder.Read.Chapters.ChapterDAO;

@Database(entities = {BookClass.class, ChapterClass.class}, version = 3, exportSchema = false)
public abstract class BookLocalDatabase extends RoomDatabase {

    public abstract BookDAO bookDao();
    public abstract ChapterDAO chapterDao();

    private static volatile BookLocalDatabase INSTANCE;

    public static BookLocalDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (BookLocalDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    BookLocalDatabase.class, "MangaFinder")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public static void resetDatabase(Context context) {
        if (INSTANCE != null) {
            INSTANCE.close();
            INSTANCE = null;

            context.deleteDatabase("MangaFinder");

            INSTANCE = getDatabase(context);
        }
    }
}