package ru.sash0k.bluetooth_moru.activity;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.MenuItem;

import ru.sash0k.bluetooth_moru.R;

/**
 * Created by sash0k on 29.11.13.
 * Настройки приложения
 */
@SuppressWarnings("deprecation")
public final class SettingsActivity extends PreferenceActivity {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//
    }
    // ============================================================================


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    // ============================================================================

    // ============================================================================
}
