package com.cortlandwalker.shortoftheweek.core.helpers

sealed interface ViewDisplayMode {
    data object Loading : ViewDisplayMode
    data class Error(val message: String) : ViewDisplayMode
    data object Empty : ViewDisplayMode
    data object Content : ViewDisplayMode
}