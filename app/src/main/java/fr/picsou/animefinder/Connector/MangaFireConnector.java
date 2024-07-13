package fr.picsou.animefinder.Connector;
import android.os.AsyncTask;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class MangaFireConnector {
    private static final String BASE_URL = "https://mangafire.to";
    private static final String CHAPTER_ENDPOINT = "/ajax/read";
    private static final String ID_REGEX = "manga/[^.]+\\.(\\w+)";
    private static final String REQUEST_OPTIONS = "";

    // Function to get chapters
    public static void MangaFire_getChapters(String mangaId, GetChaptersCallback callback, String language) {
        new GetChaptersTask(mangaId, callback, language).execute();
    }

    // Function to get pages
    public static void MangaFire_getPages(String chapterId, GetPagesCallback callback) {
        new GetPagesTask(chapterId, callback).execute();
    }

    // AsyncTask to get chapters
    private static class GetChaptersTask extends AsyncTask<Void, Void, List<Chapter>> {
        private String mangaId;
        private GetChaptersCallback callback;
        private String language;

        public GetChaptersTask(String mangaId, GetChaptersCallback callback, String language) {
            this.mangaId = mangaId;
            this.callback = callback;
            this.language = language;
        }

        @Override
        protected List<Chapter> doInBackground(Void... voids) {
            List<Chapter> chapterList = new ArrayList<>();
            try {
                String id = mangaId.replaceAll(ID_REGEX, "$1");
                URL mangauri = new URL(BASE_URL + "/manga" + id);
                HttpURLConnection connection = (HttpURLConnection) mangauri.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                reader.close();

                URL uri = new URL(BASE_URL + CHAPTER_ENDPOINT + id + "/chapter/" + language);
                HttpURLConnection chapterConnection = (HttpURLConnection) uri.openConnection();
                chapterConnection.setRequestMethod("GET");
                chapterConnection.connect();

                BufferedReader chapterReader = new BufferedReader(new InputStreamReader(chapterConnection.getInputStream()));
                StringBuilder chapterStringBuilder = new StringBuilder();
                String chapterLine;
                while ((chapterLine = chapterReader.readLine()) != null) {
                    chapterStringBuilder.append(chapterLine);
                }
                chapterReader.close();

                JSONObject jsonObject = new JSONObject(chapterStringBuilder.toString());
                Document chapterDom = Jsoup.parse(jsonObject.getJSONObject("result").getString("html"));
                Elements chaptersNodes = chapterDom.select("a");

                for (Element chapter : chaptersNodes) {
                    if (chapter.attr("href").contains("/" + "chapter" + "-")) {
                        String itemid = chapter.attr("data-id");
                        String title = chapter.text().trim();
                        Chapter chapterObject = new Chapter(itemid, "chapter", title, language);
                        chapterList.add(chapterObject);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return chapterList;
        }

        @Override
        protected void onPostExecute(List<Chapter> chapters) {
            if (callback != null) {
                callback.onChaptersLoaded(chapters);
            }
        }
    }

    // AsyncTask to get pages
    private static class GetPagesTask extends AsyncTask<Void, Void, List<String>> {
        private String chapterId;
        private GetPagesCallback callback;

        public GetPagesTask(String chapterId, GetPagesCallback callback) {
            this.chapterId = chapterId;
            this.callback = callback;
        }

        @Override
        protected List<String> doInBackground(Void... voids) {
            List<String> pages = new ArrayList<>();
            try {
                JSONObject chapterJson = new JSONObject(chapterId);
                String itemType = chapterJson.getString("itemtype");
                String itemId = chapterJson.getString("itemid");

                URL uri = new URL(BASE_URL + CHAPTER_ENDPOINT + itemType + "/" + itemId);
                HttpURLConnection connection = (HttpURLConnection) uri.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                reader.close();

                JSONObject jsonObject = new JSONObject(stringBuilder.toString());
                JSONArray imagesArray = jsonObject.getJSONObject("result").getJSONArray("images");
                for (int i = 0; i < imagesArray.length(); i++) {
                    JSONArray imageArray = imagesArray.getJSONArray(i);
                    if (imageArray.getInt(2) < 1) {
                        pages.add(imageArray.getString(0));
                    } else {
                        pages.add(createConnectorURI(imageArray));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return pages;
        }

        @Override
        protected void onPostExecute(List<String> pages) {
            if (callback != null) {
                callback.onPagesLoaded(pages);
            }
        }

        private String createConnectorURI(JSONArray imageArray) {
            // Implement the logic to create the URI from imageArray
            return "";
        }
    }

    // Callback interface for chapters
    public interface GetChaptersCallback {
        void onChaptersLoaded(List<Chapter> chapters);
    }

    // Callback interface for pages
    public interface GetPagesCallback {
        void onPagesLoaded(List<String> pages);
    }

    // Chapter model
    public static class Chapter {
        private String id;
        private String type;
        private String title;
        private String language;

        public Chapter(String id, String type, String title, String language) {
            this.id = id;
            this.type = type;
            this.title = title;
            this.language = language;
        }

        public String getId() {
            return id;
        }

        public String getType() {
            return type;
        }

        public String getTitle() {
            return title;
        }

        public String getLanguage() {
            return language;
        }
    }
}