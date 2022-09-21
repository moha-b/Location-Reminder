package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(private var reminders: MutableList<ReminderDTO>? = mutableListOf()) : ReminderDataSource {

    private var returnError = false
    // Create a fake data source to act as a double to the real data source
    fun returnError(value:Boolean){
        returnError = value
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        // Save the reminder
        reminders?.add(reminder)
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if(returnError){
            return Result.Error("not found")
        }
        // Return the reminders
        reminders?.let { return Result.Success(ArrayList(it)) }
        return Result.Error("No Reminders found")
    }
    // that confirm the correct behavior when the reminders list for some reason can't be loaded
    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if(returnError){
            return Result.Error("no Reminder found")
        }
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