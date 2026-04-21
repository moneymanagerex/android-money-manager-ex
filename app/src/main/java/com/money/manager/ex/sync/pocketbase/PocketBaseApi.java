package com.money.manager.ex.sync.pocketbase;

import java.util.Map;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface PocketBaseApi {
    @POST("api/collections/users/auth-with-password")
    Call<PocketBaseAuthResponse> authenticate(@Body PocketBaseAuthRequest request);

    @GET("api/collections/{collection}/records")
    Call<PocketBaseListResponse> getRecords(
        @Header("Authorization") String token,
        @Path("collection") String collection,
        @Query("filter") String filter,
        @Query("sort") String sort
    );

    @POST("api/collections/{collection}/records")
    Call<ResponseBody> createRecord(
        @Header("Authorization") String token,
        @Path("collection") String collection,
        @Body Map<String, Object> data
    );

    @PATCH("api/collections/{collection}/records/{id}")
    Call<ResponseBody> updateRecord(
        @Header("Authorization") String token,
        @Path("collection") String collection,
        @Path("id") String id,
        @Body Map<String, Object> data
    );
}
