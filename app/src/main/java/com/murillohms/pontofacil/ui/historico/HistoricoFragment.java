package com.murillohms.pontofacil.ui.historico;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.murillohms.pontofacil.R;

public class HistoricoFragment extends Fragment {

    private HistoricoViewModel viewModel;
    private HistoricoAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_historico, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerHistorico);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new HistoricoAdapter();
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(HistoricoViewModel.class);

        viewModel.getRegistrosLiveData().observe(getViewLifecycleOwner(), registros -> {
            adapter.submitList(registros);
        });

        viewModel.getMensagemErro().observe(getViewLifecycleOwner(), erro -> {
            if (erro != null) Toast.makeText(getContext(), erro, Toast.LENGTH_SHORT).show();
        });

        viewModel.carregarHistorico(1);
    }
}
