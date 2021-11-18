package com.example.assignment2;

import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.assignment2.databinding.FragmentCoordinateBinding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CoordinateFragment extends Fragment {

    private FragmentCoordinateBinding binding;
    DatabaseHelper mDatabaseHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        mDatabaseHelper = new DatabaseHelper(requireContext());
        binding = FragmentCoordinateBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.address.setVisibility(View.INVISIBLE);

        binding.addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String address = null;
                String lat = binding.latInputBar.getText().toString();
                String lng = binding.longInputBar.getText().toString();
                if(!lat.isEmpty() && !lng.isEmpty()){
                    address = getAddress(Double.parseDouble(lat), Double.parseDouble(lng));
                    if(!address.isEmpty()) {
                        long success = mDatabaseHelper.addData(address.toLowerCase(), lat, lng);
                        if (success > 0) {
                            Toast.makeText(requireContext(), "Added Successful", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(requireContext(), "Error while adding", Toast.LENGTH_LONG).show();
                        }
                    }
                }else{
                    Toast.makeText(requireContext() , "Enter coordinates", Toast.LENGTH_LONG).show();
                }
            }
        });

        binding.findButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String lat = binding.latInputBar.getText().toString();
                String lng = binding.longInputBar.getText().toString();
                String address = getAddressFromDatabase(lat, lng);
                if(!address.isEmpty()){
                    binding.address.setText(address);
                    binding.address.setVisibility(View.VISIBLE);
                }else{
                    Toast.makeText(requireContext() , "No data with given coordinates", Toast.LENGTH_LONG).show();
                }
            }
        });

        binding.updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String lat = binding.latInputBar.getText().toString();
                String lng = binding.longInputBar.getText().toString();
                String address = null;
                if(!lat.isEmpty() && lng.isEmpty()) {
                    address = getAddress(Double.parseDouble(lat), Double.parseDouble(lng));
                    if(!address.isEmpty()){
                        int success = mDatabaseHelper.update(address.toLowerCase(), lat, lng);
                        if (success > 0){
                            Toast.makeText(requireContext() , "Update Successful", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(requireContext() , "No latitude: " + lat + " longitude: "+lng+" in the database", Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(requireContext() , "No data with given coordinates", Toast.LENGTH_LONG).show();
                }
            }
        });

        binding.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String lat = binding.latInputBar.getText().toString();
                String lng = binding.longInputBar.getText().toString();
                int success = mDatabaseHelper.deleteGivenCoordinates(lat, lng);
                if (success > 0){
                    Toast.makeText(requireContext(), "Delete Successful", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(requireContext(), "No data with given coordinates", Toast.LENGTH_LONG).show();
                }
            }
        });

        binding.enterCoords.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(CoordinateFragment.this)
                        .navigate(R.id.action_SecondFragment_to_FirstFragment);
            }
        });
    }

    // Retrieve address from the database given the coordinates
    public String getAddressFromDatabase(String lat, String lng){
        Cursor data = mDatabaseHelper.getAddress(lat, lng);
        String address = "";
        if(data.moveToNext()){
            address = data.getString(1);
        }
        return address;
    }

    // Retrieve address using geocoding given the coordinates
    public String getAddress(double lat, double lng){

        if (Geocoder.isPresent()){
            Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
            try {
                if ((-90 <= lat && lat < 90) && (-180 <= lng && lng <= 180)){
                    List<Address> ls = geocoder.getFromLocation(lat,  lng, 1);
                    String address1 = "";
                    for (Address address: ls) {
                        String name = address.getFeatureName();
                        address1 = address.getAddressLine(0);
                        String city = address.getLocality();
                        String county = address.getSubAdminArea();
                        String prov = address.getAdminArea();
                        String country = address.getCountryName();
                        String postalCode = address.getPostalCode();
                        String phone = address.getPhone();
                        String url = address.getUrl();
                    }
                    return address1;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }}
        return "";
    }
}