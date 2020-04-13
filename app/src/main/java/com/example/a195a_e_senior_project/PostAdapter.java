package com.example.a195a_e_senior_project;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.google.firebase.Timestamp;
import androidx.annotation.NonNull;

import com.example.a195a_e_senior_project.ui.Models.Post;

import java.util.ArrayList;
import java.util.List;

public class PostAdapter extends ArrayAdapter<Post>{
    private Context mContext;
    private List<Post> postList = new ArrayList<>();

    public PostAdapter(@NonNull Context context, List<Post> posts) {
        super(context,0, posts);
        mContext = context;
        postList = posts;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View listPostView = convertView;
        if(listPostView == null){
            Log.d("Forum", "convertView is null");
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            listPostView = inflater.inflate(R.layout.post_detail,parent,false);
        }

        TextView titleTextView = (TextView) listPostView.findViewById(R.id.title);
        TextView contentTextView = (TextView) listPostView.findViewById(R.id.content);
        TextView authorTextView = (TextView) listPostView.findViewById(R.id.author);
        TextView timeTextView = (TextView) listPostView.findViewById(R.id.postTime);

        Post post = getItem(position);

        titleTextView.setText("Title: " + post.getTitle());
        contentTextView.setText("Content: " + post.getContent());
        authorTextView.setText("Author: " + post.getAuthor());
        if(post.getPostTime() != null)
            timeTextView.setText("Date: " + ((Timestamp) post.getPostTime()).toDate().toString());

        return listPostView;
    }
}
