package fr.picsou.animefinder.BookSearch;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

import fr.picsou.animefinder.FinderFragment;

public class ListAnimeAPI {
    private static final String API_BASE_URL = "http://Picsou06.fun:3001/";
    private final Context mContext;
    private RecyclerView.Adapter mainAdapter;
    private RecyclerView.Adapter searchAdapter;

    public ListAnimeAPI(Context context, RecyclerView.Adapter mainAdapter, RecyclerView.Adapter searchAdapter) {
        mContext = context;
        this.mainAdapter = mainAdapter;
        this.searchAdapter = searchAdapter;
    }

    public void fetchAnimeListFromAPI() {
        String apiUrl = API_BASE_URL + "listmanga/";

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, apiUrl, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        AsyncTask.execute(() -> {
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
                                // Insertion des livres dans la base de données
                                BookLocalDatabase.getDatabase(mContext).bookDao().DeleteAllBook();
                                BookLocalDatabase.getDatabase(mContext).bookDao().insertBooks(tempBookList);

                                // Notification à l'adaptateur après l'insertion des données
                                Activity activity = (Activity) mContext;
                                activity.runOnUiThread(() -> {
                                    mainAdapter.notifyDataSetChanged();
                                    Toast.makeText(mContext, "Données chargées avec succès", Toast.LENGTH_SHORT).show();
                                });

                            } catch (JSONException e) {
                                e.printStackTrace();
                                Activity activity = (Activity) mContext;
                                activity.runOnUiThread(() -> {
                                    Toast.makeText(mContext, "Erreur de parsing JSON : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                            }
                        });
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(mContext, "Erreur lors du téléchargement : " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        RequestQueue queue = Volley.newRequestQueue(mContext);
        queue.add(jsonArrayRequest);
    }

    public void searchBooksFromDatabase(String searchText, List language) {
        new Thread(() -> {
            List<BookClass> bookList = BookLocalDatabase.getDatabase(mContext).bookDao().searchBooks(searchText, language);

            Activity activity = (Activity) mContext;
            activity.runOnUiThread(() -> {
                ((BookSearchAdapter) searchAdapter).clearBooks();
                ((BookSearchAdapter) searchAdapter).updateBooks(bookList);
            });
        }).start();
    }

    public void fetchBooksFromDatabase(int limit, int offset, RecyclerView.Adapter adapter, List language) {
        new Thread(() -> {
            List<BookClass> bookList = BookLocalDatabase.getDatabase(mContext).bookDao().getBooks(limit, offset, language);

            // Mise à jour de l'interface utilisateur sur le thread principal
            Activity activity = (Activity) mContext;
            activity.runOnUiThread(() -> {
                if (adapter instanceof BookDownloaderAdapter) {
                    ((BookDownloaderAdapter) adapter).updateBooks(bookList);
                } else if (adapter instanceof BookSearchAdapter) {
                    ((BookSearchAdapter) adapter).updateBooks(bookList);
                }
                adapter.notifyDataSetChanged();
            });
        }).start();
    }

    public void updateDatabase(long numberOfValue) {
        String apiUrl = API_BASE_URL + "isupdated/" + numberOfValue;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, apiUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    boolean isUpToDate = response.getBoolean("updated");
                    if (!isUpToDate) {
                        fetchAnimeListFromAPI();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(mContext, "HELPER JSON parsing error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(mContext, "HELPER Volley error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        RequestQueue queue = Volley.newRequestQueue(mContext);
        queue.add(jsonObjectRequest);
    }
}

