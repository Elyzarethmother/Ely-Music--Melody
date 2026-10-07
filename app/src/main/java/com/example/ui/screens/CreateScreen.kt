package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.OrchestralPiece
import com.example.model.StylePresets
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateScreen(
    viewModel: MusicStudioViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val customTitle by viewModel.customTitleInput.collectAsState()
    val customLyrics by viewModel.customLyricsInput.collectAsState()
    val prompt by viewModel.promptInput.collectAsState()
    val tempo by viewModel.tempoBpm.collectAsState()
    val key by viewModel.keySignature.collectAsState()
    val mood by viewModel.emotionalMood.collectAsState()
    val selectedPreset by viewModel.selectedPreset.collectAsState()
    val vocalStyle by viewModel.vocalStyle.collectAsState()
    val isInstrumental by viewModel.isInstrumental.collectAsState()

    val generationState by viewModel.generationState.collectAsState()
    val currentPiece by viewModel.currentPiece.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentTimeMs by viewModel.currentTimeMs.collectAsState()
    val totalTimeMs by viewModel.totalTimeMs.collectAsState()
    val amplitudes by viewModel.visualizerAmplitudes.collectAsState()
    val exportStatus by viewModel.exportStatusMessage.collectAsState()
    val pieces by viewModel.pieces.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))

            // App Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(GoldPrimary, RoyalViolet)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Symphonia Studio",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = "Generate Full Audio (Lyrics + Music MP3)",
                            style = MaterialTheme.typography.labelSmall.copy(color = GoldLight)
                        )
                    }
                }
            }
        }

        // ==========================================
        // STEP 1: INPUT LYRICS (VOCAL AUDIO)
        // ==========================================
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("lyrics_input_card"),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "1. Input Song Lyrics",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                        }

                        // Character / line count badge
                        Text(
                            text = "${customLyrics.lines().filter { it.isNotBlank() }.size} lines",
                            fontSize = 11.sp,
                            color = GoldLight
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Genre Lyric Starters
                    Text(
                        text = "Quick Lyric Starters:",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "Country Soul",
                            "Pop Ballad",
                            "R&B Soul",
                            "Latin Pop",
                            "Reggaeton",
                            "Dance / K-Pop",
                            "Cinematic Anthem",
                            "Acoustic Folk",
                            "Clear"
                        ).forEach { genre ->
                            FilterChip(
                                selected = false,
                                onClick = { viewModel.loadLyricPreset(genre) },
                                label = { Text(genre, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = DarkSurfaceVariant,
                                    labelColor = if (genre == "Clear") TextMuted else GoldLight
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Title Field
                    OutlinedTextField(
                        value = customTitle,
                        onValueChange = { viewModel.customTitleInput.value = it },
                        label = { Text("Song Title", fontSize = 12.sp) },
                        placeholder = { Text("e.g. Midnight River Harmony", color = TextMuted) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_title_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = DarkSurfaceHighlight,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = GoldPrimary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Lyrics Text Area
                    OutlinedTextField(
                        value = customLyrics,
                        onValueChange = { viewModel.customLyricsInput.value = it },
                        placeholder = {
                            Text(
                                "Enter or paste your lyrics here...\n\n[Verse 1]\nDust on the highway, twilight falling slow...\n\n[Chorus]\nHold me in the harmony, singing through the night...",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("lyrics_input_field"),
                        minLines = 6,
                        maxLines = 10,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = DarkSurfaceHighlight,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = GoldPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick Structure Tags
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Add Tag:",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        listOf("[Verse]", "[Chorus]", "[Bridge]", "[Outro]").forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(DarkSurfaceVariant)
                                    .clickable { viewModel.insertSectionTag(tag) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = tag,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = GoldPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Vocal Style Selector
                    Text(
                        text = "Vocal Audio Performance:",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "Duet (Call & Response)",
                            "Solo Lead Singer",
                            "Epic Choir"
                        ).forEach { vStyle ->
                            val isSel = vocalStyle == vStyle
                            FilterChip(
                                selected = isSel,
                                onClick = { viewModel.vocalStyle.value = vStyle },
                                label = { Text(vStyle, fontSize = 11.sp) },
                                leadingIcon = if (isSel) {
                                    {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GoldPrimary,
                                    selectedLabelColor = Color(0xFF110C1C),
                                    containerColor = DarkSurfaceVariant,
                                    labelColor = TextSecondary
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // STEP 2: CHOOSE MUSICAL STYLE
        // ==========================================
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("style_input_card"),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "2. Choose Musical Style",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Select Musical Genre & Arrangement:",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // Style Presets (Horizontal / Flow)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        StylePresets.ALL.take(8).forEach { preset ->
                            val isSelected = selectedPreset.id == preset.id
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.applyPreset(preset) },
                                label = {
                                    Text(
                                        text = "${preset.title} (${preset.tempoBpm} BPM)",
                                        fontSize = 11.sp
                                    )
                                },
                                leadingIcon = if (isSelected) {
                                    {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GoldPrimary,
                                    selectedLabelColor = Color(0xFF110C1C),
                                    containerColor = DarkSurfaceVariant,
                                    labelColor = TextSecondary
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Style Prompt / Description
                    OutlinedTextField(
                        value = prompt,
                        onValueChange = { viewModel.promptInput.value = it },
                        label = { Text("Style & Instrumentation Description", fontSize = 12.sp) },
                        placeholder = {
                            Text(
                                "e.g. fingerpicked acoustic guitar, warm electric piano, rounded bass, brushed drums",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("style_prompt_field"),
                        minLines = 2,
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = DarkSurfaceHighlight,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = GoldPrimary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Tempo Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tempo:",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                        )
                        Text(
                            text = "$tempo BPM",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldLight
                        )
                    }

                    Slider(
                        value = tempo.toFloat(),
                        onValueChange = { viewModel.tempoBpm.value = it.toInt() },
                        valueRange = 60f..180f,
                        colors = SliderDefaults.colors(
                            thumbColor = GoldPrimary,
                            activeTrackColor = GoldPrimary,
                            inactiveTrackColor = DarkSurfaceHighlight
                        )
                    )

                    // Key Signature & Mood Quick Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Key: $key",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "Mood: $mood",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // ==========================================
        // STEP 3: GENERATE BUTTON
        // ==========================================
        item {
            val isGenerating = generationState.isGenerating

            Button(
                onClick = { viewModel.generatePieces() },
                enabled = !isGenerating,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("generate_song_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPrimary,
                    contentColor = Color(0xFF110C1C)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color(0xFF110C1C),
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Generating Audio + Music...",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "GENERATE SONG (AUDIO + MUSIC MP3)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            if (isGenerating) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = generationState.statusText.ifBlank { "Synthesizing vocal audio & musical arrangement in $mood..." },
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = GoldLight,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // ==========================================
        // STEP 4: FINAL RESULT — AUDIO + MUSIC MP3
        // ==========================================
        val activePiece = currentPiece
        if (activePiece != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("generated_song_result_card"),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(
                            listOf(GoldPrimary, RoyalViolet, GoldPrimary)
                        )
                    ),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = activePiece.title,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${activePiece.styleDescription} • ${activePiece.tempoBpm} BPM",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Audio Format Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(GoldPrimary.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "MP3 AUDIO",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldLight
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Waveform Visualizer
                        val progress = if (totalTimeMs > 0) currentTimeMs.toFloat() / totalTimeMs else 0f
                        OrchestralWaveformView(
                            amplitudes = amplitudes,
                            progress = progress,
                            isPlaying = isPlaying,
                            modifier = Modifier.fillMaxWidth(),
                            onSeekFraction = { fraction ->
                                viewModel.seekTo((fraction * totalTimeMs).toLong())
                            }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Player Controls: Play/Pause and Timeline
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val currentSec = (currentTimeMs / 1000).toInt()
                            val totalSec = (totalTimeMs / 1000).toInt()
                            Text(
                                text = "%d:%02d / %d:%02d".format(
                                    currentSec / 60,
                                    currentSec % 60,
                                    totalSec / 60,
                                    totalSec % 60
                                ),
                                fontSize = 12.sp,
                                color = GoldLight,
                                fontWeight = FontWeight.SemiBold
                            )

                            // Big Play / Pause button
                            IconButton(
                                onClick = { viewModel.togglePlayPause() },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(GoldPrimary)
                                    .testTag("play_pause_song_button")
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "Pause" else "Play",
                                    tint = Color(0xFF110C1C),
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Text(
                                text = "Vocals + Music",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Synchronized Karaoke Lyrics Display
                        if (activePiece.lyrics.isNotBlank()) {
                            Text(
                                text = "Synchronized Lyrics:",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextSecondary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(DarkSurfaceVariant)
                                    .padding(12.dp)
                            ) {
                                val lyricLines = activePiece.lyrics.lines().filter { it.isNotBlank() }
                                val activeLineIndex = if (totalTimeMs > 0 && lyricLines.isNotEmpty()) {
                                    ((currentTimeMs.toFloat() / totalTimeMs) * lyricLines.size).toInt().coerceIn(0, lyricLines.lastIndex)
                                } else 0

                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    lyricLines.forEachIndexed { idx, line ->
                                        val isCurrent = idx == activeLineIndex && isPlaying
                                        Text(
                                            text = line,
                                            fontSize = if (isCurrent) 13.sp else 12.sp,
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isCurrent) GoldPrimary else if (line.startsWith("[")) GoldLight.copy(alpha = 0.7f) else TextSecondary
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                        }

                        // ==========================================
                        // DOWNLOAD & SHARE MP3 / AUDIO BUTTONS
                        // ==========================================
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Download / Save MP3 button
                            Button(
                                onClick = { viewModel.saveCurrentSongToMusicFolder(context, asMp3 = true) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("download_mp3_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GoldPrimary,
                                    contentColor = Color(0xFF110C1C)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Save MP3", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }

                            // Download / Save Studio WAV button
                            OutlinedButton(
                                onClick = { viewModel.saveCurrentSongToMusicFolder(context, asMp3 = false) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("download_wav_button"),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = GoldLight
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = GoldLight
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Save WAV", fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                            }

                            // Share MP3 button
                            OutlinedButton(
                                onClick = { viewModel.shareCurrentSong(context) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("share_mp3_button"),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = TextPrimary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Share", fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                            }
                        }

                        if (!exportStatus.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = exportStatus ?: "",
                                fontSize = 11.sp,
                                color = GoldLight,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // PREVIOUS SONGS IN LIBRARY
        // ==========================================
        if (pieces.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Library Songs (${pieces.size})",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
            }

            items(pieces) { piece ->
                val isThisPlaying = activePiece?.id == piece.id && isPlaying
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.loadPiece(piece, autoPlay = true) }
                        .testTag("piece_item_${piece.id}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (activePiece?.id == piece.id) DarkSurfaceHighlight else DarkSurface
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (activePiece?.id == piece.id) {
                                    viewModel.togglePlayPause()
                                } else {
                                    viewModel.loadPiece(piece, autoPlay = true)
                                }
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(GoldPrimary)
                        ) {
                            Icon(
                                imageVector = if (isThisPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color(0xFF110C1C),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = piece.title,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${piece.styleDescription} • ${piece.tempoBpm} BPM",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        IconButton(
                            onClick = {
                                viewModel.loadPiece(piece, autoPlay = false)
                                viewModel.shareCurrentSong(context)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = GoldLight,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
