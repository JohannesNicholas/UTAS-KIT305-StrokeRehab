package au.edu.utas.joeyn.strokerehab.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import au.edu.utas.joeyn.strokerehab.databinding.FragmentHistoryBinding

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textDashboard
        textView.text = "History is working"
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}