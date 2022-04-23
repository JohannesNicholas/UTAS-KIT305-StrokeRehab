package au.edu.utas.joeyn.strokerehab.ui.history

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import au.edu.utas.joeyn.strokerehab.R
import au.edu.utas.joeyn.strokerehab.databinding.ActivityAttemptDisplayBinding
import au.edu.utas.joeyn.strokerehab.databinding.ActivityNormalGameBinding

const val ATTEMPT_ID_KEY = "attempt_id_key"

class AttemptDisplayActivity : AppCompatActivity() {

    private lateinit var ui : ActivityAttemptDisplayBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ui = ActivityAttemptDisplayBinding.inflate(layoutInflater)

        //TODO add code for this activity

        setContentView(ui.root)
    }
}