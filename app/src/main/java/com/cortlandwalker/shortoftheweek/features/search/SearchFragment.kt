package com.cortlandwalker.shortoftheweek.features.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.cortlandwalker.shortoftheweek.R
import com.cortlandwalker.ghettoxide.ReducerFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SearchFragment : ReducerFragment<SearchState, SearchAction, SearchEffect, SearchReducer>() {
    override val initialState: SearchState = SearchState()

    @Inject override lateinit var reducer: SearchReducer

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val state = vm.state.collectAsState().value
                SearchScreen(state, reducer)
            }
        }
    }

    override fun onEffect(effect: SearchEffect) {
        when (effect) {
            is SearchEffect.OpenFilmDetail -> {
                findNavController().navigate(
                    R.id.action_searchFragment_to_filmDetailFragment,
                    bundleOf(
                        "film" to effect.film
                    )
                )
            }
        }
    }
}
