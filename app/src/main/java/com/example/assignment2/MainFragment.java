package com.example.assignment2;

import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.assignment2.databinding.FragmentMainBinding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MainFragment extends Fragment {

    private FragmentMainBinding binding;
    DatabaseHelper mDatabaseHelper;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        mDatabaseHelper = new DatabaseHelper(requireContext());
        binding = FragmentMainBinding.inflate(inflater, container, false);

        // Create database with 50 locations
        if(mDatabaseHelper.checkEmpty()){
            Random rand = new Random();
            mDatabaseHelper.addData("2000 Simcoe St N", "43.95", "-78.89");
            mDatabaseHelper.addData("Taj Mahal", "27.18", "78.04");
            mDatabaseHelper.addData("419 King St W, Oshawa", "43.89", "-78.88");
            mDatabaseHelper.addData("220 Yonge St, Toronto", "43.65", "-79.38");
            mDatabaseHelper.addData("1800 Sheppard Ave, North York", "43.76", "-79.41");
            for (int i = 0; i < 45; i++){
                int latitude = rand.nextInt(90) * (rand .nextBoolean() ? -1 : 1);
                int longitude = rand.nextInt(180) * (rand .nextBoolean() ? -1 : 1);
                String address = getAddress(Double.valueOf(latitude), Double.valueOf(longitude));
                mDatabaseHelper.addData(address, Integer.toString(latitude), Integer.toString(longitude));
            }
        }
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.latitude.setVisibility(View.INVISIBLE);
        binding.longitude.setVisibility(View.INVISIBLE);

        binding.addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String address = binding.findInputBar.getText().toString();
                if(!address.isEmpty()){
                    ArrayList<Double> coordinates = getLocationFromAddress(address);
                    if(coordinates.size()>0){
                        long success = mDatabaseHelper.addData(address, Double.toString(coordinates.get(0)), Double.toString(coordinates.get(1)));
                        if (success > 0){
                            Toast.makeText(requireContext() , "Added Successful", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(requireContext() , "No data with given address", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(requireContext() , "Enter address", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.findButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String address = binding.findInputBar.getText().toString();
                ArrayList<String> coordinates = getCoordinatesFromDatabase(address);
                if(coordinates.size() > 0){
                    binding.latitude.setText(coordinates.get(0));
                    binding.longitude.setText(coordinates.get(1));
                    binding.latitude.setVisibility(View.VISIBLE);
                    binding.longitude.setVisibility(View.VISIBLE);
                }else{
                    Toast.makeText(requireContext() , "No data with given address", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String address = binding.findInputBar.getText().toString();
                if(!address.isEmpty()) {
                    ArrayList<Double> coordinates = getLocationFromAddress(address);
                    if (coordinates.size() > 0) {
                        int success = mDatabaseHelper.update(address, String.format("%.2f", coordinates.get(0)), String.format("%.2f", coordinates.get(1)));
                        if (success > 0) {
                            Toast.makeText(requireContext(), "Update Successful", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), "No address: " + address + " in the database", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(requireContext(), "No data with given address", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(requireContext(), "Enter address", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String address = binding.findInputBar.getText().toString();
                int success = mDatabaseHelper.deleteGivenAddress(address);
                if (success > 0){
                    Toast.makeText(requireContext(), "Delete Successful", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(requireContext(), "No data with given address", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.enterCoords.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(MainFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // Retrieve the coordinates from the database given the address
    public ArrayList<String> getCoordinatesFromDatabase(String address){
        Cursor data = mDatabaseHelper.getCoordinates(address);
        ArrayList<String> coords = new ArrayList<>();
        if(data.moveToNext()){
            coords.add(data.getString(2));
            coords.add(data.getString(3));
        }
        return coords;
    }


    // Retrieve coordinates using geocoding given the address
    public ArrayList<Double> getLocationFromAddress(String address){

        if (Geocoder.isPresent()) {
            Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
            ArrayList<Double> coords = new ArrayList<>();
            try {
                List<Address> ls= geocoder.getFromLocationName(address,1);
                for (Address addr: ls) {
                    coords.add(addr.getLatitude());
                    coords.add(addr.getLongitude());
                }
                return coords;
            } catch (IOException e) {
                e.printStackTrace();
            }}
        return null;
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