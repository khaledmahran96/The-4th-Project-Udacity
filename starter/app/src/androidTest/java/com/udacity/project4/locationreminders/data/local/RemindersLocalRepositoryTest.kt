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
import org.hamcrest.CoreMatchers.*
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

//    TODO: Add testing implementation to the RemindersLocalRepository.kt

    private lateinit var database: RemindersDatabase
    private lateinit var reminderRepository: RemindersLocalRepository

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup(){
        // Using an in-memory database so that the information stored here disappears when the
        // process is killed.
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()

        reminderRepository =
            RemindersLocalRepository(
                database.reminderDao(),
                Dispatchers.Main)
    }

    @After
    fun cleanUp() {
        database.close()
    }

    // runBlocking is used here because of https://github.com/Kotlin/kotlinx.coroutines/issues/1204
    @Test
    fun saveReminder_retrievesReminder() = runBlocking {
        // GIVEN - A new reminder saved in the database.
        val newReminder = ReminderDTO("Test" ,
            "Test" ,
            "Test" ,
            20.0 ,
            30.0)
        reminderRepository.saveReminder(newReminder)

        // WHEN  - Reminder retrieved by ID.
        val result = reminderRepository.getReminder(newReminder.id)

        // THEN - Same reminder is returned.
        assertThat(result is Result.Success , `is`(true))
        result as Result.Success

        assertThat(result.data.title , `is`(newReminder.title))
        assertThat(result.data.description , `is`(newReminder.description))
        assertThat(result.data.location , `is`(newReminder.location))
        assertThat(result.data.longitude , `is`(newReminder.longitude))
        assertThat(result.data.latitude , `is`(newReminder.latitude))
    }

    @Test
    fun deleteReminder_retrieveReminderById() = runBlocking {
        // GIVEN - A new reminder saved in the database.
        val reminder = ReminderDTO("Test" ,
            "Test" ,
            "Test" ,
            20.0 ,
            30.0)
        reminderRepository.saveReminder(reminder)

        // WHEN  - Reminder is deleted.
        reminderRepository.deleteAllReminders()
        val result = reminderRepository.getReminder(reminder.id)

        // THEN - Error should be shown and result cannot be empty.
        assertThat(result is Result.Error , `is`(true))
        result as Result.Error
        assertThat(result.message , `is`("Reminder not found!"))
    }

}