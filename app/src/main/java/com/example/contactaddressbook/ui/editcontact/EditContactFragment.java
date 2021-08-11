package com.example.contactaddressbook.ui.editcontact;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.contactaddressbook.R;
import com.example.contactaddressbook.databinding.FragmentEditContactBinding;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;


public class EditContactFragment extends Fragment {

    // class variables
    private EditContactViewModel editContactViewModel;
    private FragmentEditContactBinding binding;
    private static final int REQUEST_CALL = 1;
    private final String TAG = "EditContactFragment";
    private String contactID = "";

    // xml variables
    private View root;
    private Dialog loadingDialog;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // initialise the loading dialog
        loadingDialog = new Dialog(getContext());
        loadingDialog.setContentView(R.layout.dialog_loading);
        loadingDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        if (getArguments() != null) contactID = getArguments().getString("contactID");

        // initialise view model and binding
        editContactViewModel =
                new ViewModelProvider(this).get(EditContactViewModel.class);
        editContactViewModel.setContactID(contactID);
        binding = FragmentEditContactBinding.inflate(getLayoutInflater());
        binding.setEditContactViewModel(editContactViewModel);
        root = binding.getRoot();

        setEditTextObservables();
        editContactViewModel.loadContactData();
        setOnClickListenersAddPhoto();
        setOnClickListenerCallContact();
        setProfileImageUrlObservable();
        setDialogEventListener();
        setNavigateEventListener();
        setToastEventListener();
        setLoadingDialogListener();
        initialiseToolbar();

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadingDialog.hide();
    }

    /**
     * Set the observables for edit text fields to update when
     * corresponding Mutable Live Data is changed.
     */
    public void setEditTextObservables() {
        // initialise first name edit text observable
        editContactViewModel.getFirstName().observe(getViewLifecycleOwner(), s -> {
            if (!binding.editTextFirstName.getText().toString().equals(s)) {
                binding.editTextFirstName.setText(s);
            }
        });
        // initialise last name edit text observable
        editContactViewModel.getLastName().observe(getViewLifecycleOwner(), s -> {
            if (!binding.editTextLastName.getText().toString().equals(s)) {
                binding.editTextLastName.setText(s);
            }
        });
        // initialise phone number edit text observable
        editContactViewModel.getPhone().observe(getViewLifecycleOwner(), s -> {
            if (!binding.editTextTelephone.getText().toString().equals(s)) {
                binding.editTextTelephone.setText(s);
            }
        });
        // initialise email edit text observable
        editContactViewModel.getEmail().observe(getViewLifecycleOwner(), s -> {
            if (!binding.editTextEmail.getText().toString().equals(s)) {
                binding.editTextEmail.setText(s);
            }
        });
        // initialise dob edit text observable
        editContactViewModel.getDob().observe(getViewLifecycleOwner(), s -> {
            if (!binding.editTextDob.getText().toString().equals(s)) {
                binding.editTextDob.setText(s);
            }
        });
        // initialise street one edit text observable
        editContactViewModel.getStreetOne().observe(getViewLifecycleOwner(), s -> {
            if (!binding.editTextStreetOne.getText().toString().equals(s)) {
                binding.editTextStreetOne.setText(s);
            }
        });
        // initialise street two edit text observable
        editContactViewModel.getStreetTwo().observe(getViewLifecycleOwner(), s -> {
            if (!binding.editTextStreetTwo.getText().toString().equals(s)) {
                binding.editTextStreetTwo.setText(s);
            }
        });
        // initialise city edit text observable
        editContactViewModel.getCity().observe(getViewLifecycleOwner(), s -> {
            if (!binding.editTextCity.getText().toString().equals(s)) {
                binding.editTextCity.setText(s);
            }
        });
        // initialise street two edit text observable
        editContactViewModel.getPostcode().observe(getViewLifecycleOwner(), s -> {
            if (!binding.editTextPostcode.getText().toString().equals(s)) {
                binding.editTextPostcode.setText(s);
            }
        });
    }

    /**
     * Observe the contact's profile image Uri and set
     * the ui image accordingly.
     */
    public void setProfileImageUrlObservable() {
        editContactViewModel.getProfileImageURL().observe(getViewLifecycleOwner(), s -> {
            if (getContext() != null) {
                if (!s.toString().equals("null")) {
                    Glide.with(getContext()).load(s).into(binding.imageViewProfile);
                    binding.textRemovePhoto.setText(
                            getResources().getString(R.string.text_remove_photo));
                    binding.textAddPhoto.setText("");
                } else {
                    int image = R.drawable.ic_baseline_account_circle_24;
                    binding.imageViewProfile.setImageResource(image);
                    binding.textRemovePhoto.setText("");
                    binding.textAddPhoto.setText(getResources().getString(R.string.text_add_photo));
                }
            }
        });
    }

    /**
     * Set the listener to show the Date Picker Dialog when the a dialog event
     * has been received.
     */
    public void setDialogEventListener() {
        binding.editTextDob.setOnClickListener(v -> {
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

    /**
     * Set the listener to navigate back to the Contacts fragment
     * when a navigation event has been received.
     */
    public void setNavigateEventListener() {
        // set the listener for the navigate event
        editContactViewModel.getNavigateEvent().observe(this, o -> {
            // navigate back to the contacts fragment
            Navigation.findNavController(root).navigate(
                    R.id.action_navigation_edit_contact_to_navigation_contacts);
            Toast.makeText(getContext(), o.toString(), Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Set the listener to show or hide the loading dialog
     * depending on the state of the isLoading variable in NewContactViewModel.
     */
    public void setLoadingDialogListener() {
        // set the listener for loading status
        editContactViewModel.getIsLoading().observe(getViewLifecycleOwner(), o -> {
            if (editContactViewModel.getIsLoading().getValue() != null) {
                if (editContactViewModel.getIsLoading().getValue()) {
                    loadingDialog.show();
                } else {
                    loadingDialog.hide();
                }
            }

        });
    }

    /**
     * Set the listener for toast events.
     * Displays the given toast message when an event is received.
     */
    public void setToastEventListener() {
        editContactViewModel.getToastEvent().observe(this, o -> {
            // generate the toast message
            Toast.makeText(getContext(), o.toString(), Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Initialise the toolbar and set the back button in the menu listener.
     */
    private void initialiseToolbar() {
        Toolbar toolbar = root.findViewById(R.id.toolbar_edit);
        toolbar.inflateMenu(R.menu.edit_contact_menu);
        binding.toolbarTitle.setText(getToolbarTitle());

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
        String title = "";
        if (getArguments() != null) {
            String firstName = getArguments().getString("firstName");
            String lastName = getArguments().getString("lastName");
            title = firstName + " " + lastName;
        }
        return title;
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
                            editContactViewModel.setNewProfileImageUrl(data.getData());
                            binding.imageViewProfile.setImageURI(
                                    editContactViewModel.getNewProfileImageUrl().getValue());
                            binding.textAddPhoto.setText("");
                            binding.textRemovePhoto.setText(
                                    getResources().getString(R.string.text_remove_photo));
                        } else {
                            Toast.makeText(getContext(), "No image selected",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        // set the onClick for the image view.
        binding.imageViewProfile.setOnClickListener(v -> {
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
        binding.textAddPhoto.setOnClickListener(v -> {
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
     * Set the onClickListener for the call contact image view.
     */
    private void setOnClickListenerCallContact() {
        binding.imageViewPhoneCall.setOnClickListener(v -> {
            try {
                String number = Objects.requireNonNull(binding.editTextTelephone.getText()).toString();
                // check if phone number is valid
                if (number.trim().length() > 0) {
                    // ask for permissions if not currently granted
                    if (ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL);
                    } else {
                        // make the phone call
                        Intent callIntent = new Intent(Intent.ACTION_CALL);
                        callIntent.setData(Uri.parse("tel:" + number));
                        startActivity(callIntent);
                    }
                } else {
                    Toast.makeText(getContext(),
                            "Enter a phone number", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.d(TAG, "Issue calling contact: " + e.getMessage());
            }
        });
    }
}
