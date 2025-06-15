package fr.upjv.miage2025.carnetdevoyage;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import fr.upjv.miage2025.carnetdevoyage.R;



public class HeaderUtils {
    public static void setupHeaderMenu(Activity activity) {
        ImageView menuIcon = activity.findViewById(R.id.header_menu);
        if (menuIcon == null) return;

        menuIcon.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(activity, menuIcon);
            popup.getMenuInflater().inflate(R.menu.header_menu, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.action_accueil) {
                    activity.startActivity(new Intent(activity, CreerVoyageActivity.class));
                    return true;
                } else if (id == R.id.action_voir_voyages) {
                    activity.startActivity(new Intent(activity, ListeVoyagesActivity.class));
                    return true;
                } else {
                    return false;
                }

            });

            popup.show();
        });
    }
}
