package com.example.musicplayer.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.musicplayer.R
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import durdinapps.rxfirebase2.RxFirebaseAuth
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableMaybeObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class LoginActivity : AppCompatActivity() {
    private val TAG = "LoginActivity"
    lateinit var mAuth: FirebaseAuth

    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonLogin: Button
    private lateinit var buttonSignUp: Button

    private lateinit var disposable: Disposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mAuth = FirebaseAuth.getInstance()

        initViews()

    }

    override fun onStart() {
        super.onStart()
        if (mAuth.currentUser != null){
            launchMainActivity()
        }
    }

    private fun initViews() {
        editTextEmail = findViewById(R.id.et_email)
        editTextPassword = findViewById(R.id.et_password)

        buttonLogin = findViewById(R.id.button_login)
        buttonLogin.setOnClickListener{
            //login()
            lifecycleScope.launch {
                val email = editTextEmail.text.toString()
                val password = editTextPassword.text.toString()
                val authResult = logInWithEmail(mAuth, email, password)
                if (authResult != null && authResult.user != null)
                {
                    // success
                    Log.d(TAG, "Log in success")
                    launchMainActivity()
                }
                else {
                    Log.d(TAG, "Log in failed")
                    Toast.makeText(this@LoginActivity, "Operation failed", Toast.LENGTH_SHORT).show()
                }
            }
        }

        buttonSignUp = findViewById(R.id.button_sign_up)
        buttonSignUp.setOnClickListener{
            launchSignUpActivity()
        }
    }

    private fun launchMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun launchSignUpActivity() {
        val email = editTextEmail.text.toString()
        val password = editTextPassword.text.toString()

        val intent = Intent(this@LoginActivity, SignUpActivity::class.java)
        intent.putExtra("email", email)
        intent.putExtra("password", password)
        startActivity(intent)
    }


    private suspend fun logInWithEmail(firebaseAuth: FirebaseAuth,
                               email:String,password:String): AuthResult? {
        return try{
            val data = firebaseAuth
                .signInWithEmailAndPassword(email,password)
                .await()
            data
        }catch (e : Exception){
            null
        }
    }

    private fun login(){
//        val email = editTextEmail.text.toString()
//        val password = editTextPassword.text.toString()
//
////        val disposable = RxFirebaseAuth.signInWithEmailAndPassword(mAuth, email, password)
////            .subscribeOn(Schedulers.computation())
////            .observeOn(AndroidSchedulers.mainThread())
////            .map { authResult -> authResult.user != null  }
////            .subscribe{
////
////                if (it){
////                    Log.d(TAG, "Auth successful")
////                    finish()
////                }
////                else{
////                    Toast.makeText(this, "Operation failed. Try again", Toast.LENGTH_SHORT).show()
////                    Log.d(TAG, "Auth failed")
////                }
////            }
//
//         disposable = RxFirebaseAuth.signInWithEmailAndPassword(mAuth, email, password)
//            .subscribeOn(Schedulers.computation())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe(
//                // onSuccess()
//                x -> System.out.print("Emitted item: " + x),
//                ex -> System.out.println("Error: " + ex.getMessage()),
//                () -> System.out.println("Completed. No items.")
////                if (it.user != null){
////                    Log.d(TAG, "Auth successful")
////                    launchMainActivity()
////                }
////                else{
////                    Toast.makeText(this, "Operation failed. Try again", Toast.LENGTH_SHORT).show()
////                    Log.d(TAG, "Auth failed")
////                }
//                )

    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }
}