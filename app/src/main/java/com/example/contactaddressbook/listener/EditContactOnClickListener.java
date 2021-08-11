package com.example.contactaddressbook.listener;

import androidx.lifecycle.MutableLiveData;

public interface EditContactOnClickListener {
    void onUpdateClick(
            MutableLiveData<String> firstName,
            MutableLiveData<String> lastName,
            MutableLiveData<String> phone,
            MutableLiveData<String> email,
            MutableLiveData<String> dob,
            MutableLiveData<String> streetOne,
            MutableLiveData<String> streetTwo,
            MutableLiveData<String> city,
            MutableLiveData<String> postcode
    );
    void onDelete();
    void onRemoveImage();
}
