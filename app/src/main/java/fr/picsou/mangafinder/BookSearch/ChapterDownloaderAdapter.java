package fr.picsou.mangafinder.BookSearch;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import fr.picsou.mangafinder.ChapitreFinderSelectorActivity;
import fr.picsou.mangafinder.Connector.MangaFireConnector;
import fr.picsou.mangafinder.R;

public class ChapterDownloaderAdapter extends RecyclerView.Adapter<ChapterDownloaderAdapter.ViewHolder> {
    private final List<MangaFireConnector.Chapter> chapters;
    private final Context context;
    private final OnChapterClickListener listener;

    public interface OnChapterClickListener {
        void onChapterClick(MangaFireConnector.Chapter chapter);
        void onDownloadClick(MangaFireConnector.Chapter chapter, String mangaTitle);
    }

    public ChapterDownloaderAdapter(Context context, List<MangaFireConnector.Chapter> chapters, ChapitreFinderSelectorActivity listener) {
        this.context = context;
        this.chapters = chapters;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chapter, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MangaFireConnector.Chapter chapter = chapters.get(position);
        holder.chapterName.setText(chapter.getTitle());

        holder.actionbutton.setImageResource(R.drawable.ic_download_black);
        holder.itemView.setOnClickListener(v -> listener.onChapterClick(chapter));
        holder.actionbutton.setOnClickListener(v -> listener.onDownloadClick(chapter, holder.chapterName.getText().toString()));
    }

    @Override
    public int getItemCount() {
        return chapters.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView chapterName;
        public ImageButton actionbutton;

        public ViewHolder(View itemView) {
            super(itemView);
            chapterName = itemView.findViewById(R.id.chapter_name);
            actionbutton = itemView.findViewById(R.id.action_button);
        }
    }
}
