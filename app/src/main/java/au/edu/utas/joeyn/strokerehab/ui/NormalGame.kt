package au.edu.utas.joeyn.strokerehab.ui

import android.content.ContentValues.TAG
import android.content.Context
import android.content.res.Resources
import android.icu.text.SimpleDateFormat
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.preference.PreferenceManager
import au.edu.utas.joeyn.strokerehab.R
import au.edu.utas.joeyn.strokerehab.Record
import au.edu.utas.joeyn.strokerehab.RecordMessage
import au.edu.utas.joeyn.strokerehab.databinding.ActivityNormalGameBinding
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*

const val FREE_PLAY_KEY = "free_play"

class NormalGame : AppCompatActivity() {

    private var numberOfButtons = 3
    private var numberOfRounds = 5
    private var randomOrder = true
    private var highlightNextButton = true
    private var buttonSize = 1
    private var freePlay = false

    val db = Firebase.firestore
    private lateinit var documentID : String
    private lateinit var recordData : Record

    private lateinit var ui : ActivityNormalGameBinding
    private lateinit var buttons : Array<Button>
    private var nextNumber = 1
    private var round = 0

    private lateinit var scoreText : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityNormalGameBinding.inflate(layoutInflater)

        freePlay = intent.getBooleanExtra(FREE_PLAY_KEY, false)

        buttons = arrayOf(
            ui.buttonA,
            ui.buttonB,
            ui.buttonC,
            ui.buttonD,
            ui.buttonE
        )


        //load settings
        val perfs = PreferenceManager.getDefaultSharedPreferences(this)
        numberOfRounds = perfs.getString(getString(R.string.pref_key_normal_task_reps), "5")?.toInt() ?: 5
        numberOfButtons = perfs.getString(getString(R.string.pref_key_normal_task_buttons), "3")?.toInt() ?: 3
        randomOrder = perfs.getBoolean(getString(R.string.pref_key_normal_task_random), true)
        highlightNextButton = perfs.getBoolean(getString(R.string.pref_key_normal_task_highlight), true)
        buttonSize = arrayOf(70,100,120,150)[
                perfs.getString(
                    getString(R.string.pref_key_normal_task_size)
                    , "1")?.toInt() ?: 1]


        scoreText = ui.scoreText
        documentID = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(Date())

        recordData = Record(
            title = getString(R.string.normal_task),
            messages = mutableListOf(),
            reps = if (!randomOrder) numberOfRounds else null,
            buttonsOrNotches = numberOfButtons)




        for (btn in buttons){

            //change the size of the buttons
            val mParams = btn.layoutParams as ConstraintLayout.LayoutParams
            mParams.apply {
                width = buttonSize.toPx.toInt()
                height = buttonSize.toPx.toInt()
            }
            btn.layoutParams = mParams


            btn.setOnClickListener {
                buttonPressed(btn.text.toString().toInt())
            }
        }



        newRound()
        setContentView(ui.root)




        db.collection("Records").document(documentID).set(recordData)
            .addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot added")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }
    }


    //called when a button is pressed, passed the number of the button
    private fun buttonPressed(number: Int){
        record("$number Pressed", (number == nextNumber))

        if (number == nextNumber){ //correct button was pressed

            if (nextNumber == numberOfButtons){ //that was the last button in the round
                newRound()
            }
            else { //next button
                nextNumber++
            }

            highlightNextNum()
        }
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
                Log.d(TAG, "DocumentSnapshot added")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error setting document", e)
            }
    }


    //is called at the end of a game.
    private fun endOfGame(){
        record("Complete!")
        Toast.makeText(ui.root.context, "🏆 COMPLETE! 🏆", Toast.LENGTH_LONG).show()
        finish()
    }




    //prepares the board for a new round
    private fun newRound(){
        nextNumber = 1
        round++

        if (round > numberOfRounds && !freePlay){
            endOfGame()
            return
        }


        scoreText.text = if (freePlay) "$round/∞️" else "$round/$numberOfRounds"
        record("Round $round")
        val numbers = mutableListOf(1,2,3,4,5)

        if (randomOrder){
            numbers.shuffle()
        }


        for (i in 0..4){
            val button = buttons[i]
            val number = numbers[i]

            if (number <= numberOfButtons){
                button.visibility = View.VISIBLE
                button.text = number.toString()
            } else {
                button.visibility = View.INVISIBLE
            }
        }

        highlightNextNum()
    }

    //highlights the next button to be pressed, de highlights the others
    private fun highlightNextNum(){
        for (i in 0..4){
            val button = buttons[i]

            button.setBackgroundColor( getColor(R.color.orange_main) )

            if (button.text != nextNumber.toString() && highlightNextButton)
            {
                button.setBackgroundColor( getColor(R.color.not_button_grey) )
            }
        }
    }

    val Number.toPx get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics)
}