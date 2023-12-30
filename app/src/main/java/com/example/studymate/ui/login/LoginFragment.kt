package com.example.studymate.ui.login

import android.content.Intent
import androidx.lifecycle.Observer
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.MutableLiveData
import com.example.studymate.MainActivity
import com.example.studymate.databinding.FragmentLoginBinding
import com.example.studymate.R
import com.example.studymate.data.model.User
import com.example.studymate.data.model.ApplicationState
import com.example.studymate.ui.LoadingDialog
import com.example.studymate.ui.register.RegisterFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

private const val TAG = "LOGIN_FRAGMENT"

class LoginFragment : Fragment() {

    private val _loginForm = MutableLiveData<LoginFormState>()
    private var _binding: FragmentLoginBinding? = null
    private lateinit var fAuth: FirebaseAuth
    private val db = Firebase.firestore
    private val userViewModel = ApplicationState
    private lateinit var loadingDialog: LoadingDialog

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val usernameEditText = binding.username
        val passwordEditText = binding.password
        val loginButton = binding.login
        val toRegister = binding.toRegister
        loadingDialog = LoadingDialog(requireActivity())
        fAuth = FirebaseAuth.getInstance()
        toRegister.setOnClickListener {
            swapToRegister()
        }

        _loginForm.observe(viewLifecycleOwner,
            Observer { loginFormState ->
                if (loginFormState == null) {
                    return@Observer
                }
                loginFormState.passwordError?.let {
                    passwordEditText.error = getString(it)
                }
            })

        passwordEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                login(
                    usernameEditText.text.toString(),
                    passwordEditText.text.toString()
                )
            }
            false
        }

        loginButton.setOnClickListener {
            loadingDialog.show()
            login(
                usernameEditText.text.toString(),
                passwordEditText.text.toString()
            )
        }
    }

    private fun updateUiWithUser() {
        val intent = Intent(activity, MainActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }

    private fun showLoginFailed(errorString: String) {
        loadingDialog.dismiss()
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, errorString, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun login(username: String, password: String) {
        db.collection("users").whereEqualTo("username", username).get()
            .addOnCompleteListener {task ->
                if(task.isSuccessful && !task.result.isEmpty){
                    val documentItem = task.result.documents[0].toObject<User>()
                    Log.d(TAG, documentItem.toString())
                    val email = documentItem?.email ?: return@addOnCompleteListener
                    fAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener {task->
                            if(task.isSuccessful) {
                                Log.d(TAG, "loginUserWithEmail:success")
                                val user = fAuth.currentUser
                                val docRef = db.collection("users").document(email)
                                docRef.get()
                                    .addOnCompleteListener { task ->
                                        loadingDialog.dismiss()
                                        if (task.isSuccessful) {
                                            // Document found in the offline cache
                                            val document = task.result
                                            Log.d(TAG, "Cached document data: ${document?.data}")
                                            val user = User(documentItem.email, documentItem.displayName, documentItem.username, documentItem.location, documentItem.accountRole, documentItem.schedule)
                                            userViewModel.loginUser(user)
                                            updateUiWithUser()
                                        } else {
                                            showLoginFailed(resources.getString(R.string.login_failed))
                                            Log.d(TAG, "Cached get failed: ", task.exception)
                                        }
                                    }

                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "loginUserWithEmail:failure", task.exception)
                                //CASES:
                                showLoginFailed(resources.getString(R.string.login_failed))
                                //TODO: Add checks for when the user already exists
                                task.exception?.let {
                                    _loginForm.value = LoginFormState(passwordError = R.string.invalid_login)
                                }
                            }
                        }
                }
                else{
                    Log.w(TAG, "cannot find user with that username: $username")
                    showLoginFailed(resources.getString(R.string.login_failed))
                }
            }
    }

    private fun swapToRegister() {
        var fragment: Fragment = RegisterFragment()
        val ft: FragmentTransaction = parentFragmentManager.beginTransaction()
        ft.replace(R.id.auth_fragment, fragment)
        ft.commit()
    }

}