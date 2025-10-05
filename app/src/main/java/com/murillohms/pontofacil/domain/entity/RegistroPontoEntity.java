package com.murillohms.pontofacil.domain.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "registro_ponto")
public class RegistroPontoEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private int funcionarioId;
    private String data;
    private String entrada;
    private String almocoSaida;
    private String almocoRetorno;
    private String saida;
    private String observacao;

    // Construtor
    public RegistroPontoEntity() {
    }

    // Getters e Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFuncionarioId() {
        return funcionarioId;
    }

    public void setFuncionarioId(int funcionarioId) {
        this.funcionarioId = funcionarioId;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getEntrada() {
        return entrada;
    }

    public void setEntrada(String entrada) {
        this.entrada = entrada;
    }

    public String getAlmocoSaida() {
        return almocoSaida;
    }

    public void setAlmocoSaida(String almocoSaida) {
        this.almocoSaida = almocoSaida;
    }

    public String getAlmocoRetorno() {
        return almocoRetorno;
    }

    public void setAlmocoRetorno(String almocoRetorno) {
        this.almocoRetorno = almocoRetorno;
    }

    public String getSaida() {
        return saida;
    }

    public void setSaida(String saida) {
        this.saida = saida;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public String calcularHorasTrabalhadas() {
        if (entrada == null || saida == null) {
            return "--:--";
        }

        try {
            String[] entradaParts = entrada.split(":");
            String[] saidaParts = saida.split(":");

            int entradaMinutos = Integer.parseInt(entradaParts[0]) * 60 + Integer.parseInt(entradaParts[1]);
            int saidaMinutos = Integer.parseInt(saidaParts[0]) * 60 + Integer.parseInt(saidaParts[1]);

            int totalMinutos = saidaMinutos - entradaMinutos;

            // Descontar tempo de almoço
            if (almocoSaida != null && almocoRetorno != null) {
                String[] almocoSaidaParts = almocoSaida.split(":");
                String[] almocoRetornoParts = almocoRetorno.split(":");

                int almocoSaidaMinutos = Integer.parseInt(almocoSaidaParts[0]) * 60 + Integer.parseInt(almocoSaidaParts[1]);
                int almocoRetornoMinutos = Integer.parseInt(almocoRetornoParts[0]) * 60 + Integer.parseInt(almocoRetornoParts[1]);

                totalMinutos -= (almocoRetornoMinutos - almocoSaidaMinutos);
            }

            int horas = totalMinutos / 60;
            int minutos = totalMinutos % 60;

            return String.format("%02d:%02d", horas, minutos);
        } catch (Exception e) {
            return "--:--";
        }
    }
}