package com.money.manager.ex.sync;

import com.google.gson.JsonObject;
import java.util.Map;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

/**
 * Retrofit interface for PocketBase Web API.
 */
public interface PocketBaseApiService {

    @POST("api/collections/{collection}/auth-with-password")
    Call<JsonObject> authWithPassword(@Path("collection") String collection, @Body JsonObject body);

    @POST("api/collections/{collection}/auth-refresh")
    Call<JsonObject> authRefresh(@Path("collection") String collection);

    @GET("api/collections/{collection}/records")
    Call<JsonObject> getRecords(
            @Path("collection") String collection,
            @QueryMap Map<String, String> options
    );

    @GET("api/collections/{collection}/records/{id}")
    Call<JsonObject> getOne(
            @Path("collection") String collection,
            @Path("id") String id
    );

    @POST("api/collections/{collection}/records")
    Call<JsonObject> create(
            @Path("collection") String collection,
            @Body JsonObject body
    );

    @PATCH("api/collections/{collection}/records/{id}")
    Call<JsonObject> update(
            @Path("collection") String collection,
            @Path("id") String id,
            @Body JsonObject body
    );

    @DELETE("api/collections/{collection}/records/{id}")
    Call<ResponseBody> delete(
            @Path("collection") String collection,
            @Path("id") String id
    );
}
