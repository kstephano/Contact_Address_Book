package com.example.contactaddressbook.ui.editcontact;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.contactaddressbook.R;
import com.google.api.LogDescriptor;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;


public class EditContactFragment extends Fragment {

    private static final int REQUEST_CALL = 1;
    private final String TAG = "EditContactFragment";
    private String contactID;
    private Uri profileImageURL;
    private Boolean isImageUpdated = false;
    private final Calendar calendar = Calendar.getInstance();
    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageReference;
    private StorageReference profileImageRef = null;

    // xml variables
    private View root;
    private CircleImageView profileIV;
    private ImageView callContactIV;
    private TextView addPictureTV;
    private TextView removePictureTV;
    private EditText firstNameET;
    private EditText lastNameET;
    private EditText dobET;
    private EditText emailET;
    private EditText phoneET;
    private EditText streetLineOneET;
    private EditText streetLineTwoET;
    private EditText cityET;
    private EditText postcodeET;
    private Button updateBtn;
    private Button deleteBtn;
    private Dialog loadingDialog;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_edit_contact, container, false);
        // initialise the loading dialog
        loadingDialog = new Dialog(getContext());
        loadingDialog.setContentView(R.layout.loading_dialog);

        contactID = getArguments().getString("contactID");
        initialiseToolbar();
        initialiseViews();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("ImageFolder");
        loadContactData();

        setOnClickListenersAddPhoto();
        setOnClickListenerRemovePhoto();
        setOnClickListenerDobPicker();
        setOnClickListenerUpdateBtn();
        setOnClickListenerDeleteBtn();

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadingDialog.hide();
    }

    /**
     * Load the contact's data into the UI.
     */
    public void loadContactData() {
        loadingDialog.show();
        loadingDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // initialise the contact document reference
        DocumentReference documentReference = firebaseFirestore.collection("Contacts")
                .document(contactID);
        // get the contact documentSnapshot from Firebase
        documentReference.get().addOnSuccessListener(documentSnapshot -> {
            // load user data into views or variable
            String profileImageURL = documentSnapshot.getString("profileImageURL");
            firstNameET.setText(documentSnapshot.getString("firstName"));
            lastNameET.setText(documentSnapshot.getString("lastName"));
            dobET.setText(documentSnapshot.getString("dob"));
            emailET.setText(documentSnapshot.getString("email"));
            phoneET.setText(documentSnapshot.getString("phone"));
            streetLineOneET.setText(documentSnapshot.getString("streetLineOne"));
            streetLineTwoET.setText(documentSnapshot.getString("streetLineTwo"));
            cityET.setText(documentSnapshot.getString("city"));
            postcodeET.setText(documentSnapshot.getString("postcode"));

            // load picture if not URL is not empty
            if (!profileImageURL.equals("null")) {
                Glide.with(getContext()).load(profileImageURL).into(profileIV);
                removePictureTV.setText(getResources().getString(R.string.text_remove_photo));
                profileImageRef = FirebaseStorage.getInstance().getReferenceFromUrl(profileImageURL);
            } else {
                addPictureTV.setText(getResources().getString(R.string.text_add_photo));
            }

            // set the onClickListener for the call contact button
            setOnClickListenerCallContact();
            loadingDialog.hide();
        }).addOnFailureListener(e -> Log.d(TAG, "Failed to load contact: " + e.getMessage()));
    }


    /**
     * Checks if the current form is valid to create a new contact.
     * @return True if valid, false otherwise.
     */
    private Boolean isFormValid() {
        String firstName = firstNameET.getText().toString();
        String lastName = lastNameET.getText().toString();
        String phone = phoneET.getText().toString();
        String email = emailET.getText().toString();

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
     * Initialise the toolbar and set the back button in the menu listener.
     */
    private void initialiseToolbar() {
        Toolbar toolbar = root.findViewById(R.id.toolbar_edit);
        toolbar.inflateMenu(R.menu.edit_contact_menu);

        // set the back button for the toolbar
        toolbar.setOnMenuItemClickListener(item -> {
            Navigation.findNavController(root).navigate(
                    R.id.action_navigation_edit_contact_to_navigation_contacts);
            return false;
        });
    }

    /**
     * Get the toolbar title.
     * @return Title of the toolbar, made up of the contact's first name
     * and last name.
     */
    private String getToolbarTitle() {
        String title;
        String firstName = getArguments().getString("firstName");
        String lastName = getArguments().getString("lastName");
        title = firstName + " " + lastName;
        return title;
    }

    /**
     * Uploads the form to Firebase, updating the contact.
     */
    private void upLoadToFirebase() {
        loadingDialog.show();
        loadingDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        Uri localProfileImageURL = profileImageURL;

        HashMap<String, Object> contactData = new HashMap<>();
        contactData.put("firstName", firstNameET.getText().toString());
        contactData.put("lastName", lastNameET.getText().toString());
        contactData.put("dob", dobET.getText().toString());
        contactData.put("email", emailET.getText().toString());
        contactData.put("phone", phoneET.getText().toString());
        contactData.put("streetLineOne", streetLineOneET.getText().toString());
        contactData.put("streetLineTwo", streetLineTwoET.getText().toString());
        contactData.put("city", cityET.getText().toString());
        contactData.put("postcode", postcodeET.getText().toString());

        // upload image if URL is not null
        if (profileImageURL != null) {
            String imageName = contactID + "." + getExtension(localProfileImageURL);
            StorageReference imageRef = storageReference.child(imageName);

            // upload image to Firestore
            UploadTask uploadTask = imageRef.putFile(localProfileImageURL);
            uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    Log.i(TAG, "Couldn't upload image");
                }
                return imageRef.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {

                    // upload contact to Firestore
                    contactData.put("profileImageURL", task.getResult().toString());

                    firebaseFirestore.collection("Contacts")
                            .document(contactID)
                            .update(contactData)
                            .addOnSuccessListener(aVoid -> {
                                loadingDialog.hide();
                                // navigate back to the contacts fragment
                                Navigation.findNavController(getActivity(),
                                        R.id.nav_host_fragment).
                                        navigate(R.id.action_navigation_edit_contact_to_navigation_contacts);
                                Toast.makeText(getContext(),
                                        getResources().getString(R.string.toast_contact_updated),
                                        Toast.LENGTH_SHORT).show();
                            });
                }
            });
        } else {
            if (isImageUpdated) {
                // remove the image URL from the database.
                contactData.put("profileImageURL", "null");
                deleteOldPictureFromStorage();
            }
            // update user
            firebaseFirestore.collection("Contacts")
                    .document(contactID)
                    .update(contactData)
                    .addOnSuccessListener(aVoid -> {
                        // navigate back to the contacts fragment
                        loadingDialog.hide();
                        Navigation.findNavController(getActivity(),
                                R.id.nav_host_fragment).
                                navigate(R.id.action_navigation_edit_contact_to_navigation_contacts);
                        Toast.makeText(getContext(),
                                getResources().getString(R.string.toast_contact_updated),
                                Toast.LENGTH_SHORT).show();
                    });
        }
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
     * Get the extension for an image given a Uri.
     * @param uri The Uri of an image.
     * @return The file extension for the given Uri, or null.
     */
    private String getExtension(Uri uri) {
        String extension = null;
        try {
            ContentResolver contentResolver = getContext().getContentResolver();
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            extension = mime.getExtensionFromMimeType(contentResolver.getType(uri));
        } catch (Exception e) {
            Log.d(TAG, "Couldn't get extension");
        }
        return extension;
    }

    /**
     * Set the onClickListener for the submit button.
     */
    private void setOnClickListenerUpdateBtn() {
        updateBtn.setOnClickListener(v -> {
            // check if form is valid and upload data to Firebase.
            if (isFormValid()) {
                upLoadToFirebase();
            } else {
                Toast.makeText(getContext(), "Invalid contact", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Set the onClickListener for the delete button.
     * Deletes the contact from the Firebase database.
     */
    private void setOnClickListenerDeleteBtn() {
        deleteBtn.setOnClickListener(v -> firebaseFirestore.collection("Contacts")
                .document(contactID)
                .delete().addOnSuccessListener(aVoid -> {
                    deleteOldPictureFromStorage();
            // navigate back to the contacts fragment
            Navigation.findNavController(root).navigate(
                    R.id.action_navigation_edit_contact_to_navigation_contacts);
            Toast.makeText(getContext(), getResources().
                    getString(R.string.toast_contact_deleted), Toast.LENGTH_SHORT).show();
        }));
    }

    /**
     * Set the ActivityResultLauncher and onClickListeners for the
     * profile Image View and add photo Text View.
     * Opens up the image gallery onClick.
     */
    private void setOnClickListenersAddPhoto() {
        // set the ActivityResultLauncher
        ActivityResultLauncher<Intent> imageGalleryResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // check if result code is valid
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        // check if data is null and set the photo
                        if (data.getData() != null) {
                            profileImageURL = data.getData();
                            profileIV.setImageURI(profileImageURL);
                            isImageUpdated = true;
                            addPictureTV.setText("");
                            removePictureTV.setText(
                                    getResources().getString(R.string.text_remove_photo));
                        } else {
                            Toast.makeText(getContext(), "No image selected",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        // set the onClick for the image view.
        profileIV.setOnClickListener(v -> {
            try {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setDataAndType(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        "image/*");
                imageGalleryResultLauncher.launch(galleryIntent);
            } catch (Exception e) {
                Log.d(TAG, "Couldn't open image gallery: " + e.getMessage());
            }
        });

        // set the onClick for the text view.
        addPictureTV.setOnClickListener(v -> {
            try {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setDataAndType(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                imageGalleryResultLauncher.launch(galleryIntent);
            } catch (Exception e) {
                Log.d(TAG, "Couldn't open image gallery: " + e.getMessage());
            }
        });
    }

    /**
     * Set the onClickListener for the remove photo Text View.
     */
    private void setOnClickListenerRemovePhoto() {
        removePictureTV.setOnClickListener(v -> {
            int image = R.drawable.ic_baseline_account_circle_24;
            profileIV.setImageResource(image);
            profileImageURL = null;
            isImageUpdated = true;
            removePictureTV.setText("");
            addPictureTV.setText(getResources().getString(R.string.text_add_photo));
        });
    }

    /**
     * Set the DatePickerDialog OnDateSetListener and the onClick for the
     * dobET to open up the dialog onCLick.
     */
    private void setOnClickListenerDobPicker() {
        // initialise the DateSetListener
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            String dateFormat = "dd/MM/yyyy";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat, Locale.UK);
            dobET.setText(simpleDateFormat.format(calendar.getTime()));
        };

        // set onClickListener for dobET
        dobET.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    getContext(),
                    R.style.MyDialogTheme,
                    dateSetListener,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });
    }

    /**
     * Set the onClickListener for the call contact image view.
     */
    private void setOnClickListenerCallContact() {
        callContactIV.setOnClickListener(v -> {
            String number = phoneET.getText().toString();
            // check if phone number is valid
            if (number.trim().length() > 0) {
                // ask for permissions if not currently granted
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL);
                } else {
                    // make the phone call
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:" + number));
                    startActivity(callIntent);
                }
            } else {
                Toast.makeText(getContext(), "Enter a phone number", Toast.LENGTH_SHORT).show();
            }
        });
    }


    /**
     * Attach XML views to Java objects.
     */
    private void initialiseViews() {
        profileIV = root.findViewById(R.id.image_view_profile);
        callContactIV = root.findViewById(R.id.image_view_phone_call);
        TextView toolbarTitleTV = root.findViewById(R.id.toolbar_title);
        addPictureTV = root.findViewById(R.id.text_add_photo);
        removePictureTV = root.findViewById(R.id.text_remove_photo);
        firstNameET = root.findViewById(R.id.edit_text_first_name);
        lastNameET = root.findViewById(R.id.edit_text_last_name);
        dobET = root.findViewById(R.id.edit_text_dob);
        emailET = root.findViewById(R.id.edit_text_email);
        phoneET = root.findViewById(R.id.edit_text_telephone);
        streetLineOneET = root.findViewById(R.id.edit_text_street_one);
        streetLineTwoET = root.findViewById(R.id.edit_text_street_two);
        cityET = root.findViewById(R.id.edit_text_city);
        postcodeET = root.findViewById(R.id.edit_text_postcode);
        updateBtn = root.findViewById(R.id.button_submit);
        deleteBtn = root.findViewById(R.id.button_delete);

        toolbarTitleTV.setText(getToolbarTitle());
    }

}
