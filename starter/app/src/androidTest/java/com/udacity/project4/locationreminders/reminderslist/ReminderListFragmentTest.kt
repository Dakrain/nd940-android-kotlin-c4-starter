package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.monitorFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.not
import org.koin.androidx.viewmodel.dsl.viewModel

import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest {

    private lateinit var repository: ReminderDataSource


    @Before
    fun setupKoin() {
        stopKoin()

        val appContext = getApplicationContext<Application>()
        val testModules = module {
            viewModel {
                RemindersListViewModel(
                    appContext, get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext, get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }


        startKoin { modules(testModules) }

        repository = GlobalContext.get().get()
        runBlocking {
            repository.deleteAllReminders()
        }
    }


    @Test
    fun clickAddReminderButton_navigatesToSaveReminderFragment() {
        // Set the NavController property on the fragment
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.requireView(), navController)
        }

        // Click the add reminder FAB
        onView(withId(R.id.addReminderFAB)).perform(click())

        // Verify that the navigation command is triggered with the correct destination
        verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder()
        )
    }


    @Test
    fun noReminders_verifyShowNoData() {
        // GIVEN - Reminder Items is empty

        // WHEN - On the ReminderList screen
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        // THEN - Verify that no data is shown
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
    }


    @Test
    fun remindersList_displayedOnUI() {
        // Create a list of sample reminders
        val reminder = ReminderDTO("1", "Title 1", "Description 1", 10.0, 20.0)

        runBlocking {
            repository.saveReminder(reminder)
        }

        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        // Verify that the "No Data" text is not displayed
        onView(withId(R.id.noDataTextView)).check(matches(not(isDisplayed())))
        // Verify that all the reminders are shown on the screen
        onView(withText(reminder.title)).check(matches(isDisplayed()))
        onView(withText(reminder.description)).check(matches(isDisplayed()))
        onView(withText(reminder.location)).check(matches(isDisplayed()))

    }

}