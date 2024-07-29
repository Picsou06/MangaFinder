package fr.picsou.mangafinder;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import fr.picsou.mangafinder.BookSearch.ChapterDownloaderAdapter;
import fr.picsou.mangafinder.BookSearch.DownloadJob;
import fr.picsou.mangafinder.Connector.MangaFireConnector;

public class ChapitreFinderSelectorActivity extends AppCompatActivity implements ChapterDownloaderAdapter.OnChapterClickListener {

    private ChapterDownloaderAdapter adapter;
    private List<MangaFireConnector.Chapter> mangaChapters;
    private String language;

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

            RecyclerView recyclerView = findViewById(R.id.list_chapters);
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
                mangaChapters.addAll(loadedChapters);
                adapter.notifyDataSetChanged();
            }
        }, language, mangaName, imageURL);
    }

    @Override
    public void onChapterClick(MangaFireConnector.Chapter chapter) {
        System.out.println("HELPER, "+chapter.getId()+ " " + chapter.getTitle());
    }

    @Override
    public void onDownloadClick(MangaFireConnector.Chapter chapter, String mangaTitle) {
        DownloadJob downloadJob = new DownloadJob(chapter, new DownloadJob.DownloadCallback() {
            @Override
            public void onDownloadCompleted() {
                runOnUiThread(() -> {
                    Toast.makeText(ChapitreFinderSelectorActivity.this, "Download completed", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onDownloadFailed(String message) {
                runOnUiThread(() -> {
                    Toast.makeText(ChapitreFinderSelectorActivity.this, "Download failed: " + message, Toast.LENGTH_SHORT).show();
                });
            }
        },getFilesDir());

        downloadJob.downloadPages();
    }

}
