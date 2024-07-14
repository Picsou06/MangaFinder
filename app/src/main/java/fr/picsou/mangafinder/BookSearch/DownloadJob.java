package fr.picsou.mangafinder.BookSearch;

import android.os.AsyncTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import fr.picsou.mangafinder.Connector.MangaFireConnector;
import fr.picsou.mangafinder.MainActivity;

public class DownloadJob {
    private static final int CHUNK_SIZE = 8388608; // 8 MB
    private static final String BASE_URL = "https://mangafire.to";
    private static final String CHAPTER_ENDPOINT = "/ajax/read";

    private MangaFireConnector.Chapter chapter;
    private String language;
    private DownloadCallback callback;
    private String mangaTitle;
    private File basedir;

    public DownloadJob(MangaFireConnector.Chapter chapter, String language, DownloadCallback callback, String mangaTitle, File basedir) {
        this.chapter = chapter;
        this.language = language;
        this.callback = callback;
        this.mangaTitle = mangaTitle;
        this.basedir = basedir;
    }

    public void downloadPages() {
        new DownloadPagesTask().execute();
    }

    private class DownloadPagesTask extends AsyncTask<Void, Void, List<String>> {
        @Override
        protected List<String> doInBackground(Void... voids) {
            List<String> pages = null;
            try {
                String chapterId = chapter.getId();
                String id = chapterId.replaceAll("manga/[^.]+\\.(\\w+)", "$1");

                URL uri = new URL(BASE_URL + CHAPTER_ENDPOINT + id + "/chapter/" + language);
                HttpURLConnection connection = (HttpURLConnection) uri.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                InputStream inputStream = connection.getInputStream();
                // TODO: Parse JSON or other format to extract page URLs
                // For demonstration, using simulated pages
                pages = List.of("https://example.com/page1.jpg", "https://example.com/page2.jpg", "https://example.com/page3.jpg");

            } catch (Exception e) {
                e.printStackTrace();
            }
            return pages;
        }

        @Override
        protected void onPostExecute(List<String> pages) {
            if (pages == null || pages.isEmpty()) {
                callback.onDownloadFailed("Empty page list");
            } else {
                new DownloadPageTask().execute(pages.toArray(new String[0]));
            }
        }
    }

    private class DownloadPageTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... urls) {
            try {
                File mangaDir = new File(basedir, "MangaFinder/" + mangaTitle);
                if (!mangaDir.exists()) {
                    mangaDir.mkdirs();
                }

                // Download cover image (using the first page as cover for demonstration)
                downloadCoverImage(urls[0], mangaDir);

                // Create CBZ archive for chapter
                createCBZArchive(urls, mangaDir, "nomduchapitre");

                callback.onDownloadCompleted();

            } catch (Exception e) {
                e.printStackTrace();
                callback.onDownloadFailed("Error downloading pages: " + e.getMessage());
            }
            return null;
        }

        private void downloadCoverImage(String coverUrl, File mangaDir) throws Exception {
            URL url = new URL(coverUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            File coverFile = new File(mangaDir, "cover.png");
            FileOutputStream outputStream = new FileOutputStream(coverFile);

            InputStream inputStream = connection.getInputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();
            inputStream.close();
        }

        private void createCBZArchive(String[] pages, File mangaDir, String chapterName) throws Exception {
            File cbzFile = new File(mangaDir, chapterName + ".cbz");
            FileOutputStream fos = new FileOutputStream(cbzFile);
            ZipOutputStream zos = new ZipOutputStream(fos);

            byte[] buffer = new byte[1024];

            for (int i = 0; i < pages.length; i++) {
                String pageUrl = pages[i];
                URL url = new URL(pageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                InputStream in = connection.getInputStream();
                zos.putNextEntry(new ZipEntry("page_" + (i + 1) + ".jpg"));

                int len;
                while ((len = in.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }

                zos.closeEntry();
                in.close();
                connection.disconnect();
            }

            zos.close();
        }
    }

    public interface DownloadCallback {
        void onDownloadCompleted();
        void onDownloadFailed(String message);
    }
}
