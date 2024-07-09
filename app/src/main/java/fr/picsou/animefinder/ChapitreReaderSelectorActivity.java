package fr.picsou.animefinder;

import android.os.Bundle;
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

import fr.picsou.animefinder.BookRead.ChapterAdapter;

public class ChapitreReaderSelectorActivity extends AppCompatActivity implements ChapterAdapter.OnChapterClickListener {
    private ChapterAdapter adapter;
    private List<File> chapterFiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapitre_selector);

        // Initialisation du Toolbar
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

            Glide.with(this)
                    .load(coverUrl)
                    .into(imageViewCover);

            toolbar.setTitle(animeName);

            chapterFiles = getChapterFiles(animeName);

            RecyclerView recyclerView = findViewById(R.id.list_chapters);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            adapter = new ChapterAdapter(this, chapterFiles, this);
            recyclerView.setAdapter(adapter);
        }
    }

    private List<File> getChapterFiles(String animeName) {
        List<File> chapters = new ArrayList<>();
        File animeDir = new File(getFilesDir(), "AnimeFinder" + File.separator + animeName);
        if (animeDir.exists() && animeDir.isDirectory()) {
            File[] files = animeDir.listFiles((dir, name) -> name.endsWith(".cbz"));
            if (files != null) {
                for (File file : files) {
                    chapters.add(file);
                }
            }
        }
        return chapters;
    }

    @Override
    public void onChapterClick(File chapter) {
        String chapterName = chapter.getName();
        System.out.println("HELPER, " + chapterName);
    }

    @Override
    public void onDeleteClick(File chapter) {
        if (chapter.delete()) {
            chapterFiles.remove(chapter);
            adapter.notifyDataSetChanged();
        }
    }
}
