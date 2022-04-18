package au.edu.utas.joeyn.strokerehab.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import au.edu.utas.joeyn.strokerehab.R
import au.edu.utas.joeyn.strokerehab.databinding.ActivityNormalGameBinding

class NormalGame : AppCompatActivity() {

    private lateinit var ui : ActivityNormalGameBinding
    private val numberOfButtons = 3
    private lateinit var buttons : Array<Button>
    private var nextNumber = 1
    private var round = 0

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

        for (btn in buttons){
            btn.setOnClickListener {
                buttonPressed(btn.text.toString().toInt())
            }
        }
        newRound()
        setContentView(ui.root)
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

    private fun record(message: String){

    }


    //prepares the board for a new round
    private fun newRound(){
        nextNumber = 1
        round++
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
                button.setBackgroundColor( getColor(R.color.gray_light) )
            }
        }
    }
}