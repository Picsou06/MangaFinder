package fr.picsou.mangafinder;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;

import fr.picsou.mangafinder.downloader.DownloaderFragment;
import fr.picsou.mangafinder.reader.MangaReaderListFragment;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createMangaFinderFolder();

        viewPager = findViewById(R.id.viewPager);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        FragmentStateAdapter pagerAdapter = new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                switch (position) {
                    case 0:
                        return new DownloaderFragment();
                    case 1:
                        return new MangaReaderListFragment();
                    default:
                        return null;
                }
            }

            @Override
            public int getItemCount() {
                return 2;
            }
        };

        viewPager.setAdapter(pagerAdapter);

        // Écouteur pour la sélection d'items dans BottomNavigationView
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.FinderFragment:
                        viewPager.setCurrentItem(0); // Sélectionne le fragment FinderFragment
                        return true;
                    case R.id.ReaderFragment:
                        viewPager.setCurrentItem(1); // Sélectionne le fragment ReaderFragment
                        return true;
                    default:
                        return false;
                }
            }
        });

        // Écouteur pour le changement de page dans ViewPager2
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        bottomNavigationView.setSelectedItemId(R.id.FinderFragment); // Sélectionne l'item FinderFragment
                        break;
                    case 1:
                        bottomNavigationView.setSelectedItemId(R.id.ReaderFragment); // Sélectionne l'item ReaderFragment
                        break;
                }
            }
        });
    }

    private void createMangaFinderFolder() {
        File MangaFinderDir = new File(getFilesDir(), "MangaFinder");
        if (!MangaFinderDir.exists()) {
            if (MangaFinderDir.mkdir()) {
                Toast.makeText(getBaseContext(), "Dossier MangaFinder créé avec succès.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getBaseContext(), "Échec de la création du dossier MangaFinder.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public File getBaseFile() {
        return getFilesDir();
    }

    // Méthode pour démarrer SettingsActivity et attendre un résultat
    public void openSettingsActivity() {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivityForResult(intent, 100);
    }

    // Gestion des résultats de SettingsActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK) {
            Fragment currentFragment = getSupportFragmentManager().findFragmentByTag("DownloaderFragment");
            if (currentFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .remove(currentFragment)
                        .replace(R.id.viewPager, new DownloaderFragment(), "DownloaderFragment")
                        .commit();
            }
        }
    }
}
