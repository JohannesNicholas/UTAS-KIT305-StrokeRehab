package au.edu.utas.joeyn.strokerehab.ui.settings

import android.os.Bundle
import android.text.InputType
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import au.edu.utas.joeyn.strokerehab.R


class SettingsFragment : PreferenceFragmentCompat() {



    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {


        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        //set numbers only for these preferences
        findPreference<EditTextPreference>(getString(R.string.pref_key_normal_task_reps))?.setOnBindEditTextListener { editText ->
            editText.inputType = InputType.TYPE_CLASS_NUMBER
        }
        findPreference<EditTextPreference>(getString(R.string.pref_key_slider_task_reps))?.setOnBindEditTextListener { editText ->
            editText.inputType = InputType.TYPE_CLASS_NUMBER
        }



    }
}