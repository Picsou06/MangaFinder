package fr.picsou.mangafinder.Connector;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MangaFireConnector {
    private static final String BASE_URL = "https://mangafire.to";
    private static final String CHAPTER_ENDPOINT = "/ajax/read";
    private static final String ID_REGEX = "manga/[^.]+\\.(\\w+)";
    private static final String REQUEST_OPTIONS = "";

    // Function to get chapters
    public static void MangaFire_getChapters(String mangaId, GetChaptersCallback callback, String language, String mangaName, String imageURL) {
        new GetChaptersTask(mangaId, callback, language, mangaName, imageURL).execute();
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
        private String mangaName;
        private String imageURL;

        public GetChaptersTask(String mangaId, GetChaptersCallback callback, String language, String mangaName, String imageURL) {
            this.mangaId = mangaId;
            this.callback = callback;
            this.language = language;
            this.mangaName = mangaName;
            this.imageURL = imageURL;
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
                System.out.println("HELPER,"+BASE_URL + CHAPTER_ENDPOINT + id + "/chapter/" + language);
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
                        Chapter chapterObject = new Chapter(itemid, "chapter", title, language, imageURL, mangaName);
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
            System.out.println("HELPER,"+chapterId);
            try {
                URL chapterUrl = new URL(BASE_URL + CHAPTER_ENDPOINT + chapterId + "/chapter/en");
                HttpURLConnection connection = (HttpURLConnection) chapterUrl.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                reader.close();

                Document document = Jsoup.parse(stringBuilder.toString());
                Elements pageElements = document.select("img[data-src]");

                for (Element page : pageElements) {
                    String imageUrl = page.attr("data-src");
                    pages.add(imageUrl);
                }
            } catch (Exception e) {
                Log.e("GetPagesTask", "Error fetching pages", e);
            }
            return pages;
        }

        @Override
        protected void onPostExecute(List<String> pages) {
            if (callback != null) {
                callback.onPagesLoaded(pages);
            }
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
        private String imageURL;
        private String MangaName;

        public Chapter(String id, String type, String title, String language, String imageURL, String MangaName) {
            this.id = id;
            this.type = type;
            this.title = title;
            this.language = language;
            this.imageURL = imageURL;
            this.MangaName = MangaName;
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
        public String getImageURL() {
            return imageURL;
        }
        public String getMangaName() {
            return MangaName;
        }
    }
}
