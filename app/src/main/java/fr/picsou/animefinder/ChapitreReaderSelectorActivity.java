package fr.picsou.animefinder;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
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

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        ImageView deleteButton = new ImageView(this);
        Toolbar.LayoutParams lp = new Toolbar.LayoutParams(Toolbar.LayoutParams.WRAP_CONTENT, Toolbar.LayoutParams.WRAP_CONTENT);
        lp.gravity = (android.view.Gravity.END | android.view.Gravity.CENTER_VERTICAL);
        deleteButton.setLayoutParams(lp);
        deleteButton.setImageResource(R.drawable.ic_delete_black_24dp);
        deleteButton.setOnClickListener(v -> deleteAnime(v));

        ImageView imageViewCover = findViewById(R.id.image_cover);

        Bundle args = getIntent().getExtras();
        if (args != null) {
            String coverUrl = args.getString("cover", "");
            String animeName = args.getString("animeName", "");

            if (coverUrl != null && !coverUrl.isEmpty()) {
                Glide.with(this)
                        .load(coverUrl)
                        .into(imageViewCover);
            }

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
            if (adapter.getItemCount()==0){
                boolean deleted = deleteAnimeFolder();
                if (deleted) {
                    DownloadedListFragment.refreshBookList();
                    finish();
                }
            }
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }
    }

    public void deleteAnime(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmation");
        builder.setMessage("Voulez-vous vraiment supprimer cet anime ?");
        builder.setPositiveButton("Oui", (dialog, which) -> {
            boolean deleted = deleteAnimeFolder();
            if (deleted) {
                DownloadedListFragment.refreshBookList();
                finish();
            }
        });
        builder.setNegativeButton("Non", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private boolean deleteAnimeFolder() {
        String animeName = Objects.requireNonNull(getSupportActionBar()).getTitle().toString();
        File animeDir = new File(getFilesDir(), "AnimeFinder" + File.separator + animeName);
        if (animeDir.exists() && animeDir.isDirectory()) {
            File[] files = animeDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (!file.delete()) {
                        return false; // Si la suppression d'un fichier échoue
                    }
                }
            }
            return animeDir.delete(); // Supprime le dossier de l'anime
        }
        return false; // Si le dossier n'existe pas ou n'est pas un répertoire
    }
}