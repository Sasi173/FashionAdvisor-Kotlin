package com.project.fashion.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.IGNORE
import androidx.room.Query

@Dao
interface Dao {
    @Insert(onConflict = IGNORE)
    suspend fun createPoint(user: User)

    @Query("select * from user where username=:name or email=:email")
    suspend fun checkUserName(name: String, email: String): List<User>

    @Query("select * from user where email=:email and password=:password")
    suspend fun checkLogin(email: String, password: String): List<User>

}