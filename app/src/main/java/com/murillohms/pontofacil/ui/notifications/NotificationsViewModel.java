package com.murillohms.pontofacil.ui.notifications;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.murillohms.pontofacil.domain.entity.FuncionarioEntity;
import com.murillohms.pontofacil.infrastructure.repository.PontoRepository;

public class NotificationsViewModel extends AndroidViewModel {

    private final PontoRepository repository;
    private final LiveData<FuncionarioEntity> funcionarioAtivo;
    private final MutableLiveData<Integer> totalRegistros = new MutableLiveData<>();

    public NotificationsViewModel(@NonNull Application application) {
        super(application);
        repository = new PontoRepository(application);
        funcionarioAtivo = repository.getFuncionarioAtivo();
    }

    public void limparHistorico(int funcionarioId) {
        repository.limparRegistros(funcionarioId);
    }

    public PontoRepository getRepository() {
        return repository;
    }

    public LiveData<FuncionarioEntity> getFuncionarioAtivo() {
        return funcionarioAtivo;
    }

    public LiveData<Integer> getTotalRegistros(int funcionarioId) {
        return repository.getCountRegistros(funcionarioId);
    }
}