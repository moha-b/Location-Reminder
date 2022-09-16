package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(
    private var reminders: MutableList<ReminderDTO> = mutableListOf()
) : ReminderDataSource {
    // DONE: Create a fake data source to act as a double to the real data source
    //private var isReturnError = false
//    fun setReturnsError(value: Boolean) {
//        isReturnError = value
//    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        // DONE: "Save the reminder"
        reminders?.add(reminder)
    }

    override suspend fun deleteAllReminders() {
        // DONE: "Delete all the reminders"
        reminders?.clear()
    }

    override suspend fun delete(id: String) {
        val remind = reminders.find {
            it.id == id
        }
        reminders.remove(remind)
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        // DONE: "Return the reminders"
        reminders?.let {
            return Result.Success(ArrayList(it))
        }
        // if there is no reminder in database return the error message
        return Result.Error("Reminders not found")
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        // DONE: "Return the reminder with the id"
        reminders?.let { reminderList->
            for (i in reminderList){
                if(i.id ==id)
                    return Result.Success(i)
            }
        }
        // if there is no reminder in database return the error message
        return Result.Error("Reminder not found")

    }
}
