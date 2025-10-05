package com.murillohms.pontofacil.domain.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.murillohms.pontofacil.domain.entity.RegistroPontoEntity;

import java.util.List;

@Dao
public interface RegistroPontoDao {

    @Insert
    long insert(RegistroPontoEntity registro);

    @Update
    void update(RegistroPontoEntity registro);

    @Query("SELECT * FROM registro_ponto WHERE funcionarioId = :funcionarioId ORDER BY data DESC")
    LiveData<List<RegistroPontoEntity>> getAllByFuncionario(int funcionarioId);

    @Query("SELECT * FROM registro_ponto WHERE funcionarioId = :funcionarioId AND data LIKE '%/' || :mesAno ORDER BY data DESC")
    LiveData<List<RegistroPontoEntity>> getByMes(int funcionarioId, String mesAno);

    @Query("SELECT * FROM registro_ponto WHERE funcionarioId = :funcionarioId AND data LIKE '%/' || :mesAno ORDER BY data DESC")
    List<RegistroPontoEntity> getByMesSync(int funcionarioId, String mesAno);

    @Query("SELECT * FROM registro_ponto WHERE funcionarioId = :funcionarioId AND data = :data LIMIT 1")
    RegistroPontoEntity getByData(int funcionarioId, String data);

    @Query("DELETE FROM registro_ponto WHERE funcionarioId = :funcionarioId")
    void deleteAllByFuncionario(int funcionarioId);

    @Query("SELECT COUNT(*) FROM registro_ponto WHERE funcionarioId = :funcionarioId")
    LiveData<Integer> getCountByFuncionario(int funcionarioId);
}
