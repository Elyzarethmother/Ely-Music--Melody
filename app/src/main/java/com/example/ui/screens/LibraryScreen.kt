package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.OrchestralPiece
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceHighlight
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.RoyalViolet
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.MusicStudioViewModel
import com.example.viewmodel.StudioTab

@Composable
fun LibraryScreen(
    viewModel: MusicStudioViewModel,
    modifier: Modifier = Modifier
) {
    val pieces by viewModel.pieces.collectAsState()
    val currentPiece by viewModel.currentPiece.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }

    val filteredPieces = pieces.filter { piece ->
        val matchesQuery = piece.title.contains(searchQuery, ignoreCase = true) ||
                piece.emotionalMood.contains(searchQuery, ignoreCase = true) ||
                piece.keySignature.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (selectedFilter) {
            "Favorites" -> piece.isFavorite
            "Instrumental" -> piece.isInstrumental
            "Vocal/Lyrics" -> !piece.isInstrumental
            else -> true
        }
        matchesQuery && matchesFilter
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LibraryMusic,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Orchestral Catalog",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = "${pieces.size} compositions created",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                    )
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by title, mood, or key...", color = TextMuted) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = TextSecondary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("library_search_input"),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = DarkSurfaceHighlight,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Filter Chips
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("All", "Favorites", "Instrumental", "Vocal/Lyrics").forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GoldPrimary.copy(alpha = 0.25f),
                            selectedLabelColor = GoldLight,
                            containerColor = DarkSurfaceVariant,
                            labelColor = TextSecondary
                        )
                    )
                }
            }
        }

        items(filteredPieces) { piece ->
            LibraryPieceItem(
                piece = piece,
                isCurrent = currentPiece?.id == piece.id,
                isPlaying = isPlaying && currentPiece?.id == piece.id,
                onPlayToggle = {
                    if (currentPiece?.id == piece.id) {
                        viewModel.togglePlayPause()
                    } else {
                        viewModel.loadPiece(piece, autoPlay = true)
                    }
                },
                onCardClick = {
                    viewModel.loadPiece(piece, autoPlay = false)
                    viewModel.setTab(StudioTab.PLAYER)
                },
                onToggleFavorite = { viewModel.toggleFavorite(piece.id) },
                onDownloadMp3 = {
                    viewModel.loadPiece(piece, autoPlay = false)
                    viewModel.saveCurrentSongToMusicFolder(context, asMp3 = true)
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun LibraryPieceItem(
    piece: OrchestralPiece,
    isCurrent: Boolean,
    isPlaying: Boolean,
    onPlayToggle: () -> Unit,
    onCardClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDownloadMp3: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent) DarkSurfaceHighlight else DarkSurface
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (isCurrent) androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f)) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Play Button / Thumbnail
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(piece.accentColorHex), RoyalViolet)
                        )
                    )
                    .clickable { onPlayToggle() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = piece.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(GoldPrimary.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = piece.variationLabel,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldLight
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${piece.emotionalMood} • ${piece.keySignature} • ${piece.tempoBpm} BPM",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                Text(
                    text = "${piece.durationFormatted} • ${if (piece.isInstrumental) "Instrumental" else "Vocal/Lyrics"}",
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }

            IconButton(onClick = onDownloadMp3) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Download MP3",
                    tint = GoldLight,
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (piece.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (piece.isFavorite) Color(0xFFFB7185) else TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
