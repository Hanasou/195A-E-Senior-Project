package com.example.a195a_e_senior_project.ui.Models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;

public class Post {
    //private firestore doc id
    @DocumentId
    private String documentId;
//    private String postKey;
    private String title;
    private String content;
    private String author;
//    private String picture;
//    private String userId;
//    private String userPhoto;
    private Timestamp postTime;

    public Post() {}

    public Post(String title, String content, String author, Timestamp postTime) {
        this.title = title;
        this.content = content;
        this.postTime = postTime;
//        this.postTime = ServerValue.TIMESTAMP;
    }


    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getAuthor() {
        return author;
    }

    public Object getPostTime() {
        return postTime;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setPostTime(Timestamp postTime) {
        this.postTime = postTime;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
}