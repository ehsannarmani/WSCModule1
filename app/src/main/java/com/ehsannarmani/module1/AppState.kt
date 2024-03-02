package com.ehsannarmani.module1

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.CoroutineScope

class AppState(
    val context: Context,
    val navController:NavHostController,
    val coroutineScope: CoroutineScope
) {
}

@Composable
fun rememberAppState():AppState {
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    return remember {
        AppState(
            context = context,
            navController = navController,
            coroutineScope = coroutineScope
        )
    }
}

val LocalAppState = staticCompositionLocalOf<AppState> { error("No State Provided Yet!") }