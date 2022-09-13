package com.udacity.project4.locationreminders.reminderslist

import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.*

/**
 * data class acts as a data mapper between the DB and the UI
 */
data class ReminderDataItem(
    var title: String?,
    var description: String?,
    var location: String?,
    var latitude: Double?,
    var longitude: Double?,
    @PrimaryKey
    val id: String
) : Serializable