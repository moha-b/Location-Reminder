package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.getOrAwaitValue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.hamcrest.CoreMatchers.nullValue

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    private lateinit var saveReminder: SaveReminderViewModel
    private lateinit var data: FakeDataSource

    private val item1 = ReminderDataItem("Reminder1", "Description1", "Location1", 1.0, 1.0,"1")
    private val item2 = ReminderDataItem("", "Description2", "location2", 2.0, 2.0, "2")
    private val item3 = ReminderDataItem("Reminder3", "Description3", "", 3.0, 3.0, "3")

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var coroutineRule = MainCoroutineRule()

    @Before
    fun setUpViewModel(){ stopKoin()
        data = FakeDataSource()
        saveReminder = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), data)
    }

    @Test
    fun onClearsReminderLiveData(){

        saveReminder.reminderTitle.value = item1.title
        saveReminder.reminderDescription.value = item1.description
        saveReminder.reminderSelectedLocationStr.value = item1.location
        saveReminder.latitude.value = item1.latitude
        saveReminder.longitude.value = item1.longitude
        saveReminder.reminderId.value = item1.id

        saveReminder.onClear()

        assertThat(saveReminder.reminderTitle.getOrAwaitValue(), `is` (nullValue()))
        assertThat(saveReminder.reminderDescription.getOrAwaitValue(), `is`(nullValue()))
        assertThat(saveReminder.reminderSelectedLocationStr.getOrAwaitValue(), `is`(nullValue()))
        assertThat(saveReminder.latitude.getOrAwaitValue(), `is`(nullValue()))
        assertThat(saveReminder.longitude.getOrAwaitValue(), `is`(nullValue()))
        assertThat(saveReminder.reminderId.getOrAwaitValue(), `is`(nullValue()))
    }

    @Test
    fun editReminderSetsLiveDataOfReminderToBeEdited(){

        saveReminder.editReminder(item1)

        assertThat(saveReminder.reminderTitle.getOrAwaitValue(), `is` (item1.title))
        assertThat(saveReminder.reminderDescription.getOrAwaitValue(), `is`(item1.description))
        assertThat(saveReminder.reminderSelectedLocationStr.getOrAwaitValue(), `is`(item1.location))
        assertThat(saveReminder.latitude.getOrAwaitValue(), `is`(item1.latitude))
        assertThat(saveReminder.longitude.getOrAwaitValue(), `is`(item1.longitude))
        assertThat(saveReminder.reminderId.getOrAwaitValue(), `is`(item1.id))
    }

    @Test
    fun saveReminderAndAddsReminderToDataSource() = coroutineRule.runBlockingTest{

        saveReminder.saveReminder(item1)
        val checkReminder = data.getReminder("1") as Result.Success

        assertThat(checkReminder.data.title, `is` (item1.title))
        assertThat(checkReminder.data.description, `is` (item1.description))
        assertThat(checkReminder.data.location, `is` (item1.location))
        assertThat(checkReminder.data.latitude, `is` (item1.latitude))
        assertThat(checkReminder.data.longitude, `is` (item1.longitude))
        assertThat(checkReminder.data.id, `is` (item1.id))
    }

    @Test
    fun saveReminderAndCheckLoading()= coroutineRule.runBlockingTest{

        coroutineRule.pauseDispatcher()

        saveReminder.saveReminder(item1)

        assertThat(saveReminder.showLoading.getOrAwaitValue(), `is`(true))

        coroutineRule.resumeDispatcher()

        assertThat(saveReminder.showLoading.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun validateData_missingTitle_showSnackAndReturnFalse(){

        val valid = saveReminder.validateEnteredData(item2)

        assertThat(saveReminder.showSnackBarInt.getOrAwaitValue(), `is` (R.string.err_enter_title))
        assertThat(valid, `is` (false))
    }

    @Test
    fun validateData_missingLocation_showSnackAndReturnFalse(){

        val valid = saveReminder.validateEnteredData(item3)

        assertThat(saveReminder.showSnackBarInt.getOrAwaitValue(), `is` (R.string.err_select_location))
        assertThat(valid, `is` (false))
    }




}