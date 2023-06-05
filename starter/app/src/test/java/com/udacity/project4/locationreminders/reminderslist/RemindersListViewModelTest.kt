package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var fakeDataSource: FakeDataSource

    private lateinit var viewModel: RemindersListViewModel

    @Before
    fun setUp() {
        fakeDataSource = FakeDataSource()
        viewModel =
            RemindersListViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun loadReminders_success() = runTest {
        // GIVEN - a fresh viewModel

        // WHEN - load reminders
        viewModel.loadReminders()

        // THEN - check that the data is loaded
        MatcherAssert.assertThat(viewModel.showLoading.getOrAwaitValue(), CoreMatchers.`is`(false))
        MatcherAssert.assertThat(
            viewModel.remindersList.getOrAwaitValue().isEmpty(),
            CoreMatchers.`is`(false)
        )
    }

    @Test
    fun loadReminders_error() = runTest {
        // GIVEN - a fresh viewModel
        fakeDataSource.setReturnError()

        // WHEN - load reminders
        viewModel.loadReminders()

        // THEN - check that the data is loaded
        MatcherAssert.assertThat(viewModel.showLoading.getOrAwaitValue(), CoreMatchers.`is`(false))
        MatcherAssert.assertThat(
            viewModel.showSnackBar.getOrAwaitValue(),
            CoreMatchers.`is`("Test exception")
        )
    }

    @Test
    fun loadReminders_noData() = runTest {
        // GIVEN - a fresh viewModel with no data
        fakeDataSource.deleteAllReminders()

        // WHEN - load reminders
        viewModel.loadReminders()

        // THEN - check that the data is loaded
        MatcherAssert.assertThat(viewModel.showLoading.getOrAwaitValue(), CoreMatchers.`is`(false))
        MatcherAssert.assertThat(
            viewModel.showNoData.getOrAwaitValue(),
            CoreMatchers.`is`(true)
        )
    }

}