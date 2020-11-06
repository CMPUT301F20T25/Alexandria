package com.example.alexandria;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class CustomList extends ArrayAdapter<Book> {

    private ArrayList<Book> books;
    private Context context;

    public CustomList(Context context, ArrayList<Book> books) {
        super(context, 0, books);
        this.books = books;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
//        return super.getView(position, convertView, parent);
        View view = convertView;

        if(view == null){
            view = LayoutInflater.from(context).inflate(R.layout.content, parent,false);
        }

        Book book = books.get(position);

        TextView authorName = view.findViewById(R.id.author_name);
        TextView bookName = view.findViewById(R.id.book_name);

        authorName.setText(book.getAuthorName());
        bookName.setText(book.getBookName());

        return view;

    }
}
