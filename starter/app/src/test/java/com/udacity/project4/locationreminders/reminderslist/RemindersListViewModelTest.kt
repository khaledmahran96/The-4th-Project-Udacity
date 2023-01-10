package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@Config(sdk = [Build.VERSION_CODES.P])
class RemindersListViewModelTest {

    //TODO: provide testing to the RemindersListViewModel and its live data objects
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var reminderRepository : FakeDataSource
    private lateinit var remindersListViewModel: RemindersListViewModel

    @Before
    fun setupViewModel(){
        stopKoin()
        FirebaseApp.initializeApp(getApplicationContext())
        reminderRepository = FakeDataSource()
        remindersListViewModel = RemindersListViewModel(getApplicationContext() , reminderRepository)
    }


    @Test
    fun shouldReturnError() = mainCoroutineRule.runBlockingTest {

        // Make the repository return errors.
        reminderRepository.shouldReturnError(true)
        remindersListViewModel.loadReminders()


        //Checking that is there is no reminder that SnackBar should not be null
        assertThat(remindersListViewModel.showSnackBar.getOrAwaitValue() , `is`(notNullValue()))
    }

    @Test
    fun check_loading() = mainCoroutineRule.runBlockingTest {
        val reminderItem = ReminderDTO("Test" ,
            "Test" ,
            "Test" ,
            20.0 ,
            30.0)
        reminderRepository.saveReminder(reminderItem)
        //Pausing the process to make sure that the viewmodel is loading the reminders
        mainCoroutineRule.pauseDispatcher()

        remindersListViewModel.loadReminders()
        //Checking that the reminders is being loading or not
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue() , `is`(true))

        mainCoroutineRule.resumeDispatcher()
        //Checking that the reminders is done loading
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(false))
    }
    

}