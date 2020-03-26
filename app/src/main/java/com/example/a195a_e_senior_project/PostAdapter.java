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

import java.util.List;

public class PostAdapter extends ArrayAdapter<Post>{
    public PostAdapter(Context context, List<Post> posts) {
        super(context,0, posts);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        if(convertView == null){
            Log.d("Forum", "convertView is null");
            convertView =  ((Activity)getContext()).getLayoutInflater().inflate(R.layout.post_detail,parent,false);
        }

        TextView titleTextView = (TextView) convertView.findViewById(R.id.title);
        TextView contentTextView = (TextView) convertView.findViewById(R.id.content);
        TextView authorTextView = (TextView) convertView.findViewById(R.id.author);
        TextView timeTextView = (TextView) convertView.findViewById(R.id.postTime);

        Post post = getItem(position);

        titleTextView.setText(post.getTitle());
        contentTextView.setText(post.getContent());
        authorTextView.setText(post.getAuthor());
        if(post.getPostTime() != null)
            timeTextView.setText(((Timestamp) post.getPostTime()).toDate().toString());

        return convertView;
    }
}
