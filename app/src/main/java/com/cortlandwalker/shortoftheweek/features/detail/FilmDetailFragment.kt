package com.cortlandwalker.shortoftheweek.features.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ComposeView
import androidx.navigation.fragment.navArgs
import com.cortlandwalker.ghettoxide.ReducerFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FilmDetailFragment : ReducerFragment<FilmDetailState, FilmDetailAction, FilmDetailEffect, FilmDetailReducer>() {
    private val args: FilmDetailFragmentArgs by navArgs()

    override val initialState: FilmDetailState by lazy {
        if (args.filmId > -1 && args.film == null) {
            FilmDetailState(filmId = args.filmId, film = null)
        } else {
            FilmDetailState(filmId = -1, film = args.film)
        }
    }

    @Inject override lateinit var reducer: FilmDetailReducer

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val state = vm.state.collectAsState().value
                FilmDetailScreen(state, reducer)
            }
        }
    }

    override fun onEffect(effect: FilmDetailEffect) {
        // no-op
    }
}
