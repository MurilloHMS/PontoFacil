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
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
        statusText.postValue("Aguardando registro de entrada");
        horasTrabalhadas.postValue("--:--");
        botaoEntradaHabilitado.postValue(true);
        botaoAlmocoSaidaHabilitado.postValue(false);
        botaoAlmocoRetornoHabilitado.postValue(false);
        botaoSaidaHabilitado.postValue(false);
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
            mensagemErro.postValue("Configure seus dados primeiro!");
            return;
        }

        if (registroAtual != null) {
            mensagemErro.postValue("Você já registrou a entrada hoje!");
            return;
        }

        registroAtual = new RegistroPontoEntity();
        registroAtual.setFuncionarioId(funcionarioAtual.getId());
        registroAtual.setData(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()));
        registroAtual.setEntrada(obterHoraAtual());

        repository.inserirRegistro(registroAtual, id -> {
            registroAtual.setId((int) id);
            mensagemSucesso.postValue("✓ Entrada registrada: " + registroAtual.getEntrada());
            atualizarInterface();
            iniciarAtualizacaoAutomatica();
        });
    }

    public void registrarAlmocoSaida() {
        if (registroAtual == null || registroAtual.getEntrada() == null) {
            mensagemErro.postValue("Registre a entrada primeiro!");
            return;
        }

        if (registroAtual.getAlmocoSaida() != null) {
            mensagemErro.postValue("Almoço já foi registrado!");
            return;
        }

        registroAtual.setAlmocoSaida(obterHoraAtual());

        repository.atualizarRegistro(registroAtual);
        mensagemSucesso.postValue("✓ Saída para almoço: " + registroAtual.getAlmocoSaida());
        atualizarInterface();
        pararAtualizacaoAutomatica();
    }

    public void registrarAlmocoRetorno() {
        if (registroAtual == null || registroAtual.getAlmocoSaida() == null) {
            mensagemErro.postValue("Registre a saída para almoço primeiro!");
            return;
        }

        if (registroAtual.getAlmocoRetorno() != null) {
            mensagemErro.postValue("Retorno já foi registrado!");
            return;
        }

        registroAtual.setAlmocoRetorno(obterHoraAtual());

        repository.atualizarRegistro(registroAtual);
        mensagemSucesso.postValue("✓ Retorno do almoço: " + registroAtual.getAlmocoRetorno());
        atualizarInterface();
        iniciarAtualizacaoAutomatica();
    }

    public void registrarSaida() {
        if (registroAtual == null || registroAtual.getEntrada() == null) {
            mensagemErro.postValue("Registre a entrada primeiro!");
            return;
        }

        if (registroAtual.getSaida() != null) {
            mensagemErro.postValue("Saída já foi registrada!");
            return;
        }

        registroAtual.setSaida(obterHoraAtual());

        String horasFinais = registroAtual.calcularHorasTrabalhadas();

        repository.atualizarRegistro(registroAtual);
        mensagemSucesso.postValue("✓ Ponto finalizado!\nTotal trabalhado: " + horasFinais);

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
                botaoEntradaHabilitado.postValue(false);
                botaoAlmocoSaidaHabilitado.postValue(true);
                botaoAlmocoRetornoHabilitado.postValue(false);
                botaoSaidaHabilitado.postValue(true);
            }

            if (registroAtual.getAlmocoSaida() != null) {
                status.append("\n✓ Almoço saída: ").append(registroAtual.getAlmocoSaida());
                botaoAlmocoSaidaHabilitado.postValue(false);
                botaoAlmocoRetornoHabilitado.postValue(true);
                botaoSaidaHabilitado.postValue(false);
            }

            if (registroAtual.getAlmocoRetorno() != null) {
                status.append("\n✓ Almoço retorno: ").append(registroAtual.getAlmocoRetorno());
                botaoAlmocoRetornoHabilitado.postValue(false);
                botaoSaidaHabilitado.postValue(true);
            }

            statusText.postValue(status.toString());
            atualizarHorasTrabalhadas();
        }
    }

    private void atualizarHorasTrabalhadas() {
        if (registroAtual == null || registroAtual.getEntrada() == null) {
            horasTrabalhadas.postValue("--:--");
            return;
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault());
            LocalTime entrada = LocalTime.parse(registroAtual.getEntrada(), formatter);
            LocalTime agora = LocalTime.now();

            Duration duracao = Duration.between(entrada, agora);

            // Ajusta almoço
            if (registroAtual.getAlmocoSaida() != null && registroAtual.getAlmocoRetorno() != null) {
                LocalTime almocoSaida = LocalTime.parse(registroAtual.getAlmocoSaida(), formatter);
                LocalTime almocoRetorno = LocalTime.parse(registroAtual.getAlmocoRetorno(), formatter);
                Duration almoco = Duration.between(almocoSaida, almocoRetorno);
                duracao = duracao.minus(almoco);
            } else if (registroAtual.getAlmocoSaida() != null) {
                LocalTime almocoSaida = LocalTime.parse(registroAtual.getAlmocoSaida(), formatter);
                duracao = Duration.between(entrada, almocoSaida);
            }

            long horas = duracao.toHours();
            long minutos = duracao.toMinutes() % 60;

            horasTrabalhadas.postValue(String.format(Locale.getDefault(), "%02d:%02d", horas, minutos));
        } catch (Exception e) {
            horasTrabalhadas.postValue("--:--");
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