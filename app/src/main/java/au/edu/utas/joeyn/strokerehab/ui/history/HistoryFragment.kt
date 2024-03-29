package au.edu.utas.joeyn.strokerehab.ui.history

import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import au.edu.utas.joeyn.strokerehab.R
import au.edu.utas.joeyn.strokerehab.Record
import au.edu.utas.joeyn.strokerehab.Totals
import au.edu.utas.joeyn.strokerehab.databinding.ActivityAttemptDisplayBinding
import au.edu.utas.joeyn.strokerehab.databinding.FragmentHistoryBinding
import au.edu.utas.joeyn.strokerehab.databinding.ListViewItemThreeTextBinding
import au.edu.utas.joeyn.strokerehab.ui.FREE_PLAY_KEY
import au.edu.utas.joeyn.strokerehab.ui.NormalGame
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.local.QueryResult
import com.google.firebase.ktx.Firebase
import java.util.*

const val FIREBASE_LOG_TAG = "Firebase"
const val RECORDS_COLLECTION_PATH = "Records"

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private var snapshotListener: ListenerRegistration? = null
    val db = Firebase.firestore
    var recordDocuments : QuerySnapshot? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private lateinit var binding : FragmentHistoryBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        binding = FragmentHistoryBinding.inflate(layoutInflater)



        binding.historyList.adapter = HistoryItemAdapter(records = recordDocuments)
        binding.historyList.layoutManager = LinearLayoutManager(binding.root.context)

        binding.chipGoals.setOnClickListener { updateRecordsList() }
        binding.chipFreePlay.setOnClickListener { updateRecordsList() }
        binding.chipNormal.setOnClickListener { updateRecordsList() }
        binding.chipSlider.setOnClickListener { updateRecordsList() }


        binding.shareButton2.setOnClickListener {
            var csv = "title, start, end, repetition count, presses count\n"

            recordDocuments?.documents?.forEach {

                val record = it.toObject<Record>()
                val title = record?.title ?: "Untitled"
                val start = it.id
                val end = (record?.messages?.lastOrNull()?.datetime ?: "?").toString()
                val repC = ((record?.messages?.lastOrNull()?.rep ?: 1) - 1).toString()
                var pressC = 0

                record?.messages?.forEach { message ->
                    if (message.correctPress != null){
                        pressC++
                    }
                }

                csv += "$title, $start, $end, $repC, $pressC\n"
            }


            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_subject))
            intent.putExtra(Intent.EXTRA_TEXT, csv)
            startActivity(Intent.createChooser(intent, getString(R.string.share_using)))
        }

        updateRecordsList()

        snapshotListener = db.collection("totals")
            .document("totals")
            .addSnapshotListener { value, error ->
                val totalButtonPresses = value?.toObject<Totals>()?.correctButtonPresses

                binding.correctButttonPresses.text = "$totalButtonPresses correct presses in total"

            }



        return binding.root
    }



    //updates the list of records that is used in displaying the list of attempts
    private fun updateRecordsList(){

        recordDocuments = null

        var query: Query = db.collection(RECORDS_COLLECTION_PATH)


        //filter options
        val freePlay = binding.chipFreePlay.isChecked
        val goals = binding.chipGoals.isChecked
        val slider = binding.chipSlider.isChecked
        val normal = binding.chipNormal.isChecked

        if (freePlay && !goals){
            query = query.whereEqualTo("reps", null)
        }
        if (goals && !freePlay){
            query = query.whereEqualTo("goals", true)
        }
        if (slider && !normal){
            query = query.whereEqualTo("title", getString(R.string.slider_task))
        }
        if (normal && !slider){
            query = query.whereEqualTo("title", getString(R.string.normal_task))
        }


        query.get().addOnSuccessListener { result ->
                recordDocuments = result

                Log.d(FIREBASE_LOG_TAG, "Firebase connected.")

                Log.d(FIREBASE_LOG_TAG, "Number of records: " + (recordDocuments?.documents?.size ?: 1).toString())
                (binding.historyList.adapter as HistoryItemAdapter).notifyDataSetChanged()

            }



    }

    override fun onDestroyView() {
        snapshotListener?.remove()
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()

        updateRecordsList()
    }



    inner class ItemView(var ui: ListViewItemThreeTextBinding) : RecyclerView.ViewHolder(ui.root) {}

    inner class HistoryItemAdapter(private val records: QuerySnapshot?) : RecyclerView.Adapter<ItemView>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemView {
            val ui = ListViewItemThreeTextBinding.inflate(layoutInflater, parent, false)   //inflate a new row from the my_list_item.xml
            return ItemView(ui)
        }

        override fun onBindViewHolder(holder: ItemView, position: Int) {
            val reversedPosition = (recordDocuments?.documents?.size ?: 0) - position - 1
            val recordDocument = recordDocuments?.documents?.get(reversedPosition)
            val record = recordDocument?.toObject<Record>()   //get the data at the requested position
            if (recordDocument != null) {
                record?.start = Timestamp(SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).parse(recordDocument.id))
            }




            if (record != null){

                holder.ui.leftText.text =

                    //if a modern record
                    if (record.start?.toDate()?.after(Date(2022, 4, 21)) == true) {
                        //use the modern title
                        record.title //set the TextView in the row we are recycling
                    }
                    else {
                        //convert the legacy title into a modern title
                        record.title?.split(" - ")?.get(0)

                    }


                holder.ui.layout.setOnClickListener {
                    val intent = Intent(binding.root.context, AttemptDisplayActivity::class.java)
                    intent.putExtra(ATTEMPT_ID_KEY, recordDocument.id)
                    startActivity(intent)
                }



                holder.ui.middleText.text = SimpleDateFormat("d MMM ha", Locale.ENGLISH).format(
                    record.start?.toDate() ?: Date(0)
                )

                holder.ui.rightText.text =
                    (record.reps ?: "∞").toString() +
                    " x " +
                    (record.buttonsOrNotches ?: "?").toString()
            }

        }

        override fun getItemCount(): Int {
            return recordDocuments?.documents?.size ?: 1
        }
    }
}



//items.clear()
//for (document in result)
//{
//    Log.d("Firebase", document.toString())
//    val record = document.toObject<Record>()
//
//    if (record.messages?.size ?: 0 > 0){
//        record.start = (record.messages?.get(0)?.datetime ?: Timestamp.now())
//    }
//    else{
//        record.start = Timestamp.now()
//    }
//
//    items.add(record)
//}