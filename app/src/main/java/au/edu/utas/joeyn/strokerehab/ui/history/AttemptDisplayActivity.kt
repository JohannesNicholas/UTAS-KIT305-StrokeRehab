package au.edu.utas.joeyn.strokerehab.ui.history

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import au.edu.utas.joeyn.strokerehab.R
import au.edu.utas.joeyn.strokerehab.databinding.ActivityNormalGameBinding

class AttemptDisplayActivity : AppCompatActivity() {

    private lateinit var ui : ActivityNormalGameBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ui = ActivityNormalGameBinding.inflate(layoutInflater)

        //TODO add code for this activity

        setContentView(ui.root)
    }
}