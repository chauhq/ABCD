package com.example.abc.model;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AddressComponent;
import com.google.android.libraries.places.api.model.Place;

import java.util.List;

public class GeoResponse {
    private List<MyPlace> results;
    private String status;

    public GeoResponse() {

    }

    public GeoResponse(List<MyPlace> results, String status) {
        this.results = results;
        this.status = status;
    }

    public List<MyPlace> getResults() {
        return results;
    }

    public void setResults(List<MyPlace> results) {
        this.results = results;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public class MyPlace {

        private List<AddressComponents> address_components;

        public MyPlace() {

        }

        public List<AddressComponents> getAddress_components() {
            return address_components;
        }

        public void setAddress_components(List<AddressComponents> address_components) {
            this.address_components = address_components;
        }
    }


    public class AddressComponents {
        private String long_name;
        private String short_name;
        private List<String> types;


        public String getLong_name() {
            return long_name;
        }

        public void setLong_name(String long_name) {
            this.long_name = long_name;
        }

        public String getShort_name() {
            return short_name;
        }

        public void setShort_name(String short_name) {
            this.short_name = short_name;
        }

        public List<String> getTypes() {
            return types;
        }

        public void setTypes(List<String> types) {
            this.types = types;
        }
    }
}
