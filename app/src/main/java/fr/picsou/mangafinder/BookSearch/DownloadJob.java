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

public class DownloadJob {
    private static final int CHUNK_SIZE = 8388608; // 8 MB
    private static final String BASE_URL = "https://mangafire.to";
    private static final String CHAPTER_ENDPOINT = "/ajax/read";

    private MangaFireConnector.Chapter chapter;
    private String language;
    private DownloadCallback callback;
    private String mangaTitle;
    private File basedir;
    private String imageURL;  // Ajouter imageURL

    public DownloadJob(MangaFireConnector.Chapter chapter, DownloadCallback callback, File basedir) {
        this.chapter = chapter;
        this.language = chapter.getLanguage();
        this.callback = callback;
        this.mangaTitle = chapter.getMangaName();
        this.basedir = basedir;
        this.imageURL = chapter.getImageURL();
    }

    public void downloadPages() {
        // Utiliser MangaFireConnector pour obtenir la liste des pages
        MangaFireConnector.MangaFire_getPages(chapter.getId(), new MangaFireConnector.GetPagesCallback() {
            @Override
            public void onPagesLoaded(List<String> pages) {
                if (pages == null || pages.isEmpty()) {
                    callback.onDownloadFailed("Empty page list");
                } else {
                    new DownloadPageTask().execute(pages.toArray(new String[0]));
                }
            }
        });
    }

    private class DownloadPageTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... urls) {
            try {
                File mangaDir = new File(basedir, "MangaFinder/" + mangaTitle);
                if (!mangaDir.exists()) {
                    mangaDir.mkdirs();
                }

                downloadCoverImage(imageURL, mangaDir);

                createCBZArchive(urls, mangaDir, chapter.getTitle());

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

            File coverFile = new File(mangaDir, "cover.jpg");
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
