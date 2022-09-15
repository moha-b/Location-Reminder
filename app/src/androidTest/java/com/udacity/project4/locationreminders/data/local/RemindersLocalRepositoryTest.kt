package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat

/*
* The purpose of this test class is to evaluate our RemindersLocal Repository. We must test the following four functions:
* 1. getReminders
* 2. SaveReminder
* 3. getReminder by Id
* 4. Delete All Reminders
* 5. deleteReminder by Id
*/
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {


    private lateinit var localRepo: RemindersLocalRepository
    private lateinit var database: RemindersDatabase

    private val item1 = ReminderDTO("Reminder1", "Description1", "Location1", 1.0, 1.0,"1")
    private val item2 = ReminderDTO("Reminder2", "Description2", "location2", 2.0, 2.0, "2")
    private val item3 = ReminderDTO("Reminder3", "Description3", "location3", 3.0, 3.0, "3")
    // use Architecture Components to concurrently carry out each job.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initial() {
        // testing with an in-memory database because it won't survive stopping the process
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()

        localRepo = RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)
    }

    @After
    fun cleanUp() {
        database.close()
    }
    // This function allows us to save a item(reminder) and subsequently retrieve it using its ID.
    @Test
    fun saveReminderAndRetrievesReminderById() = runBlocking {
        // GIVEN - A fresh item(reminder) was entered into the database.
        localRepo.saveReminder(item1)
        // WHEN - item(reminder) retrieved by ID.
        val res = localRepo.getReminder(item1.id)
        // THEN - item(reminder) returned.
        res as Result.Success
        assertThat(res.data.title, `is`(item1.title))
        assertThat(res.data.description, `is`(item1.description))
        assertThat(res.data.location, `is`(item1.location))
        assertThat(res.data.latitude, `is`(item1.latitude))
        assertThat(res.data.longitude, `is`(item1.longitude))
        assertThat(res.data.id, `is`(item1.id))
    }
    // In this function, we save all of the items(reminders) listed above and then get them all once again.
    @Test
    fun saveRemindersAndRetrievesAllReminders() = runBlocking {
        // GIVEN - A new items(reminders) saved in the database.
        localRepo.saveReminder(item1)
        localRepo.saveReminder(item2)
        localRepo.saveReminder(item3)
        // WHEN  - We retrieve all items(reminders)
        val res = localRepo.getReminders()
        // THEN - Correct number of items(reminders) returned which is 3.
        res as Result.Success
        assertThat(res.data.size, `is`(3))
    }
    // In this function, all items(reminders) are saved before being deleted one by one by Id.
    @Test
    fun saveRemindersAndDeletesOneReminderById() = runBlocking {
        // GIVEN - A new items(reminders) saved in the database.
        localRepo.saveReminder(item1)
        localRepo.saveReminder(item2)
        localRepo.saveReminder(item3)
        // WHEN - Delete one (reminder) by Id and retrieve all items(reminders)
        localRepo.delete(item1.id)
        val res = localRepo.getReminders()
        //THEN - expect the size to be just 2 items(reminders) left.
        res as Result.Success
        assertThat(res.data.size, `is`(2))
        assertThat(res.data[0].location, `is`(item2.location))
    }
    // This function stores all items(reminders), after which they are all deleted.
    @Test
    fun saveRemindersAndDeletesAllReminders() = runBlocking {
        // GIVEN - A new items(reminders) saved in the database.
        localRepo.saveReminder(item1)
        localRepo.saveReminder(item2)
        localRepo.saveReminder(item3)
        // WHEN - Delete all items(Reminders) and try to retrieve all items(reminders)
        localRepo.deleteAllReminders()

        val res = localRepo.getReminders()
        // THEN - expect we retrieve 0 items(reminders) cause we deleted all previously.
        res as Result.Success
        assertThat(res.data.size, `is`(0))
    }
    // If there are any items(reminders), we remove them all in this method and attempt to obtain a reminder using an invalid id.
    @Test
    fun getReminderAndReturnsError() = runBlocking {
        //GIVEN - Empty Database
        localRepo.deleteAllReminders()
        //WHEN - try to retrieve item(reminder) by id which does not exist
        val res = localRepo.getReminder(item1.id) as Result.Error
        //THEN - We get an Result.Error message
        assertThat(res.message, `is`("Reminder not found!"))
    }

}