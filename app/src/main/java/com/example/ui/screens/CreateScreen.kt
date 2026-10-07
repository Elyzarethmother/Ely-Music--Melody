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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.InstrumentType
import com.example.model.OrchestralPiece
import com.example.model.StylePresets
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateScreen(
    viewModel: MusicStudioViewModel,
    modifier: Modifier = Modifier
) {
    val prompt by viewModel.promptInput.collectAsState()
    val isCustomMode by viewModel.isCustomMode.collectAsState()
    val customTitle by viewModel.customTitleInput.collectAsState()
    val customLyrics by viewModel.customLyricsInput.collectAsState()
    val isInstrumental by viewModel.isInstrumental.collectAsState()
    val tempo by viewModel.tempoBpm.collectAsState()
    val key by viewModel.keySignature.collectAsState()
    val mood by viewModel.emotionalMood.collectAsState()
    val selectedInstruments by viewModel.selectedInstruments.collectAsState()
    val generationState by viewModel.generationState.collectAsState()
    val currentPiece by viewModel.currentPiece.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val pieces by viewModel.pieces.collectAsState()

    val inspirationPrompts = listOf(
        "Modern Country Soul" to StylePresets.ALL[0],
        "Emotional Pop Ballad" to StylePresets.ALL[1],
        "Contemporary R&B / Soul" to StylePresets.ALL[2],
        "Latin Pop" to StylePresets.ALL[3],
        "Reggaeton Pop" to StylePresets.ALL[4],
        "Global Dance / K-Pop" to StylePresets.ALL[5],
        "Cinematic Emotional" to StylePresets.ALL[6],
        "Acoustic Folk / Indie" to StylePresets.ALL[7],
        "Blues Rock / Southern Soul" to StylePresets.ALL[8],
        "Indian Fusion / Soul" to StylePresets.ALL[9]
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Symphonia Studio",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                    }
                    Text(
                        text = "Compose orchestral masterworks with AI melody & harmony",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                    )
                }

                // Mode switch: Simple vs Custom (like Suno)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(DarkSurfaceVariant)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Custom",
                        fontSize = 12.sp,
                        color = if (isCustomMode) GoldPrimary else TextMuted,
                        fontWeight = if (isCustomMode) FontWeight.Bold else FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Switch(
                        checked = isCustomMode,
                        onCheckedChange = { viewModel.isCustomMode.value = it },
                        modifier = Modifier.testTag("custom_mode_toggle"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = GoldPrimary,
                            checkedTrackColor = GoldPrimary.copy(alpha = 0.3f),
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = DarkSurfaceHighlight
                        )
                    )
                }
            }
        }

        // Prompt Input Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isCustomMode) "Style & Orchestration Prompt" else "Orchestral Vision Prompt",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = GoldLight
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = prompt,
                        onValueChange = { viewModel.promptInput.value = it },
                        placeholder = {
                            Text(
                                "e.g., A soaring brass symphony with swelling strings, thunderous timpani drops, and majestic choir...",
                                color = TextMuted,
                                fontSize = 14.sp
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("prompt_input_field"),
                        minLines = 3,
                        maxLines = 5,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = DarkSurfaceHighlight,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = GoldPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Quick Inspiration Presets:",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        inspirationPrompts.forEach { (label, preset) ->
                            FilterChip(
                                selected = false,
                                onClick = { viewModel.applyPreset(preset) },
                                label = { Text(label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = DarkSurfaceVariant,
                                    labelColor = TextSecondary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }
            }
        }

        // Custom Mode Section (Title, Lyrics, Instrument Toggles, Tempo)
        item {
            AnimatedVisibility(visible = isCustomMode) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Custom Composition Studio",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = RoyalViolet
                            )
                        )

                        // Title Field
                        OutlinedTextField(
                            value = customTitle,
                            onValueChange = { viewModel.customTitleInput.value = it },
                            label = { Text("Movement / Piece Title (Optional)") },
                            placeholder = { Text("e.g. Coronation of the Star King", color = TextMuted) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("custom_title_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RoyalViolet,
                                unfocusedBorderColor = DarkSurfaceHighlight,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )

                        // Instrumental Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Instrumental Only",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary
                                    )
                                )
                                Text(
                                    text = "Pure orchestral symphony without vocals or lyrics",
                                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                                )
                            }
                            Switch(
                                checked = isInstrumental,
                                onCheckedChange = { viewModel.isInstrumental.value = it },
                                modifier = Modifier.testTag("instrumental_toggle"),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = GoldPrimary,
                                    checkedTrackColor = GoldPrimary.copy(alpha = 0.3f)
                                )
                            )
                        }

                        // Lyrics Section (if not instrumental)
                        if (!isInstrumental) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Lyrics / Poetic Stanzas",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = GoldLight
                                        )
                                    )
                                    IconButton(
                                        onClick = { viewModel.requestAiLyricSuggestion() },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = "Suggest Lyrics",
                                            tint = GoldPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                OutlinedTextField(
                                    value = customLyrics,
                                    onValueChange = { viewModel.updateLyrics(it) },
                                    placeholder = {
                                        Text(
                                            "[Verse 1]\nBeyond the misty mountain crest...\n\n[Chorus]\nRaise the spears against the dark!",
                                            color = TextMuted,
                                            fontSize = 13.sp
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("custom_lyrics_input"),
                                    minLines = 4,
                                    maxLines = 6,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = RoyalViolet,
                                        unfocusedBorderColor = DarkSurfaceHighlight,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }

                        // Instrumentation Selection Rack
                        Column {
                            Text(
                                text = "Orchestral Instrument Rack:",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                InstrumentType.values().forEach { inst ->
                                    val isSelected = selectedInstruments.contains(inst)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.toggleInstrumentSelection(inst) },
                                        label = { Text(inst.displayName, fontSize = 11.sp) },
                                        leadingIcon = if (isSelected) {
                                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                        } else null,
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = GoldPrimary.copy(alpha = 0.25f),
                                            selectedLabelColor = GoldLight,
                                            containerColor = DarkSurfaceVariant,
                                            labelColor = TextSecondary
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            }
                        }

                        // Tempo & Mood Row
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Tempo: $tempo BPM", color = TextPrimary, fontSize = 13.sp)
                                Text("Key: $key", color = GoldLight, fontSize = 13.sp)
                            }
                            Slider(
                                value = tempo.toFloat(),
                                onValueChange = { viewModel.tempoBpm.value = it.toInt() },
                                valueRange = 40f..200f,
                                colors = SliderDefaults.colors(
                                    thumbColor = GoldPrimary,
                                    activeTrackColor = GoldPrimary,
                                    inactiveTrackColor = DarkSurfaceHighlight
                                )
                            )
                        }
                    }
                }
            }
        }

        // Generate Master Button (Suno Style)
        item {
            Button(
                onClick = { viewModel.generatePieces() },
                enabled = !generationState.isGenerating,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("generate_pieces_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPrimary,
                    disabledContainerColor = DarkSurfaceHighlight
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (generationState.isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color(0xFF13101E),
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = generationState.statusText.ifBlank { "Composing Masterpiece..." },
                        color = Color(0xFF13101E),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFF13101E),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Compose Orchestral Piece (2 Variations)",
                        color = Color(0xFF13101E),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }

        // Generated Pieces Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Composed Movements",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Text(
                    text = "${pieces.size} pieces",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                )
            }
        }

        items(pieces) { piece ->
            PieceCard(
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
                onOpenStudio = {
                    viewModel.loadPiece(piece, autoPlay = false)
                    viewModel.setTab(StudioTab.PLAYER)
                },
                onToggleFavorite = { viewModel.toggleFavorite(piece.id) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(80.dp)) // Padding for bottom mini-player
        }
    }
}

@Composable
fun PieceCard(
    piece: OrchestralPiece,
    isCurrent: Boolean,
    isPlaying: Boolean,
    onPlayToggle: () -> Unit,
    onOpenStudio: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("piece_card_${piece.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent) DarkSurfaceHighlight else DarkSurface
        ),
        shape = RoundedCornerShape(14.dp),
        border = if (isCurrent) androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f)) else null
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Artwork & Play Button Overlay
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(piece.accentColorHex),
                                    RoyalViolet
                                )
                            )
                        )
                        .clickable { onPlayToggle() },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.45f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Title & Details
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = piece.title,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
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
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldLight
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${piece.emotionalMood} • ${piece.keySignature} • ${piece.tempoBpm} BPM",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary),
                        maxLines = 1
                    )
                    Text(
                        text = "${piece.durationFormatted} • ${if (piece.isInstrumental) "Instrumental" else "Vocal/Lyrics"}",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextMuted),
                        maxLines = 1
                    )
                }

                // Favorite Button
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (piece.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (piece.isFavorite) Color(0xFFFB7185) else TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Row: Open in Full Studio / Harmony / Lyrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onOpenStudio,
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Open in Studio",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}
