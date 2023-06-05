package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.android.get
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var fakeDataSource: FakeDataSource

    private lateinit var viewModel: SaveReminderViewModel

    private lateinit var app: Application

    @Before
    fun setUp() {
        fakeDataSource = FakeDataSource()

        viewModel =
            SaveReminderViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)
        app = ApplicationProvider.getApplicationContext()
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun saveReminder_Success() = runTest {
        // GIVEN - a fresh viewModel
        val item = ReminderDataItem(
            "title", "description", "location", 0.0, 0.0
        )
        // WHEN - save reminder
        viewModel.validateAndSaveReminder(item)

        // THEN - check that the data is save
        MatcherAssert.assertThat(
            viewModel.showToast.getOrAwaitValue(),
            CoreMatchers.`is`(app.getString(R.string.reminder_saved))
        )
        MatcherAssert.assertThat(
            viewModel.navigationCommand.getOrAwaitValue(),
            CoreMatchers.`is`(NavigationCommand.Back)
        )
    }

    @Test
    fun saveReminder_TitleIsEmpty() = runTest {
        // GIVEN - a fresh viewModel
        val item = ReminderDataItem(
            "", "description", "location", 0.0, 0.0
        )
        // WHEN - save reminder
        viewModel.validateAndSaveReminder(item)

        // THEN - check that the data is save
        MatcherAssert.assertThat(
            viewModel.showSnackBarInt.getOrAwaitValue(), CoreMatchers.`is`(R.string.err_enter_title)
        )
    }

    @Test
    fun saveReminder_LocationIsEmpty() = runTest {
        // GIVEN - a fresh viewModel
        val item = ReminderDataItem(
            "title", "description", "", 0.0, 0.0
        )
        // WHEN - save reminder
        viewModel.validateAndSaveReminder(item)

        // THEN - check that the data is save
        MatcherAssert.assertThat(
            viewModel.showSnackBarInt.getOrAwaitValue(),
            CoreMatchers.`is`(R.string.err_select_location)
        )
    }

    @Test
    fun saveReminder_TitleIsNull() = runTest {
        // GIVEN - a fresh viewModel
        val item = ReminderDataItem(
            null, "description", "location", 0.0, 0.0
        )
        // WHEN - save reminder
        viewModel.validateAndSaveReminder(item)

        // THEN - check that the data is save
        MatcherAssert.assertThat(
            viewModel.showSnackBarInt.getOrAwaitValue(), CoreMatchers.`is`(R.string.err_enter_title)
        )
    }

    @Test
    fun saveReminder_LocationIsNull() = runTest {
        // GIVEN - a fresh viewModel
        val item = ReminderDataItem(
            "title", "description", null, 0.0, 0.0
        )

        // WHEN - save reminder
        viewModel.validateAndSaveReminder(item)

        // THEN - check that the data is save
        MatcherAssert.assertThat(
            viewModel.showSnackBarInt.getOrAwaitValue(),
            CoreMatchers.`is`(R.string.err_select_location)
        )
    }

}