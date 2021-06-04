package com.example.musicplayer.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.musicplayer.R
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import durdinapps.rxfirebase2.RxFirebaseAuth
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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

        initViews()
    }

    override fun onStart() {
        super.onStart()
        if (mAuth.currentUser != null){
            finish()
        }
    }

    private fun initViews() {
        editTextEmail = findViewById(R.id.et_email)
        editTextPassword = findViewById(R.id.et_password)
        editTextPasswordConfirm = findViewById(R.id.et_passwordConfirm)

        buttonSignUp = findViewById(R.id.button_sign_up)
        buttonSignUp.setOnClickListener{
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()
            val passwordConfirm = editTextPasswordConfirm.text.toString()
            if (password != passwordConfirm){
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {

                val authResult = signUp(mAuth, email, password)
                if (authResult != null && authResult.user != null)
                {
                    // success
                    Log.d(TAG, "Sign up in success")
                    launchMainActivity()
                }
                else {
                    Log.d(TAG, "Sign up failed")
                    Toast.makeText(this@SignUpActivity, "Operation failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun launchMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private suspend fun signUp(firebaseAuth: FirebaseAuth, email: String, password: String): AuthResult?{
        return try{
            firebaseAuth
                .createUserWithEmailAndPassword(email, password)
                .await()
        }
        catch (e: Exception){
            null
        }
    }

//    private fun signUp(){
//        val email = editTextEmail.text.toString()
//        val password = editTextPassword.text.toString()
//
//        val disposable = RxFirebaseAuth.createUserWithEmailAndPassword(mAuth, email, password)
//            .subscribeOn(Schedulers.computation())
//            .observeOn(AndroidSchedulers.mainThread())
//            .map { authResult -> authResult.user != null  }
//            .subscribe{
//
//                if (it){
//                    Log.d(TAG, "Sign up successful")
//                    finish()
//                }
//                else
//                    Log.d(TAG, "Sign up failed")
//            }
//    }
}