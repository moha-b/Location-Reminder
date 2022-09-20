import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(var reminders: MutableList<ReminderDTO>? = mutableListOf()) :
    ReminderDataSource {

    private var returnError = false

    fun setreturnError(value:Boolean){
        returnError = value
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if(returnError){
            return Result.Error("not found")
        }
        reminders?.let { return Result.Success(ArrayList(it)) }
        return Result.Error("not found")

    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if(returnError){
            return Result.Error("not found")
        }
        val reminder = reminders?.find {
            it.id == id
        }
        return if (reminder!=null){
            Result.Success(reminder)
        } else{
            Result.Error("not found")
        }
    }

    override suspend fun deleteAllReminders() {
        reminders?.clear()
    }

    override suspend fun delete(id: String) {
        val reminder = reminders?.find {
            it.id == id
        }
        reminders?.remove(reminder)
    }
}