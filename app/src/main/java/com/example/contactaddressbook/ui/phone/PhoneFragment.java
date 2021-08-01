package com.example.contactaddressbook.ui.phone;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.contactaddressbook.R;

public class PhoneFragment extends Fragment {

    // xml variables
    private View root;
    private Toolbar toolbar;

    private PhoneViewModel phoneViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        phoneViewModel =
                new ViewModelProvider(this).get(PhoneViewModel.class);
        root = inflater.inflate(R.layout.fragment_phone, container, false);
        toolbar = root.findViewById(R.id.toolbar_phone);
        toolbar.setTitle(getResources().getString(R.string.title_phone));

        final TextView textView = root.findViewById(R.id.text_home);

        phoneViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        return root;
    }
}