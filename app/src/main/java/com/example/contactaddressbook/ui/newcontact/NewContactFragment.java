package com.example.contactaddressbook.ui.newcontact;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentResolver;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.contactaddressbook.R;
import com.example.contactaddressbook.databinding.FragmentNewContactBinding;
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

    private final String TAG = "NewContactFragment";
    private Uri profileImageURL;
    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageReference;
    private NewContactViewModel newContactViewModel;
    private FragmentNewContactBinding binding;

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

        // initialise Firebase variables
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("ImageFolder");

        //root = inflater.inflate(R.layout.fragment_new_contact, container, false);

        // initialise the loading dialog
        loadingDialog = new Dialog(getContext());
        loadingDialog.setContentView(R.layout.dialog_loading);

        // initialiseViews();
        // setOnClickListenerImageIV();
        // setOnClickListenerDobPicker();
        // setOnClickListenerSubmitBtn();
        // setOnClickListenerRemovePhoto();

        newContactViewModel =
                new ViewModelProvider(this).get(NewContactViewModel.class);

        binding = FragmentNewContactBinding.inflate(getLayoutInflater());
        binding.setNewContactViewModel(newContactViewModel);
        binding.setOnclicklistener(newContactViewModel);
        root = binding.getRoot();

        setDialogEventListener();
        setNavigateEventListener();
        setOnClickListenerImageIV();


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

    public void setDialogEventListener() {
        newContactViewModel.getDialogEvent().observe(this, o -> {
            Calendar calendar = Calendar.getInstance();
            // initialise the DateSetListener
            DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                String dateFormat = "dd/MM/yyyy";
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat, Locale.UK);
                binding.editTextDob.setText(simpleDateFormat.format(calendar.getTime()));
            };

            // Initialise the data picker dialog and show it
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    getContext(),
                    R.style.MyDialogTheme,
                    dateSetListener,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.setButton(DatePickerDialog.BUTTON_NEUTRAL, "Clear",
                    (dialog, which) -> binding.editTextDob.setText(""));
            datePickerDialog.show();
        });
    }

    public void setNavigateEventListener() {
        // set the listener for the navigate event
        newContactViewModel.getNavigateEvent().observe(this, o -> {
            // navigate back to the contacts fragment
            Navigation.findNavController(root).navigate(
                    R.id.action_navigation_new_contact_to_navigation_contacts);
            Toast.makeText(getContext(), o.toString(), Toast.LENGTH_SHORT).show();
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
                            newContactViewModel.setProfileImageURL(data.getData());
                            binding.imageViewProfile.setImageURI(
                                    newContactViewModel.getProfileImageURL().getValue());
                        } else {
                            Toast.makeText(getContext(), "No image selected",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        // set the onClickListener for the Image View
        binding.imageViewProfile.setOnClickListener(v -> {
            try {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                imageGalleryResultLauncher.launch(galleryIntent);
            } catch (Exception e) {
                Log.d(TAG, "Couldn't open image gallery: " + e.getMessage());
            }
        });

        binding.textAddPhoto.setOnClickListener(v -> {
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
        binding.textRemovePhoto.setOnClickListener(v -> {
            int image = R.drawable.ic_baseline_account_circle_24;
            binding.imageViewProfile.setImageResource(image);
            newContactViewModel.setProfileImageURL(null);
            removePhotoTV.setText("");
            binding.textRemovePhoto.setText("");
            binding.textAddPhoto.setText(getResources().getString(R.string.text_add_photo));
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
}