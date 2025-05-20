package fr.upjv.miage2025.carnetdevoyage.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import fr.upjv.miage2025.carnetdevoyage.R;
import fr.upjv.miage2025.carnetdevoyage.model.Voyage;

public class VoyageAdapter extends RecyclerView.Adapter<VoyageAdapter.VoyageViewHolder> {

    private List<Voyage> voyages;

    public VoyageAdapter(List<Voyage> voyages) {
        this.voyages = voyages;
    }

    @NonNull
    @Override
    public VoyageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vue = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_voyage, parent, false);
        return new VoyageViewHolder(vue);
    }

    @Override
    public void onBindViewHolder(@NonNull VoyageViewHolder holder, int position) {
        Voyage voyage = voyages.get(position);
        holder.nomVoyage.setText(voyage.getDestination());

        // Initiale = première lettre en majuscule
        String initiale = voyage.getDestination().substring(0, 1).toUpperCase();
        holder.initiale.setText(initiale);
    }

    @Override
    public int getItemCount() {
        return voyages.size();
    }

    public static class VoyageViewHolder extends RecyclerView.ViewHolder {
        TextView nomVoyage, initiale;

        public VoyageViewHolder(@NonNull View itemView) {
            super(itemView);
            nomVoyage = itemView.findViewById(R.id.nom_voyage);
            initiale = itemView.findViewById(R.id.initiale);
        }
    }
}
