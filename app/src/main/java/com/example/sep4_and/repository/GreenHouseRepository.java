package com.example.sep4_and.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.sep4_and.BuildConfig;
import com.example.sep4_and.dao.AppDatabase;
import com.example.sep4_and.dao.GreenHouseDao;

import com.example.sep4_and.model.GreenHouse;
import com.example.sep4_and.network.RetrofitInstance;
import com.example.sep4_and.network.api.GreenHouseApi;
import com.example.sep4_and.utils.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GreenHouseRepository {
    private GreenHouseDao greenHouseDao;
    private GreenHouseApi greenHouseApi;
    private ExecutorService executorService;

    public GreenHouseRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        greenHouseDao = db.greenHouseDao();
        greenHouseApi = RetrofitInstance.getClient(BuildConfig.BASE_URL).create(GreenHouseApi.class);
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<Long> insert(GreenHouse greenHouse) {
        MutableLiveData<Long> result = new MutableLiveData<>();
        executorService.execute(() -> {
            if (Config.isUseApi()) {
                greenHouseApi.createGreenHouse(greenHouse).observeForever(response -> {
                    if (response != null) {
                        result.postValue((long) response.getId()); // Convert int to Long
                    } else {
                        result.postValue(null);
                    }
                });
            } else {
                long id = greenHouseDao.insert(greenHouse);
                result.postValue(id);
            }
        });
        return result;
    }

    public LiveData<List<GreenHouse>> getGreenHousesByUserId(int userId) {
        if (Config.isUseApi()) {
            MutableLiveData<List<GreenHouse>> result = new MutableLiveData<>();
            greenHouseApi.getUserGreenhouses(userId).observeForever(result::postValue);
            return result;
        } else {
            return greenHouseDao.getGreenHousesByUserId(userId);
        }
    }

    public void delete(GreenHouse greenHouse) {
        executorService.execute(() -> {
            if (Config.isUseApi()) {
                greenHouseApi.deleteGreenHouse(greenHouse.getId()).observeForever(response -> {
                    // handle API response
                });
            } else {
                greenHouseDao.delete(greenHouse);
            }
        });
    }

    public LiveData<GreenHouse> getGreenHouseById(int greenHouseId) {
        if (Config.isUseApi()) {
            MutableLiveData<GreenHouse> result = new MutableLiveData<>();
            greenHouseApi.getGreenhouseById(greenHouseId).observeForever(result::postValue);
            return result;
        } else {
            return greenHouseDao.getGreenHouseById(greenHouseId);
        }
    }
}