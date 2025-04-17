package dev.equestria.luna

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class DataViewModel @Inject constructor(
    private val repository: DataStoreRepository
) : ViewModel() {
    fun getToken(): String? = runBlocking {
        repository.getString("token")
    }

    fun saveToken(token: String) {
        viewModelScope.launch {
            repository.putString("token", token)
        }
    }

    fun clearToken() {
        viewModelScope.launch {
            repository.deleteString("token")
        }
    }
}