package fr.picsou.animefinder.BookRead;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

import fr.picsou.animefinder.R;

public class ChapterAdapter extends RecyclerView.Adapter<ChapterAdapter.ViewHolder> {
    private final List<File> chapters;
    private final Context context;
    private final OnChapterClickListener listener;

    public interface OnChapterClickListener {
        void onChapterClick(File chapter);
        void onDeleteClick(File chapter);
    }

    public ChapterAdapter(Context context, List<File> chapters, OnChapterClickListener listener) {
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
        File chapter = chapters.get(position);
        holder.chapterName.setText(chapter.getName());

        holder.itemView.setOnClickListener(v -> listener.onChapterClick(chapter));
        holder.deleteButton.setOnClickListener(v -> listener.onDeleteClick(chapter));
    }

    @Override
    public int getItemCount() {
        return chapters.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView chapterName;
        public ImageButton deleteButton;

        public ViewHolder(View itemView) {
            super(itemView);
            chapterName = itemView.findViewById(R.id.chapter_name);
            deleteButton = itemView.findViewById(R.id.button_delete);
        }
    }
}
