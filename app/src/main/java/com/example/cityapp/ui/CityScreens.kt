package com.example.cityapp.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.cityapp.data.PaysList
import com.example.cityapp.model.City

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityListScreen(
    viewModel: CityViewModel,
    onAddCity: () -> Unit,
    onEditCity: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFilterDialog by remember { mutableStateOf(false) }
    var cityToDelete by remember { mutableStateOf<City?>(null) }

    val sortedCities = remember(uiState.cities, uiState.favorites) {
        uiState.cities.sortedByDescending { uiState.favorites.contains(it.id.toString()) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Villes") },
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filtrer")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddCity) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                label = { Text("Rechercher une ville") },
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
            )

            LazyColumn {
                items(sortedCities) { city ->
                    CityItem(
                        city = city,
                        isFavorite = uiState.favorites.contains(city.id.toString()),
                        onToggleFavorite = { viewModel.toggleFavorite(city.id) },
                        onDelete = { cityToDelete = city },
                        onClick = { onEditCity(city.id) }
                    )
                }
            }
        }
    }

    if (showFilterDialog) {
        FilterDialog(
            currentMinPop = uiState.minPopulation,
            currentSort = uiState.sortBy,
            onDismiss = { showFilterDialog = false },
            onApply = { minPop, sort ->
                viewModel.updateMinPopulation(minPop)
                viewModel.updateSortOption(sort)
                showFilterDialog = false
            },
            onReset = {
                viewModel.resetFilters()
                showFilterDialog = false
            }
        )
    }

    // Dialog de confirmation de suppression
    cityToDelete?.let { city ->
        DeleteConfirmationDialog(
            cityName = city.name,
            onConfirm = {
                viewModel.deleteCity(city)
                cityToDelete = null
            },
            onDismiss = { cityToDelete = null }
        )
    }
}

@Composable
fun DeleteConfirmationDialog(
    cityName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = { Text("Confirmer la suppression") },
        text = { Text("Êtes-vous sûr de vouloir supprimer la ville \"$cityName\" ? Cette action est irréversible.") },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Supprimer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@Composable
fun CityItem(
    city: City,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp).clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = city.name, style = MaterialTheme.typography.titleLarge)
                Text(text = "${city.country} - ${city.population} hab.", style = MaterialTheme.typography.bodyMedium)
            }
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "Favori",
                    tint = if (isFavorite) Color.Yellow else Color.Gray
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Supprimer")
            }
        }
    }
}

@Composable
fun FilterDialog(
    currentMinPop: Int,
    currentSort: SortOption,
    onDismiss: () -> Unit,
    onApply: (Int, SortOption) -> Unit,
    onReset: () -> Unit
) {
    var minPop by remember { mutableStateOf(currentMinPop.toString()) }
    var sortOption by remember { mutableStateOf(currentSort) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filtres et Tri") },
        text = {
            Column {
                OutlinedTextField(
                    value = minPop,
                    onValueChange = { minPop = it },
                    label = { Text("Population min") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Trier par :")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = sortOption == SortOption.NAME, onClick = { sortOption = SortOption.NAME })
                    Text("Nom")
                    Spacer(modifier = Modifier.width(8.dp))
                    RadioButton(selected = sortOption == SortOption.POPULATION, onClick = { sortOption = SortOption.POPULATION })
                    Text("Population")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onApply(minPop.toIntOrNull() ?: 0, sortOption) }) {
                Text("Appliquer")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onReset) {
                    Text("Réinitialiser", color = MaterialTheme.colorScheme.error)
                }
                TextButton(onClick = onDismiss) {
                    Text("Annuler")
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCityScreen(
    cityId: Int?,
    viewModel: CityViewModel,
    onNavigateBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var population by remember { mutableStateOf("") }
    var selectedCountry by remember { mutableStateOf("") }
    var countryDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(cityId) {
        if (cityId != null) {
            val city = viewModel.getCityById(cityId)
            city?.let {
                name = it.name
                population = it.population.toString()
                selectedCountry = it.country
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (cityId == null) "Ajouter une ville" else "Modifier la ville") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nom") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Liste déroulante des pays
            ExposedDropdownMenuBox(
                expanded = countryDropdownExpanded,
                onExpandedChange = { countryDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedCountry,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Pays") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = countryDropdownExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )

                ExposedDropdownMenu(
                    expanded = countryDropdownExpanded,
                    onDismissRequest = { countryDropdownExpanded = false }
                ) {
                    PaysList.Pays.forEach { country ->
                        DropdownMenuItem(
                            text = { Text(country) },
                            onClick = {
                                selectedCountry = country
                                countryDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = population,
                onValueChange = { population = it },
                label = { Text("Population") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (name.isNotBlank() && selectedCountry.isNotBlank()) {
                        if (cityId == null) {
                            viewModel.addCity(name, population.toIntOrNull() ?: 0, selectedCountry)
                        } else {
                            viewModel.updateCity(
                                City(
                                    id = cityId,
                                    name = name,
                                    population = population.toIntOrNull() ?: 0,
                                    country = selectedCountry
                                )
                            )
                        }
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && selectedCountry.isNotBlank()
            ) {
                Text("Enregistrer")
            }
        }
    }
}