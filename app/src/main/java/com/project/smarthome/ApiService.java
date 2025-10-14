package com.project.smarthome;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public class ApiService extends Service {
    public ApiService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}