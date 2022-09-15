package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(
    private var reminders: MutableList<ReminderDTO> = mutableListOf()
) : ReminderDataSource {

    // DONE: Create a fake data source to act as a double to the real data source
    private var shouldReturnError = false

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        // DONE: "Return the reminders"
        if (shouldReturnError) {
            return Result.Error("Test exception")
        }
        return try {
            Result.Success(ArrayList(reminders))
        } catch (ex: Exception) {
            Result.Error(ex.localizedMessage)
        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        // DONE: "Save the reminder"
        reminders.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        // DONE: "Return the reminder with the id"
        if (shouldReturnError) {
            Result.Error("Test exception")
        }
        return try {
            val reminder = reminders.find {
                it.id == id
            }
            if (reminder != null) {
                Result.Success(reminder)
            } else {
                Result.Error("Reminder not found!")
            }
        } catch (e: Exception) {
            Result.Error(e.localizedMessage)
        }
    }

    override suspend fun deleteAllReminders() {
        // DONE: "Delete all the reminders"
        reminders = mutableListOf()
    }

    override suspend fun delete(id: String) {
        val remind = reminders.find {
            it.id == id
        }
        reminders.remove(remind)
    }
}