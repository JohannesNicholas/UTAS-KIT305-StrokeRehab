package au.edu.utas.joeyn.strokerehab.ui

import android.content.ContentValues
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import au.edu.utas.joeyn.strokerehab.R
import au.edu.utas.joeyn.strokerehab.Record
import au.edu.utas.joeyn.strokerehab.RecordMessage
import au.edu.utas.joeyn.strokerehab.databinding.ActivitySliderGameBinding
import au.edu.utas.joeyn.strokerehab.ui.history.ATTEMPT_ID_KEY
import au.edu.utas.joeyn.strokerehab.ui.history.AttemptDisplayActivity
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

    private var timer: CountDownTimer? = null
    private var timeLimit = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ui = ActivitySliderGameBinding.inflate(layoutInflater)

        freePlay = intent.getBooleanExtra(FREE_PLAY_KEY, false)




        //load settings
        val perfs = PreferenceManager.getDefaultSharedPreferences(this)
        numberOfRounds = perfs.getString(getString(R.string.pref_key_slider_task_reps), "5")?.toInt() ?: 5
        timeLimit = perfs.getString(getString(R.string.pref_key_slider_task_time), "0")?.toInt() ?: 0
        numberOfNotches = perfs.getString(getString(R.string.pref_key_slider_task_notches), "3")?.toInt() ?: 3
        randomOrder = perfs.getBoolean(getString(R.string.pref_key_slider_task_random), true)
        sliderHandleSize = arrayOf(70,100,120,150)[
                perfs.getString(
                    getString(R.string.pref_key_slider_task_size)
                    , "1")?.toInt() ?: 1]





        //time limit
        ui.timeBar2.max = timeLimit
        if (timeLimit != 0) {
            timer = object: CountDownTimer((timeLimit * 1000).toLong(), 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    ui.timeNumber2.text = (millisUntilFinished / 1000).toString()
                    ui.timeBar2.progress = (timeLimit - (millisUntilFinished / 1000)).toInt()
                }
                override fun onFinish() {
                    endOfGame(timeOut = true)
                }
            }
            timer?.start()
        }
        else{
            ui.timeBar2.visibility = View.INVISIBLE
            ui.timeNumber2.visibility = View.INVISIBLE
        }


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
            reps = if (!freePlay) numberOfRounds else null,
            buttonsOrNotches = numberOfNotches,
            start = Timestamp.now(),
        )



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

    override fun finish() {
        timer?.cancel()
        super.finish()
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

        if (round > numberOfRounds && !freePlay  && numberOfRounds != 0){
            endOfGame()
            return
        }

        scoreText.text = if (freePlay) "$round/∞️" else "$round/$numberOfRounds"

        slider.progress = 0
        nextNotch = numberOfNotches

        if (randomOrder){
            nextNotch = (1..numberOfNotches).random()
            slider.progress = (0..1).random() * (numberOfNotches + 1)
        }

        targetBar.progress = nextNotch
    }


    //is called at the end of a game.
    private fun endOfGame(timeOut: Boolean = false){
        timer?.cancel()
        val message = if (timeOut) getString(R.string.time_out) else getString(R.string.complete)

        record(message)
        Toast.makeText(ui.root.context, message, Toast.LENGTH_LONG).show()

        val intent = Intent(ui.root.context, AttemptDisplayActivity::class.java)
        intent.putExtra(ATTEMPT_ID_KEY, documentID)
        startActivity(intent)

        finish()
    }

    //adds a message into the record and stores it in the database
    private fun record(message: String, correctPress: Boolean? = null){
        recordData.messages?.add(
            RecordMessage(
                datetime = Timestamp.now(),
                message = message,
                correctPress = correctPress,
                rep = round
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