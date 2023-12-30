package com.example.studymate.ui.register

/**
 * Data validation state of the login form.
 */
data class RegisterFormState(
    val emailError: Int? = null,
    val passwordError: Int? = null,
    val usernameError: Int? = null,
    val displayError: Int? = null,
    val locationError: Int? = null,
    val accountRoleError: Int? = null,
    val isDataValid: Boolean = false
)