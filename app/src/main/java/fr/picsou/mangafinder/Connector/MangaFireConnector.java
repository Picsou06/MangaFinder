package fr.picsou.mangafinder.Connector;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fr.picsou.mangafinder.downloader.BookClass;

public class MangaFireConnector {
    private String API_BASE_URL;
    private final Context context;
    private final ExecutorService executorService;

    public MangaFireConnector(Context context) {
        this.context = context;
        this.executorService = Executors.newFixedThreadPool(4);
        updateApiBaseUrl();
    }

    private void updateApiBaseUrl() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
        String serverUrl = sharedPreferences.getString("server_url", "default_url");
        String portString = sharedPreferences.getString("server_port", "3000");
        int port = portString.isEmpty() ? 3000 : Integer.parseInt(portString);
        API_BASE_URL = String.format("http://%s:%d/", serverUrl, port);
    }

    public void MangaFire_getChapters(String mangaId, String language, String cover, String MangaName, GetChaptersCallback callback) {
        updateApiBaseUrl();
        executorService.execute(new GetChaptersTask(mangaId, language, cover, MangaName, callback));
    }

    private class GetChaptersTask implements Runnable {
        private final String mangaId;
        private final String language;
        private final String cover;
        private final String MangaName;
        private final GetChaptersCallback callback;

        public GetChaptersTask(String mangaId, String language, String cover, String MangaName, GetChaptersCallback callback) {
            this.mangaId = mangaId;
            this.language = language;
            this.cover = cover;
            this.MangaName = MangaName;
            this.callback = callback;
        }

        @Override
        public void run() {
            List<Chapter> chapters = new ArrayList<>();
            try {
                String id = mangaId.substring(mangaId.lastIndexOf('.') + 1);
                URL url = new URL(API_BASE_URL + "chapter/" + id + "?language=" + language);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                reader.close();

                JSONArray chaptersArray = new JSONArray(stringBuilder.toString());

                for (int i = 0; i < chaptersArray.length(); i++) {
                    JSONObject chapterObject = chaptersArray.getJSONObject(i);
                    String chapterId = chapterObject.getString("itemid");
                    String title = chapterObject.getString("title");
                    Chapter chapter = new Chapter(chapterId, "chapter", title, language, cover, MangaName);
                    chapters.add(chapter);
                }
            } catch (Exception e) {
                Log.e("GetChaptersTask", "Error fetching chapters", e);
            }
            if (callback != null) {
                callback.onChaptersLoaded(chapters);
            }
        }
    }

    public interface GetChaptersCallback {
        void onChaptersLoaded(List<Chapter> chapters);
    }

    public void MangaFire_getPages(String chapterId, GetPagesCallback callback) {
        updateApiBaseUrl();
        executorService.execute(new GetPagesTask(chapterId, callback));
    }

    private class GetPagesTask implements Runnable {
        private final String chapterId;
        private final GetPagesCallback callback;

        public GetPagesTask(String chapterId, GetPagesCallback callback) {
            this.chapterId = chapterId;
            this.callback = callback;
        }

        @Override
        public void run() {
            List<String> pages = new ArrayList<>();
            try {
                URL url = new URL(API_BASE_URL + "page/" + chapterId);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                reader.close();

                JSONArray jsonArray = new JSONArray(stringBuilder.toString());

                for (int i = 0; i < jsonArray.length(); i++) {
                    String pageUrl = jsonArray.getString(i);
                    pages.add(pageUrl);
                }
            } catch (Exception e) {
                Log.e("GetPagesTask", "Error fetching pages", e);
            }
            if (callback != null) {
                callback.onPagesLoaded(pages);
            }
        }
    }

    public interface GetPagesCallback {
        void onPagesLoaded(List<String> pages);
    }

    public static class Chapter {
        private String id;
        private String type;
        private String title;
        private String language;
        private String imageURL;
        private String mangaName;
        private boolean isDownloaded;

        public Chapter(String id, String type, String title, String language, String imageURL, String mangaName) {
            this.id = id;
            this.type = type;
            this.title = title;
            this.language = language;
            this.imageURL = imageURL;
            this.mangaName = mangaName;
            this.isDownloaded = false;
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
            return mangaName;
        }

        public boolean isDownloaded() {
            return isDownloaded;
        }

        public void setDownloaded(boolean downloaded) {
            isDownloaded = downloaded;
        }
    }
}