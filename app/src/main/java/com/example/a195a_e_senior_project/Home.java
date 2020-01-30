package com.example.a195a_e_senior_project;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import com.example.a195a_e_senior_project.ui.Models.Post;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import android.net.Uri;


import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class Home extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private static final int perReqCode = 2;
    private static final int REQUESTCODE = 2;

    FirebaseAuth mAth;
    FirebaseUser currUser;
    Dialog popAddPost;
    ImageView popupUserImage, popupPostImage, popupAddButton;
    TextView popupTitle, popupDes;
    ProgressBar popupClickProgress;
    private Uri pickedImaUri = null;

    private FirebaseFirestore db;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home2);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        // ini
        mAth = FirebaseAuth.getInstance();
        currUser = mAth.getCurrentUser();
        db = FirebaseFirestore.getInstance();



        // ini pop-up
            initialPop();
        // waiting for creating user photo part
        //  setupPopupImageClick();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popAddPost.show();
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        // display user's info
        View headerView = navigationView.getHeaderView(0);
        TextView navUserName = headerView.findViewById(R.id.nav_users_name);
        navUserName.setText(currUser.getDisplayName());




        // need to figure it out




        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow,
                R.id.nav_tools, R.id.nav_share, R.id.nav_send)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }


    // pop up add method
    private void initialPop(){

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


        // load current user profile photo
        Glide.with(Home.this).load(currUser.getPhotoUrl()).into(popupUserImage);


        // Add post click button listener
        popupAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                popupAddButton.setVisibility(View.INVISIBLE);
                popupClickProgress.setVisibility(View.VISIBLE);

                // test input fields (title and description) and post picture

                if(!popupTitle.getText().toString().isEmpty() && !popupDes.getText().toString().isEmpty()
                        && pickedImaUri == null){           // now no picture can be used
                    // everything work
                    // TODO Create add to Firebase.

                   // enter database and upload image
                   StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Forum_images");
                   final StorageReference imageFilePath = storageReference.child(pickedImaUri.getLastPathSegment());
                   imageFilePath.putFile(pickedImaUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                       @Override
                       public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                           imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                               @Override
                               public void onSuccess(Uri uri) {
                                   String imageDownload = uri.toString();

                                   // create post object
                                    Post post = new Post(popupTitle.getText().toString(),popupDes.getText().toString(),imageDownload,
                                   currUser.getUid(), currUser.getPhotoUrl().toString());

                                    // add post to database
                                   addPost(post);

                               }
                           }).addOnFailureListener(new OnFailureListener() {
                               @Override
                               public void onFailure(@NonNull Exception e) {
                                   // fail upload
                                   showMessage(e.getMessage());
                                   popupClickProgress.setVisibility(View.INVISIBLE);
                                   popupAddButton.setVisibility(View.VISIBLE);
                               }
                           });
                       }
                   });

                }else{
                    showMessage("Please check all your input");
                    popupAddButton.setVisibility(View.VISIBLE);
                    popupClickProgress.setVisibility(View.INVISIBLE);

                }
            }
        });
    }

    private void addPost(Post post) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference mRef = database.getReference("Posts").push();


        // get post unique ID and update post key
        String key = mRef.getKey();
        post.setPostKey(key);


        // add post data to database
        mRef.setValue(post).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                showMessage("Post added successful!");
                popupClickProgress.setVisibility(View.INVISIBLE);
                popupAddButton.setVisibility(View.VISIBLE);
                popAddPost.dismiss();
            }
        });

    }


    private void showMessage(String message){
        Toast.makeText(Home.this, message, Toast.LENGTH_LONG).show();
    }

    // waiting for registering code for getting user photo

//    private void setupPopupImageClick(){
//
//        popupPostImage.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View view){
//                // when image clicked we need to open the gallery
//                // before we open the gallery we need to check if our app have the access to user file
//
//
//                 checkForPermission();
//            }
//        });
//    }
//
//
//    // To check and require permission to access user file
//    private void checkForPermission(){
//        if(ContextCompat.checkSelfPermission(Home.this, Manifest.permission.READ_EXTERNAL_STORAGE)
//            != PackageManager.PERMISSION_GRANTED){
//            if(ActivityCompat.shouldShowRequestPermissionRationale(Home.this, Manifest.permission.READ_EXTERNAL_STORAGE)){
//                Toast.makeText(Home.this,"Please accept for required permission", Toast.LENGTH_SHORT).show();
//            }else{
//                ActivityCompat.requestPermissions(Home.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
//                        perReqCode);
//            }
//        }else
//            openGallery();
//    }
//
//    private void openGallery(){
//        // open gallery intent and wait for user to pick an image
//        Intent gIntent = new Intent(Intent.ACTION_GET_CONTENT);
//        gIntent.setType("image*/");
//        startActivityForResult(gIntent,REQUESTCODE);
//    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK && requestCode == REQUESTCODE && data != null){
            // an image picked by user
            // save reference to Uri variable
            pickedImaUri = data.getData();
            popupPostImage.setImageURI(pickedImaUri);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
