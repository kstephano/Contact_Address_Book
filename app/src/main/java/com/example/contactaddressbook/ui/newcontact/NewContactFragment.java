package com.example.contactaddressbook.ui.newcontact;

import android.app.Activity;
import android.app.DatePickerDialog;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.contactaddressbook.R;
import com.example.contactaddressbook.databinding.NewContactFragmentBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.protobuf.Any;

import org.intellij.lang.annotations.JdkConstants;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class NewContactFragment extends Fragment {

    //private NewContactViewModel newContactViewModel;
    //private NewContactFragmentBinding binding;

    private static int REQUEST_CODE = 1;
    private String TAG = "NewContactViewModel";
    private Uri profileImageURL;
    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageReference;
    private Calendar calendar = Calendar.getInstance();

    // xml variables
    private View root;
    private CircleImageView profileIV;
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

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        /*
        newContactViewModel =
           new ViewModelProvider(this).get(NewContactViewModel.class);
        View root = inflater.inflate(R.layout.new_contact_fragment, container, false);

        binding = DataBindingUtil.inflate(inflater, R.layout.new_contact_fragment, container, false);
        binding = NewContactFragmentBinding.inflate(inflater, container, false);
        newContactViewModel = new ViewModelProvider(this).get(NewContactViewModel.class);
        View root = binding.getRoot();
        binding.setNewContactViewModel(newContactViewModel);


        /*
        final EditText firstNameET = root.findViewById(R.id.edit_text_first_name);
        final EditText lastNameET = root.findViewById(R.id.edit_text_last_name);
        final EditText phoneET = root.findViewById(R.id.edit_text_telephone);
        final EditText emailET = root.findViewById(R.id.edit_text_email);
        final EditText dobET = root.findViewById(R.id.edit_text_dob);
        final EditText streetOneET = root.findViewById(R.id.edit_text_street_one);
        final EditText streetTwoET = root.findViewById(R.id.edit_text_street_two);
        final EditText cityET = root.findViewById(R.id.edit_text_city);
        final EditText postcodeET = root.findViewById(R.id.edit_text_postcode);
        */
        /*
        newContactViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
         */
        // initialise Firebase variables
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("ImageFolder");

        root = inflater.inflate(R.layout.new_contact_fragment, container, false);
        initialiseViews();
        setOnClickListenerImageIV();
        setOnClickListenerDobPicker();
        setOnClickListenerSubmitBtn();

        return root;
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
     * Uploads the form to Firebase, creating a new contact.
     */
    private void upLoadToFirebase() {
        Uri localProfileImageURL = profileImageURL;
        HashMap<String, Object> contactData = new HashMap<>();

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
                    String contactID = firstNameET.getText().toString() + lastNameET.getText().toString() +
                            phoneET.getText().toString();

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
                                // navigate back to the contacts fragment
                                Navigation.findNavController(root).navigate(
                                        R.id.action_navigation_new_contact_to_navigation_contacts);
                                Toast.makeText(getContext(), "Contact added", Toast.LENGTH_SHORT).show();
                            });
                }
            });
        } else {
            // upload with null image url
            String contactID = firstNameET.getText().toString() + lastNameET.getText().toString() +
                    phoneET.getText().toString();

            contactData.put("profileImageURL", "null");
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
                        // navigate back to the contacts fragment
                        Navigation.findNavController(root).navigate(
                                R.id.action_navigation_new_contact_to_navigation_contacts);
                        Toast.makeText(getContext(), "Contact added", Toast.LENGTH_SHORT).show();
                    });
        }

    }

    /**
     * Set the onClickListener for the submit button.
     */
    private void setOnClickListenerSubmitBtn() {
        submitBtn.setOnClickListener(v -> {
            if (isFormValid() == true) {
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

        // set the onClick for the image view
        profileIV.setOnClickListener(v -> {
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