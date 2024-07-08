package fr.picsou.animefinder.BookSearch;

import android.content.Context;
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

public class ListAnimeAPI {

    private static final String API_BASE_URL = "http://Picsou06.fun:3000/";

    private Context mContext;
    private List<BookClass> mBookList;
    private RecyclerView.Adapter mAdapter;

    public ListAnimeAPI(Context context, RecyclerView.Adapter adapter, List<BookClass> bookList) {
        mContext = context;
        mAdapter = adapter;
        mBookList = bookList;
    }

    public void purgeBookList() {
        mBookList.clear();
    }

    public List<BookClass> fetchAnimeListFromAPI() {
        purgeBookList();
        String apiUrl = API_BASE_URL + "listmanga/";

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                (Request.Method.GET, apiUrl, null, new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            List<BookClass> tempBookList = new ArrayList<>();

                            for (int i = 0; i < response.length(); i++) {
                                JSONObject manga = response.getJSONObject(i);
                                System.out.println("HELPER, Manga: " + manga.toString()+"  "+response.length());
                                String id = manga.getString("id");
                                String title = manga.getString("title");
                                String imageUrl = manga.getString("picture");
                                String website = manga.getString("website");

                                BookClass book = new BookClass(id, title, imageUrl, website);
                                tempBookList.add(book);
                                System.out.println("HELPER, Book added: " + book.toString());
                            }

                            mBookList.clear();
                            mBookList.addAll(tempBookList);

                            System.out.println("HELPER, Adapter notified with new data.");

                        } catch (JSONException e) {
                            Toast.makeText(mContext, "JSON parsing error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(mContext, "Error fetching anime list: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        // Ajouter la requête à la file d'attente de Volley
        RequestQueue queue = Volley.newRequestQueue(mContext);
        queue.add(jsonArrayRequest);
        return mBookList;
    }

    public void fetchAnimeTitleFromAPI(String title) {
        purgeBookList();
        String apiUrl = API_BASE_URL + "searchmanga/" + title;

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                (Request.Method.GET, apiUrl, null, new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        System.out.println("HELPER, chercher: "+response);
                        try {
                            List<BookClass> tempBookList = new ArrayList<>();

                            for (int i = 0; i < response.length(); i++) {
                                JSONObject manga = response.getJSONObject(i);
                                System.out.println("HELPER, Manga: " + manga.toString()+"  "+response.length());
                                String id = manga.getString("id");
                                String title = manga.getString("title");
                                String imageUrl = manga.getString("picture");
                                String website = manga.getString("website");

                                BookClass book = new BookClass(id, title, imageUrl, website);
                                tempBookList.add(book);
                                System.out.println("HELPER, Book added: " + book.toString());
                            }

                            mBookList.clear();
                            mBookList.addAll(tempBookList);

                            // Notify the adapter of data changes
                            mAdapter.notifyDataSetChanged();
                            System.out.println("HELPER, Adapter notified with new data.");

                        } catch (JSONException e) {
                            Toast.makeText(mContext, "JSON parsing error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(mContext, "Error fetching anime list: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        // Ajouter la requête à la file d'attente de Volley
        RequestQueue queue = Volley.newRequestQueue(mContext);
        queue.add(jsonArrayRequest);
    }

// Dans votre méthode isupdated de ListAnimeAPI.java

    public void isupdated(long count) {
        String apiUrl = API_BASE_URL + "isupdated/" + count;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, apiUrl, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            boolean updated = response.getBoolean("updated");

                            if (updated) {
                                // Faire quelque chose si mis à jour
                            } else {
                                fetchAnimeListFromAPI();
                            }

                        } catch (JSONException e) {
                            Toast.makeText(mContext, "JSON parsing error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(mContext, "Error checking update status: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        // Ajouter la requête à la file d'attente de Volley
        RequestQueue queue = Volley.newRequestQueue(mContext);
        queue.add(jsonObjectRequest);
    }

}
