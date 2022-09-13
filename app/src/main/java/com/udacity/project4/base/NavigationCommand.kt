package com.udacity.project4.base

import androidx.navigation.NavDirections

/**
 * Sealed class used with the live data to navigate between the fragments
 */
sealed class NavigationCommand {

    data class To(val directions: NavDirections) : NavigationCommand()

    object Back : NavigationCommand()

    data class BackTo(val destinationId: Int) : NavigationCommand()
}