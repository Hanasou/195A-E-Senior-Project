package com.example.a195a_e_senior_project;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.a195a_e_senior_project.ui.Models.Post;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.core.OrderBy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ForumActivity extends AppCompatActivity{
    private FirebaseFirestore db;
    private FirebaseAuth mAth;
    private FirebaseUser currUser;

    private ListView mListView;
    private List<Post> mPostList;
    private PostAdapter mPostAdapter;

    private Dialog popAddPost;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum);

        // init
        mAth = FirebaseAuth.getInstance();
        currUser = mAth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        mListView = (ListView) findViewById(R.id.listView);
        mPostList = new ArrayList<Post>();
        mPostAdapter = new PostAdapter(ForumActivity.this, mPostList);

        // init pop-up
        initialPop();
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popAddPost.show();
            }
        });

        // init post
        getPost();

        updatePost();
    }

    // pop up add method
    private void initialPop(){
        final ImageView popupUserImage, popupPostImage, popupAddButton;
        final TextView popupTitle, popupDes;
        final ProgressBar popupClickProgress;
        final Uri pickedImaUri = null;

        popAddPost = new Dialog(this);
        popAddPost.setContentView(R.layout.add_post);
        popAddPost.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popAddPost.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT);
        popAddPost.getWindow().getAttributes().gravity = Gravity.TOP;

        // ini pop-up widgets
        popupUserImage = popAddPost.findViewById(R.id.popup_user_image);
        popupPostImage = popAddPost.findViewById(R.id.popup_bgIma);
        popupTitle = popAddPost.findViewById(R.id.popup_title);
        popupDes = popAddPost.findViewById(R.id.popup_description);
        popupAddButton = popAddPost.findViewById(R.id.popup_add);
        popupClickProgress = popAddPost.findViewById(R.id.popup_progressBar);

        // Add post click button listener
        popupAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                popupAddButton.setVisibility(View.INVISIBLE);
                popupClickProgress.setVisibility(View.VISIBLE);

                // test input fields (title and description) and post picture
                if(!popupTitle.getText().toString().isEmpty() && !popupDes.getText().toString().isEmpty()){
                    Log.d("addButton", "In on Click");
                    addPost(popupTitle.getText().toString(), popupDes.getText().toString());
                }

                if(!popupTitle.getText().toString().isEmpty() && !popupDes.getText().toString().isEmpty()){
                    showMessage("Success to post");
                    //getPost();
                    popAddPost.dismiss();
                    // now no picture can be used
                }else{
                    showMessage("Please check all your input");
                    popupAddButton.setVisibility(View.VISIBLE);
                    popupClickProgress.setVisibility(View.INVISIBLE);
                    //getPost();
                    popAddPost.dismiss();
                }
            }
        });
    }

    private void addPost(String title, String content) {
        Map<String, Object> newPost = new HashMap<>();
        newPost.put("title", title);
        newPost.put("content", content);
        newPost.put("author", currUser.getEmail());
        newPost.put("postTime", FieldValue.serverTimestamp());
        db.collection("forum")
                .add(newPost)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        //Log.d("forum", "Post successfully with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("forum", "Error adding post", e);
                    }
                });
    }

    private void getPost(){
        db.collection("forum").orderBy("postTime", Query.Direction.DESCENDING).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            mPostList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Post aPost = document.toObject(Post.class);
                                mPostList.add(aPost);
                                //Log.d("Forum", document.getId() + " => " + document.getData());
                            }
                            mListView.setAdapter(mPostAdapter);
                        } else {
                            Log.d("Forum", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void updatePost(){
        db.collection("forum").orderBy("postTime", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w("Forum listener", "Listen failed.", e);
                            return;
                        }
                        mPostList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            if (doc != null) {
                                //Log.d("Forum listener", (String) doc.get("title"));
                                mPostAdapter.add(doc.toObject(Post.class));
                            }
                        }
                        //Log.d("Forum listener", "Add new post(s) successfully");
                    }
                });
    }

    private void showMessage(String message){
        Toast.makeText(ForumActivity.this, message, Toast.LENGTH_LONG).show();
    }
}
