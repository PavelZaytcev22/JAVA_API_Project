package com.project.smarthome.models.families;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.project.smarthome.api.SharedPrefManager;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FamilySetupViewModel extends ViewModel {

    private final FamilyRepository repository = new FamilyRepository();

    private final MutableLiveData<List<FamilyMember>> members = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public LiveData<List<FamilyMember>> getMembers() {
        return members;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void loadMembers(int familyId) {
        String token = SharedPrefManager.getInstance().getToken();

        repository.getFamily(token, familyId)
                .thenAccept(response -> members.postValue(response.getMembers()))
                .exceptionally(throwable -> {
                    error.postValue(throwable.getMessage());
                    return null;
                });
    }

    public void inviteUser(String username, int familyId) {
        String token = SharedPrefManager.getInstance().getToken();

        // Формируем DTO для API
        FamilyMemberAdd addReq = new FamilyMemberAdd(username);

        repository.createFamily(token, username, familyId) // если у вас есть отдельный endpoint для добавления участников — замените
                .thenAccept(response -> members.postValue(response.getMembers()))
                .exceptionally(throwable -> {
                    error.postValue(throwable.getMessage());
                    return null;
                });
    }
}
