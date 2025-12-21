package com.project.smarthome.models.families;

import com.project.smarthome.api.*;

import java.util.concurrent.CompletableFuture;

import retrofit2.*;


public class FamilyRepository {

    private ApiService api = ApiClient.getApiService();

    public CompletableFuture<FamilyResponse> createFamily(String token, String name, int homeId) {
        CompletableFuture<FamilyResponse> f = new CompletableFuture<>();

        FamilyCreateRequest req = new FamilyCreateRequest(name, homeId);

        api.createFamily(token, req).enqueue(new Callback<FamilyResponse>() {
            @Override
            public void onResponse(Call<FamilyResponse> call, Response<FamilyResponse> resp) {
                if (resp.isSuccessful()) {
                    f.complete(resp.body());
                } else {
                    f.completeExceptionally(new Exception("Failed to create family, code = " + resp.code()));
                }
            }

            @Override
            public void onFailure(Call<FamilyResponse> call, Throwable t) {
                f.completeExceptionally(t);
            }
        });

        return f;
    }

    public CompletableFuture<FamilyResponse> getFamily(String token, int familyId) {
        CompletableFuture<FamilyResponse> f = new CompletableFuture<>();

        api.getFamily(token, familyId).enqueue(new Callback<FamilyResponse>() {
            @Override
            public void onResponse(Call<FamilyResponse> call, Response<FamilyResponse> resp) {
                if (resp.isSuccessful()) {
                    f.complete(resp.body());
                } else {
                    f.completeExceptionally(new Exception("Family load failed, code = " + resp.code()));
                }
            }

            @Override
            public void onFailure(Call<FamilyResponse> call, Throwable t) {
                f.completeExceptionally(t);
            }
        });

        return f;
    }

    public CompletableFuture<FamilyResponse> updateFamily(String token, int familyId, String newName) {
        CompletableFuture<FamilyResponse> f = new CompletableFuture<>();

        FamilyUpdateRequest req = new FamilyUpdateRequest(newName);

        api.updateFamily(token, familyId, req).enqueue(new Callback<FamilyResponse>() {
            @Override
            public void onResponse(Call<FamilyResponse> call, Response<FamilyResponse> resp) {
                if (resp.isSuccessful()) {
                    f.complete(resp.body());
                } else {
                    f.completeExceptionally(new Exception("Update failed, code: " + resp.code()));
                }
            }

            @Override
            public void onFailure(Call<FamilyResponse> call, Throwable t) {
                f.completeExceptionally(t);
            }
        });

        return f;
    }
}

