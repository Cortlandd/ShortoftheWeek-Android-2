package com.cortlandwalker.shortoftheweek.features.news

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
class NewsFragment : ReducerFragment<NewsState, NewsAction, NewsEffect, NewsReducer>() {
    override val initialState: NewsState = NewsState()

    @Inject override lateinit var reducer: NewsReducer

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val state = vm.state.collectAsState().value
                NewsScreen(state, reducer)
            }
        }
    }

    override fun onEffect(effect: NewsEffect) {
        when (effect) {
            is NewsEffect.OpenFilmDetail -> {
                findNavController().navigate(
                    R.id.action_newsFragment_to_filmDetailFragment,
                    bundleOf("filmId" to effect.filmId)
                )
            }
        }
    }
}
