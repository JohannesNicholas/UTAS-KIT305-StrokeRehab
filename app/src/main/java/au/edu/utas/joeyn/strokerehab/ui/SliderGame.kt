package au.edu.utas.joeyn.strokerehab.ui

import android.content.ContentValues
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import au.edu.utas.joeyn.strokerehab.R
import au.edu.utas.joeyn.strokerehab.Record
import au.edu.utas.joeyn.strokerehab.RecordMessage
import au.edu.utas.joeyn.strokerehab.databinding.ActivitySliderGameBinding
import com.google.firebase.Timestamp
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
    private var nextNotch = 1
    private var round = 0

    private lateinit var scoreText : TextView
    private lateinit var slider : SeekBar
    private lateinit var targetBar : ProgressBar


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
        slider = ui.seekBar2
        targetBar = ui.progressBar


        slider.max = numberOfNotches + 1
        targetBar.max = numberOfNotches + 1
        slider.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                sliderChanged()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {

            }
        })

        documentID = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(Date())
        recordData = Record(
            title = getString(R.string.slider_task),
            messages = mutableListOf(),
            reps = if (!randomOrder) numberOfRounds else null,
            buttonsOrNotches = numberOfNotches)


        //TODO newRound()

        setContentView(ui.root)

        newRound()

        db.collection("Records").document(documentID).set(recordData)
            .addOnSuccessListener {
                Log.d(ContentValues.TAG, "DocumentSnapshot added")
            }
            .addOnFailureListener { e ->
                Log.w(ContentValues.TAG, "Error adding document", e)
            }
    }


    //called when a slider is slid, passed the number of the notch
    private fun sliderChanged(){
        val number = slider.progress
        record("Slid to $number", (number == nextNotch))

        if (number == nextNotch){ //correct notch was pressed
            newRound()
        }
    }


    //prepares the board for a new round
    private fun newRound(){
        round++

        if (round > numberOfRounds && !freePlay){
            endOfGame()
            return
        }

        scoreText.text = if (freePlay) "$round/∞️" else "$round/$numberOfRounds"
        record("Round $round")

        slider.progress = 0
        nextNotch = numberOfNotches

        if (randomOrder){
            nextNotch = (1..numberOfNotches).random()
            slider.progress = (0..1).random() * (numberOfNotches + 1)
        }

        targetBar.progress = nextNotch
    }


    //is called at the end of a game.
    private fun endOfGame(){
        record("Complete!")
        Toast.makeText(ui.root.context, "🏆 COMPLETE! 🏆", Toast.LENGTH_LONG).show()
        finish()
    }

    //adds a message into the record and stores it in the database
    private fun record(message: String, correctPress: Boolean? = null){
        recordData.messages?.add(
            RecordMessage(
                datetime = Timestamp.now(),
                message = message,
                correctPress = correctPress
            )
        )

        db.collection("Records").document(documentID).set(recordData)
            .addOnSuccessListener {
                Log.d(ContentValues.TAG, "DocumentSnapshot added")
            }
            .addOnFailureListener { e ->
                Log.w(ContentValues.TAG, "Error setting document", e)
            }
    }
}