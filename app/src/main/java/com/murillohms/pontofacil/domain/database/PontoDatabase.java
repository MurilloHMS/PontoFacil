package com.murillohms.pontofacil.domain.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.murillohms.pontofacil.domain.dao.FuncionarioDao;
import com.murillohms.pontofacil.domain.dao.RegistroPontoDao;
import com.murillohms.pontofacil.domain.entity.FuncionarioEntity;
import com.murillohms.pontofacil.domain.entity.RegistroPontoEntity;

@Database(entities = {RegistroPontoEntity.class, FuncionarioEntity.class}, version = 1, exportSchema = false)
public abstract class PontoDatabase extends RoomDatabase{

    private static PontoDatabase instance;

    public abstract RegistroPontoDao registroPontoDao();
    public abstract FuncionarioDao funcionarioDao();

    public static synchronized PontoDatabase getInstance(Context context){
        if(instance == null){
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    PontoDatabase.class,
                    "ponto_database"
            ).fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
