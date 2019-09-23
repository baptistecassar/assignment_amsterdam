package com.example.project.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.example.project.databinding.FragmentGameDetailsBinding
import com.example.project.model.Game

/**
 * @author Baptiste Cassar
 * displays a game details
 **/
class GameDetailsFragment : Fragment() {

    private lateinit var binding: FragmentGameDetailsBinding

    companion object {
        private const val ARG_GAME = "ARG_GAME"
        fun newInstance(game: Game) = GameDetailsFragment().apply {
            arguments = Bundle().apply {
                putSerializable(ARG_GAME, game)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGameDetailsBinding.inflate(inflater)
        (activity as? MainActivity)?.setSupportActionBar(binding.toolbar as Toolbar)
        val game = arguments?.getSerializable(ARG_GAME) as? Game
        binding.game = game
        return binding.root
    }
}