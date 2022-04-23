package au.edu.utas.joeyn.strokerehab.ui.history

import android.content.Intent
import android.icu.text.SimpleDateFormat
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import au.edu.utas.joeyn.strokerehab.R
import au.edu.utas.joeyn.strokerehab.Record
import au.edu.utas.joeyn.strokerehab.databinding.ActivityAttemptDisplayBinding
import au.edu.utas.joeyn.strokerehab.databinding.ActivityNormalGameBinding
import au.edu.utas.joeyn.strokerehab.databinding.ListViewItemThreeTextBinding
import au.edu.utas.joeyn.strokerehab.ui.FREE_PLAY_KEY
import com.google.firebase.Timestamp
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import java.time.Period
import java.util.*

const val ATTEMPT_ID_KEY = "attempt_id_key"

class AttemptDisplayActivity : AppCompatActivity() {

    private lateinit var ui : ActivityAttemptDisplayBinding

    val db = Firebase.firestore

    var record : Record? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityAttemptDisplayBinding.inflate(layoutInflater)



        ui.recordList.adapter = RecordItemAdapter()
        ui.recordList.layoutManager = LinearLayoutManager(ui.root.context)

        //get the record
        val documentID = intent.getStringExtra(ATTEMPT_ID_KEY)
        title = "Loading ($documentID)"
        if (documentID != null) {
            db.collection("Records")
                .document(documentID)
                .get()
                .addOnSuccessListener { result ->
                    record = result.toObject<Record>()

                    if (record?.start == null){
                        record?.start = Timestamp(SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).parse(documentID))
                    }

                    title = (record?.title ?: "") +
                            SimpleDateFormat("  -  dd MMM yyyy  -  hh:mm:ss a", Locale.ENGLISH)
                                .format(record?.start?.toDate() ?: Date(0))

                    //x repetitions in x.xxx seconds
                    val lastMessage = record?.messages?.last()
                    if (lastMessage != null){
                        ui.repetitionsInSeconds.text = (lastMessage.rep ?: "?").toString() +
                                " repetitions in " +
                                (((lastMessage.datetime?.toDate()?.time ?: 0) -
                                        (record?.start?.toDate()?.time ?: 0)) / 1000f) +
                                " seconds"
                    }


                    //correct button presses
                    var correctButtonPresses = 0
                    record?.messages?.forEach { m ->
                        if (m.correctPress == true){
                            correctButtonPresses++
                        }
                    }
                    ui.correctButtonPresses.text = correctButtonPresses.toString() + " correct button presses"


                    (ui.recordList.adapter as RecordItemAdapter).notifyDataSetChanged()
                }
        }



        //TODO add code for this activity

        setContentView(ui.root)
    }



    inner class ItemView(var ui: ListViewItemThreeTextBinding) : RecyclerView.ViewHolder(ui.root) {}

    inner class RecordItemAdapter() : RecyclerView.Adapter<ItemView>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemView {
            val ui = ListViewItemThreeTextBinding.inflate(layoutInflater, parent, false)   //inflate a new row from the my_list_item.xml
            return ItemView(ui)
        }

        override fun onBindViewHolder(holder: ItemView, position: Int) {

            //hide clickable marker arrow
            holder.ui.rightArrow.visibility = View.GONE

            val message = record?.messages?.get(position)


            if (message != null){
                //message
                holder.ui.leftText.text = message.message

                //time
                val time = (message.datetime?.toDate() ?: Date(0))
                val attemptStart = (record?.start?.toDate() ?: Date(0))
                val difference = (time.time - attemptStart.time) / 1000f //time after start in seconds
                holder.ui.middleText.text = String.format("+ %.1f s", difference)


                //correct or not
                holder.ui.rightText.text =
                    when (message.correctPress) {
                        true -> "✅️"
                        false -> "❌️️"
                        null -> "️"
                    }


            }

        }

        override fun getItemCount(): Int {
            return record?.messages?.size ?: 1
        }
    }
}