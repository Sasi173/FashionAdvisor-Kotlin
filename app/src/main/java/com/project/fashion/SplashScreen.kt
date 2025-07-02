package com.project.fashion

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.project.fashion.databinding.ActivitySplashScreenBinding
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext


@SuppressLint("CustomSplashScreen")
class SplashScreen : AppCompatActivity() {
    private val bind by lazy {
        ActivitySplashScreenBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(bind.root)
        lifecycleScope.async {
            var point = 0
            for (i in 0..100) {
                point += 10
                delay(3)
                withContext(Main) {
                    bind.logo.setPadding(0, 0, point, 0)
                }
            }
            withContext(Main) {
                bind.logo.setImageResource(R.drawable.shirt)
            }
            for (i in 0..100) {
                point -= 10
                delay(3)
                withContext(Main) {
                    bind.logo.setPadding(point, 0, 0, 0)
                }
            }
            for (i in 0..100) {
                point += 10
                delay(3)
                withContext(Main) {
                    bind.logo.setPadding(0, point, 0, 0)
                }
            }
            withContext(Main) {
                get().let {
                    if (it.username.isNotEmpty()) {
                        startActivity(Intent(applicationContext, DashBoard::class.java))
                    } else {
                        startActivity(Intent(applicationContext, LoginActivity::class.java))

                    }
                }
                finish()
            }
        }.start()
    }
}