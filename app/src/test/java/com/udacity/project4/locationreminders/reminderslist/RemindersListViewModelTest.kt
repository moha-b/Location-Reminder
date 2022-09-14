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
    
    private lateinit var remindersList: RemindersListViewModel

    private lateinit var data: FakeDataSource

    private val item1 = ReminderDTO("Reminder1", "Description1", "Location1", 1.0, 1.0,"1")
    private val item2 = ReminderDTO("Reminder2", "Description2", "location2", 2.0, 2.0, "2")
    private val item3 = ReminderDTO("Reminder3", "Description3", "location3", 3.0, 3.0, "3")

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

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

    @Test
    fun invalidateShowNoDataShowNoDataIsTrue()= coroutineRule.runBlockingTest{

        data.deleteAllReminders()
        remindersList.loadReminders()

        assertThat(remindersList.remindersList.getOrAwaitValue().size, `is` (0))
        assertThat(remindersList.showNoData.getOrAwaitValue(), `is` (true))
    }

    @Test
    fun loadRemindersLoadsThreeReminders()= coroutineRule.runBlockingTest {

        data.deleteAllReminders()

        data.saveReminder(item1)
        data.saveReminder(item2)
        data.saveReminder(item3)

        remindersList.loadReminders()

        assertThat(remindersList.remindersList.getOrAwaitValue().size, `is` (3))
        assertThat(remindersList.showNoData.getOrAwaitValue(), `is` (false))

    }

    @Test
    fun loadRemindersCheckLoading()= coroutineRule.runBlockingTest{
        coroutineRule.pauseDispatcher()

        data.deleteAllReminders()
        data.saveReminder(item1)

        remindersList.loadReminders()

        assertThat(remindersList.showLoading.getOrAwaitValue(), `is`(true))

        coroutineRule.resumeDispatcher()

        assertThat(remindersList.showLoading.getOrAwaitValue(), `is`(false))

    }

    @Test
    fun loadRemindersShouldReturnError()= coroutineRule.runBlockingTest{
        // give : set should return error to "true
        data.shouldReturnError(true)
        // when : we load Reminders
        remindersList.loadReminders()
        // then : we receive "no Reminders detected" from showSnackBar in the view model.
        assertThat(remindersList.showSnackBar.getOrAwaitValue(), `is`("no Reminders found"))
    }

}