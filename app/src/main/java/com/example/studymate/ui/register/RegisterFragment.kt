package com.example.studymate.ui.register

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.studymate.AuthenticationActivity
import com.example.studymate.R
import com.example.studymate.data.model.User
import com.example.studymate.data.model.ApplicationState
import com.example.studymate.databinding.FragmentRegisterBinding
import com.example.studymate.ui.LoadingDialog
import com.example.studymate.ui.locationSelector.LocationItemFragment
import com.example.studymate.ui.login.LoginFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


private const val TAG = "REGISTER_FRAGMENT"

private var ACCOUNT_ROLES = listOf<String>("Student", "Educator", "Other")

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val _registerForm = MutableLiveData<RegisterFormState>()
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val db = Firebase.firestore
    private val userViewModel = ApplicationState
    private lateinit var loadingDialog: LoadingDialog

    private val viewModel: RegisterViewModel by activityViewModels()

    private lateinit var fAuth: FirebaseAuth



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fAuth = FirebaseAuth.getInstance()
        val usernameEditText = binding.username
        val displayNameEditText = binding.displayName
        val passwordEditText = binding.password
        val loginButton = binding.login
        val btnToLogin = binding.toLogin
        val emailEditText = binding.email
        val selectLocationLayout = binding.selectLocationBtn
        val selectLocationTV = binding.selectLocationTv
        val accountRollDropdown:AutoCompleteTextView = binding.selectAccountRole



        if(viewModel.getUsername() != null) usernameEditText.setText( viewModel.getUsername().toString())
        if(viewModel.getPassword() != null) passwordEditText.setText( viewModel.getPassword().toString())
        if(viewModel.getDisplayName() != null) displayNameEditText.setText( viewModel.getDisplayName().toString())
        if(viewModel.getEmail() != null) emailEditText.setText( viewModel.getEmail().toString())
        if(viewModel.getAccountRole() != null) accountRollDropdown.setText(viewModel.getAccountRole().toString())
        if(viewModel.getLocation().value !== null) selectLocationTV.text = viewModel.getLocation().value!!.name
        val adapter: ArrayAdapter<String> = ArrayAdapter(
            requireContext(),
            R.layout.dropdown_account_role,
            ACCOUNT_ROLES
        )


        accountRollDropdown.setAdapter(adapter)
        loadingDialog = LoadingDialog(requireActivity())
        btnToLogin.setOnClickListener {
            swapToLogin()
            viewModel.reset()
        }

        selectLocationLayout.setOnClickListener{
            changeFragment(LocationItemFragment())
        }

        viewModel.getLocation().observe(viewLifecycleOwner) {
            item ->
            item?.name?.let {
                formDataChanged(
                    emailEditText.text.toString(),
                    usernameEditText.text.toString(),
                    displayNameEditText.text.toString(),
                    passwordEditText.text.toString(),
                    it,
                    accountRollDropdown.text.toString()
                )
            }
        }

        _registerForm.observe(viewLifecycleOwner,
            Observer { registerFormState ->
                if (registerFormState == null) {
                    return@Observer
                }
                loginButton.isEnabled = registerFormState.isDataValid
                registerFormState.emailError?.let {
                    emailEditText.error = getString(it)
                }
                registerFormState.usernameError?.let {
                    usernameEditText.error = getString(it)
                }
                registerFormState.displayError?.let {
                    displayNameEditText.error = getString(it)
                }
                registerFormState.passwordError?.let {
                    passwordEditText.error = getString(it)
                }
                registerFormState.locationError?.let {
                    selectLocationTV.error = getString(it)
                }
                registerFormState.accountRoleError?.let {
                    accountRollDropdown.error = getString(it)
                }
            })


        val afterTextChangedListener = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // ignore
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // ignore
            }

            override fun afterTextChanged(s: Editable) {
                formDataChanged(
                    emailEditText.text.toString(),
                    usernameEditText.text.toString(),
                    displayNameEditText.text.toString(),
                    passwordEditText.text.toString(),
                    selectLocationTV.text.toString(),
                    accountRollDropdown.text.toString()
                )
            }
        }

        usernameEditText.addTextChangedListener(afterTextChangedListener)
        emailEditText.addTextChangedListener(afterTextChangedListener)
        accountRollDropdown.addTextChangedListener(afterTextChangedListener)
        displayNameEditText.addTextChangedListener(afterTextChangedListener)
        passwordEditText.addTextChangedListener(afterTextChangedListener)
        selectLocationTV.addTextChangedListener {
            Log.d("TEST", "location name changed: " + selectLocationTV.text.toString())
            formDataChanged(
                emailEditText.text.toString(),
                usernameEditText.text.toString(),
                displayNameEditText.text.toString(),
                passwordEditText.text.toString(),
                selectLocationTV.text.toString(),
                accountRollDropdown.text.toString()
            )
        }
        passwordEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                register(
                    emailEditText.text.toString(),
                    usernameEditText.text.toString(),
                    displayNameEditText.text.toString(),
                    passwordEditText.text.toString(),
                    selectLocationTV.text.toString(),
                    accountRollDropdown.text.toString()
                )
            }
            false
        }

        loginButton.setOnClickListener {
            loadingDialog.show()
            register(
                emailEditText.text.toString(),
                usernameEditText.text.toString(),
                displayNameEditText.text.toString(),
                passwordEditText.text.toString(),
                selectLocationTV.text.toString(),
                accountRollDropdown.text.toString()
            )
        }
    }

    private fun updateUiWithUser() {
        val intent = Intent(activity, AuthenticationActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }

    private fun formDataChanged(email: String, username: String, displayName: String, password: String, locationName: String, accountRole: String) {
        viewModel.setUsername(username)
        viewModel.setEmail(email)
        viewModel.setDisplayName(displayName)
        viewModel.setPassword(password)
        viewModel.setAccountRole(accountRole)
        if(!isEmailValid(email)) {
            _registerForm.value = RegisterFormState(emailError = R.string.invalid_email)
        }
        else if (username.length < 5) {
            _registerForm.value = RegisterFormState(usernameError = R.string.invalid_username)
        } else if (displayName.isBlank()){
            _registerForm.value = RegisterFormState(displayError = R.string.invalid_password)
        }
        else if (!isPasswordValid(password)) {
            _registerForm.value = RegisterFormState(passwordError = R.string.invalid_password)
        } else if (!isPasswordValid(password)) {
            _registerForm.value = RegisterFormState(passwordError = R.string.invalid_password)
        } else if(accountRole.isEmpty()) {
            //_registerForm.value = RegisterFormState(accountRoleError = R.string.invalid_account_role)
        }
        else if (locationName.isEmpty() || locationName == "Select University") {
            _registerForm.value = RegisterFormState(locationError = R.string.invalid_location)
        }
        else {
            _registerForm.value = RegisterFormState(isDataValid = true)
        }
    }

    private fun changeFragment(target: Fragment) {
        val ft: FragmentTransaction = parentFragmentManager.beginTransaction()
        ft.replace(R.id.auth_fragment, target)
        ft.commit()
    }



    private fun register(email:String, username: String, displayName:String, password: String, locationName: String, accountRole: String) {

        db.collection("users").whereEqualTo("username", username).get()
            .addOnCompleteListener{ task ->
                if(task.isSuccessful && task.result.isEmpty) {
                    db.collection("locations").document(locationName).get()
                        .addOnCompleteListener{ task ->
                            Log.d(TAG, "Done getting location info")
                            val doc = task.result
                            if(doc.exists()) {
                                Log.d(TAG, "location exists")
                                createUser(email, username, displayName, password, locationName, accountRole)
                            }
                            else {
                                Log.d(TAG, "Need to craete location")
                                val location = viewModel.getLocation().value
                                val locationToAdd = hashMapOf(
                                    "id" to location?.id,
                                    "name" to location?.name,
                                    "lat_lng" to location?.lat_lng.toString(),
                                    "types" to location?.types.toString(),
                                    "iconURL" to location?.iconURL,
                                    "iconColor" to location?.iconColor,
                                    "iconColor" to location?.iconColor,
                                    "addressComponent" to location?.addressComponent.toString(),
                                    "address" to location?.address,
                                )
                                db.collection("locations").document(locationName).set(locationToAdd)
                                    .addOnCompleteListener { task ->
                                        Log.d(TAG, "Done setting location")
                                        if (task.isSuccessful) {
                                            // Document found in the offline cache
                                            Log.d(TAG, "location registered")
                                            Log.d(TAG, "Cached document data: ${task.result}")
                                            createUser(email, username, displayName, password, locationName, accountRole)
                                        } else {
                                            Log.d(TAG, "Unable to register location")
                                            showRegisterFailed(resources.getString(R.string.register_failed))
                                            Log.d(TAG, "Cached get failed: ", task.exception)
                                        }
                                    }
                            }

                        }
                }
                else{
                    showRegisterFailed("Error: Username Already In Use")
                    Log.d(TAG, "Duplicate username: $username")
                }
            }
    }

    private fun createUser(email:String, username: String, displayName:String, password: String, locationName: String, accountRole: String) {
        Log.d(TAG, "In Crate user")
        fAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {task->
                if(task.isSuccessful) {
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = fAuth.currentUser


                    val userToAdd = hashMapOf(
                        "email" to email,
                        "displayName" to displayName,
                        "username" to username,
                        "location" to locationName,
                        "accountRole" to accountRole,
                        "schedules" to HashMap<String, ArrayList<Boolean>>()
                    )
                    db.collection("users").document(email).set(userToAdd)
                        .addOnCompleteListener { task ->
                            loadingDialog.dismiss()
                            if (task.isSuccessful) {
                                // Document found in the offline cache
                                Log.d(TAG, "Cached document data: ${task.result}")
                                val currUser = User(email, displayName, username, locationName, accountRole, HashMap())

                                updateUiWithUser()
                                userViewModel.loginUser(currUser)
                            } else {
                                showRegisterFailed(resources.getString(R.string.register_failed))
                                Log.d(TAG, "Cached get failed: ", task.exception)
                            }
                        }

                } else {
                    // If sign in fails, display a message to the user.
                    //CASES:
                    showRegisterFailed(resources.getString(R.string.register_failed))
                    //TODO: Add checks for when the user already exists
                }
            }
    }
    // A placeholder username validation check
    private fun isEmailValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }

    private fun showRegisterFailed(errorString: String) {
        loadingDialog.dismiss()
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, errorString, Toast.LENGTH_LONG).show()
    }

    private fun swapToLogin() {
        var fragment: Fragment = LoginFragment()
        val ft: FragmentTransaction = parentFragmentManager.beginTransaction()
        ft.replace(R.id.auth_fragment, fragment)
        ft.commit()
    }
}