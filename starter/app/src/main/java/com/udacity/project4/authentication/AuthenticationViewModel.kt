package com.udacity.project4.authentication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.map

class AuthenticationViewModel: ViewModel() {

    //Creating an authenticationState variable based off of the FirebaseUserLiveData object.
    // By creating this authenticationState variable,
    // other classes can now query for whether the user is logged in or not through the AuthenticationViewModel

    enum class AuthenticationState {
        AUTHENTICATED, UNAUTHENTICATED, INVALID_AUTHENTICATION
    }

    val authenticationState = FirebaseUserLiveData().map { user ->
        if (user != null) {
            AuthenticationState.AUTHENTICATED
        } else {
            AuthenticationState.UNAUTHENTICATED
        }
    }


}