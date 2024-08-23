package fr.picsou.mangafinder.downloader;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import fr.picsou.mangafinder.Connector.MangaFireConnector;
import fr.picsou.mangafinder.R;
import fr.picsou.mangafinder.reader.MangaViewer;

public class ChapitreDownloaderActivity extends AppCompatActivity implements ChapterDownloaderAdapter.OnChapterClickListener {
    private ChapterDownloaderAdapter adapter;
    private List<MangaFireConnector.Chapter> mangaChapters;
    private String language;
    private RecyclerView recyclerView;
    private MangaFireConnector mangaFireConnector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapitre_selector);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        ImageView imageViewCover = findViewById(R.id.image_cover);

        Bundle args = getIntent().getExtras();
        if (args != null) {
            String coverUrl = args.getString("cover", "");
            String MangaName = args.getString("MangaName", "");
            String id = args.getString("id", "");
            language = args.getString("language", "en");
            System.out.println("HELPER,  information when chapter open: " + coverUrl + " " + MangaName + " " + id + " " + language);

            if (coverUrl != null && !coverUrl.isEmpty()) {
                Glide.with(this)
                        .load(coverUrl)
                        .into(imageViewCover);
            }

            toolbar.setTitle(MangaName);

            mangaChapters = new ArrayList<>();

            recyclerView = findViewById(R.id.list_chapters);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            adapter = new ChapterDownloaderAdapter(this, mangaChapters, this);
            recyclerView.setAdapter(adapter);

            mangaFireConnector = new MangaFireConnector(this);
            loadChapters(id, coverUrl, MangaName);
        }
    }

    private void loadChapters(String mangaId, String cover, String mangaName) {
        mangaFireConnector.MangaFire_getChapters(mangaId, language, cover, mangaName, new MangaFireConnector.GetChaptersCallback() {
            @Override
            public void onChaptersLoaded(List<MangaFireConnector.Chapter> loadedChapters) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mangaChapters.clear();
                        for (MangaFireConnector.Chapter chapter : loadedChapters) {
                            File file = new File(getFilesDir(), "MangaFinder/" + language + "-" + chapter.getMangaName() + "/" + chapter.getTitle() + ".cbz");
                            chapter.setDownloaded(file.exists());
                            mangaChapters.add(chapter);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    @Override
    public void onChapterClick(MangaFireConnector.Chapter chapter) {
        if (chapter.isDownloaded()) {
            String chapterName = chapter.getTitle();
            File file = new File(getFilesDir(), "MangaFinder/" + language + "-" + chapter.getMangaName() + "/" + chapter.getTitle() + ".cbz");
            Intent intent = new Intent(ChapitreDownloaderActivity.this, MangaViewer.class);
            intent.putExtra("MANGA_NAME", chapterName);
            intent.putExtra("CBZ_FILE_PATH", file.getAbsolutePath());
            startActivity(intent);
        }
    }

    @Override
    public void onDownloadClick(MangaFireConnector.Chapter chapter, String mangaTitle) {
        int position = mangaChapters.indexOf(chapter);
        if (position != -1) {
            View view = recyclerView.findViewHolderForAdapterPosition(position).itemView;
            ImageButton downloadButton = view.findViewById(R.id.action_button);
            ProgressBar progressBar = view.findViewById(R.id.progress_bar);

            downloadButton.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);

            mangaFireConnector.MangaFire_getPages(chapter.getId(), new MangaFireConnector.GetPagesCallback() {
                @Override
                public void onPagesLoaded(List<String> pages) {
                    if (pages.isEmpty()) {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            downloadButton.setVisibility(View.VISIBLE);
                            Toast.makeText(ChapitreDownloaderActivity.this, "No pages found", Toast.LENGTH_SHORT).show();
                        });
                        return;
                    }

                    DownloadJob downloadJob = new DownloadJob(chapter, pages, new DownloadJob.DownloadCallback() {
                        @Override
                        public void onDownloadCompleted() {
                            runOnUiThread(() -> {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(ChapitreDownloaderActivity.this, "Download completed", Toast.LENGTH_SHORT).show();
                                chapter.setDownloaded(true);
                                adapter.updateChapterState(chapter);
                            });
                        }

                        @Override
                        public void onDownloadFailed(String message) {
                            runOnUiThread(() -> {
                                progressBar.setVisibility(View.GONE);
                                downloadButton.setVisibility(View.VISIBLE);
                                Toast.makeText(ChapitreDownloaderActivity.this, "Download failed: " + message, Toast.LENGTH_SHORT).show();
                            });
                        }
                    }, getFilesDir());

                    downloadJob.downloadPages();
                }
            });
        }
    }
}