package fr.picsou.animefinder;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.Objects;

import fr.picsou.animefinder.BookRead.BookReaderAdapter;
import fr.picsou.animefinder.BookRead.BookReaderClass;

public class ChapitreSelectorFragment extends Fragment implements BookReaderAdapter.OnBookClickListener {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chapitre_selector, container, false);

        // Initialisation du Toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        activity.setSupportActionBar(toolbar);
        Objects.requireNonNull(activity.getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setTitle("");

        toolbar.setNavigationOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        ImageView imageViewCover = view.findViewById(R.id.image_cover);

        Bundle args = getArguments();
        if (args != null) {
            String coverUrl = args.getString("cover", "");
            String animeName = args.getString("animeName", "");

            Glide.with(this)
                    .load(coverUrl)
                    .into(imageViewCover);

            toolbar.setTitle(animeName);
        }

        RecyclerView recyclerView = view.findViewById(R.id.list_chapters);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        BookReaderAdapter bookAdapter = new BookReaderAdapter(getContext(), null);
        recyclerView.setAdapter(bookAdapter);
        bookAdapter.setOnBookClickListener(this); // Définition du listener ici

        return view;
    }

    @Override
    public void onBookClick(BookReaderClass book) {
        System.out.println("HELPER, " + book.getTitle());
    }
}
