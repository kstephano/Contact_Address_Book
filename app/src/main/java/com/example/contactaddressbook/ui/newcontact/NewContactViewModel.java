package com.example.contactaddressbook.ui.newcontact;

import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.webkit.MimeTypeMap;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.contactaddressbook.events.SingleLiveEvent;
import com.example.contactaddressbook.listener.NewContactOnClickListener;
import com.example.contactaddressbook.model.Contact;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class NewContactViewModel extends AndroidViewModel implements NewContactOnClickListener {

    // mutable live data variables
    private final SingleLiveEvent<String> navigateEvent = new SingleLiveEvent<>();
    private final SingleLiveEvent<String> dialogEvent = new SingleLiveEvent<>();
    private final SingleLiveEvent<String> toastEvent = new SingleLiveEvent<>();
    private final MutableLiveData<String> firstName = new MutableLiveData<>("");
    private final MutableLiveData<String> lastName = new MutableLiveData<>("");
    private final MutableLiveData<String> dob = new MutableLiveData<>("");
    private final MutableLiveData<String> email = new MutableLiveData<>("");
    private final MutableLiveData<String> phone = new MutableLiveData<>("");
    private final MutableLiveData<String> streetOne = new MutableLiveData<>("");
    private final MutableLiveData<String> streetTwo = new MutableLiveData<>("");
    private final MutableLiveData<String> city = new MutableLiveData<>("");
    private final MutableLiveData<String> postcode = new MutableLiveData<>("");
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Uri> profileImageURL = new MutableLiveData<>(null);

    // class variables
    private final String TAG = "NewContactViewModel";
    private Contact newContact;
    private final FirebaseFirestore firebaseFirestore;
    private final StorageReference storageReference;

    public NewContactViewModel(Application application) {
        super(application);
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("ImageFolder");
    }

    /**
     * Uploads the form to Firebase Firestore when the submit button is clicked.
     * @param firstName First name of the new contact.
     * @param lastName Last name of the new contact.
     * @param phone Phone number of the new contact.
     * @param email Email address of the new contact.
     * @param dob Date of birth of the new contact.
     * @param streetOne Street line one of the new contact.
     * @param streetTwo Street line two of the new contact.
     * @param city City of the new contact.
     * @param postcode Postcode of the new contact.
     */
    @Override
    public void onSubmitClick(
            MutableLiveData<String> firstName,
            MutableLiveData<String> lastName,
            MutableLiveData<String> phone,
            MutableLiveData<String> email,
            MutableLiveData<String> dob,
            MutableLiveData<String> streetOne,
            MutableLiveData<String> streetTwo,
            MutableLiveData<String> city,
            MutableLiveData<String> postcode) {
        // check if the form is valid
        if (isFormValid(firstName.getValue(),
                lastName.getValue(),
                phone.getValue(),
                email.getValue())) {
            // set isLoading boolean to true to show the progress bar
            isLoading.setValue(true);
            Uri localProfileImageURL = profileImageURL.getValue();
            String contactID = firstName.getValue() + lastName.getValue() +
                    phone.getValue();

            // upload contact with image if URL is not null
            if (localProfileImageURL != null) {
                String imageName = contactID + "." + getExtension(localProfileImageURL);
                StorageReference imageRef = storageReference.child(imageName);

                // upload image to Firestore
                UploadTask uploadTask = imageRef.putFile(localProfileImageURL);
                uploadTask.continueWithTask( task -> {
                    if (!task.isSuccessful()) {
                        Log.i(TAG, "Couldn't upload image");
                    }
                    return imageRef.getDownloadUrl();
                }).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // create a new contact object
                        newContact = new Contact(
                                contactID,
                                phone.getValue(),
                                firstName.getValue(),
                                lastName.getValue(),
                                email.getValue(),
                                dob.getValue(),
                                streetOne.getValue(),
                                streetTwo.getValue(),
                                city.getValue(),
                                postcode.getValue(),
                                task.getResult().toString()
                        );
                        // upload the contact data
                        firebaseFirestore.collection("Contacts")
                                .document(contactID)
                                .set(newContact)
                                .addOnSuccessListener(aVoid -> {
                                    isLoading.setValue(false);
                                    // navigate back to the contacts fragment
                                    navigateEvent.setValue("Contacted added");
                                }).addOnFailureListener(e -> {
                            isLoading.setValue(false);
                            Log.d(TAG, "Couldn't upload user: " + e.getMessage());
                        });
                    }
                }).addOnFailureListener(e -> {
                    isLoading.setValue(false);
                    Log.d(TAG, "Couldn't upload image: " + e.getMessage());
                });
            } else {
                // upload with null image url
                // create a new contact object
                newContact = new Contact(
                        contactID,
                        phone.getValue(),
                        firstName.getValue(),
                        lastName.getValue(),
                        email.getValue(),
                        dob.getValue(),
                        streetOne.getValue(),
                        streetTwo.getValue(),
                        city.getValue(),
                        postcode.getValue(),
                        "null"
                );
                // upload the contact data
                firebaseFirestore.collection("Contacts")
                        .document(contactID)
                        .set(newContact)
                        .addOnSuccessListener(aVoid -> {
                            isLoading.setValue(false);
                            // navigate back to the contacts fragment
                            navigateEvent.setValue("Contacted added");
                        }).addOnFailureListener(e -> {
                    isLoading.setValue(false);
                    Log.d(TAG, "Couldn't upload user: " + e.getMessage());
                });
            }
        } else {
            toastEvent.setValue("Invalid contact");
        }
    }

    @Override
    public void onShowDialog() {
        dialogEvent.setValue("Contact added");
    }

    /**
     * Checks if the current form is valid to create a new contact.
     * @return True if valid, false otherwise.
     */
    private Boolean isFormValid(String firstName, String lastName, String phone, String email) {
        // validate firstName/lastName are filled and phone OR email is filled and valid.
        return !firstName.isEmpty() && !lastName.isEmpty() &&
                (!phone.isEmpty() && PhoneNumberUtils.isGlobalPhoneNumber(phone)) ||
                (!email.isEmpty() && isValidEmail(email));
    }

    /**
     * Check if the target input is a valid email address.
     * @param target The char sequence to check.
     * @return True if a valid email, false otherwise.
     */
    private Boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    /**
     * Get the extension for an image given a Uri.
     * @param uri The Uri of an image.
     * @return The file extension for the given Uri, or null.
     */
    private String getExtension(Uri uri) {
        String extension = null;
        Context context = getApplication().getApplicationContext();
        try {
            ContentResolver contentResolver = context.getContentResolver();
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            extension = mime.getExtensionFromMimeType(contentResolver.getType(uri));
        } catch (Exception e) {
            Log.d(TAG, "Couldn't get extension");
        }
        return extension;
    }

    public SingleLiveEvent<String> getNavigateEvent() { return navigateEvent; }

    public SingleLiveEvent<String> getDialogEvent() { return dialogEvent; }

    public SingleLiveEvent<String> getToastEvent() { return toastEvent; }

    public MutableLiveData<String> getFirstName() { return firstName; }

    public MutableLiveData<String> getLastName() { return lastName; }

    public MutableLiveData<String> getDob() { return dob; }

    public MutableLiveData<String> getEmail() { return email; }

    public MutableLiveData<String> getPhone() { return phone; }

    public MutableLiveData<String> getStreetOne() { return streetOne; }

    public MutableLiveData<String> getStreetTwo() { return streetTwo; }

    public MutableLiveData<String> getCity() { return city; }

    public MutableLiveData<String> getPostcode() { return postcode; }

    public MutableLiveData<Boolean> getIsLoading() { return isLoading; }

    public MutableLiveData<Uri> getProfileImageURL() { return profileImageURL; }

    public void setProfileImageURL(Uri profileImageURL) {
        if (this.profileImageURL.getValue() != profileImageURL) {
            this.profileImageURL.setValue(profileImageURL);
        }
    }
}
