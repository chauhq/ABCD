package com.example.abc.service;

import com.example.abc.model.DirectionApiResponse;
import com.example.abc.model.GeoResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GoogleService {
    @GET("maps/api/directions/json")
    Call<DirectionApiResponse> getDirectionResponse(@Query("origin") String origin, @Query("destination") String destination, @Query("key") String key);

    @GET("maps/api/geocode/json")
    Call<GeoResponse> getGeoResponse(@Query("latlng") String latlng, @Query("sensor") boolean sensor, @Query("key") String key);
}
