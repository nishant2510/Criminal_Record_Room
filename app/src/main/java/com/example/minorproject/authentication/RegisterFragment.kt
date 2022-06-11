package com.example.minorproject.authentication

import android.os.Bundle
import android.util.Patterns
import android.util.Patterns.EMAIL_ADDRESS
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_register.*
import kotlinx.android.synthetic.main.fragment_register.progressBar
import kotlinx.android.synthetic.main.fragment_register.user_email
import kotlinx.android.synthetic.main.fragment_register.user_email_container
import kotlinx.android.synthetic.main.fragment_register.user_password
import kotlinx.android.synthetic.main.fragment_register.user_password_container
import java.util.regex.Pattern

class RegisterFragment : Fragment() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(com.example.minorproject.R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        already_registered_button.setOnClickListener {
            findNavController().navigate(RegisterFragmentDirections.actionRegisterFragmentToLoginFragment())
        }

        register_button.setOnClickListener {
            user_email_container.error=null
            user_password_container.error=null

            val email = user_email.text.toString()
            val password = user_password.text.toString()

            if(validateInput(email, password)){
                progressBar.visibility=View.VISIBLE

                auth.createUserWithEmailAndPassword(email, password).
                addOnCompleteListener(requireActivity()){ task ->
                    progressBar.visibility=View.INVISIBLE

                    if(task.isSuccessful){
                        findNavController().navigate(RegisterFragmentDirections.actionRegisterFragmentToCriminalListFragment())
                    }
                    else{
                        Toast.makeText(requireActivity(), "Registered Failed: {${task.exception?.message}}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        var valid=true
        //val domain:String="nitj.ac.in"
        if(email.isBlank()){
            user_email_container.error="Please Enter An Email Address"
            valid=false
        }

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