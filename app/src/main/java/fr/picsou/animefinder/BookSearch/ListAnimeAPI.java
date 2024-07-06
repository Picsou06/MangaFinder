package fr.picsou.animefinder.BookSearch;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.List;

public class ListAnimeAPI {

    private static final String API_BASE_URL = "http://Picsou06.fun:3000/manga/mangadex/a";

    private Context mContext;
    private List<BookClass> mBookList;
    private RecyclerView.Adapter mAdapter;

    public ListAnimeAPI(Context context, RecyclerView.Adapter adapter, List<BookClass> bookList) {
        mContext = context;
        mAdapter = adapter;
        mBookList = bookList;
    }

    public void fetchAnimeList() {
        System.out.println("HELPER, Fetching anime list from: " + API_BASE_URL);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, API_BASE_URL, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println("HELPER, Response received: " + response.toString());
                        try {
                            JSONArray animeArray = response.getJSONArray("results");
                            for (int i = 0; i < animeArray.length(); i++) {
                                JSONObject animeObject = animeArray.getJSONObject(i);
                                String id = animeObject.getString("id");
                                String title = animeObject.getString("title");
                                String imageUrl = animeObject.optString("image", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSSLt36mzLsH1TvlwQfHM3ZeTFVodb0yTc-iJkvl5yp1AZnHNkn");

                                // Créer un objet BookClass et l'ajouter à mBookList
                                BookClass book = new BookClass(id, title, imageUrl);
                                mBookList.add(book);
                                System.out.println("HELPER, Book added: " + book.toString());
                            }

                            // Notify the adapter of data changes
                            mAdapter.notifyDataSetChanged();
                            System.out.println("HELPER, Adapter notified with new data.");

                        } catch (JSONException e) {
                            System.out.println("HELPER, JSON parsing error: " + e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("HELPER, Error fetching anime list: " + error.getMessage());
                    }
                });

        // Ajouter la requête à la file d'attente de Volley
        RequestQueue queue = Volley.newRequestQueue(mContext);
        queue.add(jsonObjectRequest);
    }
}
