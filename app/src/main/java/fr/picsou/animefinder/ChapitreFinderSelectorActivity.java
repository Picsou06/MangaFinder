package fr.picsou.animefinder;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import fr.picsou.animefinder.BookRead.ChapterReaderAdapter;
import fr.picsou.animefinder.BookSearch.ChapterDownloaderAdapter;
import fr.picsou.animefinder.Connector.MangaFireConnector;

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

            loadChapters(id);
        }
    }

    private void loadChapters(String mangaId) {
        MangaFireConnector.MangaFire_getChapters(mangaId, new MangaFireConnector.GetChaptersCallback() {
            @Override
            public void onChaptersLoaded(List<MangaFireConnector.Chapter> loadedChapters) {
                mangaChapters.clear();
                mangaChapters.addAll(loadedChapters);
                adapter.notifyDataSetChanged();
            }
        }, language);
    }

    @Override
    public void onChapterClick(MangaFireConnector.Chapter chapter) {
    }

    @Override
    public void onDownloadClick(MangaFireConnector.Chapter chapter) {
        System.out.println("HELPER, "+chapter.getId()+ " " + chapter.getTitle());
        MangaFireConnector.MangaFire_getPages(chapter.getId(), new MangaFireConnector.GetPagesCallback() {
            @Override
            public void onPagesLoaded(List<String> pages) {
                System.out.println("HELPER,"+pages.toString());
                System.out.println("HELPER,"+pages.get(0).toString());
            }
        });
    }
}
