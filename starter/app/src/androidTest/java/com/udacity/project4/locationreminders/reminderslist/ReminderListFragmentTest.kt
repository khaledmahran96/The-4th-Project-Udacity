package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.GlobalContext
import org.koin.core.context.GlobalContext.get
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.get
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest {
//    TODO: test the navigation of the fragments.
//    TODO: test the displayed data on the UI.
//    TODO: add testing for the error messages.

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */

    @Before
    fun initDb() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }

        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }

        //Get our real repository
        repository = GlobalContext.get().koin.get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    @After
    fun clearDb() = stopKoin()


    @Test
    fun emptyReminders_noDataShow() = runBlockingTest{
        // GIVEN - Where theres no reminders

        // WHEN - ReminderList fragment launched to display reminders
        launchFragmentInContainer<ReminderListFragment>(Bundle.EMPTY , R.style.AppTheme)

        // THEN - There is no data to show.
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
    }

    @Test
    fun thereIsReminder_checkingThatReminders() = runBlockingTest {
        // GIVEN - Where theres is reminders
        val reminder1 = ReminderDTO("Test1" ,
            "Desc1" ,
            "Loc1" ,
            10.0 ,
            20.0)

        val reminder2 = ReminderDTO("Test2" ,
            "Desc2" ,
            "Loc2" ,
            30.0 ,
            40.0)

        runBlocking {
            repository.saveReminder(reminder1)
            repository.saveReminder(reminder2)
        }
        // WHEN - ReminderList fragment launched to display reminders

        launchFragmentInContainer<ReminderListFragment>(Bundle.EMPTY , R.style.AppTheme)

        // THEN - There is data to show.
        onView(withId(R.id.noDataTextView)).check(matches(not(isDisplayed())))
        onView(withText(reminder1.title)).check(matches(isDisplayed()))
        onView(withText(reminder1.title)).check(matches(withText("Test1")))
        onView(withText(reminder2.title)).check(matches(isDisplayed()))
        onView(withText(reminder2.title)).check(matches(withText("Test2")))

    }

    @Test
    fun clickAddReminderButton_navigateToAddReminder() = runBlockingTest {
        // GIVEN - On the reminder list screen
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!! , navController)
        }

        //when - Click on the add button
        onView(withId(R.id.addReminderFAB)).perform(click())

        //Then - nav to SaveReminder
        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }


}