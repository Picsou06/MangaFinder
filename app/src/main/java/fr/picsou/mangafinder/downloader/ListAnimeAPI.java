package fr.picsou.mangafinder.downloader;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ListAnimeAPI {
    private static final String API_BASE_URL = "http://%s:%d/";
    private final Context mContext;
    private final RecyclerView.Adapter mainAdapter;
    private final RecyclerView.Adapter searchAdapter;
    private final String ip;
    private final int port;
    private final RequestQueue requestQueue;
    private final Handler mainHandler;

    public ListAnimeAPI(Context context, RecyclerView.Adapter mainAdapter, RecyclerView.Adapter searchAdapter, String ip, int port) {
        this.mContext = context;
        this.mainAdapter = mainAdapter;
        this.searchAdapter = searchAdapter;
        this.ip = ip;
        this.port = port;
        this.requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void fetchAnimeListFromAPI() {
        String apiUrl = String.format(API_BASE_URL, ip, port) + "manga/listmanga/";

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, apiUrl, null,
                response -> {
                    List<BookClass> tempBookList = new ArrayList<>();
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject manga = response.getJSONObject(i);
                            String id = manga.getString("id");
                            String title = manga.getString("title");
                            String imageUrl = manga.getString("picture");
                            String website = manga.getString("website");
                            String language = manga.getString("language");

                            BookClass book = new BookClass(id, title, imageUrl, website, language);
                            tempBookList.add(book);
                        }

                        new Thread(() -> {
                            BookLocalDatabase.getDatabase(mContext).bookDao().DeleteAllBook();
                            BookLocalDatabase.getDatabase(mContext).bookDao().insertBooks(tempBookList);
                        }).start();

                        mainHandler.post(() -> {
                            mainAdapter.notifyDataSetChanged();
                            Toast.makeText(mContext, "Données chargées avec succès", Toast.LENGTH_SHORT).show();
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                        mainHandler.post(() -> {
                            Toast.makeText(mContext, "Erreur de parsing JSON: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }
                }, error -> {
            Toast.makeText(mContext, "Erreur lors du téléchargement: " + error.getMessage(), Toast.LENGTH_SHORT).show();
        });

        requestQueue.add(jsonArrayRequest);
    }

    public void searchBooksFromDatabase(String searchText, List<String> language) {
        new Thread(() -> {
            List<BookClass> bookList = BookLocalDatabase.getDatabase(mContext).bookDao().searchBooks(searchText, language);
            mainHandler.post(() -> {
                if (searchAdapter instanceof BookDownloaderAdapter) {
                    ((BookDownloaderAdapter) searchAdapter).clearBooks();
                    ((BookDownloaderAdapter) searchAdapter).updateBooks(bookList);
                }
            });
        }).start();
    }

    public void fetchBooksFromDatabase(int limit, int offset, RecyclerView.Adapter adapter, List<String> language) {
        new Thread(() -> {
            List<BookClass> bookList = BookLocalDatabase.getDatabase(mContext).bookDao().getBooks(limit, offset, language);
            mainHandler.post(() -> {
                if (adapter instanceof BookDownloaderAdapter) {
                    ((BookDownloaderAdapter) adapter).updateBooks(bookList);
                }
                adapter.notifyDataSetChanged();
            });
        }).start();
    }

    public void updateDatabase(long numberOfValue) {
        String apiUrl = String.format(API_BASE_URL, ip, port) + "manga/isupdated/" + numberOfValue;
        System.out.println("HELPER : " + apiUrl);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, apiUrl, null,
                response -> {
                    try {
                        boolean isUpToDate = response.getBoolean("updated");
                        if (!isUpToDate) {
                            fetchAnimeListFromAPI();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(mContext, "Erreur de parsing JSON: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }, error -> {
            Toast.makeText(mContext, "Erreur de mise à jour: " + error.getMessage(), Toast.LENGTH_SHORT).show();
        });

        requestQueue.add(jsonObjectRequest);
    }
}