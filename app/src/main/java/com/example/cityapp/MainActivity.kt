package com.example.cityapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.cityapp.database.CityDatabase
import com.example.cityapp.repository.CityRepository
import com.example.cityapp.store.FavoriteStore
import com.example.cityapp.ui.AddEditCityScreen
import com.example.cityapp.ui.CityListScreen
import com.example.cityapp.ui.CityViewModel
import com.example.cityapp.ui.CityViewModelFactory
import com.example.cityapp.ui.theme.CityAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialisation manuelle simple (pour l'exemple sans DI comme Hilt)
        val database = CityDatabase.getDatabase(applicationContext)
        val repository = CityRepository(database.cityDao())
        val favoriteStore = FavoriteStore(applicationContext)
        val factory = CityViewModelFactory(repository, favoriteStore)

        setContent {
            CityAppTheme {
                CityAppNavigation(factory)
            }
        }
    }
}

@Composable
fun CityAppNavigation(factory: CityViewModelFactory) {
    val navController = rememberNavController()
    val viewModel: CityViewModel = viewModel(factory = factory)

    NavHost(navController = navController, startDestination = "city_list") {
        composable("city_list") {
            CityListScreen(
                viewModel = viewModel,
                onAddCity = { navController.navigate("add_edit_city") },
                onEditCity = { cityId -> navController.navigate("add_edit_city?cityId=$cityId") }
            )
        }
        composable(
            route = "add_edit_city?cityId={cityId}",
            arguments = listOf(
                navArgument("cityId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val cityId = backStackEntry.arguments?.getString("cityId")?.toIntOrNull()
            AddEditCityScreen(
                cityId = cityId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
