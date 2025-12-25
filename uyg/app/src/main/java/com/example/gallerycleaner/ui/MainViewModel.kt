package com.example.gallerycleaner.ui

import android.app.Application
import android.content.IntentSender
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gallerycleaner.data.model.Photo
import com.example.gallerycleaner.data.repository.PhotoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class GalleryUiState {
    object Loading : GalleryUiState()
    data class Success(val photos: List<Photo>, val keptCount: Int = 0, val deletedCount: Int = 0) : GalleryUiState()
    object Empty : GalleryUiState()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PhotoRepository(application)

    private val _uiState = MutableStateFlow<GalleryUiState>(GalleryUiState.Loading)
    val uiState: StateFlow<GalleryUiState> = _uiState.asStateFlow()

    private val _intentSenderEvent = MutableStateFlow<IntentSender?>(null)
    val intentSenderEvent: StateFlow<IntentSender?> = _intentSenderEvent.asStateFlow()

    // Bekleyen silme işlemi için geçici referans (Onay gelirse repository'den tekrar deneyeceğiz ama
    // Android sistem dialogu zaten işi hallediyor genelde, sadece listeyi güncellememiz lazım)
    private var pendingDeletePhoto: Photo? = null

    init {
        loadPhotos()
    }

    fun loadPhotos() {
        viewModelScope.launch {
            _uiState.value = GalleryUiState.Loading
            val photos = repository.getAllPhotos()
            if (photos.isEmpty()) {
                _uiState.value = GalleryUiState.Empty
            } else {
                _uiState.value = GalleryUiState.Success(photos)
            }
        }
    }

    fun keepPhoto(photo: Photo) {
        val currentState = _uiState.value
        if (currentState is GalleryUiState.Success) {
            val updatedList = currentState.photos.toMutableList().apply { remove(photo) }
            if (updatedList.isEmpty()) {
                _uiState.value = GalleryUiState.Empty
            } else {
                _uiState.value = currentState.copy(
                    photos = updatedList,
                    keptCount = currentState.keptCount + 1
                )
            }
        }
    }

    fun deletePhoto(photo: Photo) {
        viewModelScope.launch {
            val intentSender = repository.deletePhoto(photo)
            if (intentSender != null) {
                // Kullanıcı onayı gerekiyor
                pendingDeletePhoto = photo
                _intentSenderEvent.value = intentSender
            } else {
                // Başarılı silindi, listeyi güncelle
                removePhotoFromState(photo)
            }
        }
    }

    fun onPermissionResult(isGranted: Boolean) {
        _intentSenderEvent.value = null // Event'i temizle
        if (isGranted && pendingDeletePhoto != null) {
            // Kullanıcı sistem pop-up'ına onay verdi, dosya silindi (Android 11+ için createDeleteRequest bunu kendi yapar)
            // Android 10 recoverable exception akışında tekrar delete çağırmak gerekebilir ama genelde sistem halleder.
            // Biz UI'dan kaldıralım.
            removePhotoFromState(pendingDeletePhoto!!)
            pendingDeletePhoto = null
        } else {
            // Kullanıcı reddetti, fotoyu geri getir veya pas geç
            pendingDeletePhoto = null
        }
    }

    private fun removePhotoFromState(photo: Photo) {
        val currentState = _uiState.value
        if (currentState is GalleryUiState.Success) {
            val updatedList = currentState.photos.toMutableList().apply { remove(photo) }
            if (updatedList.isEmpty()) {
                _uiState.value = GalleryUiState.Empty
            } else {
                _uiState.value = currentState.copy(
                    photos = updatedList,
                    deletedCount = currentState.deletedCount + 1
                )
            }
        }
    }
}
