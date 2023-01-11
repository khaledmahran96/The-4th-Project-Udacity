package com.udacity.project4.locationreminders.savereminder

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.Matchers
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class SaveReminderViewModelTest {
    //TODO: provide testing to the SaveReminderView and its live data objects

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var reminderRepository : FakeDataSource
    private lateinit var saveRemindersViewModel: SaveReminderViewModel

    @Before
    fun setupViewModel(){
        stopKoin()
        FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
        reminderRepository = FakeDataSource()
        saveRemindersViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext() , reminderRepository)
    }

    @Test
    fun shouldReturnError_noTitle() = mainCoroutineRule.runBlockingTest {

        val testMissingDataTitle = ReminderDataItem(null ,
            "Test" ,
            "Test" ,
            20.0 ,
            30.0)

        saveRemindersViewModel.validateAndSaveReminder(testMissingDataTitle)

        assertThat(saveRemindersViewModel.showSnackBarInt.getOrAwaitValue() , notNullValue())
    }

    @Test
    fun shouldReturnError_noLocation() = mainCoroutineRule.runBlockingTest {

        val testMissingDataLocation = ReminderDataItem("Test" ,
            "Test" ,
            null ,
            20.0 ,
            30.0)

        saveRemindersViewModel.validateAndSaveReminder(testMissingDataLocation)

        assertThat(saveRemindersViewModel.showSnackBarInt.getOrAwaitValue() , notNullValue())
    }

    @Test
    fun dataAvailable_saveButtonClicked(){
        val reminder = ReminderDataItem("Title" ,
            "Description" ,
            "Location" ,
            20.0 ,
            30.0)
        saveRemindersViewModel.saveReminder(reminder)
        assertThat(saveRemindersViewModel.showToast.getOrAwaitValue() , `is`("Reminder Saved !"))
    }

    @Test
    fun check_loading() = mainCoroutineRule.runBlockingTest {
        val reminder = ReminderDataItem("Test" ,
            "Test" ,
            "Test" ,
            20.0 ,
            30.0)

        mainCoroutineRule.pauseDispatcher()

        saveRemindersViewModel.validateAndSaveReminder(reminder)

        assertThat(saveRemindersViewModel.showLoading.getOrAwaitValue() , `is`(true))

        mainCoroutineRule.resumeDispatcher()

        assertThat(saveRemindersViewModel.showLoading.getOrAwaitValue() , `is`(false))

    }
}