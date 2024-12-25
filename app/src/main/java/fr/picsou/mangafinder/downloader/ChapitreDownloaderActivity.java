// ChapitreDownloaderActivity.java
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import fr.picsou.mangafinder.Connector.APIConnector;
import fr.picsou.mangafinder.R;
import fr.picsou.mangafinder.reader.MangaViewer;

public class ChapitreDownloaderActivity extends AppCompatActivity implements ChapterDownloaderAdapter.OnChapterClickListener {
    private ChapterDownloaderAdapter adapter;
    private List<APIConnector.Chapter> mangaChapters;
    private String mangaID;
    private String language;
    private RecyclerView recyclerView;
    private APIConnector APIConnector;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapitre_selector);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        mangaID = getIntent().getStringExtra("mangaId");

        ImageView imageViewCover = findViewById(R.id.image_cover);

        Bundle args = getIntent().getExtras();
        if (args != null) {
            String coverUrl = args.getString("cover", "");
            String MangaName = args.getString("MangaName", "");
            String id = args.getString("id", "");
            language = args.getString("language", "en");

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

            APIConnector = new APIConnector(this);
            loadChapters(id, coverUrl, MangaName);
        }

        // Initialize SwipeRefreshLayout
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Reload chapters
                Bundle args = getIntent().getExtras();
                if (args != null) {
                    String id = args.getString("id", "");
                    String coverUrl = args.getString("cover", "");
                    String MangaName = args.getString("MangaName", "");
                    loadChapters(id, coverUrl, MangaName);
                }
            }
        });
    }

    private void loadChapters(String mangaId, String cover, String mangaName) {
        APIConnector.API_getChapters(mangaId, language, cover, mangaName, new APIConnector.GetChaptersCallback() {
            @Override
            public void onChaptersLoaded(List<APIConnector.Chapter> loadedChapters) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mangaChapters.clear();
                        mangaChapters.addAll(loadedChapters);
                        adapter.notifyDataSetChanged();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });
    }

    @Override
    public void onChapterClick(APIConnector.Chapter chapter) {
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
    public void onDownloadClick(APIConnector.Chapter chapter, String mangaTitle) {
        int position = mangaChapters.indexOf(chapter);
        if (position != -1) {
            View view = recyclerView.findViewHolderForAdapterPosition(position).itemView;
            ImageButton downloadButton = view.findViewById(R.id.action_button);
            ProgressBar progressBar = view.findViewById(R.id.progress_bar);

            downloadButton.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);

            APIConnector.API_getPages(chapter.getId(), mangaID, new APIConnector.GetPagesCallback() {
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