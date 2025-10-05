package com.murillohms.pontofacil.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.murillohms.pontofacil.R;

public class DashboardFragment extends Fragment {

    private DashboardViewModel dashboardViewModel;
    private RecyclerView rvHistorico;
    private TextView tvTotalHorasMes, tvDiasTrabalhados, tvMediaDiaria, tvSemDados;
    private HistoricoAdapter historicoAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        inicializarViews(root);
        configurarRecyclerView();
        observarViewModel();

        return root;
    }

    private void inicializarViews(View root) {
        rvHistorico = root.findViewById(R.id.rvHistorico);
        tvTotalHorasMes = root.findViewById(R.id.tvTotalHorasMes);
        tvDiasTrabalhados = root.findViewById(R.id.tvDiasTrabalhados);
        tvMediaDiaria = root.findViewById(R.id.tvMediaDiaria);
        tvSemDados = root.findViewById(R.id.tvSemDados);
    }

    private void configurarRecyclerView() {
        historicoAdapter = new HistoricoAdapter();
        rvHistorico.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvHistorico.setAdapter(historicoAdapter);
    }

    private void observarViewModel() {

        dashboardViewModel.getRegistrosMes().observe(getViewLifecycleOwner(), registros -> {
            if (registros != null && !registros.isEmpty()) {

                historicoAdapter.submitList(registros);
                rvHistorico.setVisibility(View.VISIBLE);
                tvSemDados.setVisibility(View.GONE);


                dashboardViewModel.calcularEstatisticas(registros);
            } else {
                historicoAdapter.submitList(null);
                rvHistorico.setVisibility(View.GONE);
                tvSemDados.setVisibility(View.VISIBLE);

                tvTotalHorasMes.setText("0h 0min");
                tvDiasTrabalhados.setText("0");
                tvMediaDiaria.setText("0h 0min");
            }
        });

        dashboardViewModel.getTotalHorasMes().observe(getViewLifecycleOwner(), total -> {
            tvTotalHorasMes.setText(total);
        });

        dashboardViewModel.getDiasTrabalhados().observe(getViewLifecycleOwner(), dias -> {
            tvDiasTrabalhados.setText(dias);
        });

        dashboardViewModel.getMediaDiaria().observe(getViewLifecycleOwner(), media -> {
            tvMediaDiaria.setText(media);
        });
    }
}