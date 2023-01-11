package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(var reminders: MutableList<ReminderDTO>? = mutableListOf()) : ReminderDataSource {

//    TODO: Create a fake data source to act as a double to the real data source

    private var returnError =false

    fun shouldReturnError(value: Boolean){
        returnError = value
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if(returnError) return Result.Error("Reminders not found")
        else {
            reminders?.let{return Result.Success(ArrayList(it))}
            return Result.Error("Reminder not found")
        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        val reminder = reminders?.find { reminderDTO ->
            reminderDTO.id == id
        }

        return when {
            returnError -> {
                Result.Error("Reminder not found!")
            }

            reminder != null -> {
                Result.Success(reminder)
            }
            else -> {
                Result.Error("Reminder not found!")
            }
        }
    }

    override suspend fun deleteAllReminders() {
        reminders = mutableListOf()
    }


}