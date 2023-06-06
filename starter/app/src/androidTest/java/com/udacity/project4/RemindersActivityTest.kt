package com.udacity.project4

import android.app.Application
import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get


@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application
    private lateinit var viewModel: SaveReminderViewModel
    private lateinit var decorView: View

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()
        viewModel = get()
        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    @After
    fun tearDown() {
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    @Test
    fun addNewReminder_verifyNewReminderInTheList() {
        val activity = ActivityScenario.launch(RemindersActivity::class.java)
        activity.onActivity {
            decorView = it.window.decorView
        }

        val title = "Something"
        val description = "Something description"
        // Verify no data is displayed
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))

        //Click FAB
        onView(withId(R.id.addReminderFAB)).perform(click())

        //Type data
        onView(withId(R.id.reminderTitle)).perform(ViewActions.typeText(title))
        onView(withId(R.id.reminderDescription)).perform(ViewActions.typeText(description))
        Espresso.closeSoftKeyboard()

        // Click save reminder
        onView(withId(R.id.saveReminder)).perform(click())

        //Check toast show correctly
        onView(withText(R.string.reminder_saved))
            .inRoot(withDecorView(Matchers.not(decorView)))
            .check(matches(isDisplayed()))

        //Verify item on list
        onView(withId(R.id.reminderssRecyclerView)).check(matches(isDisplayed()))

        //Click reminder item
        onView(withText(title)).perform(click())

        //Verify data
        onView(withText(title)).check(matches(isDisplayed()))
        onView(withText(description)).check(matches(isDisplayed()))

        activity.close()
    }

    @Test
    fun saveReminder_EmptyTitle_verifyShowErrorSnackBar() {
        val activity = ActivityScenario.launch(RemindersActivity::class.java)
        activity.onActivity {
            decorView = it.window.decorView
        }

        val description = "Something description"
        // Verify no data is displayed
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))

        //Click FAB
        onView(withId(R.id.addReminderFAB)).perform(click())

        //Type data
        onView(withId(R.id.reminderDescription)).perform(ViewActions.typeText(description))
        Espresso.closeSoftKeyboard()

        // Select location
        onView(withId(R.id.selectLocation)).perform(click())

        //Delay a moment for the google map loaded
        Thread.sleep(2000)

        // Click any position in the map
        onView(withId(R.id.map)).perform(click())

        //Delay a moment for the google map to get address
        Thread.sleep(2000)

        // Click save
        onView(withId(R.id.btnSaveLocation)).perform(click())

        // Click save reminder
        onView(withId(R.id.saveReminder)).perform(click())


        //Check snackbar show correctly
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.err_enter_title)))


        activity.close()
    }

    @Test
    fun saveReminder_EmptyLocation_verifyShowErrorSnackBar() {
        val activity = ActivityScenario.launch(RemindersActivity::class.java)
        activity.onActivity {
            decorView = it.window.decorView
        }
        val title = "Something"
        val description = "Something description"
        // Verify no data is displayed
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))

        //Click FAB
        onView(withId(R.id.addReminderFAB)).perform(click())

        //Type data
        onView(withId(R.id.reminderDescription)).perform(ViewActions.typeText(description))
        onView(withId(R.id.reminderTitle)).perform(ViewActions.typeText(title))
        Espresso.closeSoftKeyboard()

        // Click save reminder
        onView(withId(R.id.saveReminder)).perform(click())


        //Check snackbar show correctly
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.err_select_location)))

        activity.close()
    }

}
