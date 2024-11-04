package com.example.weatherapp.booktutorial;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherapp.R;

import java.util.ArrayList;

public class BookListRecyclerAdapter extends RecyclerView.Adapter<BookListRecyclerAdapter.BookListRecyclerViewHolder>{

    private Context context;
    Activity activity;
    private final ArrayList<String> book_id, book_title, book_author, book_pages;

    public BookListRecyclerAdapter(Activity activity, Context context, ArrayList<String> book_id, ArrayList<String> book_title, ArrayList<String> book_author, ArrayList<String> book_pages) {
        this.activity = activity;
        this.context = context;
        this.book_id = book_id;
        this.book_title = book_title;
        this.book_author = book_author;
        this.book_pages = book_pages;
    }

    @NonNull
    @Override
    public BookListRecyclerAdapter.BookListRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.book_recycler_layout, parent, false);
        return new BookListRecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookListRecyclerViewHolder holder, int position) {
        String id = book_id.get(position);
        String title = book_title.get(position);
        String author = book_author.get(position);
        String pages = book_pages.get(position);

        holder.bookId_tv.setText(String.valueOf(book_id.get(position)));
        holder.bookTitle_tv.setText(String.valueOf(book_title.get(position)));
        holder.bookAuthor_tv.setText(String.valueOf(book_author.get(position)));
        holder.bookPages_tv.setText(String.valueOf(book_pages.get(position)));
        holder.mainBookUpdateLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, UpdateBookActivity.class);
                intent.putExtra("id", id);
                intent.putExtra("title", title);
                intent.putExtra("author", author);
                intent.putExtra("pages", pages);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return book_id.size();
    }

    public static class BookListRecyclerViewHolder extends RecyclerView.ViewHolder{

        TextView bookId_tv;
        TextView bookTitle_tv;
        TextView bookAuthor_tv;
        TextView bookPages_tv;
        ConstraintLayout mainBookUpdateLayout;

        public BookListRecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            bookId_tv = itemView.findViewById(R.id.bookId_tv);
            bookTitle_tv = itemView.findViewById(R.id.bookTitle_tv);
            bookAuthor_tv = itemView.findViewById(R.id.bookAuthor_tv);
            bookPages_tv = itemView.findViewById(R.id.bookPages_tv);
            mainBookUpdateLayout = itemView.findViewById(R.id.bookUpdateLayout);
        }
    }
}
