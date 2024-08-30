package com.syed.soundmixer.login

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import com.syed.soundmixer.R
import com.syed.soundmixer.databinding.ActivitySignInBinding
import com.syed.soundmixer.home.HomeActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private lateinit var viewModel: SignInViewModel

    private val signInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                viewModel.handleSignInResult(result.data)
            } else {
                viewModel.handleSignInResult(null)
            }
            Handler(Looper.getMainLooper()).postDelayed({
                binding.googleLogo.clearAnimation()
                binding.googleSignInText.clearAnimation()
            }, 1500)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this)[SignInViewModel::class.java]

        val rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate_animation)
        val fadeAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_animation)

        binding.googleSignInContainer.setOnClickListener {
            binding.googleLogo.startAnimation(rotateAnimation)
            binding.googleSignInText.startAnimation(fadeAnimation)
            val signInIntent = viewModel.getSignInIntent()
            signInLauncher.launch(signInIntent)
        }

        setContentView(binding.root)
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.signInState.observe(this) { state ->
            when (state) {
                is SignInState.Success -> {
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                }

                is SignInState.Error -> {
                    Toast.makeText(this, "Sign-In Failed: ${state.message}", Toast.LENGTH_SHORT)
                        .show()
                }

                is SignInState.Loading -> {
                }
            }
        }
    }
}
