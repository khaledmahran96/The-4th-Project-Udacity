package com.udacity.project4

import android.app.Activity
import android.app.Application
import android.os.Build
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@LargeTest
@ExperimentalCoroutinesApi
@Config(sdk = [Build.VERSION_CODES.P])

//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    // An idling resource that waits for Data Binding to have no pending bindings.
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @get:Rule
    var activityTestRule: ActivityTestRule<RemindersActivity>?= ActivityTestRule<RemindersActivity>(
        RemindersActivity::class.java
    )



    private fun getActivity(activityScenario: ActivityScenario<RemindersActivity>):Activity?{
        var activity: Activity? = null
        activityScenario.onActivity {
            activity = it
        }
        return activity
    }


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
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    @After
    fun cleanDb() = runBlocking {
        repository.deleteAllReminders()
    }

    /**
     * Idling resources tell Espresso that the app is idle or busy. This is needed when operations
     * are not scheduled in the main Looper (for example when executed on a different thread).
     */
    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    /**
     * Unregister your Idling Resource so it can be garbage collected and does not leak any memory.
     */
    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }


    //    TODO: add End to End testing to the app
    @Test
    fun addReminder_checkThatReminderAndToastBeingShown() = runBlockingTest {
        //GIVEN - At the start of app with no reminders.
        // Start up Reminders screen.
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        //WHEN - pressing the addFab and adding new reminder
        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.reminderTitle)).perform(replaceText("Reminder Title"))
        onView(withId(R.id.reminderDescription)).perform(replaceText("Reminder Description"))
        onView(withId(R.id.selectLocation)).perform(click())
        onView(withId(R.id.select_location_map)).perform(longClick())
        onView(withId(R.id.save_button)).perform(click())
        onView(withId(R.id.saveReminder)).perform(click())

        //THEN - A new reminder should be shown with a toast Reminder Saved!
        //I faced a lot of errors with toast test so i searched and used this codes but the toast test
        //cannot run on API 29 and above.
        //https://stackoverflow.com/questions/28390574/checking-toast-message-in-android-espresso
        //The issue still open til now.
        //https://github.com/android/android-test/issues/803
        //but i think this code should work properly on API28

        onView(withText(R.string.reminder_saved)).inRoot(withDecorView(not(`is`(getActivity(activityScenario)?.window?.decorView))))
            .check(matches(isDisplayed()))

        onView(withText("Reminder Title")).check(matches(isDisplayed()))

        // Make sure the activity is closed before resetting the db:
        activityScenario.close()

    }

    @Test
    fun addNewReminderWithMissingData_clickOnSaveButton() = runBlockingTest {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // GIVEN - Saving reminder without location
        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.reminderTitle)).perform(replaceText("Title"))
        onView(withId(R.id.reminderDescription)).perform(replaceText("Description"))

        // WHEN - Clicking on saveButton
        onView(withId(R.id.saveReminder)).perform(click())

        // THEN - Snackbar shows up
        val snackBar = appContext.getString(R.string.err_select_location)
        onView(withText(snackBar)).check(matches(isDisplayed()))

        // Make sure the activity is closed before resetting the db:
        activityScenario.close()
    }


}
