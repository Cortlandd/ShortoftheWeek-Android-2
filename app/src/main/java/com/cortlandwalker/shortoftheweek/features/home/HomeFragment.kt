package com.cortlandwalker.shortoftheweek.features.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.cortlandwalker.ghettoxide.ReducerFragment
import com.cortlandwalker.shortoftheweek.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : ReducerFragment<HomeState, HomeAction, HomeEffect, HomeReducer>() {
    override val initialState: HomeState = HomeState()

    @Inject override lateinit var reducer: HomeReducer

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return ComposeView(requireContext()).apply {
            setContent {
                val state = vm.state.collectAsState().value
                HomeScreen(state, reducer)
            }
        }
    }

    override fun onEffect(effect: HomeEffect) {
        when (effect) {
            is HomeEffect.OpenFilmDetail -> {
                findNavController().navigate(
                    R.id.action_homeFragment_to_filmDetailFragment,
                    bundleOf("filmId" to effect.filmId)
                )
            }
        }
    }
}