package fr.picsou.mangafinder.downloader;

import android.annotation.SuppressLint;
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
    private MangaFireConnector.Chapter chapter;
    private List<String> pageUrls;
    private DownloadCallback callback;
    private File basedir;

    public DownloadJob(MangaFireConnector.Chapter chapter, List<String> pageUrls, DownloadCallback callback, File basedir) {
        this.chapter = chapter;
        this.pageUrls = pageUrls;
        this.callback = callback;
        this.basedir = basedir;
    }

    public void downloadPages() {
        if (pageUrls == null || pageUrls.isEmpty()) {
            callback.onDownloadFailed("No pages to download");
            return;
        }

        new DownloadPageTask().execute();
    }

    @SuppressLint("StaticFieldLeak")
    private class DownloadPageTask extends AsyncTask<Void, Integer, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                // Create directory for the manga
                File mangaDir = new File(basedir, "MangaFinder/" + chapter.getLanguage() + "-" + chapter.getMangaName());
                if (!mangaDir.exists()) {
                    if (!mangaDir.mkdirs()) {
                        return false; // Failed to create directory
                    }
                }

                // Download cover image
                downloadCoverImage(chapter.getImageURL(), mangaDir);

                // Create CBZ archive
                return createCBZArchive(pageUrls, mangaDir, chapter.getTitle());
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                callback.onDownloadCompleted();
            } else {
                callback.onDownloadFailed("Failed to download or create CBZ file");
            }
        }

        private void downloadCoverImage(String coverUrl, File mangaDir) throws Exception {
            if (coverUrl == null || coverUrl.isEmpty()) return;

            URL url = new URL(coverUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            File coverFile = new File(mangaDir, "cover.jpg");
            try (FileOutputStream outputStream = new FileOutputStream(coverFile);
                 InputStream inputStream = connection.getInputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
        }

        private boolean createCBZArchive(List<String> pages, File mangaDir, String chapterName) throws Exception {
            File cbzFile = new File(mangaDir, chapterName + ".cbz");
            try (FileOutputStream fos = new FileOutputStream(cbzFile);
                 ZipOutputStream zos = new ZipOutputStream(fos)) {

                byte[] buffer = new byte[1024];

                for (int i = 0; i < pages.size(); i++) {
                    String pageUrl = pages.get(i);
                    URL url = new URL(pageUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.connect();

                    try (InputStream in = connection.getInputStream()) {
                        zos.putNextEntry(new ZipEntry("page_" + (i + 1) + ".jpg"));

                        int len;
                        while ((len = in.read(buffer)) > 0) {
                            zos.write(buffer, 0, len);
                        }

                        zos.closeEntry();
                    }
                }

                return true;
            }
        }
    }

    public interface DownloadCallback {
        void onDownloadCompleted();
        void onDownloadFailed(String message);
    }
}
