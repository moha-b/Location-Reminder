package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(private var reminders: MutableList<ReminderDTO>? = mutableListOf()) : ReminderDataSource {

    private var returnError = false

    fun shouldReturnError(value:Boolean){
        returnError = value
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders?.add(reminder)
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if(returnError){
            return Result.Error("no Reminders found")
        }
        reminders?.let { return Result.Success(ArrayList(it)) }
        return Result.Error("Noo Reminders found")

    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if(returnError){
            return Result.Error("no Reminder found")
        }
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
        reminders?.clear()
    }

}