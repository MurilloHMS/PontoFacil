package com.murillohms.pontofacil.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.murillohms.pontofacil.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private TextView tvData, tvStatus, tvHorasTrabalhadas;
    private Button btnEntrada, btnAlmocoSaida, btnAlmocoRetorno, btnSaida;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        inicializarViews(root);
        observarViewModel();
        configurarBotoes();

        return root;
    }

    private void inicializarViews(View root) {
        tvData = root.findViewById(R.id.tvData);
        tvStatus = root.findViewById(R.id.tvStatus);
        tvHorasTrabalhadas = root.findViewById(R.id.tvHorasTrabalhadas);

        btnEntrada = root.findViewById(R.id.btnEntrada);
        btnAlmocoSaida = root.findViewById(R.id.btnAlmocoSaida);
        btnAlmocoRetorno = root.findViewById(R.id.btnAlmocoRetorno);
        btnSaida = root.findViewById(R.id.btnSaida);

        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd/MM/yyyy", new Locale("pt", "BR"));
        tvData.setText(sdf.format(new Date()));
    }

    private void configurarBotoes() {
        btnEntrada.setOnClickListener(v -> {
            homeViewModel.registrarEntrada();
            Toast.makeText(requireContext(), "✓ Entrada registrada!", Toast.LENGTH_SHORT).show();
        });

        btnAlmocoSaida.setOnClickListener(v -> {
            homeViewModel.registrarAlmocoSaida();
            Toast.makeText(requireContext(), "✓ Saída para almoço!", Toast.LENGTH_SHORT).show();
        });

        btnAlmocoRetorno.setOnClickListener(v -> {
            homeViewModel.registrarAlmocoRetorno();
            Toast.makeText(requireContext(), "✓ Retorno do almoço!", Toast.LENGTH_SHORT).show();
        });

        btnSaida.setOnClickListener(v -> {
            String horasTrabalhadas = homeViewModel.finalizarPonto();
            Toast.makeText(requireContext(),
                    "✓ Ponto finalizado!\nTotal: " + horasTrabalhadas,
                    Toast.LENGTH_LONG).show();
        });
    }

    private void observarViewModel() {
        homeViewModel.getStatusText().observe(getViewLifecycleOwner(), status -> {
            tvStatus.setText(status);
        });

        homeViewModel.getHorasTrabalhadas().observe(getViewLifecycleOwner(), horas -> {
            tvHorasTrabalhadas.setText(horas);
        });

        homeViewModel.getBotaoEntradaHabilitado().observe(getViewLifecycleOwner(), habilitado -> {
            btnEntrada.setEnabled(habilitado);
            btnEntrada.setAlpha(habilitado ? 1.0f : 0.5f);
        });

        homeViewModel.getBotaoAlmocoSaidaHabilitado().observe(getViewLifecycleOwner(), habilitado -> {
            btnAlmocoSaida.setEnabled(habilitado);
            btnAlmocoSaida.setAlpha(habilitado ? 1.0f : 0.5f);
        });

        homeViewModel.getBotaoAlmocoRetornoHabilitado().observe(getViewLifecycleOwner(), habilitado -> {
            btnAlmocoRetorno.setEnabled(habilitado);
            btnAlmocoRetorno.setAlpha(habilitado ? 1.0f : 0.5f);
        });

        homeViewModel.getBotaoSaidaHabilitado().observe(getViewLifecycleOwner(), habilitado -> {
            btnSaida.setEnabled(habilitado);
            btnSaida.setAlpha(habilitado ? 1.0f : 0.5f);
        });

        homeViewModel.getMensagemErro().observe(getViewLifecycleOwner(), mensagem -> {
            if (mensagem != null && !mensagem.isEmpty()) {
                Toast.makeText(requireContext(), mensagem, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        homeViewModel.iniciarAtualizacaoAutomatica();
    }

    @Override
    public void onPause() {
        super.onPause();
        homeViewModel.pararAtualizacaoAutomatica();
    }
}