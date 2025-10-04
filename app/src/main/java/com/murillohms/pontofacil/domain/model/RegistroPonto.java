package com.murillohms.pontofacil.domain.model;

public class RegistroPonto {

    private String data;
    private String entrada;
    private String almocoSaida;
    private String almocoRetorno;
    private String saida;

    public RegistroPonto(){

    }

    public RegistroPonto(String data, String entrada, String almocoSaida, String almocoRetorno, String saida) {
        this.data = data;
        this.entrada = entrada;
        this.almocoSaida = almocoSaida;
        this.almocoRetorno = almocoRetorno;
        this.saida = saida;
    }

    // Getters
    public String getData() {
        return data;
    }

    public String getEntrada() {
        return entrada;
    }

    public String getAlmocoSaida() {
        return almocoSaida;
    }

    public String getAlmocoRetorno() {
        return almocoRetorno;
    }

    public String getSaida() {
        return saida;
    }

    // Setters
    public void setData(String data) {
        this.data = data;
    }

    public void setEntrada(String entrada) {
        this.entrada = entrada;
    }

    public void setAlmocoSaida(String almocoSaida) {
        this.almocoSaida = almocoSaida;
    }

    public void setAlmocoRetorno(String almocoRetorno) {
        this.almocoRetorno = almocoRetorno;
    }

    public void setSaida(String saida) {
        this.saida = saida;
    }

    public String calcularHorasTrabalhadas(){
        if(entrada == null || saida == null){
            return "--:--";
        }

        try{
            String[] entradaParts = entrada.split(":");
            String[] saidaParts = saida.split(":");

            int entradaMinutos = Integer.parseInt(entradaParts[0]) * 60 + Integer.parseInt(entradaParts[1]);
            int saidaMinutos = Integer.parseInt(saidaParts[0]) * 60 + Integer.parseInt(saidaParts[1]);

            int totalMinutos = saidaMinutos - entradaMinutos;

            if(almocoSaida != null && almocoRetorno != null){
                String[] almocoSaidaParts = almocoSaida.split(":");
                String[] almocoRetornoParts = almocoRetorno.split(":");

                int almocoSaidaMinutos = Integer.parseInt(almocoSaidaParts[0]) * 60 + Integer.parseInt(almocoSaidaParts[1]);
                int almocoRetornoMinutos = Integer.parseInt(almocoRetornoParts[0]) * 60 + Integer.parseInt(almocoRetornoParts[1]);

                totalMinutos -= (almocoRetornoMinutos - almocoSaidaMinutos);
            }

            int horas = totalMinutos / 60;
            int minutos = totalMinutos % 60;

            return String.format("%02d:%02d", horas, minutos);
        }catch (Exception e){
            return "--:--";
        }
    }

    public boolean isCompleted(){
        return entrada != null && saida != null;
    }

    public boolean isNoAlmoco(){
        return almocoSaida != null && almocoRetorno == null;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("Data: ").append(data != null ? data : "--").append("\n");
        sb.append("Entrada: ").append(entrada != null ? entrada : "--").append("\n");
        sb.append("Almoço: ");

        if(almocoSaida != null && almocoRetorno != null){
            sb.append(almocoSaida).append(" - ").append(almocoRetorno);
        }else {
            sb.append("--");
        }
        sb.append("\n");
        sb.append("Saida: ").append(saida != null ? saida : "--").append("\n");
        sb.append("Total: ").append(calcularHorasTrabalhadas());

        return sb.toString();
    }

}
