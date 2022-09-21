package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.After

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {
    //  under test
    private lateinit var remindersList: RemindersListViewModel
    // Inject a fake data source into the viewModel.
    private lateinit var data: FakeDataSource

    private val item1 = ReminderDTO("Reminder1", "Description1", "Location1", 1.0, 1.0,"1")
    private val item2 = ReminderDTO("Reminder2", "Description2", "location2", 2.0, 2.0, "2")
    private val item3 = ReminderDTO("Reminder3", "Description3", "location3", 3.0, 3.0, "3")
    // Uses Architecture Components to execute each job in a synchronous manner.    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()
    //For unit testing, set the primary coroutine dispatcher.
    @ExperimentalCoroutinesApi
    @get:Rule
    var coroutineRule = MainCoroutineRule()

    @Before
    fun model(){ stopKoin()
        data = FakeDataSource()
        remindersList = RemindersListViewModel(ApplicationProvider.getApplicationContext(), data)
    }

    @After
    fun clearData() = runBlockingTest{
        data.deleteAllReminders()
    }

    /*
    * This function tries to load the reminders from our View Model after testing removing all of the reminders.
    * Two variables are being tested here :
        1. show No Data [invalidateShowNoDataShowNoDataIsTrue]
        2. reminder list [loadRemindersLoadsThreeReminders]
    * */
    @Test
    fun invalidateShowNoDataShowNoDataIsTrue()= coroutineRule.runBlockingTest{
        // Empty DB
        data.deleteAllReminders()
        // Try to load Reminders
        remindersList.loadReminders()
        // expect that our reminder list Live data size is 0 and show no data is true
        assertThat(remindersList.remindersList.getOrAwaitValue().size, `is` (0))
        assertThat(remindersList.showNoData.getOrAwaitValue(), `is` (true))
    }
    // We test retrieving the three reminders we're placing in this method.
    @Test
    fun loadRemindersLoadsThreeReminders()= coroutineRule.runBlockingTest {
        //  just 3 Reminders in the DB
        data.deleteAllReminders()

        data.saveReminder(item1)
        data.saveReminder(item2)
        data.saveReminder(item3)
        // try to load Reminders
        remindersList.loadReminders()
        // expect to have only 3 reminders in remindersList and showNoData is false cause we have data
        assertThat(remindersList.remindersList.getOrAwaitValue().size, `is` (3))
        assertThat(remindersList.showNoData.getOrAwaitValue(), `is` (false))

    }
    // Here, we are testing checkLoading in this test.
    @Test
    fun loadRemindersCheckLoading()= coroutineRule.runBlockingTest{
        // Stop dispatcher so we may inspect initial values.
        coroutineRule.pauseDispatcher()
        //  Only 1 Reminder
        data.deleteAllReminders()
        data.saveReminder(item1)
        // load Reminders
        remindersList.loadReminders()
        // The loading indicator is displayed, then it is hidden after we are done.
        assertThat(remindersList.showLoading.getOrAwaitValue(), `is`(true))
        // Execute pending coroutines actions
        coroutineRule.resumeDispatcher()
        // Then loading indicator is hidden
        assertThat(remindersList.showLoading.getOrAwaitValue(), `is`(true))
    }
    // testing showing an Error
    @Test
    fun loadRemindersShouldReturnError()= coroutineRule.runBlockingTest{
        // give : set should return error to "true
        data.returnError(true)
        // when : we load Reminders
        remindersList.loadReminders()
        // then : We get showSnackBar in the view model giving us "not found"
        assertThat(remindersList.showSnackBar.getOrAwaitValue(), `is`("no Reminder found"))
    }

}