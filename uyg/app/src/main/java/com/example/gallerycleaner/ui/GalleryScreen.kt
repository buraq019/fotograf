package com.example.gallerycleaner.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gallerycleaner.ui.components.SwipeablePhotoCard

@Composable
fun GalleryScreen(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // İstatistik Başlığı
        if (uiState is GalleryUiState.Success) {
            val state = uiState as GalleryUiState.Success
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total", fontWeight = FontWeight.Bold)
                    Text("${state.photos.size + state.keptCount + state.deletedCount}")
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Kept", color = Color.Green, fontWeight = FontWeight.Bold)
                    Text("${state.keptCount}")
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Deleted", color = Color.Red, fontWeight = FontWeight.Bold)
                    Text("${state.deletedCount}")
                }
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is GalleryUiState.Loading -> {
                    CircularProgressIndicator()
                }
                is GalleryUiState.Empty -> {
                    Text(
                        "No photos left! Great job.",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
                is GalleryUiState.Success -> {
                    if (state.photos.isEmpty()) {
                        Text("No photos to show.")
                    } else {
                        // Performans için sadece en üstteki 2 kartı çiz (Z-order: En son çizilen en üsttedir)
                        // Listeyi sondan başa doğru alalım ki index 0 en altta kalsın mantığı yerine,
                        // Box içinde sırayla çizildiği için, listenin son elemanı en üstte görünür using standard iteration?
                        // Hayır, Box içinde children sırayla çizilir. item1 çizilir, item2 onun üstüne çizilir.
                        // Yanı listenin SON elemanı en üsttedir.
                        // Swipe yapınca listenin son elemanını (en üsttekini) işlememiz lazım.

                        val visiblePhotos = state.photos.takeLast(2)

                        visiblePhotos.forEachIndexed { index, photo ->
                            // En üstteki kart (listenin sonuncusu) hareket edebilir
                            // Arkadaki kart (listenin sondan bir öncesi) sabit durur veya scale efekti verilebilir
                            val isTopCard = (photo == state.photos.last())
                            
                            key(photo.id) {
                                SwipeablePhotoCard(
                                    photo = photo,
                                    onSwipeLeft = { 
                                         if (isTopCard) viewModel.deletePhoto(photo) 
                                    },
                                    onSwipeRight = { 
                                         if (isTopCard) viewModel.keepPhoto(photo) 
                                    },
                                    modifier = Modifier.fillMaxSize() // Normalde biraz scale küçültülebilir arkadaki için
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
