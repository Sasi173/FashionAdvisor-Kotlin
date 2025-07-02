package com.project.fashion

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.core.content.edit
import androidx.core.text.HtmlCompat
import com.project.fashion.data.User

fun Activity.toast(message: Any?) {
    Toast.makeText(applicationContext, "$message", Toast.LENGTH_SHORT).show()
}

fun Activity.store(user: User) {
    getSharedPreferences("user", Context.MODE_PRIVATE).edit {
        putString("email", user.email)
        putString("username", user.username)
        putString("id", user.id.toString())
        putString("password", user.password)
    }
}
fun spanned(string: String)= HtmlCompat.fromHtml(
    string, HtmlCompat.FROM_HTML_OPTION_USE_CSS_COLORS
)

fun Activity.get(): User {
    getSharedPreferences("user", Context.MODE_PRIVATE).let {
        return User(
            email = it.getString("email", "").toString(),
            username = it.getString("username", "").toString(),
            password = it.getString("id", "").toString(),
        )
    }
}