package com.udacity.project4

import android.app.Activity
import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.`is`
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.get
import org.hamcrest.core.IsNot.not
import org.koin.test.KoinTest


@RunWith(AndroidJUnit4::class)
@LargeTest

class RemindersActivityTest :
    KoinTest {

    private lateinit var repo: ReminderDataSource
    private lateinit var context: Application
    private val dataBindingResource = DataBindingIdlingResource()

    @get:Rule
    val activityRule = ActivityTestRule(RemindersActivity::class.java)

    private fun getActivity(activityScenario: ActivityScenario<RemindersActivity>): Activity? {
        var activity: Activity? = null
        activityScenario.onActivity {
            activity = it
        }
        return activity
    }

    @Before
    fun init() {
        stopKoin()
        context = getApplicationContext()
        val myModule = module {
            viewModel { RemindersListViewModel(context, get() as ReminderDataSource) }

            single { SaveReminderViewModel(context, get() as ReminderDataSource) }

            single { RemindersLocalRepository(get()) as ReminderDataSource }

            single { LocalDB.createRemindersDao(context) }
        }
        startKoin { modules(listOf(myModule)) }
        repo = get()

        runBlocking { repo.deleteAllReminders() }
    }

    @After
    fun unregisterResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingResource)
    }

    @Before
    fun registerResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingResource)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun showReminderToast() = runBlocking{
        val scenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingResource.monitorActivity(scenario)

        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.reminderTitle)).perform(typeText("TITLE1"), closeSoftKeyboard())
        onView(withId(R.id.reminderDescription)).perform(typeText("DESC1"), closeSoftKeyboard())
        onView(withId(R.id.selectLocation)).perform(click())
        onView(withId(com.google.android.material.R.id.snackbar_action)).perform(click())
        onView(withId(R.id.map)).perform(longClick())
        onView(withId(R.id.save)).perform(click())
        onView(withId(R.id.saveReminder)).perform(click())
        onView(withText(R.string.reminder_saved)).inRoot(withDecorView(not(`is`(getActivity(scenario)?.window?.decorView))))
            .check(matches(isDisplayed()))
        scenario.close()
    }

    @Test
    fun showSnackAndEnterTitle(){

        val scenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingResource.monitorActivity(scenario)

        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.saveReminder)).perform(click())
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.err_enter_title)))

        scenario.close()
    }

    @Test
    fun showSnackAndEnterLocation(){

        val scenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingResource.monitorActivity(scenario)

        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.reminderTitle)).perform(typeText("TITLE1"), closeSoftKeyboard())
        onView(withId(R.id.saveReminder)).perform(click())
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.err_select_location)))

        scenario.close()
    }
}
