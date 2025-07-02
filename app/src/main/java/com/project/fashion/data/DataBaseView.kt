package com.project.fashion.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [User::class], exportSchema = true, version = 1)
abstract class DataBaseView : RoomDatabase() {


    abstract fun dao(): com.project.fashion.data.Dao


    companion object {
        fun getContext(context: Context): DataBaseView {
            return Room.databaseBuilder(context, DataBaseView::class.java, "ViewData")
                .allowMainThreadQueries().build()
        }
    }
}