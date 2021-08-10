package com.example.contactaddressbook.listener;

import androidx.lifecycle.MutableLiveData;

public interface OnClickListener {
    void onSubmitClick(
            MutableLiveData<String> firstName,
            MutableLiveData<String> lastName,
            MutableLiveData<String> dob,
            MutableLiveData<String> email,
            MutableLiveData<String> phone,
            MutableLiveData<String> streetOne,
            MutableLiveData<String> streetTwo,
            MutableLiveData<String> city,
            MutableLiveData<String> postcode
    );
    void onShowDialog();
}
