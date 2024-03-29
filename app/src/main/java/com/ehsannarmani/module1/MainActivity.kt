package com.ehsannarmani.module1

import android.annotation.SuppressLint
import android.content.Context
import android.health.connect.datatypes.Metadata
import android.health.connect.datatypes.StepsRecord
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationRequest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ehsannarmani.module1.database.AppDatabase
import com.ehsannarmani.module1.screens.CameraScreen
import com.ehsannarmani.module1.screens.CheckpointScreen
import com.ehsannarmani.module1.screens.HomeScreen
import com.ehsannarmani.module1.screens.RecordScreen
import com.ehsannarmani.module1.ui.theme.Module1Theme
import java.time.Instant

class MainActivity : ComponentActivity() {
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppDatabase.setup(this)

        setContent {
            Module1Theme(false) {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val appState = LocalAppState.current

                    NavHost(
                        navController = appState.navController,
                        startDestination = Navigation.Home.route
                    ){
                        composable(Navigation.Home.route){
                            HomeScreen()
                        }
                        composable(Navigation.Record.route){
                            RecordScreen()
                        }
                        composable(Navigation.Checkpoint.route){
                            CheckpointScreen()
                        }
                        composable(Navigation.Camera.route){
                            CameraScreen()
                        }
                    }
                }
            }
        }
    }
}
