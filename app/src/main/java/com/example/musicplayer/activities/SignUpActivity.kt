package com.example.musicplayer.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import com.example.musicplayer.R
import com.google.firebase.auth.FirebaseAuth
import durdinapps.rxfirebase2.RxFirebaseAuth
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class SignUpActivity : AppCompatActivity() {
    private val TAG = "SignUpActivity2"
    lateinit var mAuth: FirebaseAuth

    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var editTextPasswordConfirm: EditText
    private lateinit var buttonSignUp: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        mAuth = FirebaseAuth.getInstance()

        editTextEmail = findViewById(R.id.et_email)
        editTextPassword = findViewById(R.id.et_password)
        editTextPasswordConfirm = findViewById(R.id.et_passwordConfirm)

        buttonSignUp = findViewById(R.id.button_sign_up)
        buttonSignUp.setOnClickListener{
            signUp()
        }
    }

    override fun onStart() {
        super.onStart()
        if (mAuth.currentUser != null){
            finish()
        }
    }

    private fun signUp(){
        val email = editTextEmail.text.toString()
        val password = editTextPassword.text.toString()

        val disposable = RxFirebaseAuth.createUserWithEmailAndPassword(mAuth, email, password)
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .map { authResult -> authResult.user != null  }
            .subscribe{

                if (it){
                    Log.d(TAG, "Sign up successful")
                    finish()
                }
                else
                    Log.d(TAG, "Sign up failed")
            }
    }
}