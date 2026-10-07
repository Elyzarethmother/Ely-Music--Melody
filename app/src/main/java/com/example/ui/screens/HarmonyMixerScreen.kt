package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.SwitchAccessShortcut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.InstrumentType
import com.example.audio.TrackLayer
import com.example.model.HarmonicStyle
import com.example.ui.components.PianoRollView
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
fun HarmonyMixerScreen(
    viewModel: MusicStudioViewModel,
    modifier: Modifier = Modifier
) {
    val currentPiece by viewModel.currentPiece.collectAsState()
    val currentTimeMs by viewModel.currentTimeMs.collectAsState()
    val totalTimeMs by viewModel.totalTimeMs.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val selectedHarmonicStyle by viewModel.selectedHarmonicStyle.collectAsState()
    val useContraryMotion by viewModel.useContraryMotion.collectAsState()
    val useSuspensions by viewModel.useSuspensions.collectAsState()
    val useHarpArpeggios by viewModel.useHarpArpeggios.collectAsState()
    val isHarmonizing by viewModel.isHarmonizing.collectAsState()

    val piece = currentPiece

    if (piece == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(DarkBackground),
            contentAlignment = Alignment.Center
        ) {
            Text("Please load or compose a piece first", color = TextSecondary)
        }
        return
    }

    val secondsPerBeat = 60.0f / piece.tempoBpm
    val currentBeat = if (secondsPerBeat > 0) (currentTimeMs / 1000f) / secondsPerBeat else 0f
    val currentBarIndex = (currentBeat / 4f).toInt().coerceIn(0, 7)

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
                IconButton(onClick = { viewModel.setTab(StudioTab.PLAYER) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Player",
                        tint = TextPrimary
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Sophisticated Harmony Engine",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = "${piece.title} • Classical & Romantic Voicings",
                        style = MaterialTheme.typography.labelSmall.copy(color = GoldLight)
                    )
                }

                IconButton(onClick = { viewModel.togglePlayPause() }) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.GraphicEq else Icons.Default.Hearing,
                        contentDescription = "Play/Audition",
                        tint = GoldPrimary
                    )
                }
            }
        }

        // Classical & Romantic Progression Selector Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Harmonic Style & Progression",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                        }

                        Text(
                            text = piece.keySignature,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldLight
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Harmonic Style Carousel
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        HarmonicStyle.values().forEach { style ->
                            val isSelected = selectedHarmonicStyle == style
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) GoldPrimary.copy(alpha = 0.18f) else DarkSurfaceVariant)
                                    .border(
                                        width = if (isSelected) 1.5.dp else 0.5.dp,
                                        color = if (isSelected) GoldPrimary else DarkSurfaceHighlight,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { viewModel.selectedHarmonicStyle.value = style }
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = style.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (isSelected) GoldLight else TextPrimary
                                        )
                                        Text(
                                            text = style.era,
                                            fontSize = 10.sp,
                                            color = RoyalViolet,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = style.description,
                                        fontSize = 11.sp,
                                        color = TextSecondary,
                                        lineHeight = 15.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Progression: ${style.characteristicProgressions}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = GoldPrimary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Voice Leading Options Toggles
                    Text(
                        text = "Orchestral Voice Leading Rules:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Contrary Motion Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Contrary Motion Counterpoint", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text("Violins move contrary to melody to avoid parallel octaves", color = TextMuted, fontSize = 10.sp)
                        }
                        Switch(
                            checked = useContraryMotion,
                            onCheckedChange = { viewModel.useContraryMotion.value = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = GoldPrimary,
                                checkedTrackColor = GoldPrimary.copy(alpha = 0.3f)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Suspensions Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Classical Suspensions (4-3 & 7-6)", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text("Hold dissonant 4th across barline before step resolution", color = TextMuted, fontSize = 10.sp)
                        }
                        Switch(
                            checked = useSuspensions,
                            onCheckedChange = { viewModel.useSuspensions.value = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = GoldPrimary,
                                checkedTrackColor = GoldPrimary.copy(alpha = 0.3f)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Harp Arpeggios Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Concert Harp Arpeggiations", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text("Add rippling 8th/16th-note harp sweeps through chords", color = TextMuted, fontSize = 10.sp)
                        }
                        Switch(
                            checked = useHarpArpeggios,
                            onCheckedChange = { viewModel.useHarpArpeggios.value = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = GoldPrimary,
                                checkedTrackColor = GoldPrimary.copy(alpha = 0.3f)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Harmonize Button
                    Button(
                        onClick = { viewModel.reHarmonizePiece(selectedHarmonicStyle) },
                        enabled = !isHarmonizing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("generate_harmonies_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isHarmonizing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color(0xFF13101E),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Voicing Counterpoint & Chords...", color = Color(0xFF13101E), fontWeight = FontWeight.Bold)
                        } else {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFF13101E),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Harmonize Melody with ${selectedHarmonicStyle.title}",
                                color = Color(0xFF13101E),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // Roman Numeral Chord Progression Bar
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Harmonic Progression Timeline (8 Bars)",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = "Style: ${piece.harmonicStyleName}",
                            fontSize = 11.sp,
                            color = GoldLight
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val chords = piece.chordProgression
                        for (bar in 0 until 8) {
                            val chordLabel = chords.getOrNull(bar) ?: "i (Tonic)"
                            val isCurrentBar = currentBarIndex == bar && isPlaying

                            Box(
                                modifier = Modifier
                                    .width(96.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isCurrentBar) GoldPrimary else DarkSurfaceVariant)
                                    .border(
                                        width = if (isCurrentBar) 1.5.dp else 0.5.dp,
                                        color = if (isCurrentBar) GoldLight else DarkSurfaceHighlight,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(vertical = 8.dp, horizontal = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Bar ${bar + 1}",
                                        fontSize = 10.sp,
                                        color = if (isCurrentBar) Color(0xFF13101E) else TextMuted,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = chordLabel,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCurrentBar) Color(0xFF13101E) else GoldLight,
                                        textAlign = TextAlign.Center,
                                        maxLines = 2
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Piano Roll / Score Grid
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Orchestral Score & Piano Roll",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = "Tap note to audition pitch",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    PianoRollView(
                        tracks = piece.tracks,
                        totalBeats = piece.totalBeats,
                        currentBeat = currentBeat,
                        onNoteTapped = { midi, inst ->
                            viewModel.playPreviewNote(midi, inst)
                        }
                    )
                }
            }
        }

        // Stem Channel Faders Header
        item {
            Text(
                text = "Stem Layer Console (Mixer)",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
        }

        items(piece.tracks) { track ->
            TrackChannelFaderCard(
                track = track,
                onVolumeChanged = { viewModel.updateTrackVolume(track.id, it) },
                onToggleMute = { viewModel.toggleTrackMute(track.id) },
                onToggleSolo = { viewModel.toggleTrackSolo(track.id) },
                onInstrumentChanged = { viewModel.changeTrackInstrument(track.id, it) },
                onAuditionNote = { viewModel.playPreviewNote(64, track.instrument) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun TrackChannelFaderCard(
    track: TrackLayer,
    onVolumeChanged: (Float) -> Unit,
    onToggleMute: () -> Unit,
    onToggleSolo: () -> Unit,
    onInstrumentChanged: (InstrumentType) -> Unit,
    onAuditionNote: () -> Unit
) {
    var instrumentMenuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("track_fader_${track.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (track.isMuted) DarkSurfaceVariant.copy(alpha = 0.5f) else DarkSurface
        ),
        shape = RoundedCornerShape(14.dp),
        border = if (track.isSolo) androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary) else null
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Track Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(track.type.color)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = track.name,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (track.isMuted) TextMuted else TextPrimary
                        )
                    )
                }

                // Instrument Selector Dropdown
                Box {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceVariant)
                            .clickable { instrumentMenuExpanded = true }
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = track.instrument.displayName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GoldLight
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = GoldLight,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = instrumentMenuExpanded,
                        onDismissRequest = { instrumentMenuExpanded = false },
                        modifier = Modifier.background(DarkSurface)
                    ) {
                        InstrumentType.values().forEach { inst ->
                            DropdownMenuItem(
                                text = { Text(inst.displayName, color = TextPrimary) },
                                onClick = {
                                    instrumentMenuExpanded = false
                                    onInstrumentChanged(inst)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Fader Controls: Mute, Solo, Volume Slider, Audition
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Solo Button
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (track.isSolo) GoldPrimary else DarkSurfaceVariant)
                        .clickable { onToggleSolo() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "S",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (track.isSolo) Color(0xFF13101E) else TextSecondary
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Mute Button
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (track.isMuted) Color(0xFFE11D48) else DarkSurfaceVariant)
                        .clickable { onToggleMute() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "M",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (track.isMuted) Color.White else TextSecondary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Volume Slider
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Level",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                        Text(
                            text = "${(track.volume * 100).toInt()}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (track.isMuted) TextMuted else GoldLight
                        )
                    }
                    Slider(
                        value = track.volume,
                        onValueChange = onVolumeChanged,
                        valueRange = 0f..1f,
                        enabled = !track.isMuted,
                        colors = SliderDefaults.colors(
                            thumbColor = track.type.color,
                            activeTrackColor = track.type.color,
                            inactiveTrackColor = DarkSurfaceHighlight
                        )
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Audition Preview Button
                IconButton(
                    onClick = onAuditionNote,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(DarkSurfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.Hearing,
                        contentDescription = "Audition Note",
                        tint = GoldPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
