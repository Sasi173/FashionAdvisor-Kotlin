package com.project.fashion

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.project.fashion.data.DataBaseView
import com.project.fashion.data.User
import com.project.fashion.databinding.ActivitySignupBinding
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Signup : AppCompatActivity() {
    private val bind by lazy {
        ActivitySignupBinding.inflate(layoutInflater)
    }
    private val data by lazy {
        DataBaseView.getContext(this).dao()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(bind.root)
        with(bind) {
            createAcBtn.setOnClickListener {
                val uName = uName.text.toString().trim()
                val email = email.text.toString().trim()
                val password = password.text.toString().trim()
                if (uName.isEmpty()) {
                    toast("Please enter your user name")
                    return@setOnClickListener
                } else if (email.isEmpty()) {
                    toast("Please enter your email")
                    return@setOnClickListener
                } else if (!email.contains("@gmail.com")) {
                    toast("Please enter your email")
                    return@setOnClickListener
                } else if (password.isEmpty()) {
                    toast("Please enter your password")
                    return@setOnClickListener
                }

                lifecycleScope.launch {
                    val nameCheck = data.checkUserName(uName, "").isEmpty()
                    val emailCheck = data.checkUserName("", email).isEmpty()
                    withContext(Main) {
                        if (!nameCheck) {
                            toast("User name Already Exists")
                            return@withContext
                        } else if (!emailCheck) {
                            toast("Email Already Exists")
                            return@withContext
                        }
                    }
                    if (nameCheck && emailCheck) {
                        data.createPoint(
                            user = User(
                                email = email,
                                username = uName,
                                password = password
                            )
                        )
                        withContext(Main) {
                            finish()
                        }
                    }
                }

            }
            loginAc.setOnClickListener {
                finish()
            }
        }

    }
}