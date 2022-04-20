package au.edu.utas.joeyn.strokerehab.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import au.edu.utas.joeyn.strokerehab.databinding.FragmentHomeBinding
import au.edu.utas.joeyn.strokerehab.ui.FREE_PLAY_KEY
import au.edu.utas.joeyn.strokerehab.ui.NormalGame
import au.edu.utas.joeyn.strokerehab.ui.SliderGame

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.normalTaskButton.setOnClickListener {
            val intent = Intent(root.context, NormalGame::class.java)
            intent.putExtra(FREE_PLAY_KEY, false)
            startActivity(intent)
        }

        binding.normalTaskButtonFreePlay.setOnClickListener {
            val intent = Intent(root.context, NormalGame::class.java)
            intent.putExtra(FREE_PLAY_KEY, true)
            startActivity(intent)
        }

        binding.sliderTaskButton.setOnClickListener {
            val intent = Intent(root.context, SliderGame::class.java)
            intent.putExtra(FREE_PLAY_KEY, false)
            startActivity(intent)
        }

        binding.sliderTaskButtonFreePlay.setOnClickListener {
            val intent = Intent(root.context, SliderGame::class.java)
            intent.putExtra(FREE_PLAY_KEY, true)
            startActivity(intent)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}