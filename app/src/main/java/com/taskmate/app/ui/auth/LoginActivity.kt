package com.taskmate.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.ktx.Firebase
import com.taskmate.app.R
import com.taskmate.app.databinding.ActivityLoginBinding
import com.taskmate.app.ui.BaseActivity
import com.taskmate.app.ui.main.MainActivity
import com.taskmate.app.util.Constants
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginActivity : BaseActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var analytics: FirebaseAnalytics
    private var googleClient: GoogleSignInClient? = null
    private val callbackManager = CallbackManager.Factory.create()

    private fun getWebClientId(): String? {
        val resId = resources.getIdentifier("default_web_client_id", "string", packageName)
        return if (resId != 0) getString(resId) else null
    }

    private val googleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account.idToken)
        } catch (e: ApiException) {
            showError(getString(R.string.error_google_signin))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        analytics = Firebase.analytics

        if (auth.currentUser != null) {
            goToMain()
            return
        }

        val webClientId = getWebClientId()
        if (webClientId != null) {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build()
            val client = GoogleSignIn.getClient(this, gso)
            googleClient = client
            binding.btnGoogle.setOnClickListener {
                googleLauncher.launch(client.signInIntent)
            }
        } else {
            binding.btnGoogle.setOnClickListener {
                showError(getString(R.string.error_google_signin))
            }
        }

        setupFacebookLogin()

        binding.btnLogin.setOnClickListener { emailLogin() }
        binding.btnRegister.setOnClickListener { emailRegister() }
        binding.btnAnonymous.setOnClickListener { anonymousLogin() }
    }

    private fun emailLogin() {
        val email = binding.etEmail.text.toString().trim()
        val pass = binding.etPassword.text.toString()
        if (!validate(email, pass)) return
        setLoading(true)
        lifecycleScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, pass).await()
                onLoginSuccess("email")
            } catch (e: Exception) {
                setLoading(false)
                showError(e.localizedMessage ?: getString(R.string.error_login))
            }
        }
    }

    private fun emailRegister() {
        val email = binding.etEmail.text.toString().trim()
        val pass = binding.etPassword.text.toString()
        if (!validate(email, pass)) return
        setLoading(true)
        lifecycleScope.launch {
            try {
                auth.createUserWithEmailAndPassword(email, pass).await()
                onLoginSuccess("email_register")
            } catch (e: Exception) {
                setLoading(false)
                showError(e.localizedMessage ?: getString(R.string.error_register))
            }
        }
    }

    private fun anonymousLogin() {
        setLoading(true)
        lifecycleScope.launch {
            try {
                auth.signInAnonymously().await()
                onLoginSuccess("anonymous")
            } catch (e: Exception) {
                setLoading(false)
                showError(e.localizedMessage ?: getString(R.string.error_login))
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String?) {
        if (idToken == null) {
            showError(getString(R.string.error_google_signin))
            return
        }
        setLoading(true)
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        lifecycleScope.launch {
            try {
                auth.signInWithCredential(credential).await()
                onLoginSuccess("google")
            } catch (e: Exception) {
                setLoading(false)
                showError(e.localizedMessage ?: getString(R.string.error_google_signin))
            }
        }
    }

    private fun setupFacebookLogin() {
        val appId = getString(R.string.facebook_app_id)
        if (appId.startsWith("000000")) {
            binding.btnFacebook.setOnClickListener {
                showError(getString(R.string.error_facebook_signin))
            }
            return
        }
        LoginManager.getInstance().registerCallback(
            callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    firebaseAuthWithFacebook(result.accessToken)
                }

                override fun onCancel() {
                    setLoading(false)
                }

                override fun onError(error: FacebookException) {
                    setLoading(false)
                    showError(error.localizedMessage ?: getString(R.string.error_facebook_signin))
                }
            }
        )
        binding.btnFacebook.setOnClickListener {
            setLoading(true)
            LoginManager.getInstance()
                .logInWithReadPermissions(this, listOf("email", "public_profile"))
        }
    }

    private fun firebaseAuthWithFacebook(token: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(token.token)
        lifecycleScope.launch {
            try {
                auth.signInWithCredential(credential).await()
                onLoginSuccess("facebook")
            } catch (e: Exception) {
                setLoading(false)
                showError(e.localizedMessage ?: getString(R.string.error_facebook_signin))
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    private fun validate(email: String, pass: String): Boolean {
        if (email.isEmpty() || pass.length < 6) {
            showError(getString(R.string.error_invalid_credentials))
            return false
        }
        return true
    }

    private fun onLoginSuccess(method: String) {
        analytics.logEvent(Constants.EVENT_LOGIN) {
            param("method", method)
        }
        goToMain()
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) android.view.View.VISIBLE else android.view.View.GONE
        binding.btnLogin.isEnabled = !loading
        binding.btnRegister.isEnabled = !loading
        binding.btnAnonymous.isEnabled = !loading
        binding.btnGoogle.isEnabled = !loading
        binding.btnFacebook.isEnabled = !loading
    }

    private fun showError(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }
}