package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

/**
 * testing RemindersDao interface
 * Testing different ways of insertion The functions we will be testing on is:
 *    1. getReminders
 *    2. getReminderById
 *    3. saveReminder
 *    4. deleteAllReminders
 *    5. delete
 **/

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class RemindersDaoTest {
    // testing implementation to the RemindersDao.kt
    private lateinit var database: RemindersDatabase

    private val item1 = ReminderDTO("Reminder1", "Description1", "Location1", 1.0, 1.0,"1")
    private val item2 = ReminderDTO("Reminder2", "Description2", "location2", 2.0, 2.0, "2")
    private val item3 = ReminderDTO("Reminder3", "Description3", "location3", 3.0, 3.0, "3")

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDb() {
        // using an in-memory database because the information stored here disappears when the process is destroy
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() = database.close()

    // This method insert the Items(Reminders) we created above and trying to retrieve them again
    // * as the expected number of Items(reminders) is 3
    @Test
    fun insertAll() = runBlockingTest {
        // Insert a task.
        database.reminderDao().saveReminder(item1)
        database.reminderDao().saveReminder(item2)
        database.reminderDao().saveReminder(item3)
        // call getReminders() we expected to get all Items(reminders) we have.
        val loaded = database.reminderDao().getReminders()
        // The loaded data has the correct number of Items(reminders) which is 3 reminders we just inserted
        assertThat(loaded.size, `is`(3))
    }


    @Test
    fun insertReminderAndGetIt() = runBlockingTest {

        database.reminderDao().saveReminder(item1)

        val load = database.reminderDao().getReminderById(item1.id)

        assertThat<ReminderDTO>(load as ReminderDTO, notNullValue())
        assertThat(load.title, `is`(item1.title))
        assertThat(load.description, `is`(item1.description))
        assertThat(load.location, `is`(item1.location))
        assertThat(load.latitude, `is`(item1.latitude))
        assertThat(load.longitude, `is`(item1.longitude))
        assertThat(load.id, `is`(item1.id))
    }

    @Test
    fun insertAllAndDeleteAll()= runBlockingTest{

        database.reminderDao().saveReminder(item1)
        database.reminderDao().saveReminder(item2)
        database.reminderDao().saveReminder(item3)
        database.reminderDao().deleteAllReminders()

        val load = database.reminderDao().getReminders()
        assertThat(load.size, `is`(0))
    }

    @Test
    fun insertRemindersAndDeleteReminderById()= runBlockingTest{

        database.reminderDao().saveReminder(item1)
        database.reminderDao().saveReminder(item2)
        database.reminderDao().saveReminder(item3)

        database.reminderDao().delete(item1.id)

        val load = database.reminderDao().getReminders()
        assertThat(load.size, `is`(2))
        assertThat(load[0].id, `is` (item2.id))
    }

    @Test
    fun returnsError()= runBlockingTest{

        database.reminderDao().saveReminder(item1)
        database.reminderDao().saveReminder(item2)
        database.reminderDao().saveReminder(item3)

        database.reminderDao().delete(item1.id)

        val load = database.reminderDao().getReminders()
        assertThat(load.size, `is`(2))
        assertThat(load[0].id, `is` (item2.id))
    }
}