package com.renaldoviola.androidtablayoutactivity;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

public class AndroidTabLayoutActivity extends TabActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab);

        TabHost tabHost = getTabHost();

        TabHost.TabSpec restaurantespec = tabHost.newTabSpec("Restaurantes");
        // setting Title and Icon for the Tab
        restaurantespec.setIndicator("Restaurantes", getResources().getDrawable(R.drawable.icon_restaurantes_tab));
        Intent photosIntent = new Intent(this, RestaurantesActivity.class);
        restaurantespec.setContent(photosIntent);

        TabHost.TabSpec lanchonetespec = tabHost.newTabSpec("Lanchonetes");
        lanchonetespec.setIndicator("Lanchonetes", getResources().getDrawable(R.drawable.icon_lanchonetes_tab));
        Intent songsIntent = new Intent(this, LanchonetesActivity.class);
        lanchonetespec.setContent(songsIntent);

        // Tab for Videos
        TabHost.TabSpec favoritospec = tabHost.newTabSpec("Favoritos");
        favoritospec.setIndicator("Favoritos", getResources().getDrawable(R.drawable.icon_favoritos_tab));
        Intent videosIntent = new Intent(this, FavoritosActivity.class);
        favoritospec.setContent(videosIntent);

        // Adding all TabSpec to TabHost
        tabHost.addTab(restaurantespec);
        tabHost.addTab(lanchonetespec);
        tabHost.addTab(favoritospec);
    }
}
