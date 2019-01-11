package com.example.toni.tipkovnica04;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;


// Prikazuje "Preferences" (postavke) za doticnu metodu unosa teksta
// TESTIRANJE:::
// Buduci da ne postoji launcher aktivnost:
// Run -> Edit Configurations...
// Run/Debug Configurations dialog: General (tab):
// Launch Options: Launch: Nothing
public class ImePreferences extends /*PreferenceActivity*/ AppCompatActivity {

    static MyPreferenceFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.settings_name);

        // Postoji odgovarajuca xml datoteka koja opisuje postavke unosne metode:
        // addPreferencesFromResource(R.xml.ime_preferences); // <- deprecated
        // @Android API:
        // The preferred approach (as of API level 11) is to instantiate PreferenceFragment
        // objects to load preferences from a resource file:
        mFragment = new MyPreferenceFragment();
        getFragmentManager().beginTransaction().replace(android.R.id.content, mFragment).commit();
    }


    // Preference fragment; ovdje ce se definirati listeneri koji reagiraju na
    // promjenu vrijednosti upisanu u neki EditText element:
    public static class MyPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.ime_preferences);

            // Dodavanje listenera koji ce promjenu neke vrijednosti u postavkama
            // automatski vizualizirati na zaslonu;
            // (kada ovoga ne bi bilo, onda bi se takva informcija vizualizirala tek po
            // ponovnom ucitavanju Preference zaslona):
            setEditTextListener("enlarge_key_percentage");
        }


        protected void setEditTextListener(String preferenceKey){
            EditTextPreference etPref =
                    (EditTextPreference)getPreferenceManager().findPreference(preferenceKey);

            if (etPref != null) {
                etPref.setOnPreferenceChangeListener(new EditTextPreference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        EditTextPreference pref = (EditTextPreference) preference;
                        pref.setSummary(newValue.toString());
                        return true;
                    }
                });

                etPref.setSummary(etPref.getText());
            }
        }
    }

}
