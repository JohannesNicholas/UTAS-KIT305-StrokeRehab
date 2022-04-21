package au.edu.utas.joeyn.strokerehab.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import au.edu.utas.joeyn.strokerehab.databinding.FragmentHistoryBinding
import au.edu.utas.joeyn.strokerehab.databinding.ListViewItemThreeTextBinding

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    val items = mutableListOf<String>(
        "Rick",
        "Morty",
        "Beth",
        "Summer",
        "Not Jerry"
    )


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        val root: View = binding.root



        binding.historyList.adapter = HistoryItemAdapter(historyItems = items)
        binding.historyList.layoutManager = LinearLayoutManager(binding.root.context)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



    inner class ItemView(var ui: ListViewItemThreeTextBinding) : RecyclerView.ViewHolder(ui.root) {}

    inner class HistoryItemAdapter(private val historyItems: MutableList<String>) : RecyclerView.Adapter<ItemView>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemView {
            val ui = ListViewItemThreeTextBinding.inflate(layoutInflater, parent, false)   //inflate a new row from the my_list_item.xml
            return ItemView(ui)
        }

        override fun onBindViewHolder(holder: ItemView, position: Int) {
            val tx = historyItems[position]   //get the data at the requested position
            holder.ui.leftText.text = tx //set the TextView in the row we are recycling
        }

        override fun getItemCount(): Int {
            return historyItems.size
        }
    }
}