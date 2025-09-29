package com.example.myapplication

import android.os.Bundle

import android.widget.Toast
import androidx.activity.enableEdgeToEdge

import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.databinding.ActivityMainBinding
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    // 1. bikin binding dari main activity
    private lateinit var binding: ActivityMainBinding
    private lateinit var credentialManager: CredentialManager
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // 2. inisiasi binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        // 3. set content dari binding
        setContentView(binding.root)


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        credentialManager= CredentialManager.create(this)
        auth = Firebase.auth

        // 4. daftarkan event yang diperlukan
        registerEvent()
    }
    private fun registerEvent() {
        // 5. Daftarkan event ketika button di click
        binding.btnLogin.setOnClickListener {
            lifecycleScope.launch {
                val request = prepareRequest()
                loginByGoogle(request)
            }
        }
    }

    private fun prepareRequest(): GetCredentialRequest {
        val serverClientId = "7171634633-oju12g2vhuokcib64vs6eiikeijrv458.apps.googleusercontent.com"

        val googleIdOption = GetGoogleIdOption
            .Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(serverClientId)
            .build()

        val request = GetCredentialRequest
            .Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return request
    }

    private suspend fun  loginByGoogle(request: GetCredentialRequest) {
        try {
            val result = credentialManager.getCredential(
                context = this,
                request = request
            )
            val credential = result.credential
            val idToken = GoogleIdTokenCredential.createFrom(credential.data)

            firebaseLoginCallback(idToken.idToken)

        }   catch (exc: NoCredentialException) {
                Toast.makeText(this, "Login Gagal :" + exc.message, Toast.LENGTH_LONG).show()
        }   catch (exc: Exception) {
                Toast.makeText(this, "Login Gagal :" + exc.message, Toast.LENGTH_LONG).show()
        }


    }

    private fun firebaseLoginCallback(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) {task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Login Berhasil", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Login Gagal", Toast.LENGTH_LONG).show()
            }
        }
    }
}