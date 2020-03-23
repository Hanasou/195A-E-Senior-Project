package com.example.a195a_e_senior_project.ui.slideshow;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SlideshowViewModel extends ViewModel {

    private MutableLiveData<String> mText;


    // replace slideshow to terms

    public SlideshowViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Terms & Conditions fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}