package com.murillohms.pontofacil.infrastructure.settings;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;


import com.murillohms.pontofacil.MainActivity;
import com.murillohms.pontofacil.R;
import com.murillohms.pontofacil.domain.entity.FuncionarioEntity;
import com.murillohms.pontofacil.infrastructure.repository.PontoRepository;

public class ConfiguracaoInicialActivity extends AppCompatActivity {

    private EditText etNome, etMatricula, etCpf, etCargo, etEmpresa, etCnpj;
    private Button btnSalvar;
    private PontoRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracao_inicial);

        repository = new PontoRepository(getApplication());

        inicializarViews();
        configurarBotao();
    }

    private void inicializarViews() {
        etNome = findViewById(R.id.etNome);
        etMatricula = findViewById(R.id.etMatricula);
        etCpf = findViewById(R.id.etCpf);
        etCargo = findViewById(R.id.etCargo);
        etEmpresa = findViewById(R.id.etEmpresa);
        etCnpj = findViewById(R.id.etCnpj);
        btnSalvar = findViewById(R.id.btnSalvar);
    }

    private void configurarBotao() {
        btnSalvar.setOnClickListener(v -> salvarConfiguracao());
    }

    private void salvarConfiguracao() {
        String nome = etNome.getText().toString().trim();
        String matricula = etMatricula.getText().toString().trim();
        String cpf = etCpf.getText().toString().trim();
        String cargo = etCargo.getText().toString().trim();
        String empresa = etEmpresa.getText().toString().trim();
        String cnpj = etCnpj.getText().toString().trim();

        // Validações
        if (nome.isEmpty() || empresa.isEmpty()) {
            Toast.makeText(this, "Preencha pelo menos Nome e Empresa", Toast.LENGTH_SHORT).show();
            return;
        }

        FuncionarioEntity funcionario = new FuncionarioEntity();
        funcionario.setNome(nome);
        funcionario.setMatricula(matricula);
        funcionario.setCpf(cpf);
        funcionario.setCargo(cargo);
        funcionario.setEmpresa(empresa);
        funcionario.setCnpj(cnpj);
        funcionario.setAtivo(true);

        repository.inserirFuncionario(funcionario, id -> {
            runOnUiThread(() -> {
                Toast.makeText(this, "Configuração salva com sucesso!", Toast.LENGTH_SHORT).show();

                // Voltar para MainActivity
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        });
    }
}