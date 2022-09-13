/*
 * Copyright 2019, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.udacity.project4.authentication

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.FirebaseAuth
import androidx.lifecycle.LiveData

/**
 * This class observes the current FirebaseUser. If there is no logged in user, FirebaseUser will
 * be null.
 *
 * Note that onActive() and onInactive() will get triggered when the configuration changes (for
 * example when the device is rotated). This may be undesirable or expensive depending on the
 * nature of your LiveData object, but is okay for this purpose since we are only adding and
 * removing the authStateListener.
 */
class FirebaseUserLiveData : LiveData<FirebaseUser?>() {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        value = firebaseAuth.currentUser
    }

    override fun onActive() {
        firebaseAuth.addAuthStateListener(authStateListener)
    }

    override fun onInactive() {
        firebaseAuth.removeAuthStateListener(authStateListener)
    }
}