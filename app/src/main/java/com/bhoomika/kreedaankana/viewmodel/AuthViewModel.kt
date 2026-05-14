package com.bhoomika.kreedaankana.viewmodel

import androidx.lifecycle.ViewModel
import com.bhoomika.kreedaankana.repository.AuthRepository

class AuthViewModel : ViewModel() {

    private val repo = AuthRepository()

    fun signUp(
        name: String,
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        repo.signUp(name, email, password, onResult)
    }

    fun login(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        repo.login(email, password, onResult)
    }

    fun isLoggedIn(): Boolean = repo.isUserLoggedIn()
}