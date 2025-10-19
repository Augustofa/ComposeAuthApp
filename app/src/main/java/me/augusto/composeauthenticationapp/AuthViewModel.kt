package me.augusto.composeauthenticationapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = Firebase.auth

    private val _authUiState = MutableStateFlow(AuthUiState())
    val authUiState: StateFlow<AuthUiState> = _authUiState

    init {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            _authUiState.value = AuthUiState(isAuthenticated = true, userEmail = currentUser.email)
        }
    }

    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            _authUiState.value = AuthUiState(isLoading = true)
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _authUiState.value = AuthUiState(isAuthenticated = true, userEmail = auth.currentUser?.email)
                    } else {
                        _authUiState.value = AuthUiState(error = "Login failed. Please check your credentials.")
                    }
                }
        }
    }

    fun registerUser(email: String, password: String) {
        viewModelScope.launch {
            _authUiState.value = AuthUiState(isLoading = true)
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _authUiState.value = AuthUiState(isAuthenticated = true, userEmail = auth.currentUser?.email)
                    } else {
                        _authUiState.value = AuthUiState(error = "Registration failed. Please try again.")
                    }
                }
        }
    }

    fun logoutUser() {
        auth.signOut()
        _authUiState.value = AuthUiState(isAuthenticated = false, userEmail = null)
    }
}