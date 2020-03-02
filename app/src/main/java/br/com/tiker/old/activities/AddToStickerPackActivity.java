package br.com.tiker.old.activities;

import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;

import br.com.tiker.old.identities.StickerPacksContainer;
import br.com.tiker.utils.StickerPacksManager;

public class AddToStickerPackActivity extends AppCompatActivity {

    Uri stickerUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.stickerUri = this.getIntent().getData();

        StickerPacksManager.stickerPacksContainer = new StickerPacksContainer("", "", StickerPacksManager.getStickerPacks(this));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Toast.makeText(this, stickerUri.getPath(), Toast.LENGTH_LONG).show();
    }
}