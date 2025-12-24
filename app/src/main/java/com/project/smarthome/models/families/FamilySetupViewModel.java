package com.project.smarthome.models.families;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.project.smarthome.utils.SharedPrefManager;

import java.util.List;

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
                .thenAccept(response -> {
                    if (response != null && response.getMembers() != null) {
                        members.postValue(response.getMembers());
                    }
                })
                .exceptionally(t -> {
                    error.postValue(t.getMessage());
                    return null;
                });
    }

    public void inviteUser(String username, int familyId) {
        String token = SharedPrefManager.getInstance().getToken();

        // создаем объект для добавления участника
        FamilyMemberAdd addReq = new FamilyMemberAdd(username);

        // В зависимости от API, используем createFamily или отдельный endpoint addMember
        repository.createFamily(token, username, familyId)
                .thenAccept(response -> {
                    if (response != null && response.getMembers() != null) {
                        members.postValue(response.getMembers());
                    }
                })
                .exceptionally(t -> {
                    error.postValue(t.getMessage());
                    return null;
                });
    }
}
