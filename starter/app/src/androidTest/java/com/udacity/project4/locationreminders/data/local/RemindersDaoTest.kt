package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.*

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

//    TODO: Add testing implementation to the RemindersDao.kt
    private lateinit var database: RemindersDatabase

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDb(){
        // Using an in-memory database so that the information stored here disappears when the
        // process is killed.
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun insertReminderAndGetById() = runBlockingTest{
        //Given insert a reminder
        val oldReminder = ReminderDTO("Test" ,
            "Test" ,
            "Test" ,
            20.0 ,
            30.0)

        database.reminderDao().saveReminder(oldReminder)

        // WHEN - Get the reminder by id from the database.
        val newReminder = database.reminderDao().getReminderById(oldReminder.id)

        // THEN - The loaded data contains the expected values.
        Assert.assertThat<ReminderDTO>(newReminder as ReminderDTO, notNullValue())
        assertThat(newReminder.id , `is`(oldReminder.id))
        assertThat(newReminder.title , `is`(oldReminder.title))
        assertThat(newReminder.description , `is`(oldReminder.description))
        assertThat(newReminder.location , `is`(oldReminder.location))
        assertThat(newReminder.latitude , `is`(oldReminder.latitude))
        assertThat(newReminder.longitude , `is`(oldReminder.longitude))
    }

    @Test
    fun updateReminderAndGetById() = runBlockingTest {
        // 1. Insert a reminder into the DAO.
        val oldReminder = ReminderDTO("Test" ,
            "Test" ,
            "Test" ,
            20.0 ,
            30.0)
        database.reminderDao().saveReminder(oldReminder)

        // 2. Update the reminder by creating a new reminder with the same ID but different attributes.

        val newReminder = ReminderDTO("New Test" ,
            "New Test" ,
            "New Test" ,
            50.0 ,
            60.0,
            oldReminder.id)

        //Added and update fun to the dao to make sure the rest is working fine as the lessons
        database.reminderDao().updateReminder(newReminder)

        // 3. Check that when you get the reminder by its ID, it has the updated values.
        val loaded = database.reminderDao().getReminderById(oldReminder.id)
        assertThat(loaded?.id , `is`(oldReminder.id))
        assertThat(loaded?.title , `is`("New Test"))
        assertThat(loaded?.description , `is`("New Test"))
        assertThat(loaded?.location , `is`("New Test"))
        assertThat(loaded?.latitude , `is`(50.0))
        assertThat(loaded?.longitude , `is`(60.0))
    }

}