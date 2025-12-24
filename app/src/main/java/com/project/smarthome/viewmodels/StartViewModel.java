package com.project.smarthome.viewmodels;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.project.smarthome.models.homes.Home;
import com.project.smarthome.models.families.FamilyMember;
import com.project.smarthome.models.homes.room.Room;
import com.project.smarthome.models.homes.HomeRepository;
import com.project.smarthome.models.families.FamilyRepository;
import com.project.smarthome.models.homes.room.RoomRepositoryImpl;
import com.project.smarthome.utils.SharedPrefManager;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class StartViewModel extends ViewModel {

    private MutableLiveData<InitState> initState = new MutableLiveData<>();
    private MutableLiveData<String> error = new MutableLiveData<>();

    private HomeRepository homeRepository;
    private FamilyRepository familyRepository;
    private RoomRepositoryImpl roomRepository;
    private SharedPrefManager sharedPrefManager;

    public StartViewModel(Context context) {
        this.sharedPrefManager = SharedPrefManager.getInstance(context);
        // Инициализируем репозитории
        this.homeRepository = new HomeRepository();
        this.familyRepository = new FamilyRepository();
        this.roomRepository = new RoomRepositoryImpl();
    }

    public LiveData<InitState> getInitState() {
        return initState;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void loadInitState() {
        try {
            // Загружаем состояние инициализации асинхронно
            CompletableFuture.runAsync(() -> {
                InitState state = new InitState();

                try {
                    // Проверяем наличие дома
                    Home currentHome = getCurrentHome();
                    state.setHasHome(currentHome != null);

                    // Проверяем наличие семьи (если дом есть)
                    if (currentHome != null) {
                        List<FamilyMember> familyMembers = getFamilyMembers(currentHome.getId());
                        state.setHasFamily(familyMembers != null && !familyMembers.isEmpty());

                        // Проверяем наличие комнат (если дом есть)
                        List<Room> rooms = getRooms(currentHome.getId());
                        state.setHasRooms(rooms != null && !rooms.isEmpty());
                    }

                    initState.postValue(state);
                } catch (Exception e) {
                    error.postValue(e.getMessage());
                }
            });
        } catch (Exception e) {
            error.postValue(e.getMessage());
        }
    }

    private Home getCurrentHome() {
        // Используем SharedPrefManager для получения текущего активного дома
        long homeId = sharedPrefManager.getActiveHomeId();
        if (homeId != -1) {
            // Здесь должна быть логика получения дома по ID
            // Временно возвращаем null - нужно будет реализовать
            return null;
        }
        return null;
    }

    private List<FamilyMember> getFamilyMembers(int homeId) {
        // Временная реализация - нужно будет адаптировать под вашу архитектуру
        try {
            String token = sharedPrefManager.getToken();
            if (token != null && !token.isEmpty()) {
                // Вызовите метод из FamilyRepository для получения членов семьи
                // Временно возвращаем null - нужно будет реализовать
                return null;
            }
        } catch (Exception e) {
            // Логика обработки ошибки
        }
        return null;
    }

    private List<Room> getRooms(int homeId) {
        // Временная реализация - нужно будет адаптировать под вашу архитектуру
        try {
            String token = sharedPrefManager.getToken();
            if (token != null && !token.isEmpty()) {
                // Вызовите метод из RoomRepository для получения комнат
                // Временно возвращаем null - нужно будет реализовать
                return null;
            }
        } catch (Exception e) {
            // Логика обработки ошибки
        }
        return null;
    }

    // Внутренний класс для состояния инициализации
    public static class InitState {
        private boolean hasHome = false;
        private boolean hasFamily = false;
        private boolean hasRooms = false;

        public boolean hasHome() { return hasHome; }
        public void setHasHome(boolean hasHome) { this.hasHome = hasHome; }

        public boolean hasFamily() { return hasFamily; }
        public void setHasFamily(boolean hasFamily) { this.hasFamily = hasFamily; }

        public boolean hasRooms() { return hasRooms; }
        public void setHasRooms(boolean hasRooms) { this.hasRooms = hasRooms; }
    }
}


