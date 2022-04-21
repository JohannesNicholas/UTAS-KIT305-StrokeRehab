package au.edu.utas.joeyn.strokerehab.ui.history

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
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    val db = Firebase.firestore

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    val items = mutableListOf<Record>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        _binding = FragmentHistoryBinding.inflate(inflater, container, false)



        binding.historyList.adapter = HistoryItemAdapter(historyItems = items)
        binding.historyList.layoutManager = LinearLayoutManager(binding.root.context)

        db.collection("Records")
            .get()
            .addOnSuccessListener { result ->
                items.clear()
                for (document in result)
                {
                    Log.d("Firebase", document.toString())
                    val record = document.toObject<Record>()
                    items.add(record)
                }

                (binding.historyList.adapter as HistoryItemAdapter).notifyDataSetChanged()
            }



        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



    inner class ItemView(var ui: ListViewItemThreeTextBinding) : RecyclerView.ViewHolder(ui.root) {}

    inner class HistoryItemAdapter(private val historyItems: MutableList<Record>) : RecyclerView.Adapter<ItemView>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemView {
            val ui = ListViewItemThreeTextBinding.inflate(layoutInflater, parent, false)   //inflate a new row from the my_list_item.xml
            return ItemView(ui)
        }

        override fun onBindViewHolder(holder: ItemView, position: Int) {
            val record = historyItems[position]   //get the data at the requested position
            holder.ui.leftText.text = record.title //set the TextView in the row we are recycling
        }

        override fun getItemCount(): Int {
            return historyItems.size
        }
    }
}