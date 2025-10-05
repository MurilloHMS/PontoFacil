package com.murillohms.pontofacil.ui.historico;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.murillohms.pontofacil.domain.entity.RegistroPontoEntity;
import com.murillohms.pontofacil.infrastructure.repository.PontoRepository;

import java.util.List;

public class HistoricoViewModel extends AndroidViewModel {

    private final PontoRepository repository;
    private final MediatorLiveData<List<RegistroPontoEntity>> registrosLiveData = new MediatorLiveData<>();

    private final MutableLiveData<String> mensagemErro = new MutableLiveData<>();

    public HistoricoViewModel(@NonNull Application application) {
        super(application);
        repository = new PontoRepository(application);
    }

    public LiveData<List<RegistroPontoEntity>> getRegistrosLiveData() {
        return registrosLiveData;
    }

    public LiveData<String> getMensagemErro() {
        return mensagemErro;
    }

    public void carregarHistorico(int funcionarioId) {
        registrosLiveData.addSource(
                repository.getRegistrosByFuncionario(funcionarioId),
                registros -> {
                    if (registros != null) {
                        registrosLiveData.setValue(registros);
                    } else {
                        mensagemErro.setValue("Não foi possível carregar o histórico.");
                    }
                }
        );
    }


}
