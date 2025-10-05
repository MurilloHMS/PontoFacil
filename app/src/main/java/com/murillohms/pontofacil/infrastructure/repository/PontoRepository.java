package com.murillohms.pontofacil.infrastructure.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.murillohms.pontofacil.domain.dao.FuncionarioDao;
import com.murillohms.pontofacil.domain.dao.RegistroPontoDao;
import com.murillohms.pontofacil.domain.database.PontoDatabase;
import com.murillohms.pontofacil.domain.entity.FuncionarioEntity;
import com.murillohms.pontofacil.domain.entity.RegistroPontoEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PontoRepository {

    private RegistroPontoDao registroPontoDao;
    private FuncionarioDao funcionarioDao;
    private ExecutorService executorService;

    public PontoRepository(Application application){
        PontoDatabase database = PontoDatabase.getInstance(application);
        registroPontoDao = database.registroPontoDao();
        funcionarioDao = database.funcionarioDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void inserirFuncionario(FuncionarioEntity funcionario, OnSuccessListener listener){
        executorService.execute(() -> {
            funcionarioDao.desativarTodos();
            long id = funcionarioDao.insert(funcionario);
            if(listener != null){
                listener.onSuccess(id);
            }
        });
    }

    public void atualizarFuncionario(FuncionarioEntity funcionario) {
        executorService.execute(() -> funcionarioDao.update(funcionario));
    }

    public LiveData<FuncionarioEntity> getFuncionarioAtivo() {
        return funcionarioDao.getFuncionarioAtivo();
    }

    public FuncionarioEntity getFuncionarioAtivoSync() {
        return funcionarioDao.getFuncionarioAtivoSync();
    }

    public void inserirRegistro(RegistroPontoEntity registro, OnSuccessListener listener) {
        executorService.execute(() -> {
            long id = registroPontoDao.insert(registro);
            if (listener != null) {
                listener.onSuccess(id);
            }
        });
    }

    public void atualizarRegistro(RegistroPontoEntity registro) {
        executorService.execute(() -> registroPontoDao.update(registro));
    }

    public LiveData<List<RegistroPontoEntity>> getRegistrosByFuncionario(int funcionarioId) {
        return registroPontoDao.getAllByFuncionario(funcionarioId);
    }

    public LiveData<List<RegistroPontoEntity>> getRegistrosByMes(int funcionarioId, String mesAno) {
        return registroPontoDao.getByMes(funcionarioId, mesAno);
    }

    public void getRegistrosByMesAsync(int funcionarioId, String mesAno, OnRegistrosLoadedListener listener) {
        executorService.execute(() -> {
            List<RegistroPontoEntity> registros = registroPontoDao.getByMesSync(funcionarioId, mesAno);
            if (listener != null) {
                listener.onRegistrosLoaded(registros);
            }
        });
    }

    public void getRegistroByData(int funcionarioId, String data, OnRegistroLoadedListener listener) {
        executorService.execute(() -> {
            RegistroPontoEntity registro = registroPontoDao.getByData(funcionarioId, data);
            if (listener != null) {
                listener.onRegistroLoaded(registro);
            }
        });
    }

    public void limparRegistros(int funcionarioId) {
        executorService.execute(() -> registroPontoDao.deleteAllByFuncionario(funcionarioId));
    }

    public LiveData<Integer> getCountRegistros(int funcionarioId) {
        return registroPontoDao.getCountByFuncionario(funcionarioId);
    }

    public interface OnSuccessListener {
        void onSuccess(long id);
    }

    public interface OnRegistrosLoadedListener {
        void onRegistrosLoaded(List<RegistroPontoEntity> registros);
    }

    public interface OnRegistroLoadedListener {
        void onRegistroLoaded(RegistroPontoEntity registro);
    }
}
