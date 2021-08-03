package com.example.contactaddressbook.ui.newcontact;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.contactaddressbook.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class NewContactFragment extends Fragment {

    private final String TAG = "NewContactViewModel";
    private Uri profileImageURL;
    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageReference;
    private final Calendar calendar = Calendar.getInstance();

    // xml variables
    private View root;
    private CircleImageView profileIV;
    private TextView addPhotoTV;
    private TextView removePhotoTV;
    private EditText firstNameET;
    private EditText lastNameET;
    private EditText dobET;
    private EditText emailET;
    private EditText phoneET;
    private EditText streetLineOneET;
    private EditText streetLineTwoET;
    private EditText cityET;
    private EditText postcodeET;
    private Button submitBtn;
    private Dialog loadingDialog;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_new_contact, container, false);

        // initialise Firebase variables
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("ImageFolder");

        // initialise the loading dialog
        loadingDialog = new Dialog(getContext());
        loadingDialog.setContentView(R.layout.loading_dialog);

        initialiseViews();
        setOnClickListenerImageIV();
        setOnClickListenerDobPicker();
        setOnClickListenerSubmitBtn();
        setOnClickListenerRemovePhoto();

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        // check if user has selected an image and display appropriate Text View.
        if (profileImageURL != null) {
            removePhotoTV.setText(R.string.text_remove_photo);
            addPhotoTV.setText("");
        }
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
     * Uploads the form to Firebase, creating a new contact.
     */
    private void upLoadToFirebase() {
        Uri localProfileImageURL = profileImageURL;
        HashMap<String, Object> contactData = new HashMap<>();
        String contactID = firstNameET.getText().toString() + lastNameET.getText().toString() +
                phoneET.getText().toString();
        // show the loading dialog
        loadingDialog.show();
        loadingDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // upload image if not null
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
                    // upload contact to Firestore

                    contactData.put("contactID", contactID);
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
                            .set(contactData)
                            .addOnSuccessListener(aVoid -> {
                                loadingDialog.hide();
                                // navigate back to the contacts fragment
                                Navigation.findNavController(getActivity(),
                                        R.id.nav_host_fragment).
                                        navigate(R.id.action_navigation_new_contact_to_navigation_contacts);
                                Toast.makeText(getContext(), "Contact added", Toast.LENGTH_SHORT).show();
                            });
                }
            });
        } else {
            // upload with null image url
            contactData.put("profileImageURL", "null");
            contactData.put("contactID", contactID);
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
                    .set(contactData)
                    .addOnSuccessListener(aVoid -> {
                        loadingDialog.hide();
                        // navigate back to the contacts fragment
                        Navigation.findNavController(root).navigate(
                                R.id.action_navigation_new_contact_to_navigation_contacts);
                        Toast.makeText(getContext(), "Contact added", Toast.LENGTH_SHORT).show();
                    }).addOnFailureListener(e -> {
                        loadingDialog.hide();
                        Log.d(TAG, "Couldn't upload contact: " + e.getMessage());
            });
        }

    }

    /**
     * Set the onClickListener for the submit button.
     */
    private void setOnClickListenerSubmitBtn() {
        submitBtn.setOnClickListener(v -> {
            if (isFormValid()) {
                upLoadToFirebase();
            } else {
                Toast.makeText(getContext(), "Invalid contact", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Set the ActivityResultLauncher and onClickListener for the
     * profile Image View. Opens up the image gallery onClick.
     */
    private void setOnClickListenerImageIV() {
        // set the ActivityResultLauncher
        ActivityResultLauncher<Intent> imageGalleryResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data.getData() != null) {
                            profileImageURL = data.getData();
                            profileIV.setImageURI(profileImageURL);
                        } else {
                            Toast.makeText(getContext(), "No image selected",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        // set the onClickListener for the Image View
        profileIV.setOnClickListener(v -> {
            try {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                imageGalleryResultLauncher.launch(galleryIntent);
            } catch (Exception e) {
                Log.d(TAG, "Couldn't open image gallery: " + e.getMessage());
            }
        });

        // set the onClickListener for the add photo Text View
        addPhotoTV.setOnClickListener(v -> {
            try {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
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
        removePhotoTV.setOnClickListener(v -> {
            int image = R.drawable.ic_baseline_account_circle_24;
            profileIV.setImageResource(image);
            profileImageURL = null;
            removePhotoTV.setText("");
            addPhotoTV.setText(getResources().getString(R.string.text_add_photo));
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
            datePickerDialog.setButton(DatePickerDialog.BUTTON_NEUTRAL, "Clear",
                    (dialog, which) -> dobET.setText(""));
            datePickerDialog.show();
        });
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
     * Attach XML views to Java objects.
     */
    private void initialiseViews() {
        profileIV = root.findViewById(R.id.image_view_profile);
        addPhotoTV = root.findViewById(R.id.text_add_photo);
        removePhotoTV = root.findViewById(R.id.text_remove_photo);
        firstNameET = root.findViewById(R.id.edit_text_first_name);
        lastNameET = root.findViewById(R.id.edit_text_last_name);
        dobET = root.findViewById(R.id.edit_text_dob);
        emailET = root.findViewById(R.id.edit_text_email);
        phoneET = root.findViewById(R.id.edit_text_telephone);
        streetLineOneET = root.findViewById(R.id.edit_text_street_one);
        streetLineTwoET = root.findViewById(R.id.edit_text_street_two);
        cityET = root.findViewById(R.id.edit_text_city);
        postcodeET = root.findViewById(R.id.edit_text_postcode);
        submitBtn = root.findViewById(R.id.button_submit);
    }
}