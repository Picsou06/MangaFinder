package fr.picsou.animefinder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = findViewById(R.id.viewPager);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Création de l'adapter pour les fragments
        FragmentStateAdapter pagerAdapter = new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                switch (position) {
                    case 0:
                        return new FinderFragment();
                    case 1:
                        return new ReaderFragment();
                    default:
                        return null;
                }
            }

            @Override
            public int getItemCount() {
                return 2; // Nombre de fragments
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
}
