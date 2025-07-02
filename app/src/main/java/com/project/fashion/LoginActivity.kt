package com.project.fashion

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.project.fashion.data.DataBaseView
import com.project.fashion.databinding.LoginBinding
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {
    private val bind by lazy {
        LoginBinding.inflate(layoutInflater)
    }
    private val data by lazy {
        DataBaseView.getContext(this).dao()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(bind.root)
        with(bind) {
            loginBtn.setOnClickListener {
                val email = emailPoint.text.toString().trim()
                val password = passwordPoint.text.toString().trim()
                if (email.isEmpty()) {
                    toast("Please enter your email")
                } else if (password.isEmpty()) {
                    toast("Please enter your password")
                } else {
                    lifecycleScope.launch {
                        val check = data.checkLogin(email = email, password = password)
                        withContext(Main) {
                            if (check.isEmpty()) {
                                toast("Invalid User")
                            } else {
                                toast("SuccessFully Logged In")
                                check[0].let {
                                    store(it)
                                    startActivity(Intent(applicationContext, DashBoard::class.java))
                                    finish()
                                }
                            }
                        }
                    }
                }

            }
            createAc.setOnClickListener {
                startActivity(
                    Intent(applicationContext, Signup::class.java)
                )
            }
        }
    }
}