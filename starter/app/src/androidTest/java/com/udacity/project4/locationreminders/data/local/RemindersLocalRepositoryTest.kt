package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    private lateinit var localRepository: RemindersLocalRepository
    private lateinit var database: RemindersDatabase

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()

        localRepository =
            RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)
    }

    @After
    fun cleanUp() {
        database.close()
    }

    @Test
    fun getReminder_nonExistId_returnError() = runTest {
        //WHEN - Get the reminder by id from the local repo.

        val result = localRepository.getReminder("-1")

        //THEN - Verify that the result is an error
        assertThat(result, instanceOf(Result.Error::class.java))
    }

    @Test
    fun saveReminder_GetReminder_VerifyCorrectData() = runTest {
        // GIVEN - Insert a reminder.
        val reminder = ReminderDTO(
            "Reminder write a report",
            "decription",
            "somewhere",
            1.234567,
            3.45678
        )
        localRepository.saveReminder(reminder)

        // WHEN - Get the reminder by id from the local repo.
        val result = localRepository.getReminder(reminder.id)

        //THEN - Verify that the result is a success
        assertThat(result, instanceOf(Result.Success::class.java))
        result as Result.Success
        assertThat(result.data.id, `is`(reminder.id))
        assertThat(result.data.title, `is`(reminder.title))
        assertThat(result.data.description, `is`(reminder.description))
        assertThat(result.data.location, `is`(reminder.location))
        assertThat(result.data.latitude, `is`(reminder.latitude))
        assertThat(result.data.longitude, `is`(reminder.longitude))
    }

    @Test
    fun deleteAllReminders_verifyEmpty() = runTest {
        // GIVEN - Insert a reminder.
        val reminder = ReminderDTO(
            "Reminder write a report",
            "decription",
            "somewhere",
            1.234567,
            3.45678
        )
        localRepository.saveReminder(reminder)

        // WHEN - Delete all reminders
        localRepository.deleteAllReminders()

        //THEN - Verify that the result is a success
        val result = localRepository.getReminders()
        assertThat(result, instanceOf(Result.Success::class.java))
        result as Result.Success
        assertThat(result.data.isEmpty(), `is`(true))
    }

    @Test
    fun getReminders_verifyEmpty() = runTest {
        // GIVEN - delete all reminders
        localRepository.deleteAllReminders()

        // WHEN - Get all reminders
        val result = localRepository.getReminders()

        //THEN - Verify that the result is a success
        assertThat(result, instanceOf(Result.Success::class.java))
        result as Result.Success
        assertThat(result.data.isEmpty(), `is`(true))
    }

    @Test
    fun getReminders_verifyNotEmpty() = runTest {
        // GIVEN - Insert a reminder.
        val reminder = ReminderDTO(
            "Reminder write a report",
            "decription",
            "somewhere",
            1.234567,
            3.45678
        )
        localRepository.saveReminder(reminder)

        // WHEN - Get all reminders
        val result = localRepository.getReminders()

        //THEN - Verify that the result is a success
        assertThat(result, instanceOf(Result.Success::class.java))
        result as Result.Success
        assertThat(result.data.isEmpty(), `is`(false))
    }
}