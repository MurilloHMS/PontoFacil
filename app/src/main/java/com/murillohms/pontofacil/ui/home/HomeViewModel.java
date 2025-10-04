package com.murillohms.pontofacil.ui.home;

import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.murillohms.pontofacil.domain.model.RegistroPonto;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<String> statusText = new MutableLiveData<>();
    private final MutableLiveData<String> horasTrabalhadas = new MutableLiveData<>();
    private final MutableLiveData<Boolean> botaoEntradaHabilitado = new MutableLiveData<>();
    private final MutableLiveData<Boolean> botaoAlmocoSaidaHabilitado = new MutableLiveData<>();
    private final MutableLiveData<Boolean> botaoAlmocoRetornoHabilitado = new MutableLiveData<>();
    private final MutableLiveData<Boolean> botaoSaidaHabilitado = new MutableLiveData<>();
    private final MutableLiveData<String> mensagemErro = new MutableLiveData<>();

    private RegistroPonto registroAtual;
    private Handler handler;
    private Runnable atualizadorHoras;

    public HomeViewModel() {
        inicializarEstadoInicial();
        configurarAtualizadorAutomatico();
    }

    private void inicializarEstadoInicial() {
        statusText.setValue("Aguardando registro de entrada");
        horasTrabalhadas.setValue("--:--");
        botaoEntradaHabilitado.setValue(true);
        botaoAlmocoSaidaHabilitado.setValue(false);
        botaoAlmocoRetornoHabilitado.setValue(false);
        botaoSaidaHabilitado.setValue(false);
    }

    private void configurarAtualizadorAutomatico() {
        handler = new Handler(Looper.getMainLooper());
        atualizadorHoras = new Runnable() {
            @Override
            public void run() {
                if (registroAtual != null) {
                    atualizarHorasTrabalhadas();
                    handler.postDelayed(this, 60000); // Atualiza a cada 1 minuto
                }
            }
        };
    }

    public void registrarEntrada() {
        if (registroAtual != null) {
            mensagemErro.setValue("Você já registrou a entrada hoje!");
            return;
        }

        registroAtual = new RegistroPonto();
        registroAtual.setEntrada(obterHoraAtual());

        atualizarInterface();
        iniciarAtualizacaoAutomatica();
    }

    public void registrarAlmocoSaida() {
        if (registroAtual == null || registroAtual.getEntrada() == null) {
            mensagemErro.setValue("Registre a entrada primeiro!");
            return;
        }

        if (registroAtual.getAlmocoSaida() != null) {
            mensagemErro.setValue("Almoço já foi registrado!");
            return;
        }

        registroAtual.setAlmocoSaida(obterHoraAtual());
        atualizarInterface();
    }

    public void registrarAlmocoRetorno() {
        if (registroAtual == null || registroAtual.getAlmocoSaida() == null) {
            mensagemErro.setValue("Registre a saída para almoço primeiro!");
            return;
        }

        if (registroAtual.getAlmocoRetorno() != null) {
            mensagemErro.setValue("Retorno já foi registrado!");
            return;
        }

        registroAtual.setAlmocoRetorno(obterHoraAtual());
        atualizarInterface();
        iniciarAtualizacaoAutomatica();
    }

    public String finalizarPonto() {
        if (registroAtual == null || registroAtual.getEntrada() == null) {
            mensagemErro.setValue("Registre a entrada primeiro!");
            return "--:--";
        }

        if (registroAtual.getSaida() != null) {
            mensagemErro.setValue("Saída já foi registrada!");
            return registroAtual.calcularHorasTrabalhadas();
        }

        registroAtual.setSaida(obterHoraAtual());
        registroAtual.setData(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()));

        String horasFinais = registroAtual.calcularHorasTrabalhadas();

        registroAtual = null;
        pararAtualizacaoAutomatica();
        inicializarEstadoInicial();

        return horasFinais;
    }

    private void atualizarInterface() {
        if (registroAtual == null) {
            inicializarEstadoInicial();
        } else {
            StringBuilder status = new StringBuilder();

            if (registroAtual.getEntrada() != null) {
                status.append("✓ Entrada: ").append(registroAtual.getEntrada());
                botaoEntradaHabilitado.setValue(false);
                botaoAlmocoSaidaHabilitado.setValue(true);
                botaoAlmocoRetornoHabilitado.setValue(false);
                botaoSaidaHabilitado.setValue(true);
            }

            if (registroAtual.getAlmocoSaida() != null) {
                status.append("\n✓ Almoço saída: ").append(registroAtual.getAlmocoSaida());
                botaoAlmocoSaidaHabilitado.setValue(false);
                botaoAlmocoRetornoHabilitado.setValue(true);
                botaoSaidaHabilitado.setValue(false);
            }

            if (registroAtual.getAlmocoRetorno() != null) {
                status.append("\n✓ Almoço retorno: ").append(registroAtual.getAlmocoRetorno());
                botaoAlmocoRetornoHabilitado.setValue(false);
                botaoSaidaHabilitado.setValue(true);
            }

            statusText.setValue(status.toString());
            atualizarHorasTrabalhadas();
        }
    }

    private void atualizarHorasTrabalhadas() {
        if (registroAtual == null || registroAtual.getEntrada() == null) {
            horasTrabalhadas.setValue("--:--");
            return;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date entrada = sdf.parse(registroAtual.getEntrada());
            Date agora = new Date();

            long diffMillis = agora.getTime() - entrada.getTime();

            if (registroAtual.getAlmocoSaida() != null && registroAtual.getAlmocoRetorno() != null) {
                Date almocoSaida = sdf.parse(registroAtual.getAlmocoSaida());
                Date almocoRetorno = sdf.parse(registroAtual.getAlmocoRetorno());
                long almocoMillis = almocoRetorno.getTime() - almocoSaida.getTime();
                diffMillis -= almocoMillis;
            } else if (registroAtual.getAlmocoSaida() != null) {
                Date almocoSaida = sdf.parse(registroAtual.getAlmocoSaida());
                diffMillis = almocoSaida.getTime() - entrada.getTime();
            }

            long horas = diffMillis / (1000 * 60 * 60);
            long minutos = (diffMillis % (1000 * 60 * 60)) / (1000 * 60);

            horasTrabalhadas.setValue(String.format(Locale.getDefault(), "%02d:%02d", horas, minutos));
        } catch (Exception e) {
            horasTrabalhadas.setValue("--:--");
        }
    }

    private String obterHoraAtual() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date());
    }

    public void iniciarAtualizacaoAutomatica() {
        if (registroAtual != null && !registroAtual.isNoAlmoco()) {
            handler.post(atualizadorHoras);
        }
    }

    public void pararAtualizacaoAutomatica() {
        handler.removeCallbacks(atualizadorHoras);
    }

    // Getters para LiveData
    public LiveData<String> getStatusText() {
        return statusText;
    }

    public LiveData<String> getHorasTrabalhadas() {
        return horasTrabalhadas;
    }

    public LiveData<Boolean> getBotaoEntradaHabilitado() {
        return botaoEntradaHabilitado;
    }

    public LiveData<Boolean> getBotaoAlmocoSaidaHabilitado() {
        return botaoAlmocoSaidaHabilitado;
    }

    public LiveData<Boolean> getBotaoAlmocoRetornoHabilitado() {
        return botaoAlmocoRetornoHabilitado;
    }

    public LiveData<Boolean> getBotaoSaidaHabilitado() {
        return botaoSaidaHabilitado;
    }

    public LiveData<String> getMensagemErro() {
        return mensagemErro;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        pararAtualizacaoAutomatica();
    }
}