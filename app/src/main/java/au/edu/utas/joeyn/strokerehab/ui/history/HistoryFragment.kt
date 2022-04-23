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
import au.edu.utas.joeyn.strokerehab.Record
import au.edu.utas.joeyn.strokerehab.databinding.FragmentHistoryBinding
import au.edu.utas.joeyn.strokerehab.databinding.ListViewItemThreeTextBinding
import au.edu.utas.joeyn.strokerehab.ui.FREE_PLAY_KEY
import au.edu.utas.joeyn.strokerehab.ui.NormalGame
import com.google.firebase.Timestamp
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.local.QueryResult
import com.google.firebase.ktx.Firebase
import java.util.*

const val FIREBASE_LOG_TAG = "Firebase"

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    val db = Firebase.firestore
    var recordDocuments : QuerySnapshot? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        _binding = FragmentHistoryBinding.inflate(inflater, container, false)



        binding.historyList.adapter = HistoryItemAdapter(records = recordDocuments)
        binding.historyList.layoutManager = LinearLayoutManager(binding.root.context)



        db.collection("Records")
            .get()
            .addOnSuccessListener { result ->
                recordDocuments = result

                Log.d(FIREBASE_LOG_TAG, "Firebase connected.")

                Log.d(FIREBASE_LOG_TAG, "Number of records: " + (recordDocuments?.documents?.size ?: 1).toString())
                (binding.historyList.adapter as HistoryItemAdapter).notifyDataSetChanged()
            }



        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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