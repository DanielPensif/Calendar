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
    private GetUserUseCase getUser;
    private final UpdateUserUseCase updateUser;

    private final MutableLiveData<Integer> userId = new MutableLiveData<>();

    public final LiveData<UserEntity> user = Transformations.switchMap(userId, getUser::execute);

    @Inject
    public ProfileViewModel(GetUserUseCase g, UpdateUserUseCase u) {
        this.getUser    = g;
        this.updateUser = u;
    }

    public void load(int id) {
        userId.setValue(id);
    }

    public void saveName(String name, UserEntity current) {
        current.name = name;
        updateUser.execute(current);
    }

    public void saveDescription(String desc, UserEntity current) {
        current.description = desc;
        updateUser.execute(current);
    }

    public void savePhotoUri(String uri, UserEntity current) {
        current.photoUri = uri;
        updateUser.execute(current);
    }
}