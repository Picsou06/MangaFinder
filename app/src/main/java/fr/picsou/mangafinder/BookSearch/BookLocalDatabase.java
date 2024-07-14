package fr.picsou.mangafinder.BookSearch;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {BookClass.class}, version = 2, exportSchema = false)
public abstract class BookLocalDatabase extends RoomDatabase {

    public abstract BookDAO bookDao();

    private static volatile BookLocalDatabase INSTANCE;

    public static BookLocalDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (BookLocalDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    BookLocalDatabase.class, "book_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    // Ajouter une méthode pour réinitialiser la base de données si nécessaire
    public static void resetDatabase(Context context) {
        if (INSTANCE != null) {
            // Fermer l'instance actuelle de la base de données
            INSTANCE.close();
            INSTANCE = null;

            // Supprimer la base de données actuelle
            context.deleteDatabase("book_database");

            // Recréer l'instance de la base de données
            INSTANCE = getDatabase(context);
        }
    }
}
