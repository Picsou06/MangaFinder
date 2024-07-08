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
    private RecyclerView.Adapter mAdapter;

    public ListAnimeAPI(Context context, RecyclerView.Adapter adapter) {
        mContext = context;
        mAdapter = adapter;
    }

    public void fetchAnimeListFromAPI() {
        System.out.println("HELPER, UpdateDatabase");
        BookLocalDatabase.getDatabase(mContext).bookDao().DeleteAllBook();
        String apiUrl = API_BASE_URL + "listmanga/";

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                (Request.Method.GET, apiUrl, null, new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            List<BookClass> tempBookList = new ArrayList<>();

                            for (int i = 0; i < response.length(); i++) {
                                JSONObject manga = response.getJSONObject(i);
                                String id = manga.getString("id");
                                String title = manga.getString("title");
                                String imageUrl = manga.getString("picture");
                                String website = manga.getString("website");

                                BookClass book = new BookClass(id, title, imageUrl, website);
                                System.out.println("HELPER New Book: "+title);
                                tempBookList.add(book);
                            }
                            BookLocalDatabase.getDatabase(mContext).bookDao().insertBooks(tempBookList);

                            // Fetch the updated data from the database and update the adapter
                            fetchBooksFromDatabase();

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

        RequestQueue queue = Volley.newRequestQueue(mContext);
        queue.add(jsonArrayRequest);
    }

    public void searchBooksFromDatabase(String searchText) {
        new Thread(() -> {
            List<BookClass> bookList = BookLocalDatabase.getDatabase(mContext).bookDao().searchBooks(searchText);
            ((BookSearchAdapter) mAdapter).updateBooks(bookList);
        }).start();
    }


    public void fetchBooksFromDatabase() {
        new Thread(() -> {
            List<BookClass> bookList = BookLocalDatabase.getDatabase(mContext).bookDao().getAllBooks();
            ((BookSearchAdapter) mAdapter).updateBooks(bookList);
        }).start();
    }

    public void updateDatabase(long numberOfValue) {
        System.out.println("HELPER, UpdateDatabase: "+numberOfValue);
        String apiUrl = API_BASE_URL + "isupdated/" + numberOfValue;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, apiUrl, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            boolean isUpToDate = response.getBoolean("updated");
                            System.out.println("HELPER, need to be updated? : "+isUpToDate);
                            if (!isUpToDate) {
                                fetchAnimeListFromAPI();
                            }

                        } catch (JSONException e) {
                            Toast.makeText(mContext, "JSON parsing error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(mContext, "Error fetching update status: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        RequestQueue queue = Volley.newRequestQueue(mContext);
        queue.add(jsonObjectRequest);
    }
}
