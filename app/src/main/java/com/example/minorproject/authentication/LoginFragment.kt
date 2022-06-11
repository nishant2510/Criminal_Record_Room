package com.example.minorproject.authentication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.minorproject.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_login.*

class LoginFragment : Fragment() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        //If user is already logged in
        if(auth.currentUser != null){
            findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToCriminalListFragment())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //New Registration
        new_registration_button.setOnClickListener {
            findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToRegisterFragment())
        }

        login_button.setOnClickListener {
            user_email_container.error=null
            user_password_container.error=null

            val email = user_email.text.toString()
            val password = user_password.text.toString()

            if(validateInput(email, password)){
                progressBar.visibility=View.VISIBLE

                auth.signInWithEmailAndPassword(email, password).
                addOnCompleteListener(requireActivity()){ task ->
                    progressBar.visibility=View.INVISIBLE

                    if(task.isSuccessful){
                        findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToCriminalListFragment())
                    }
                    else{
                        Toast.makeText(requireActivity(), "Authentication Failed: {${task.exception?.message}}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        var valid=true
        if(email.isBlank()){
            user_email_container.error="Please Enter An Email Address"
            valid=false
        }
//        if(email.contentEquals("nitj.ac.in")){
//            user_email_container.error="Please Enter An Valid Email Address"
//            valid=false
//        }
        if(password.isBlank()){
            user_password_container.error="Please Enter Password"
            valid=false
        }
        else if(password.length<8){
            user_password_container.error="Password Length Should Be 8 Characters Or More"
            valid=false
        }
        return valid
    }
}