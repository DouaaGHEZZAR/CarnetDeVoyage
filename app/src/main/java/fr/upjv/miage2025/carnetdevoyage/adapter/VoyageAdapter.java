package fr.upjv.miage2025.carnetdevoyage.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import fr.upjv.miage2025.carnetdevoyage.R;
import fr.upjv.miage2025.carnetdevoyage.model.Voyage;

public class VoyageAdapter extends RecyclerView.Adapter<VoyageAdapter.VoyageViewHolder> {

    private List<Voyage> allVoyages;
    private List<Voyage> filteredVoyages;
    private OnVoyageClickListener listener;

    private OnVoyageDeleteListener deleteListener;

    public void setOnVoyageDeleteListener(OnVoyageDeleteListener listener) {
        this.deleteListener = listener;
    }

    public VoyageAdapter(List<Voyage> voyages) {
        this.allVoyages = new ArrayList<>(voyages);
        this.filteredVoyages = voyages;
    }

    @Override
    public VoyageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_voyage, parent, false);
        return new VoyageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(VoyageViewHolder holder, int position) {
        Voyage voyage = filteredVoyages.get(position);
        holder.nomVoyage.setText(voyage.getDestination());
        holder.initiale.setText(voyage.getDestination().substring(0, 1).toUpperCase());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onVoyageClick(voyage);
        });

        holder.deleteBtn.setOnClickListener(v -> {
            if (deleteListener != null) deleteListener.onVoyageDelete(voyage);
        });
    }

    public void setVoyages(List<Voyage> newVoyages) {
        this.allVoyages = new ArrayList<>(newVoyages);
        this.filteredVoyages = new ArrayList<>(newVoyages);
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return filteredVoyages.size();
    }

    public void setOnVoyageClickListener(OnVoyageClickListener listener) {
        this.listener = listener;
    }

    public void filter(String query) {
        query = query.toLowerCase();
        filteredVoyages = new ArrayList<>();
        for (Voyage v : allVoyages) {
            if (v.getDestination().toLowerCase().contains(query)) {
                filteredVoyages.add(v);
            }
        }
        notifyDataSetChanged();
    }

    public static class VoyageViewHolder extends RecyclerView.ViewHolder {
        TextView nomVoyage, initiale;
        ImageView deleteBtn;

        public VoyageViewHolder(View itemView) {
            super(itemView);
            nomVoyage = itemView.findViewById(R.id.nom_voyage);
            initiale = itemView.findViewById(R.id.initiale);
            deleteBtn = itemView.findViewById(R.id.btn_delete);
        }
    }



    public interface OnVoyageClickListener {
        void onVoyageClick(Voyage voyage);
    }
    public interface OnVoyageDeleteListener {
        void onVoyageDelete(Voyage voyage);
    }

}

