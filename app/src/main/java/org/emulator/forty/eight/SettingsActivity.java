// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, write to the Free Software Foundation, Inc.,
// 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

package org.emulator.forty.eight;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;

import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceDataStore;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SeekBarPreference;

import org.emulator.calculator.EmuApplication;
import org.emulator.calculator.NativeLib;
import org.emulator.calculator.Settings;
import org.emulator.calculator.Utils;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";
	protected final boolean debug = false;

    private static Settings settings;
    private HashSet<String> settingsKeyChanged = new HashSet<>();
	private SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener = (sharedPreferences, key) -> settingsKeyChanged.add(key);
    private GeneralPreferenceFragment generalPreferenceFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

	    settings = EmuApplication.getSettings();
	    settings.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        generalPreferenceFragment = new GeneralPreferenceFragment();
        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, generalPreferenceFragment).commit();
    }

    @Override
    protected void onDestroy() {
        settings.unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("changedKeys", settingsKeyChanged.toArray(new String[0]));
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    public static class GeneralPreferenceFragment extends PreferenceFragmentCompat {

        Preference preferencePort2load = null;

	    @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		    getPreferenceManager().setPreferenceDataStore(EmuApplication.getSettings());

		    // Load the preferences from an XML resource
	        setPreferencesFromResource(R.xml.pref_general, rootKey);

            setHasOptionsMenu(true);

            // Sound settings

            SeekBarPreference preferenceSoundVolume = findPreference("settings_sound_volume");
            if(preferenceSoundVolume != null) {
                if(!NativeLib.getSoundEnabled()) {
                    preferenceSoundVolume.setSummary("Cannot initialize the sound engine.");
                    preferenceSoundVolume.setEnabled(false);
                } else {
                    preferenceSoundVolume.setOnPreferenceClickListener(preference -> {
                        AlertDialog.Builder alert = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
                        alert.setTitle(R.string.settings_sound_volume_dialog_title);
                        final EditText input = new EditText(getContext());
                        input.setInputType(InputType.TYPE_CLASS_NUMBER);
                        input.setRawInputType(Configuration.KEYBOARD_12KEY);
                        input.setFocusable(true);
                        input.setText(String.format(Locale.US,"%d", preferenceSoundVolume.getValue()));
                        alert.setView(input);
                        alert.setPositiveButton(R.string.message_ok, (dialog, whichButton) -> {
                            String newValueText = input.getText().toString();
                            try {
                                int newValue = Integer.parseInt(newValueText);
                                if(newValue >= preferenceSoundVolume.getMin() && newValue <= preferenceSoundVolume.getMax())
                                    preferenceSoundVolume.setValue(newValue);
                            } catch (NumberFormatException ignored) {}
                        });
                        alert.setNegativeButton(R.string.message_cancel, (dialog, whichButton) -> {});
                        alert.show();
                        return true;
                    });
                }
            }

            // Background color settings

            Preference preferenceBackgroundFallbackColor = findPreference("settings_background_fallback_color");
//            final ColorPickerPreferenceCompat preferenceBackgroundCustomColor = (ColorPickerPreferenceCompat)findPreference("settings_background_custom_color");
            if(preferenceBackgroundFallbackColor != null /*&& preferenceBackgroundCustomColor != null*/) {
                final String[] stringArrayBackgroundFallbackColor = getResources().getStringArray(R.array.settings_background_fallback_color_item);
                Preference.OnPreferenceChangeListener onPreferenceChangeListenerBackgroundFallbackColor = (preference, value) -> {
                    if(value != null) {
                        String stringValue = value.toString();
                        int backgroundFallbackColor = -1;
                        try {
                            backgroundFallbackColor = Integer.parseInt(stringValue);
                        } catch (NumberFormatException ignored) {}
                        if(backgroundFallbackColor >= 0 && backgroundFallbackColor < stringArrayBackgroundFallbackColor.length)
                            preference.setSummary(stringArrayBackgroundFallbackColor[backgroundFallbackColor]);
//                            preferenceBackgroundCustomColor.setEnabled(backgroundFallbackColor == 2);
                    }
                    return true;
                };
                preferenceBackgroundFallbackColor.setOnPreferenceChangeListener(onPreferenceChangeListenerBackgroundFallbackColor);
                onPreferenceChangeListenerBackgroundFallbackColor.onPreferenceChange(preferenceBackgroundFallbackColor,
                        settings.getString(preferenceBackgroundFallbackColor.getKey(), "0"));


                //preferenceBackgroundCustomColor.setColorValue(customColor);

//                Preference.OnPreferenceChangeListener onPreferenceChangeListenerBackgroundCustomColor = new Preference.OnPreferenceChangeListener() {
//                    @Override
//                    public boolean onPreferenceChange(Preference preference, Object value) {
//                        if(value != null) {
//                            int customColor = (Integer)value;
//                        }
//                        return true;
//                    }
//                };
//                preferenceBackgroundCustomColor.setOnPreferenceChangeListener(onPreferenceChangeListenerBackgroundCustomColor);
//                onPreferenceChangeListenerBackgroundCustomColor.onPreferenceChange(preferenceBackgroundCustomColor, sharedPreferences.getBoolean(preferenceBackgroundCustomColor.getKey(), false));
            }

            // Macro

            Preference preferenceMacroRealSpeed = findPreference("settings_macro_real_speed");
            Preference preferenceMacroManualSpeed = findPreference("settings_macro_manual_speed");
            if(preferenceMacroRealSpeed != null && preferenceMacroManualSpeed != null) {
                Preference.OnPreferenceChangeListener onPreferenceChangeListenerMacroRealSpeed = (preference, value) -> {
                    if(value != null)
                        preferenceMacroManualSpeed.setEnabled(!(Boolean) value);
                    return true;
                };
                preferenceMacroRealSpeed.setOnPreferenceChangeListener(onPreferenceChangeListenerMacroRealSpeed);
                onPreferenceChangeListenerMacroRealSpeed.onPreferenceChange(preferenceMacroRealSpeed, settings.getBoolean(preferenceMacroRealSpeed.getKey(), true));
            }

            // Ports 1 & 2 settings

            Preference preferencePort1en = findPreference("settings_port1en");
            Preference preferencePort1wr = findPreference("settings_port1wr");
            Preference preferencePort2en = findPreference("settings_port2en");
            Preference preferencePort2wr = findPreference("settings_port2wr");
            preferencePort2load = findPreference("settings_port2load");
            if(preferencePort1en != null && preferencePort1wr != null
                    && preferencePort2en != null && preferencePort2wr != null
                    && preferencePort2load != null) {
                boolean enablePortPreferences = NativeLib.isPortExtensionPossible();

                Preference.OnPreferenceChangeListener onPreferenceChangeListenerPort1en = (preference, value) -> {
                    preferencePort1en.setEnabled(enablePortPreferences);
                    preferencePort1wr.setEnabled(enablePortPreferences);
                    return true;
                };
                preferencePort1en.setOnPreferenceChangeListener(onPreferenceChangeListenerPort1en);
                onPreferenceChangeListenerPort1en.onPreferenceChange(preferencePort1en, settings.getBoolean(preferencePort1en.getKey(), false));

                Preference.OnPreferenceChangeListener onPreferenceChangeListenerPort2en = (preference, value) -> {
                    preferencePort2en.setEnabled(enablePortPreferences);
                    preferencePort2wr.setEnabled(enablePortPreferences);
                    preferencePort2load.setEnabled(enablePortPreferences);
                    return true;
                };
                preferencePort2en.setOnPreferenceChangeListener(onPreferenceChangeListenerPort2en);
                onPreferenceChangeListenerPort2en.onPreferenceChange(preferencePort2en, settings.getBoolean(preferencePort2en.getKey(), false));

                updatePort2LoadFilename(settings.getString(preferencePort2load.getKey(), ""));
                preferencePort2load.setOnPreferenceClickListener(preference -> {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("*/*");
                    intent.putExtra(Intent.EXTRA_TITLE, "shared.bin");
                    Activity activity = getActivity();
                    if (activity != null)
                        activity.startActivityForResult(intent, MainActivity.INTENT_PORT2LOAD);
                    return true;
                });
            }
        }

        void updatePort2LoadFilename(String port2Filename) {
            if(preferencePort2load != null) {
                String displayName = port2Filename;
                try {
                    displayName = Utils.getFileName(getActivity(), port2Filename);
                } catch (Exception e) {
                    // Do nothing
                }
                preferencePort2load.setSummary(displayName);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode == Activity.RESULT_OK && data != null) {
            if(requestCode == MainActivity.INTENT_PORT2LOAD) {
                Uri uri = data.getData();
                String url;
                if (uri != null) {
	                if(debug) Log.d(TAG, "onActivityResult INTENT_PORT2LOAD " + uri.toString());
                    url = uri.toString();
	                settings.putString("settings_port2load", url);
                    makeUriPersistable(data, uri);
                    if(generalPreferenceFragment != null)
                        generalPreferenceFragment.updatePort2LoadFilename(url);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void makeUriPersistable(Intent data, Uri uri) {
        int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            getContentResolver().takePersistableUriPermission(uri, takeFlags);
    }
}
