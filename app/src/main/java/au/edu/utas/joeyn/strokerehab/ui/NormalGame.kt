package au.edu.utas.joeyn.strokerehab.ui

import android.content.ContentValues.TAG
import android.content.Context
import android.icu.text.SimpleDateFormat
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import au.edu.utas.joeyn.strokerehab.R
import au.edu.utas.joeyn.strokerehab.Record
import au.edu.utas.joeyn.strokerehab.RecordMessage
import au.edu.utas.joeyn.strokerehab.databinding.ActivityNormalGameBinding
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*

class NormalGame : AppCompatActivity() {

    private val numberOfButtons = 3
    private var numberOfRounds = 5

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

        buttons = arrayOf(
            ui.buttonA,
            ui.buttonB,
            ui.buttonC,
            ui.buttonD,
            ui.buttonE
        )

        scoreText = ui.scoreText
        documentID = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(Date())
        recordData = Record(title = "Normal - $numberOfRounds reps - $numberOfButtons buttons", messages = mutableListOf())



        for (btn in buttons){
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
        record("$number Pressed")

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
    private fun record(message: String){
        recordData.messages.add(
            RecordMessage(
                datetime = Timestamp.now(),
                message = message
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
        finish()
    }




    //prepares the board for a new round
    private fun newRound(){
        nextNumber = 1
        round++

        if (round > numberOfRounds){
            endOfGame()
            return
        }

        scoreText.text = "$round/$numberOfRounds"
        record("Round $round")
        val numbers = mutableListOf(1,2,3,4,5)
        numbers.shuffle()

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

            if (button.text == nextNumber.toString()){
                button.setBackgroundColor( getColor(R.color.orange_main) )
            }
            else {
                button.setBackgroundColor( getColor(R.color.not_button_grey) )
            }
        }
    }
}