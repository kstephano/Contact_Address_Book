
package com.example.contactaddressbook.ui.newcontact;

import android.app.DatePickerDialog;

import androidx.databinding.Bindable;
import androidx.databinding.InverseMethod;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class NewContactViewModel extends ViewModel {

    private MutableLiveData<String> mText;
    private MutableLiveData<String> profileImageURL = new MutableLiveData<>();

    private MutableLiveData<Integer> tel = new MutableLiveData<>();
    private MutableLiveData<String> firstName = new MutableLiveData<>();
    private MutableLiveData<String> lastName = new MutableLiveData<>();
    private MutableLiveData<String> email = new MutableLiveData<>();
    private MutableLiveData<String> dob = new MutableLiveData<>();
    private MutableLiveData<String> streetOne = new MutableLiveData<>();
    private MutableLiveData<String> streetTwo = new MutableLiveData<>();
    private MutableLiveData<String> city = new MutableLiveData<>();
    private MutableLiveData<String> postcode = new MutableLiveData<>();
    private MutableLiveData<DatePickerDialog.OnDateSetListener> dateSetListener;

    private String TAG = "NewContactViewModel";
    private static int REQUEST_CODE = 1;
    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageReference;
    private Calendar calendar;

    public NewContactViewModel() {
        mText = new MutableLiveData<>();
        dateSetListener = new MutableLiveData<>();

        // initialise the DateSetListener
        dateSetListener.setValue((view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            String dateFormat = "dd/MM/yyyy";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat, Locale.UK);
            dob.setValue(simpleDateFormat.format(calendar.getTime()));
        });

        // initialise Firebase variables
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("ImageFolder");

        mText.setValue("This is notifications fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }

    public LiveData<String> getFirstName() { return firstName; }

    public void setFirstName(String firstName) {
        if (this.firstName.getValue() != firstName) {
            this.firstName.setValue(firstName);
        }

    }

    public LiveData<String> getLastName() { return lastName; }

    public LiveData<String> getEmail() { return email; }
    public LiveData<String> getDob() { return dob; }
    public LiveData<String> getStreetOne() { return streetOne; }
    public LiveData<String> getStreetTwo() { return streetTwo; }
    public LiveData<String> getCity() { return city; }
    public LiveData<String> getPostCode() { return postcode; }
    public LiveData<DatePickerDialog.OnDateSetListener> getDateSetListener() { return dateSetListener; }
}
