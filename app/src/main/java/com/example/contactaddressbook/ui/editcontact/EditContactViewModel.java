package com.example.contactaddressbook.ui.editcontact;

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
import com.example.contactaddressbook.listener.EditContactOnClickListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class EditContactViewModel extends AndroidViewModel implements EditContactOnClickListener {

    // mutable live data variables
    private final SingleLiveEvent<String> navigateEvent = new SingleLiveEvent<>();
    private final SingleLiveEvent<String> toastEvent = new SingleLiveEvent<>();
    private final MutableLiveData<String> contactID = new MutableLiveData<>("");
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
    private final MutableLiveData<Uri> profileImageURL = new MutableLiveData<>(Uri.parse("null"));
    private final MutableLiveData<Uri> newProfileImageUrl = new MutableLiveData<>(Uri.parse("null"));

    // class variables
    private final String TAG = "EditContactViewModel";
    private final FirebaseFirestore firebaseFirestore;
    private final StorageReference storageReference;
    private StorageReference profileImageRef;
    private boolean isOldImageRemoved = false;

    public EditContactViewModel(Application application) {
        super(application);
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("ImageFolder");

    }

    /**
     * Load the contact's data into the UI.
     */
    public void loadContactData() {
        isLoading.setValue(true);
        // initialise the contact document reference
        DocumentReference documentReference = firebaseFirestore.collection("Contacts")
                .document(contactID.getValue());
        // get the contact documentSnapshot from Firebase
        documentReference.get().addOnSuccessListener(documentSnapshot -> {
            // load user data into views or variable
            String profileImageURL = documentSnapshot.getString("profileImageURL");
            firstName.setValue(documentSnapshot.getString("firstName"));
            lastName.setValue(documentSnapshot.getString("lastName"));
            dob.setValue(documentSnapshot.getString("dob"));
            email.setValue(documentSnapshot.getString("email"));
            phone.setValue(documentSnapshot.getString("phone"));
            streetOne.setValue(documentSnapshot.getString("streetLineOne"));
            streetTwo.setValue(documentSnapshot.getString("streetLineTwo"));
            city.setValue(documentSnapshot.getString("city"));
            postcode.setValue(documentSnapshot.getString("postcode"));

            setProfileImageURL(Uri.parse(profileImageURL));

            if (profileImageURL != null) {
                // assert the contact has an image in the database
                if (!profileImageURL.equals("null"))  {
                    // get the image reference using the image url
                    profileImageRef = FirebaseStorage.getInstance().getReferenceFromUrl(profileImageURL);
                }
            }
            isLoading.setValue(false);
        }).addOnFailureListener(e -> Log.d(TAG, "Failed to load contact: " + e.getMessage()));
    }

    @Override
    public void onUpdateClick(
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
            Uri localProfileImageURL = newProfileImageUrl.getValue();

            HashMap<String, Object> contactData = new HashMap<>();
            contactData.put("firstName", firstName.getValue());
            contactData.put("lastName", lastName.getValue());
            contactData.put("dob", dob.getValue());
            contactData.put("email",email.getValue());
            contactData.put("phone", phone.getValue());
            contactData.put("streetLineOne", streetOne.getValue());
            contactData.put("streetLineTwo", streetTwo.getValue());
            contactData.put("city", city.getValue());
            contactData.put("postcode", postcode.getValue());

            // upload new image if url is not null
            if (!localProfileImageURL.toString().equals("null")) {
                String imageName = contactID.getValue() + "." + getExtension(localProfileImageURL);
                StorageReference imageRef = storageReference.child(imageName);
                // delete old image from storage if exists
                if (profileImageRef != null) {
                    deleteOldPictureFromStorage();
                }
                // upload image to Firestore
                UploadTask uploadTask = imageRef.putFile(localProfileImageURL);
                uploadTask.continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        Log.i(TAG, "Couldn't upload image");
                    }
                    return imageRef.getDownloadUrl();
                }).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        contactData.put("profileImageURL", task.getResult().toString());
                        // update the contact in the database
                        firebaseFirestore.collection("Contacts")
                                .document(contactID.getValue())
                                .update(contactData)
                                .addOnSuccessListener(aVoid -> {
                                    isLoading.setValue(false);
                                    // navigate back to the contacts fragment
                                    navigateEvent.setValue("Contact updated");
                                });
                    }
                });
            } else {
                // delete the old image from storage if it has been removed
                if (isOldImageRemoved) {
                    contactData.put("profileImageURL", "null");
                    deleteOldPictureFromStorage();
                }
                // update the user
                firebaseFirestore.collection("Contacts")
                        .document(contactID.getValue())
                        .update(contactData)
                        .addOnSuccessListener(aVoid -> {
                            isLoading.setValue(false);
                            // navigate back to the contacts fragment
                            navigateEvent.setValue("Contact updated");
                        });
            }
        } else {
            toastEvent.setValue("Invalid contact");
        }
    }

    @Override
    public void onDelete() {
        if (contactID.getValue() != null) {
            firebaseFirestore.collection("Contacts")
                    .document(contactID.getValue())
                    .delete().addOnSuccessListener(aVoid -> {
                deleteOldPictureFromStorage();
                // navigate back to the contacts fragment
                navigateEvent.setValue("Contact deleted");
            }).addOnFailureListener(e ->
                    Log.d(TAG, "failed to remove contact: " + e.getMessage()));
        }

    }

    /**
     * Remove the contact's image, whether it is pre-existing from the
     * database or a new image uploaded via the gallery.
     */
    @Override
    public void onRemoveImage() {
        // set existing contact image to null as well as new contact image
        setProfileImageURL(Uri.parse("null"));
        setNewProfileImageUrl(Uri.parse("null"));
        isOldImageRemoved = true;
    }

    /**
     * Delete the old contact image from Firebase Storage.
     *
     */
    private void deleteOldPictureFromStorage() {
        if (profileImageRef != null) {
            profileImageRef.delete().addOnSuccessListener(aVoid ->
                    Log.d(TAG, "Old contact image removed successfully"))
                    .addOnFailureListener(e ->
                            Log.d(TAG, "Old contact image could not be removed"));
        }
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

    public SingleLiveEvent<String> getToastEvent() { return toastEvent; }


    public void setContactID(String contactID) {
        if (!this.contactID.getValue().equals(contactID)) {
            this.contactID.setValue(contactID);
        }
    }

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

    public MutableLiveData<Uri> getNewProfileImageUrl() { return newProfileImageUrl; }

    public void setNewProfileImageUrl(Uri newProfileImageUrl) {
        if (this.newProfileImageUrl.getValue() != newProfileImageUrl) {
            this.newProfileImageUrl.setValue(newProfileImageUrl);
        }
    }
}
