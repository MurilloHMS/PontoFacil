package com.murillohms.pontofacil.ui.dashboard;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.murillohms.pontofacil.domain.entity.FuncionarioEntity;
import com.murillohms.pontofacil.domain.entity.RegistroPontoEntity;
import com.murillohms.pontofacil.infrastructure.repository.PontoRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DashboardViewModel extends AndroidViewModel {

    private final PontoRepository repository;
    private final MutableLiveData<String> totalHorasMes = new MutableLiveData<>();
    private final MutableLiveData<String> diasTrabalhados = new MutableLiveData<>();
    private final MutableLiveData<String> mediaDiaria = new MutableLiveData<>();
    private final LiveData<FuncionarioEntity> funcionarioAtivo;
    private LiveData<List<RegistroPontoEntity>> registrosMes;

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        repository = new PontoRepository(application);
        funcionarioAtivo = repository.getFuncionarioAtivo();


        registrosMes = Transformations.switchMap(funcionarioAtivo, funcionario -> {
            if (funcionario != null) {
                String mesAtual = new SimpleDateFormat("MM/yyyy", Locale.getDefault())
                        .format(Calendar.getInstance().getTime());
                return repository.getRegistrosByMes(funcionario.getId(), mesAtual);
            }
            return new MutableLiveData<>(new ArrayList<>());
        });
    }

    public void calcularEstatisticas(List<RegistroPontoEntity> registros) {
        if (registros == null || registros.isEmpty()) {
            totalHorasMes.setValue("0h 0min");
            diasTrabalhados.setValue("0");
            mediaDiaria.setValue("0h 0min");
            return;
        }

        int totalMinutos = 0;
        int dias = registros.size();

        for (RegistroPontoEntity registro : registros) {
            String horasTrabalhadas = registro.calcularHorasTrabalhadas();
            if (!horasTrabalhadas.equals("--:--")) {
                try {
                    String[] partes = horasTrabalhadas.split(":");
                    int horas = Integer.parseInt(partes[0]);
                    int minutos = Integer.parseInt(partes[1]);
                    totalMinutos += (horas * 60) + minutos;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        int totalHoras = totalMinutos / 60;
        int totalMin = totalMinutos % 60;

        int mediaMinutos = dias > 0 ? totalMinutos / dias : 0;
        int mediaHoras = mediaMinutos / 60;
        int mediaMin = mediaMinutos % 60;

        totalHorasMes.setValue(String.format(Locale.getDefault(), "%dh %dmin", totalHoras, totalMin));
        diasTrabalhados.setValue(String.valueOf(dias));
        mediaDiaria.setValue(String.format(Locale.getDefault(), "%dh %dmin", mediaHoras, mediaMin));
    }

    public LiveData<FuncionarioEntity> getFuncionarioAtivo() {
        return funcionarioAtivo;
    }

    public LiveData<List<RegistroPontoEntity>> getRegistrosMes() {
        return registrosMes;
    }

    public LiveData<String> getTotalHorasMes() {
        return totalHorasMes;
    }

    public LiveData<String> getDiasTrabalhados() {
        return diasTrabalhados;
    }

    public LiveData<String> getMediaDiaria() {
        return mediaDiaria;
    }
}