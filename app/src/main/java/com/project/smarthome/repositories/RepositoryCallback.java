package com.project.smarthome.repositories;

public interface RepositoryCallback<T> {
    void onSuccess(T data);

    void onError(String errorMessage);
}
