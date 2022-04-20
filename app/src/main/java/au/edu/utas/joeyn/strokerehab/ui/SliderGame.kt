package au.edu.utas.joeyn.strokerehab.ui

import android.content.ContentValues
import android.icu.text.SimpleDateFormat
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.preference.PreferenceManager
import au.edu.utas.joeyn.strokerehab.R
import au.edu.utas.joeyn.strokerehab.Record
import au.edu.utas.joeyn.strokerehab.databinding.ActivityNormalGameBinding
import au.edu.utas.joeyn.strokerehab.databinding.ActivitySliderGameBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*

class SliderGame : AppCompatActivity() {

    private var numberOfNotches = 3
    private var numberOfRounds = 5
    private var randomOrder = true
    private var sliderHandleSize = 1
    private var freePlay = false


    val db = Firebase.firestore
    private lateinit var documentID : String
    private lateinit var recordData : Record

    private lateinit var ui : ActivitySliderGameBinding
    private lateinit var buttons : Array<Button>
    private var round = 0

    private lateinit var scoreText : TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ui = ActivitySliderGameBinding.inflate(layoutInflater)

        freePlay = intent.getBooleanExtra(FREE_PLAY_KEY, false)




        //load settings
        val perfs = PreferenceManager.getDefaultSharedPreferences(this)
        numberOfRounds = perfs.getString(getString(R.string.pref_key_slider_task_reps), "5")?.toInt() ?: 5
        numberOfNotches = perfs.getString(getString(R.string.pref_key_slider_task_notches), "3")?.toInt() ?: 3
        randomOrder = perfs.getBoolean(getString(R.string.pref_key_slider_task_random), true)
        sliderHandleSize = arrayOf(70,100,120,150)[
                perfs.getString(
                    getString(R.string.pref_key_slider_task_size)
                    , "1")?.toInt() ?: 1]


        scoreText = ui.scoreText2
        documentID = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(Date())
        recordData = Record(title = "Slider - $numberOfRounds reps - $numberOfNotches notches", messages = mutableListOf())


        //TODO newRound()

        setContentView(ui.root)


        db.collection("Records").document(documentID).set(recordData)
            .addOnSuccessListener {
                Log.d(ContentValues.TAG, "DocumentSnapshot added")
            }
            .addOnFailureListener { e ->
                Log.w(ContentValues.TAG, "Error adding document", e)
            }
    }
}