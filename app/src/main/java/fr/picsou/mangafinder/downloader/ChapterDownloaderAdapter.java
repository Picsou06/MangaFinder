package fr.picsou.mangafinder.downloader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

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

    public ChapterDownloaderAdapter(Context context, List<MangaFireConnector.Chapter> chapters, OnChapterClickListener listener) {
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
        holder.itemView.setOnClickListener(v -> listener.onChapterClick(chapter));
        holder.actionbutton.setImageResource(R.drawable.ic_download_black);

        if (chapter.isDownloaded()) {
            holder.actionbutton.setVisibility(View.GONE);
            holder.progressBar.setVisibility(View.GONE);
        } else {
            holder.actionbutton.setVisibility(View.VISIBLE);
            holder.progressBar.setVisibility(View.GONE);
            holder.actionbutton.setOnClickListener(v -> listener.onDownloadClick(chapter, chapter.getMangaName()));
        }
    }

    @Override
    public int getItemCount() {
        return chapters.size();
    }

    public void updateChapterState(MangaFireConnector.Chapter chapter) {
        int position = chapters.indexOf(chapter);
        if (position != -1) {
            notifyItemChanged(position);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView chapterName;
        public ImageButton actionbutton;
        public ProgressBar progressBar;

        public ViewHolder(View itemView) {
            super(itemView);
            chapterName = itemView.findViewById(R.id.chapter_name);
            actionbutton = itemView.findViewById(R.id.action_button);
            progressBar = itemView.findViewById(R.id.progress_bar);
        }
    }
}
