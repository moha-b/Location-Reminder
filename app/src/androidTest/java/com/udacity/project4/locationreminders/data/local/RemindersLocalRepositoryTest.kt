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

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
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

    @Test
    fun saveReminderAndRetrievesReminderById() = runBlocking {

        localRepo.saveReminder(item1)
        val res = localRepo.getReminder(item1.id)

        res as Result.Success
        assertThat(res.data.title, `is`(item1.title))
        assertThat(res.data.description, `is`(item1.description))
        assertThat(res.data.location, `is`(item1.location))
        assertThat(res.data.latitude, `is`(item1.latitude))
        assertThat(res.data.longitude, `is`(item1.longitude))
        assertThat(res.data.id, `is`(item1.id))
    }

    @Test
    fun saveRemindersAndRetrievesAllReminders() = runBlocking {

        localRepo.saveReminder(item1)
        localRepo.saveReminder(item2)
        localRepo.saveReminder(item3)

        val res = localRepo.getReminders()
        res as Result.Success
        assertThat(res.data.size, `is`(3))
    }

    @Test
    fun saveRemindersAndDeletesOneReminderById() = runBlocking {

        localRepo.saveReminder(item1)
        localRepo.saveReminder(item2)
        localRepo.saveReminder(item3)
        localRepo.delete(item1.id)

        val res = localRepo.getReminders()
        res as Result.Success
        assertThat(res.data.size, `is`(2))
        assertThat(res.data[0].location, `is`(item2.location))
    }

    @Test
    fun saveRemindersAndDeletesAllReminders() = runBlocking {

        localRepo.saveReminder(item1)
        localRepo.saveReminder(item2)
        localRepo.saveReminder(item3)
        localRepo.deleteAllReminders()

        val res = localRepo.getReminders()
        res as Result.Success
        assertThat(res.data.size, `is`(0))
    }

    @Test
    fun getReminderAndReturnsError() = runBlocking {

        localRepo.deleteAllReminders()
        val res = localRepo.getReminder(item1.id) as Result.Error
        assertThat(res.message, `is`("Reminder not found!"))
    }

}