package com.example.musicplayer.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.musicplayer.R
import com.google.firebase.auth.FirebaseAuth
import durdinapps.rxfirebase2.RxFirebaseAuth
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


class LoginActivity : AppCompatActivity() {
    private val TAG = "LoginActivity"
    lateinit var mAuth: FirebaseAuth

    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonLogin: Button
    private lateinit var buttonSignUp: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mAuth = FirebaseAuth.getInstance()

        editTextEmail = findViewById(R.id.et_email)
        editTextPassword = findViewById(R.id.et_password)

        buttonLogin = findViewById(R.id.button_login)
        buttonLogin.setOnClickListener{
            login()
        }

        buttonSignUp = findViewById(R.id.button_sign_up)
        buttonSignUp.setOnClickListener{
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()

            val intent = Intent(this@LoginActivity, SignUpActivity::class.java)
            intent.putExtra("email", email)
            intent.putExtra("password", password)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        if (mAuth.currentUser != null){
            finish()
        }
    }

    private fun login(){
        val email = editTextEmail.text.toString()
        val password = editTextPassword.text.toString()

        val disposable = RxFirebaseAuth.signInWithEmailAndPassword(mAuth, email, password)
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .map { authResult -> authResult.user != null  }
            .subscribe{

                if (it){
                    Log.d(TAG, "Auth successful")
                    finish()
                }
                else
                    Log.d(TAG, "Auth failed")
            }
    }
}