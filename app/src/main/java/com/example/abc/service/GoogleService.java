package com.example.abc.service;

import com.example.abc.model.DirectionApiResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GoogleService {
    @GET("directions/json")
    Call<DirectionApiResponse> getDirectionResponse(@Query("origin") String origin, @Query("destination") String destination, @Query("key") String key);
}
