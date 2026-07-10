package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.data.database.AppDatabase
import com.example.data.repository.SyncOfficeRepository
import com.example.ui.SyncOfficeApp
import com.example.ui.SyncOfficeViewModel
import com.example.ui.SyncOfficeViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Local Secure Room Database
        val database = AppDatabase.getDatabase(this)
        val repository = SyncOfficeRepository(database)

        // Initialize ViewModel via factory
        val factory = SyncOfficeViewModelFactory(application, repository)
        val viewModel = ViewModelProvider(this, factory)[SyncOfficeViewModel::class.java]

        setContent {
            MyApplicationTheme {
                SyncOfficeApp(viewModel = viewModel)
            }
        }
    }
}
