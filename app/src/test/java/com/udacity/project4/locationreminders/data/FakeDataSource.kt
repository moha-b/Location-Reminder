package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(private var reminders: MutableList<ReminderDTO>? = mutableListOf()) : ReminderDataSource {

    private var returnError = false
    // Create a fake data source to act as a double to the real data source
    fun shouldReturnError(value:Boolean){
        returnError = value
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        // Save the reminder
        reminders?.add(reminder)
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        // Return the reminders
        reminders?.let { return Result.Success(ArrayList(it)) }
        // show a massage there is no reminders
        return Result.Error("Noo Reminders found")
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        // Return the reminder with the id
        val reminder = reminders?.find {
            it.id == id
        }
        return if (reminder!=null){
            Result.Success(reminder)
        } else{
            Result.Error("no Reminder found")
        }
    }

    override suspend fun delete(id: String) {
        val remind = reminders?.find {
            it.id == id
        }
        reminders?.remove(remind)
    }

    override suspend fun deleteAllReminders() {
        // Delete all the reminders
        reminders?.clear()
    }
}