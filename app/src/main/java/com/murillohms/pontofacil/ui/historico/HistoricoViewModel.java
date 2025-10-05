package com.murillohms.pontofacil.ui.historico;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.murillohms.pontofacil.domain.entity.RegistroPontoEntity;
import com.murillohms.pontofacil.infrastructure.repository.PontoRepository;

import java.util.List;

public class HistoricoViewModel extends AndroidViewModel {

    private final PontoRepository repository;
    private final MutableLiveData<List<RegistroPontoEntity>> registrosLiveData = new MutableLiveData<>();
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
//        repository.getHistorico(funcionarioId, registros -> {
//            if (registros != null) {
//                registrosLiveData.postValue(registros);
//            } else {
//                mensagemErro.postValue("Não foi possível carregar o histórico.");
//            }
//        });
    }
}
