package com.murillohms.pontofacil.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.murillohms.pontofacil.R;
import com.murillohms.pontofacil.domain.entity.RegistroPontoEntity;

import java.util.ArrayList;
import java.util.List;

public class HistoricoAdapter extends RecyclerView.Adapter<HistoricoAdapter.ViewHolder> {

    private List<RegistroPontoEntity> registros = new ArrayList<>();

    public void setRegistros(List<RegistroPontoEntity> registros) {
        this.registros = registros;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_main, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RegistroPontoEntity registro = registros.get(position);
        holder.bind(registro);
    }

    @Override
    public int getItemCount() {
        return registros.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvData, tvEntrada, tvAlmoco, tvSaida, tvHoras;

        ViewHolder(View itemView) {
            super(itemView);
            tvData = itemView.findViewById(R.id.tvItemData);
            tvEntrada = itemView.findViewById(R.id.tvItemEntrada);
            tvAlmoco = itemView.findViewById(R.id.tvItemAlmoco);
            tvSaida = itemView.findViewById(R.id.tvItemSaida);
            tvHoras = itemView.findViewById(R.id.tvItemHoras);
        }

        void bind(RegistroPontoEntity registro) {
            tvData.setText(registro.getData());
            tvEntrada.setText("Entrada: " + (registro.getEntrada() != null ? registro.getEntrada() : "--"));

            if (registro.getAlmocoSaida() != null && registro.getAlmocoRetorno() != null) {
                tvAlmoco.setText("Almoço: " + registro.getAlmocoSaida() + " - " + registro.getAlmocoRetorno());
            } else {
                tvAlmoco.setText("Almoço: --");
            }

            tvSaida.setText("Saída: " + (registro.getSaida() != null ? registro.getSaida() : "--"));
            tvHoras.setText("Total: " + registro.calcularHorasTrabalhadas() + "h");
        }
    }
}