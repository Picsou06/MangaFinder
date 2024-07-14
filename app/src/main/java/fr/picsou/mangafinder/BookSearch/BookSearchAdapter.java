package fr.picsou.mangafinder.BookSearch;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import fr.picsou.mangafinder.R;

public class BookSearchAdapter extends RecyclerView.Adapter<BookSearchAdapter.BookViewHolder> {
    private final Context mContext;
    private final List<BookClass> mBookList;
    private OnBookClickListener mListener;

    public BookSearchAdapter(Context context, List<BookClass> bookList) {
        mContext = context;
        mBookList = bookList;
    }

    public void setOnBookClickListener(OnBookClickListener listener) {
        Log.d("BookSearchAdapter", "Listener Enabled");
        mListener = listener;
    }

    public void updateBooks(List<BookClass> newBookList) {
        mBookList.addAll(newBookList);
        notifyDataSetChanged();
    }

    public void clearBooks() {
        mBookList.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        int firstBookIndex = position * 2;
        int secondBookIndex = firstBookIndex + 1;

        if (firstBookIndex < mBookList.size()) {
            holder.bindFirstBook(mBookList.get(firstBookIndex));
        }

        if (secondBookIndex < mBookList.size()) {
            holder.bindSecondBook(mBookList.get(secondBookIndex));
            holder.book2Container.setVisibility(View.VISIBLE);
        } else {
            holder.hideSecondBook();
        }

        holder.book1Container.setOnClickListener(v -> {
            if (mListener != null && firstBookIndex < mBookList.size()) {
                Log.d("BookSearchAdapter", "First book clicked at position " + firstBookIndex);
                mListener.onBookClick(mBookList.get(firstBookIndex));
            }
        });

        holder.book2Container.setOnClickListener(v -> {
            if (mListener != null && secondBookIndex < mBookList.size()) {
                Log.d("BookSearchAdapter", "Second book clicked at position " + secondBookIndex);
                mListener.onBookClick(mBookList.get(secondBookIndex));
            }
        });
    }

    @Override
    public int getItemCount() {
        return (int) Math.ceil((double) mBookList.size() / 2);
    }

    public interface OnBookClickListener {
        void onBookClick(BookClass book);
    }

    public static class BookViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout book1Container, book2Container;
        ImageView book1Image, book2Image;
        TextView book1Title, book2Title;
        ImageView book1_flag, book2_flag;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            book1Container = itemView.findViewById(R.id.book1_container);
            book2Container = itemView.findViewById(R.id.book2_container);
            book1Image = itemView.findViewById(R.id.book1_image);
            book1Title = itemView.findViewById(R.id.book1_title);
            book2Image = itemView.findViewById(R.id.book2_image);
            book2Title = itemView.findViewById(R.id.book2_title);
            book1_flag = itemView.findViewById(R.id.book1_flag);
            book2_flag = itemView.findViewById(R.id.book2_flag);
        }

        public void bindFirstBook(BookClass book) {
            book1Container.setVisibility(View.VISIBLE);
            Glide.with(itemView.getContext())
                    .load(book.getImageUrl())
                    .into(book1Image);
            book1Title.setText(book.getTitle());
            book1_flag.setImageResource(book.getLanguage().equals("fr") ? R.drawable.french_on : R.drawable.english_on);
        }

        public void bindSecondBook(BookClass book) {
            book2Container.setVisibility(View.VISIBLE);
            Glide.with(itemView.getContext())
                    .load(book.getImageUrl())
                    .into(book2Image);
            book2Title.setText(book.getTitle());
            book2_flag.setImageResource(book.getLanguage().equals("fr") ? R.drawable.french_on : R.drawable.english_on);
        }

        public void hideSecondBook() {
            book2Container.setVisibility(View.INVISIBLE);
        }
    }
}
