package com.murillohms.pontofacil.ui.home;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.murillohms.pontofacil.domain.entity.FuncionarioEntity;
import com.murillohms.pontofacil.domain.entity.RegistroPontoEntity;
import com.murillohms.pontofacil.infrastructure.repository.PontoRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HomeViewModel extends AndroidViewModel {

    private final PontoRepository repository;
    private final MutableLiveData<String> statusText = new MutableLiveData<>();
    private final MutableLiveData<String> horasTrabalhadas = new MutableLiveData<>();
    private final MutableLiveData<Boolean> botaoEntradaHabilitado = new MutableLiveData<>();
    private final MutableLiveData<Boolean> botaoAlmocoSaidaHabilitado = new MutableLiveData<>();
    private final MutableLiveData<Boolean> botaoAlmocoRetornoHabilitado = new MutableLiveData<>();
    private final MutableLiveData<Boolean> botaoSaidaHabilitado = new MutableLiveData<>();
    private final MutableLiveData<String> mensagemErro = new MutableLiveData<>();
    private final MutableLiveData<String> mensagemSucesso = new MutableLiveData<>();

    private RegistroPontoEntity registroAtual;
    private FuncionarioEntity funcionarioAtual;
    private Handler handler;
    private Runnable atualizadorHoras;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        repository = new PontoRepository(application);
        inicializarEstadoInicial();
        configurarAtualizadorAutomatico();
        carregarFuncionario();
    }

    private void carregarFuncionario() {
        repository.getFuncionarioAtivo().observeForever(funcionario -> {
            if (funcionario != null) {
                funcionarioAtual = funcionario;
                verificarRegistroPendente();
            }
        });
    }

    private void verificarRegistroPendente() {
        if (funcionarioAtual == null) return;

        String dataHoje = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

        repository.getRegistroByData(funcionarioAtual.getId(), dataHoje, registro -> {
            if (registro != null && registro.getSaida() == null) {
                registroAtual = registro;
                atualizarInterface();
                iniciarAtualizacaoAutomatica();
            }
        });
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
        if (funcionarioAtual == null) {
            mensagemErro.setValue("Configure seus dados primeiro!");
            return;
        }

        if (registroAtual != null) {
            mensagemErro.setValue("Você já registrou a entrada hoje!");
            return;
        }

        registroAtual = new RegistroPontoEntity();
        registroAtual.setFuncionarioId(funcionarioAtual.getId());
        registroAtual.setData(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()));
        registroAtual.setEntrada(obterHoraAtual());

        repository.inserirRegistro(registroAtual, id -> {
            registroAtual.setId((int) id);
            mensagemSucesso.setValue("✓ Entrada registrada: " + registroAtual.getEntrada());
            atualizarInterface();
            iniciarAtualizacaoAutomatica();
        });
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

        repository.atualizarRegistro(registroAtual);
        mensagemSucesso.setValue("✓ Saída para almoço: " + registroAtual.getAlmocoSaida());
        atualizarInterface();
        pararAtualizacaoAutomatica();
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

        repository.atualizarRegistro(registroAtual);
        mensagemSucesso.setValue("✓ Retorno do almoço: " + registroAtual.getAlmocoRetorno());
        atualizarInterface();
        iniciarAtualizacaoAutomatica();
    }

    public void registrarSaida() {
        if (registroAtual == null || registroAtual.getEntrada() == null) {
            mensagemErro.setValue("Registre a entrada primeiro!");
            return;
        }

        if (registroAtual.getSaida() != null) {
            mensagemErro.setValue("Saída já foi registrada!");
            return;
        }

        registroAtual.setSaida(obterHoraAtual());

        String horasFinais = registroAtual.calcularHorasTrabalhadas();

        repository.atualizarRegistro(registroAtual);
        mensagemSucesso.setValue("✓ Ponto finalizado!\nTotal trabalhado: " + horasFinais);

        registroAtual = null;
        pararAtualizacaoAutomatica();
        inicializarEstadoInicial();
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

            // Descontar tempo de almoço
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
        if (registroAtual != null &&
                (registroAtual.getAlmocoSaida() == null || registroAtual.getAlmocoRetorno() != null)) {
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

    public LiveData<String> getMensagemSucesso() {
        return mensagemSucesso;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        pararAtualizacaoAutomatica();
    }

    public String finalizarPonto() {
        return "";
    }
}