package com.murillohms.pontofacil.ui.notifications;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.murillohms.pontofacil.R;
import com.murillohms.pontofacil.domain.entity.FuncionarioEntity;
import com.murillohms.pontofacil.infrastructure.repository.PontoRepository;
import com.murillohms.pontofacil.infrastructure.util.ExcelExporter;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class NotificationsFragment extends Fragment {

    private static final int PERMISSION_REQUEST_CODE = 100;

    private TextView tvNomeFuncionario, tvEmpresa, tvTotalRegistros;
    private Button btnExportarExcel, btnEditarDados, btnLimparHistorico, btnSobre;
    private PontoRepository repository;
    private FuncionarioEntity funcionarioAtual;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_notifications, container, false);

        repository = new PontoRepository(requireActivity().getApplication());

        inicializarViews(root);
        configurarBotoes();
        carregarDados();

        return root;
    }

    private void inicializarViews(View root) {
        tvNomeFuncionario = root.findViewById(R.id.tvNomeFuncionario);
        tvEmpresa = root.findViewById(R.id.tvEmpresa);
        tvTotalRegistros = root.findViewById(R.id.tvTotalRegistros);

        btnExportarExcel = root.findViewById(R.id.btnExportarExcel);
        btnEditarDados = root.findViewById(R.id.btnEditarDados);
        btnLimparHistorico = root.findViewById(R.id.btnLimparHistorico);
        btnSobre = root.findViewById(R.id.btnSobre);
    }

    private void configurarBotoes() {
        btnExportarExcel.setOnClickListener(v -> exportarParaExcel());
        btnEditarDados.setOnClickListener(v -> editarDadosFuncionario());
        btnLimparHistorico.setOnClickListener(v -> confirmarLimpeza());
        btnSobre.setOnClickListener(v -> mostrarSobre());
    }

    private void carregarDados() {
        repository.getFuncionarioAtivo().observe(getViewLifecycleOwner(), funcionario -> {
            if (funcionario != null) {
                funcionarioAtual = funcionario;
                tvNomeFuncionario.setText(funcionario.getNome());
                tvEmpresa.setText(funcionario.getEmpresa());

                repository.getCountRegistros(funcionario.getId()).observe(getViewLifecycleOwner(), count -> {
                    tvTotalRegistros.setText(count + " registros");
                });
            }
        });
    }

    private void exportarParaExcel() {
        if (funcionarioAtual == null) {
            Toast.makeText(requireContext(), "Configure seus dados primeiro", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_CODE);
                return;
            }
        }


        mostrarDialogoExportacao();
    }

    private void mostrarDialogoExportacao() {
        String[] opcoes = {"Mês Atual", "Mês Anterior", "Todos os Registros"};

        new AlertDialog.Builder(requireContext())
                .setTitle("Exportar Relatório")
                .setItems(opcoes, (dialog, which) -> {
                    Calendar cal = Calendar.getInstance();
                    String mesAno;

                    switch (which) {
                        case 0: // Mês atual
                            mesAno = new SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(cal.getTime());
                            break;
                        case 1: // Mês anterior
                            cal.add(Calendar.MONTH, -1);
                            mesAno = new SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(cal.getTime());
                            break;
                        default: // Todos
                            mesAno = "";
                            break;
                    }

                    realizarExportacao(mesAno);
                })
                .show();
    }

    private void realizarExportacao(String mesAno) {
        Toast.makeText(requireContext(), "Gerando relatório...", Toast.LENGTH_SHORT).show();

        repository.getRegistrosByMesAsync(funcionarioAtual.getId(), mesAno, registros -> {
            if (registros.isEmpty()) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Nenhum registro encontrado neste período", Toast.LENGTH_SHORT).show()
                );
                return;
            }

            try {
                ExcelExporter exporter = new ExcelExporter(requireContext());
                File arquivoExcel = exporter.exportarRelatorioMensal(funcionarioAtual, registros, mesAno);

                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(),
                            "Relatório salvo em Downloads!",
                            Toast.LENGTH_LONG).show();

                    abrirArquivo(arquivoExcel);
                });

            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Erro ao gerar relatório: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        });
    }

    private void abrirArquivo(File arquivo) {
        try {
            Uri uri = FileProvider.getUriForFile(requireContext(),
                    requireContext().getPackageName() + ".fileprovider",
                    arquivo);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
                startActivity(Intent.createChooser(intent, "Abrir com"));
            } else {
                Toast.makeText(requireContext(), "Nenhum app para abrir Excel", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void editarDadosFuncionario() {
        Toast.makeText(requireContext(), "Em desenvolvimento", Toast.LENGTH_SHORT).show();
    }

    private void confirmarLimpeza() {
        if (funcionarioAtual == null) return;

        new AlertDialog.Builder(requireContext())
                .setTitle("Limpar Histórico")
                .setMessage("Tem certeza que deseja apagar todos os registros? Esta ação não pode ser desfeita.")
                .setPositiveButton("Sim, apagar tudo", (dialog, which) -> {
                    repository.limparRegistros(funcionarioAtual.getId());
                    Toast.makeText(requireContext(), "Histórico limpo com sucesso", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void mostrarSobre() {
        String mensagem = "PontoFácil - Controle de Ponto\n\n" +
                "Versão: 1.0.0\n\n" +
                "Recursos:\n" +
                "• Registro de entrada e saída\n" +
                "• Controle de horário de almoço\n" +
                "• Histórico completo\n" +
                "• Estatísticas mensais\n" +
                "• Exportação para Excel\n" +
                "• Dados de funcionário e empresa\n\n" +
                "Desenvolvido para facilitar o controle de ponto pessoal.";

        new AlertDialog.Builder(requireContext())
                .setTitle("Sobre o PontoFácil")
                .setMessage(mensagem)
                .setPositiveButton("OK", null)
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                exportarParaExcel();
            } else {
                Toast.makeText(requireContext(), "Permissão necessária para salvar o arquivo", Toast.LENGTH_SHORT).show();
            }
        }
    }
}