package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.OrchestralWaveformView
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
fun StudioPlayerScreen(
    viewModel: MusicStudioViewModel,
    modifier: Modifier = Modifier
) {
    val currentPiece by viewModel.currentPiece.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentTimeMs by viewModel.currentTimeMs.collectAsState()
    val totalTimeMs by viewModel.totalTimeMs.collectAsState()
    val isLooping by viewModel.isLooping.collectAsState()
    val amplitudes by viewModel.visualizerAmplitudes.collectAsState()

    val piece = currentPiece

    if (piece == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(DarkBackground),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("No piece loaded in studio", color = TextSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.setTab(StudioTab.CREATE) },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Text("Go to Composer", color = Color(0xFF13101E))
                }
            }
        }
        return
    }

    val progress = if (totalTimeMs > 0) (currentTimeMs.toFloat() / totalTimeMs).coerceIn(0f, 1f) else 0f
    val currentSec = (currentTimeMs / 1000).toInt()
    val totalSec = (totalTimeMs / 1000).toInt()
    val timeLabel = "%d:%02d".format(currentSec / 60, currentSec % 60)
    val totalLabel = "%d:%02d".format(totalSec / 60, totalSec % 60)

    // Calculate active musical beat for section highlighting
    val secondsPerBeat = 60.0f / piece.tempoBpm
    val currentBeat = if (secondsPerBeat > 0) (currentTimeMs / 1000f) / secondsPerBeat else 0f

    val activeSection = piece.sections.firstOrNull {
        currentBeat >= it.startBeat && currentBeat < (it.startBeat + it.lengthBeats)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { viewModel.setTab(StudioTab.CREATE) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }

                Text(
                    text = "Orchestral Master Studio",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )

                IconButton(onClick = { viewModel.setTab(StudioTab.HARMONY) }) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = "Harmony Mixer",
                        tint = GoldPrimary
                    )
                }
            }
        }

        // Piece Hero Banner Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Radiant Album Crest
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(piece.accentColorHex),
                                        RoyalViolet,
                                        Color(0xFF130E26)
                                    )
                                )
                            )
                            .border(2.dp, GoldPrimary.copy(alpha = 0.4f), RoundedCornerShape(18.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = piece.title,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(GoldPrimary.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = piece.variationLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldLight
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${piece.emotionalMood} • ${piece.keySignature}",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Waveform visualizer with live animated bars
                    OrchestralWaveformView(
                        amplitudes = amplitudes,
                        progress = progress,
                        isPlaying = isPlaying,
                        modifier = Modifier.fillMaxWidth(),
                        onSeekFraction = { fraction ->
                            viewModel.seekTo((fraction * totalTimeMs).toLong())
                        }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Time labels
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(timeLabel, fontSize = 12.sp, color = GoldLight, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = activeSection?.name ?: "Full Ensemble",
                            fontSize = 12.sp,
                            color = RoyalViolet,
                            fontWeight = FontWeight.Medium
                        )
                        Text(totalLabel, fontSize = 12.sp, color = TextMuted)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Transport Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Rewind to start
                        IconButton(
                            onClick = { viewModel.seekTo(0L) },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FastRewind,
                                contentDescription = "Rewind",
                                tint = TextSecondary,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        // Loop toggle
                        IconButton(
                            onClick = { viewModel.toggleLoop() },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Repeat,
                                contentDescription = "Loop",
                                tint = if (isLooping) GoldPrimary else TextMuted,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Main Play / Pause Button
                        IconButton(
                            onClick = { viewModel.togglePlayPause() },
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(GoldPrimary)
                                .testTag("studio_play_pause_button")
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = Color(0xFF110C1C),
                                modifier = Modifier.size(38.dp)
                            )
                        }

                        // Harmony Mixer Shortcut
                        IconButton(
                            onClick = { viewModel.setTab(StudioTab.HARMONY) },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Harmony Layers",
                                tint = TextSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Edit Lyrics Shortcut
                        IconButton(
                            onClick = { viewModel.setTab(StudioTab.LYRICS) },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Lyrics",
                                tint = TextSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val context = LocalContext.current
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.saveCurrentSongToMusicFolder(context, asMp3 = true) },
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldPrimary,
                                contentColor = Color(0xFF110C1C)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save MP3", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { viewModel.shareCurrentSong(context) },
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = GoldLight
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = GoldLight
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share MP3", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Section Jump Bar (Intro, Verse, Chorus, Outro)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Orchestral Form & Section Markers:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        piece.sections.forEach { section ->
                            val isActive = activeSection?.name == section.name
                            val sectionStartTimeMs = (section.startBeat * secondsPerBeat * 1000).toLong()

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isActive) GoldPrimary else DarkSurfaceVariant)
                                    .clickable { viewModel.seekTo(sectionStartTimeMs) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = section.name,
                                    fontSize = 11.sp,
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isActive) Color(0xFF110C1C) else TextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Karaoke / Synced Poetic Lyrics View
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Orchestral Stanzas & Lyrics",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = GoldLight
                            )
                        )
                        IconButton(
                            onClick = { viewModel.setTab(StudioTab.LYRICS) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Lyrics",
                                tint = GoldPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (piece.isInstrumental) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkSurfaceVariant, RoundedCornerShape(8.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Pure Orchestral Movement\n(No vocal stanzas assigned)",
                                textAlign = TextAlign.Center,
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        val lyricLines = piece.lyrics.lines()
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            lyricLines.forEach { line ->
                                val trimmed = line.trim()
                                if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = trimmed,
                                        color = RoyalViolet,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                } else if (trimmed.isNotBlank()) {
                                    Text(
                                        text = trimmed,
                                        color = TextPrimary,
                                        fontSize = 14.sp,
                                        lineHeight = 20.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Quick Harmony Stems Overview
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Orchestral Stems & Layers",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Button(
                            onClick = { viewModel.setTab(StudioTab.HARMONY) },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Open Mixer", fontSize = 11.sp, color = GoldPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    piece.tracks.forEach { track ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(track.type.color)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = track.name,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (track.isMuted) TextMuted else TextPrimary,
                                    fontWeight = FontWeight.Medium
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = if (track.isMuted) "MUTED" else if (track.isSolo) "SOLO" else "${(track.volume * 100).toInt()}%",
                                fontSize = 11.sp,
                                color = if (track.isMuted) TextMuted else GoldLight
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
