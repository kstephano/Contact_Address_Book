package com.example.contactaddressbook.ui.editcontact;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.contactaddressbook.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;


public class EditContactFragment extends Fragment {

    // XML Views
    private View root;
    private Toolbar toolbar;
    private CircleImageView profileIV;
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

    // Firebase
    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageReference;

    private String TAG = "EditContactFragment";
    private String contactID;
    private Uri profileImageURL;
    private Calendar calendar = Calendar.getInstance();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_edit_contact, container, false);
        // show the loading dialog
        loadingDialog = new Dialog(getContext());
        loadingDialog.setContentView(R.layout.loading_dialog);

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

    /**
     * Load the contact's data into the UI.
     */
    public void loadContactData() {
        loadingDialog.show();
        loadingDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        DocumentReference documentReference = firebaseFirestore.collection("Contacts")
                .document(contactID);

        documentReference.get().addOnSuccessListener(documentSnapshot -> {
            String profilePicURL = documentSnapshot.getString("profileImageURL");

            // load picture if not URL is not empty
            if (!profilePicURL.equals("null")) {
                Glide.with(getContext()).load(profilePicURL).into(profileIV);
                profileImageURL = profileImageURL;
                removePictureTV.setText(getResources().getString(R.string.text_remove_photo));
            } else {
                addPictureTV.setText(getResources().getString(R.string.text_add_photo));
            }

            firstNameET.setText(documentSnapshot.getString("firstName"));
            lastNameET.setText(documentSnapshot.getString("lastName"));
            dobET.setText(documentSnapshot.getString("dob"));
            emailET.setText(documentSnapshot.getString("email"));
            phoneET.setText(documentSnapshot.getString("phone"));
            streetLineOneET.setText(documentSnapshot.getString("streetLineOne"));
            streetLineTwoET.setText(documentSnapshot.getString("streetLineTwo"));
            cityET.setText(documentSnapshot.getString("city"));
            postcodeET.setText(documentSnapshot.getString("postcode"));

            loadingDialog.hide();
        }).addOnFailureListener(e -> {
            Log.d(TAG, "Failed to load contact: " + e.getMessage());
        });
    }


    /**
     * Checks if the current form is valid to create a new contact.
     * @return True if valid, false otherwise.
     */
    private Boolean isFormValid() {
        if (
                !firstNameET.getText().toString().isEmpty() &&
                        !lastNameET.getText().toString().isEmpty() &&
                        (!phoneET.getText().toString().isEmpty() || !emailET.toString().isEmpty())
        ) {
            return true;
        }
        return false;
    }

    /**
     * Initialise the toolbar and set the back button in the menu listener.
     */
    private void initialiseToolbar() {
        toolbar = root.findViewById(R.id.toolbar_edit);
        toolbar.inflateMenu(R.menu.edit_contact_menu);
        toolbar.setTitle(getToolbarTitle());

        // set the back button for the toolbar
        toolbar.setOnMenuItemClickListener(item -> {
            Navigation.findNavController(root).navigate(
                    R.id.action_navigation_edit_contact_to_navigation_contacts);
            return false;
        });
    }

    /**
     * Get the toolbar title and unpack bundle.
     * @return Title of the toolbar, made up of the contact's first name
     * and last name.
     */
    private String getToolbarTitle() {
        String title;
        String firstName = getArguments().getString("firstName");
        String lastName = getArguments().getString("lastName");
        String phone = getArguments().getString("phone");
        contactID = firstName + lastName + phone;
        title = firstName + " " + lastName;

        return title;
    }

    /**
     * Uploads the form to Firebase, updating the contact.
     */
    private void upLoadToFirebase() {
        Uri localProfileImageURL = profileImageURL;
        HashMap<String, Object> contactData = new HashMap<>();
        String contactID = firstNameET.getText().toString() + lastNameET.getText().
                toString() + phoneET.getText().toString();
        loadingDialog.show();

        // upload image if not null
        if (localProfileImageURL != null) {
            String imageName = lastNameET.getText().toString() +
                    phoneET.getText().toString() + "." + getExtension(localProfileImageURL);
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
                    // upload contact to Firestore

                    contactData.put("profileImageURL", task.getResult().toString());
                    contactData.put("firstName", firstNameET.getText().toString());
                    contactData.put("lastName", lastNameET.getText().toString());
                    contactData.put("dob", dobET.getText().toString());
                    contactData.put("email", emailET.getText().toString());
                    contactData.put("phone", phoneET.getText().toString());
                    contactData.put("streetLineOne", streetLineOneET.getText().toString());
                    contactData.put("streetLineTwo", streetLineTwoET.getText().toString());
                    contactData.put("city", cityET.getText().toString());
                    contactData.put("postcode", postcodeET.getText().toString());

                    firebaseFirestore.collection("Contacts")
                            .document(contactID)
                            .update(contactData)
                            .addOnSuccessListener(aVoid -> {
                                loadingDialog.hide();
                                // navigate back to the contacts fragment
                                Navigation.findNavController(root).navigate(
                                        R.id.action_navigation_edit_contact_to_navigation_contacts);
                                Toast.makeText(getContext(),
                                        getResources().getString(R.string.toast_contact_updated),
                                        Toast.LENGTH_SHORT).show();
                            });
                }
            });
        } else {
            // upload with null image url
            contactData.put("profileImageURL", "null");
            contactData.put("firstName",  firstNameET.getText().toString());
            contactData.put("lastName", lastNameET.getText().toString());
            contactData.put("dob", dobET.getText().toString());
            contactData.put("email", emailET.getText().toString());
            contactData.put("phone", phoneET.getText().toString());
            contactData.put("streetLineOne", streetLineOneET.getText().toString());
            contactData.put("streetLineTwo", streetLineTwoET.getText().toString());
            contactData.put("city", cityET.getText().toString());
            contactData.put("postcode", postcodeET.getText().toString());

            firebaseFirestore.collection("Contacts")
                    .document(contactID)
                    .update(contactData)
                    .addOnSuccessListener(aVoid -> {
                        // navigate back to the contacts fragment
                        loadingDialog.hide();
                        Navigation.findNavController(root).navigate(
                                R.id.action_navigation_edit_contact_to_navigation_contacts);
                        Toast.makeText(getContext(),
                                getResources().getString(R.string.toast_contact_updated),
                                Toast.LENGTH_SHORT).show();
                    });
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
            if (isFormValid() == true) {
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
        deleteBtn.setOnClickListener(v -> {
            firebaseFirestore.collection("Contacts")
                    .document(contactID)
                    .delete().addOnSuccessListener(aVoid -> {
                // navigate back to the contacts fragment
                Navigation.findNavController(root).navigate(
                        R.id.action_navigation_edit_contact_to_navigation_contacts);
                Toast.makeText(getContext(), getResources().
                        getString(R.string.toast_contact_deleted), Toast.LENGTH_SHORT).show();
            });
        });
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
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data.getData() != null) {
                            profileImageURL = data.getData();
                            profileIV.setImageURI(profileImageURL);
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

    private void setOnClickListenerRemovePhoto() {
        removePictureTV.setOnClickListener(v -> {
            int image = R.drawable.ic_baseline_account_circle_24;
            profileIV.setImageResource(image);
            profileImageURL = null;
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
     * Attach XML views to Java objects.
     */
    private void initialiseViews() {
        profileIV = root.findViewById(R.id.image_view_profile);
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
    }

}
