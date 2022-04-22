package au.edu.utas.joeyn.strokerehab.ui.settings

import android.os.Bundle
import android.text.InputType
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import au.edu.utas.joeyn.strokerehab.R


class SettingsFragment : PreferenceFragmentCompat() {



    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {


        setPreferencesFromResource(R.xml.root_preferences, rootKey)





    }
}