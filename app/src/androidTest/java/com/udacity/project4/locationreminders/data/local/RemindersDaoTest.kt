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
    /*
     * We built insertReminderAndGetIt to test whether we could only obtain a single reminder using specific settings.
     * Here, we put one reminder, designated as item1 above,
     * and then attempt to retrieve it using its Id.
     */
    @Test
    fun insertReminderAndGetIt() = runBlockingTest {
        // Insert a task.
        database.reminderDao().saveReminder(item1)
        //  Get the task by id from the database.
        val load = database.reminderDao().getReminderById(item1.id)
        // The loaded data contains the expected values.
        assertThat<ReminderDTO>(load as ReminderDTO, notNullValue())
        assertThat(load.title, `is`(item1.title))
        assertThat(load.description, `is`(item1.description))
        assertThat(load.location, `is`(item1.location))
        assertThat(load.latitude, `is`(item1.latitude))
        assertThat(load.longitude, `is`(item1.longitude))
        assertThat(load.id, `is`(item1.id))
    }
    // In this function, we're trying to see whether we can remove every reminder after putting it.
    @Test
    fun insertAllAndDeleteAll()= runBlockingTest{
        // Insert a task.
        database.reminderDao().saveReminder(item1)
        database.reminderDao().saveReminder(item2)
        database.reminderDao().saveReminder(item3)
        // Deleting all the items we have
        database.reminderDao().deleteAllReminders()
        // try to get the reminders we expecting the size to be zero
        val load = database.reminderDao().getReminders()
        // The loaded data size 'is' 0.
        assertThat(load.size, `is`(0))
    }
    // The purpose of this function is to test deletion by Id.
    @Test
    fun insertRemindersAndDeleteReminderById()= runBlockingTest{
        // Insert all items
        database.reminderDao().saveReminder(item1)
        database.reminderDao().saveReminder(item2)
        database.reminderDao().saveReminder(item3)
        // Delete items by Id
        database.reminderDao().delete(item1.id)
        // There should be only 2 items in the DB since we deleted one
        val load = database.reminderDao().getReminders()
        assertThat(load.size, `is`(2))
        assertThat(load[0].id, `is` (item2.id))
    }
    /*
     * This method is designed to return a Null Value error :
       * 1- We added three items
       * 2- We removed the first item
       * 4- The item at position 0 is now item2, not item1
       * 3- We tried retrieving the deleted item by Id, but we expected to get null since we deleted it.
   */
    @Test
    fun returnsError()= runBlockingTest{
        // Insert all items
        database.reminderDao().saveReminder(item1)
        database.reminderDao().saveReminder(item2)
        database.reminderDao().saveReminder(item3)
        // Delete items by Id & Try to retrieve the same items we deleted by Id
        database.reminderDao().delete(item1.id)
        //  The value we should receive should be null Value
        val load = database.reminderDao().getReminders()
        assertThat(load.size, `is`(2))
        assertThat(load[0].id, `is` (item2.id))
    }
}