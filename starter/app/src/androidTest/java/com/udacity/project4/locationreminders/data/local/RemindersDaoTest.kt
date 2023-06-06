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
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase

    @Before
    fun initDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun saveReminder_getReminderById() = runTest {
        // Create a sample reminder
        val reminder = ReminderDTO("1", "Title", "Description", 10.0, 20.0)

        // Save the reminder to the database
        database.reminderDao().saveReminder(reminder)

        // Get the reminder by its ID
        val loadedReminder = database.reminderDao().getReminderById(reminder.id)

        // Verify that the loaded reminder matches the original reminder
        assertThat(loadedReminder, notNullValue())
        assertThat(loadedReminder?.id, `is`(reminder.id))
        assertThat(loadedReminder?.title, `is`(reminder.title))
        assertThat(loadedReminder?.description, `is`(reminder.description))
        assertThat(loadedReminder?.latitude, `is`(reminder.latitude))
        assertThat(loadedReminder?.longitude, `is`(reminder.longitude))
    }

    @Test
    fun getReminders_emptyList() = runTest {
        // Get all reminders from the empty database
        val reminders = database.reminderDao().getReminders()

        // Verify that the list is empty
        assertThat(reminders.isEmpty(), `is`(true))
    }

    @Test
    fun getReminders_nonEmptyList() = runTest {
        // Create sample reminders
        val reminder1 = ReminderDTO("1", "Title 1", "Description 1", 10.0, 20.0)
        val reminder2 = ReminderDTO("2", "Title 2", "Description 2", 30.0, 40.0)
        val reminder3 = ReminderDTO("3", "Title 3", "Description 3", 50.0, 60.0)

        // Save the reminders to the database
        database.reminderDao().saveReminder(reminder1)
        database.reminderDao().saveReminder(reminder2)
        database.reminderDao().saveReminder(reminder3)

        // Get all reminders from the database
        val reminders = database.reminderDao().getReminders()

        // Verify that the list contains all the saved reminders
        assertThat(reminders.size, `is`(3))
        assertThat(reminders, `is`(listOf(reminder1, reminder2, reminder3)))
    }

    @Test
    fun deleteAllReminders_emptyList() = runTest {
        // Delete all reminders from the empty database
        database.reminderDao().deleteAllReminders()

        // Get all reminders from the database
        val reminders = database.reminderDao().getReminders()

        // Verify that the list is empty
        assertThat(reminders.isEmpty(), `is`(true))
    }

    @Test
    fun deleteAllReminders_nonEmptyList() = runTest {
        // Create sample reminders
        val reminder1 = ReminderDTO("1", "Title 1", "Description 1", 10.0, 20.0)
        val reminder2 = ReminderDTO("2", "Title 2", "Description 2", 30.0, 40.0)
        val reminder3 = ReminderDTO("3", "Title 3", "Description 3", 50.0, 60.0)

        // Save the reminders to the database
        database.reminderDao().saveReminder(reminder1)
        database.reminderDao().saveReminder(reminder2)
        database.reminderDao().saveReminder(reminder3)

        // Delete all reminders from the database
        database.reminderDao().deleteAllReminders()

        // Get all reminders from the database
        val reminders = database.reminderDao().getReminders()

        // Verify that the list is empty
        assertThat(reminders.isEmpty(), `is`(true)  )
    }

}