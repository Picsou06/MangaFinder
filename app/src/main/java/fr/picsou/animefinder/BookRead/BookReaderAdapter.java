package fr.picsou.animefinder.BookRead;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;
import fr.picsou.animefinder.R;

public class BookReaderAdapter extends RecyclerView.Adapter<BookReaderAdapter.BookViewHolder> {
    private final Context mContext;
    private List<BookReaderClass> mBookList;
    private OnBookClickListener mListener;

    public BookReaderAdapter(Context context, List<BookReaderClass> bookList) {
        mContext = context;
        mBookList = bookList;
    }

    public void setOnBookClickListener(OnBookClickListener listener) {
        Log.d("BookSearchAdapter", "Listener Enabled");
        mListener = listener;
    }

    public void updateBooks(List<BookReaderClass> newBookList) {
        mBookList=newBookList;
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

        System.out.println("Binding books at position: " + position);
        System.out.println("First book index: " + firstBookIndex);
        System.out.println("Second book index: " + secondBookIndex);

        if (firstBookIndex < mBookList.size()) {
            holder.bindFirstBook(mBookList.get(firstBookIndex));
        }

        if (secondBookIndex < mBookList.size()) {
            System.out.println("HELPER, Bind Second Book");
            holder.bindSecondBook(mBookList.get(secondBookIndex));
            holder.showSecondBook();
        } else {
            System.out.println("HELPER, Hide Second Book");
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
        if (mBookList == null) {
            return 0;
        } else {
            return (int) Math.ceil((double) mBookList.size() / 2);
        }
    }


    public interface OnBookClickListener {
        void onBookClick(BookReaderClass book);
    }

    public static class BookViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout book1Container, book2Container;
        ImageView book1Image, book2Image;
        TextView book1Title, book2Title;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            book1Container = itemView.findViewById(R.id.book1_container);
            book2Container = itemView.findViewById(R.id.book2_container);
            book1Image = itemView.findViewById(R.id.book1_image);
            book1Title = itemView.findViewById(R.id.book1_title);
            book2Image = itemView.findViewById(R.id.book2_image);
            book2Title = itemView.findViewById(R.id.book2_title);
        }

        public void bindFirstBook(BookReaderClass book) {
            if (book.getImageCover() != null) {
                book1Container.setVisibility(View.VISIBLE);
                Glide.with(itemView.getContext())
                        .load(book.getImageCover())
                        .into(book1Image);
            }
            book1Title.setText(book.getTitle());
        }

        public void bindSecondBook(BookReaderClass book) {
            if (book.getImageCover() != null) {
                book2Container.setVisibility(View.VISIBLE);
                Glide.with(itemView.getContext())
                        .load(book.getImageCover())
                        .into(book2Image);
            }
            book2Title.setText(book.getTitle());
        }

        public void hideSecondBook() {
            book2Container.setVisibility(View.INVISIBLE);
        }

        public void showSecondBook() {
            book2Container.setVisibility(View.VISIBLE);
        }
    }
}
