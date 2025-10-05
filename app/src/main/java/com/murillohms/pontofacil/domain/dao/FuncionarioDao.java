package com.murillohms.pontofacil.domain.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.murillohms.pontofacil.domain.entity.FuncionarioEntity;

@Dao
public interface FuncionarioDao {

    @Insert
    long insert(FuncionarioEntity funcionario);

    @Update
    void update(FuncionarioEntity funcionario);

    @Query("SELECT * FROM funcionario WHERE ativo = 1 LIMIT 1")
    LiveData<FuncionarioEntity> getFuncionarioAtivo();

    @Query("SELECT * FROM funcionario WHERE ativo = 1 LIMIT 1")
    FuncionarioEntity getFuncionarioAtivoSync();

    @Query("SELECT * FROM funcionario WHERE id = :id")
    FuncionarioEntity getById(int id);

    @Query("UPDATE funcionario SET ativo = 0")
    void desativarTodos();
}
