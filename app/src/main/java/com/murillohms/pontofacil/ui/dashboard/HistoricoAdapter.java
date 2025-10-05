package com.murillohms.pontofacil.ui.dashboard;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.murillohms.pontofacil.R;
import com.murillohms.pontofacil.domain.entity.RegistroPontoEntity;

public class HistoricoAdapter extends ListAdapter<RegistroPontoEntity, HistoricoAdapter.ViewHolder> {

    public HistoricoAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<RegistroPontoEntity> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<RegistroPontoEntity>() {
                @Override
                public boolean areItemsTheSame(@NonNull RegistroPontoEntity oldItem, @NonNull RegistroPontoEntity newItem) {
                    return oldItem.getId() == newItem.getId();
                }

                @SuppressLint("DiffUtilEquals")
                @Override
                public boolean areContentsTheSame(@NonNull RegistroPontoEntity oldItem, @NonNull RegistroPontoEntity newItem) {
                    return oldItem.equals(newItem);
                }
            };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_registro, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvData, tvEntrada, tvAlmoco, tvSaida, tvHoras;

        ViewHolder(View itemView) {
            super(itemView);
            tvData = itemView.findViewById(R.id.tvData);
            tvEntrada = itemView.findViewById(R.id.tvEntrada);
            tvAlmoco = itemView.findViewById(R.id.tvAlmoco);
            tvSaida = itemView.findViewById(R.id.tvSaida);
            tvHoras = itemView.findViewById(R.id.tvHoras);
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
