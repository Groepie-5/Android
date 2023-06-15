package com.example.streamapplication;

import com.example.streamapplication.models.StreamSession;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.PUT;

public interface RetrofitAPI {
    @PUT("your-api-endpoint")
    Call<StreamSession> updateData(@Body StreamSession data);
}
