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
import fr.picsou.mangafinder.reader.ChapitreReaderListActivity;
import fr.picsou.mangafinder.reader.MangaViewer;

public class ChapitreDownloaderActivity extends AppCompatActivity implements ChapterDownloaderAdapter.OnChapterClickListener {
    private ChapterDownloaderAdapter adapter;
    private List<MangaFireConnector.Chapter> mangaChapters;
    private String language;
    private RecyclerView recyclerView;

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
            String animeName = args.getString("animeName", "");
            String id = args.getString("id", "");
            language = args.getString("language", "en");

            if (coverUrl != null && !coverUrl.isEmpty()) {
                Glide.with(this)
                        .load(coverUrl)
                        .into(imageViewCover);
            }

            toolbar.setTitle(animeName);

            mangaChapters = new ArrayList<>();

            recyclerView = findViewById(R.id.list_chapters);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            adapter = new ChapterDownloaderAdapter(this, mangaChapters, this);
            recyclerView.setAdapter(adapter);

            loadChapters(id, animeName, coverUrl);
        }
    }
    private void loadChapters(String mangaId, String mangaName, String imageURL) {
        MangaFireConnector.MangaFire_getChapters(mangaId, new MangaFireConnector.GetChaptersCallback() {
            @Override
            public void onChaptersLoaded(List<MangaFireConnector.Chapter> loadedChapters) {
                mangaChapters.clear();
                for (MangaFireConnector.Chapter chapter : loadedChapters) {
                    File file = new File(getFilesDir(), "MangaFinder/" + language + "-" + chapter.getMangaName() + "/" + chapter.getTitle() + ".cbz");
                    chapter.setDownloaded(file.exists());
                    mangaChapters.add(chapter);
                }
                adapter.notifyDataSetChanged();
            }
        }, language, mangaName, imageURL);
    }


    @Override
    public void onChapterClick(MangaFireConnector.Chapter chapter) {
        if (chapter.isDownloaded())
        {
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

            DownloadJob downloadJob = new DownloadJob(chapter, new DownloadJob.DownloadCallback() {
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
    }
}