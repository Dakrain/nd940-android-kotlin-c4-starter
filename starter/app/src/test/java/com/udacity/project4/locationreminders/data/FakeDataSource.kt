package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {
    private var returnError = false

    private val items = arrayListOf(
        ReminderDTO(
            "Title 1",
            "Description 1",
            "Location 1",
            37.1,
            -122.2
        ),
        ReminderDTO(
            "Title 2",
            "Description 2",
            "Location 2",
            38.2,
            -100.3
        ),
        ReminderDTO(
            "title 3",
            "description 3",
            "location 3",
            35.1,
            -120.1
        )
    )

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (returnError) {
            return Result.Error("Test exception")
        }
        return Result.Success(items)
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        items.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        val reminder = items.find { it.id == id }
        return if (reminder != null) {
            Result.Success(reminder)
        } else {
            Result.Error("Reminder not found!")
        }
    }

    override suspend fun deleteAllReminders() {
        items.clear()
    }

    fun setReturnError() {
        returnError = true
    }
}