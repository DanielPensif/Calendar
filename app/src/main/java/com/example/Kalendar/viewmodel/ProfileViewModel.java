package com.example.Kalendar.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.Kalendar.domain.GetUserUseCase;
import com.example.Kalendar.domain.UpdateUserUseCase;
import com.example.Kalendar.models.UserEntity;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ProfileViewModel extends ViewModel {
    private final GetUserUseCase getUser;
    private final UpdateUserUseCase updateUser;
    private final MutableLiveData<Integer> userId = new MutableLiveData<>();
    public final LiveData<UserEntity> user;

    @Inject
    public ProfileViewModel(GetUserUseCase getUser, UpdateUserUseCase updateUser) {
        this.getUser = getUser;
        this.updateUser = updateUser;

        // Инициализируем user после установки getUser
        user = Transformations.switchMap(userId, id -> this.getUser.execute(id));
    }

    public void load(int id) {
        userId.setValue(id);
    }

    public void saveName(String name, UserEntity current) {
        current.setName(name);
        updateUser.execute(current);
    }

    public void saveDescription(String desc, UserEntity current) {
        current.setDescription(desc);
        updateUser.execute(current);
    }

    public void savePhotoUri(String uri, UserEntity current) {
        current.setPhotoUri(uri);
        updateUser.execute(current);
    }
}