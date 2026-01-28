package com.example.cityapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.cityapp.model.City
import com.example.cityapp.repository.CityRepository
import com.example.cityapp.store.FavoriteStore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CityUiState(
    val cities: List<City> = emptyList(),
    val favorites: Set<String> = emptySet(),
    val searchQuery: String = "",
    val minPopulation: Int = 0,
    val sortBy: SortOption = SortOption.NAME
)

enum class SortOption { NAME, POPULATION }

class CityViewModel(
    private val repository: CityRepository,
    private val favoriteStore: FavoriteStore
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _minPopulation = MutableStateFlow(0)
    private val _sortBy = MutableStateFlow(SortOption.NAME)

    val uiState: StateFlow<CityUiState> = combine(
        repository.getAllCities(),
        favoriteStore.favorites,
        _searchQuery,
        _minPopulation,
        _sortBy
    ) { cities, favorites, query, minPop, sort ->
        val filtered = cities
            .filter { it.name.contains(query, ignoreCase = true) }
            .filter { it.population >= minPop }
            .let { list ->
                when (sort) {
                    SortOption.NAME -> list.sortedBy { it.name }
                    SortOption.POPULATION -> list.sortedByDescending { it.population }
                }
            }
        CityUiState(filtered, favorites, query, minPop, sort)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CityUiState())

    fun updateSearchQuery(query: String) { _searchQuery.value = query }
    fun updateMinPopulation(min: Int) { _minPopulation.value = min }
    fun updateSortOption(option: SortOption) { _sortBy.value = option }

    fun addCity(name: String, population: Int, country: String) {
        viewModelScope.launch {
            repository.insertCity(City(name = name, population = population, country = country))
        }
    }

    fun updateCity(city: City) {
        viewModelScope.launch { repository.updateCity(city) }
    }

    fun deleteCity(city: City) {
        viewModelScope.launch { repository.deleteCity(city) }
    }

    fun toggleFavorite(cityId: Int) {
        viewModelScope.launch { favoriteStore.toggleFavorite(cityId) }
    }

    fun resetFilters() {
        _searchQuery.value = ""
        _minPopulation.value = 0
        _sortBy.value = SortOption.NAME
    }

    suspend fun getCityById(id: Int): City? = repository.getCityById(id)
}

class CityViewModelFactory(
    private val repository: CityRepository,
    private val favoriteStore: FavoriteStore
) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CityViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CityViewModel(repository, favoriteStore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
