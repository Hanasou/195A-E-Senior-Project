package com.example.a195a_e_senior_project;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import com.example.a195a_e_senior_project.dialogs.FilterPostsDialog;
import com.example.a195a_e_senior_project.ui.Models.Post;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ForumActivity extends BaseActivity implements FilterPostsDialog.FilterPostsListener{
    private FirebaseFirestore db;
    private FirebaseAuth mAth;
    private FirebaseUser currUser;

    private ListView mListView;
    private List<Post> mPostList;
    private PostAdapter mPostAdapter;

    private ArrayList<String> selectedFilters;

    private Dialog popAddPost;
    private FilterPostsDialog filterPostsDialog;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = new MenuInflater(this);
        menuInflater.inflate(R.menu.forum_menu, menu);

        /*
        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search_bar).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
         */
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.filter_posts:
                showFiltersDialog();

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

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

        // Filter information
        selectedFilters = new ArrayList<String>();

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            // Do Search
            searchPosts(query);
        }

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

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("Forum listener", "Successful listen on click");
                //Toast.makeText(ForumActivity.this,"Selected "+(position +1) +" \nID："+ mPostList.get(position).getDocumentId(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ForumActivity.this, ForumActivity2.class);
                intent.putExtra("POST_ID", mPostList.get(position).getDocumentId());
                startActivity(intent);
            }
        });
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
                    //Log.d("addButton", "In on Click");
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

    /**
     * Filter posts based on what user selected in FilterPostsDialog
     * @param activeFilters List of filters that user selected
     */
    public void filterPosts(ArrayList<String> activeFilters) {
        if (activeFilters.contains("My Posts")) {
            ArrayList<Post> myPosts = new ArrayList<Post>();
            PostAdapter filteredPostAdapter = new PostAdapter(ForumActivity.this, myPosts);
            for (Post p : mPostList) {
                if (p.getAuthor().equals(currUser.getEmail())) {
                    myPosts.add(p);
                }
            }
            mListView.setAdapter(filteredPostAdapter);
        }
        else {
            mListView.setAdapter(mPostAdapter);
        }
    }

    public void searchPosts(String searchTerm) {
        if (searchTerm.equals("")) {
            mListView.setAdapter(mPostAdapter);
        }
        else {
            ArrayList<Post> searchedPosts = new ArrayList<Post>();
            PostAdapter searchedPostAdapter = new PostAdapter(ForumActivity.this, searchedPosts);
            for (Post p : mPostList) {
                if (p.getTitle().contains(searchTerm)) {
                    searchedPosts.add(p);
                }
            }
            mListView.setAdapter(searchedPostAdapter);
        }
    }

    public void showFiltersDialog() {

        filterPostsDialog = new FilterPostsDialog();
        filterPostsDialog.show(getSupportFragmentManager(), "FilterPostsDialogFragment");
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        // User touched the dialog's positive button
        this.selectedFilters = filterPostsDialog.getSelectedItems();
        Log.d("Active Filters", selectedFilters.toString());
        filterPosts(selectedFilters);
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // User touched the dialog's negative button

    }
}
