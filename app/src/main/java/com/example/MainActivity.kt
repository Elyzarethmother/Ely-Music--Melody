package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.MiniPlayerBar
import com.example.ui.screens.CreateScreen
import com.example.ui.screens.HarmonyMixerScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.LyricComposerScreen
import com.example.ui.screens.StudioPlayerScreen
import com.example.ui.screens.StyleLabScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.SymphoniaTheme
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.viewmodel.MusicStudioViewModel
import com.example.viewmodel.StudioTab

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SymphoniaTheme {
                SymphoniaApp()
            }
        }
    }
}

@Composable
fun SymphoniaApp(
    viewModel: MusicStudioViewModel = viewModel()
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val currentPiece by viewModel.currentPiece.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentTimeMs by viewModel.currentTimeMs.collectAsState()
    val totalTimeMs by viewModel.totalTimeMs.collectAsState()
    val isLooping by viewModel.isLooping.collectAsState()

    // Handle system back navigation
    BackHandler(enabled = currentTab != StudioTab.CREATE) {
        viewModel.setTab(StudioTab.CREATE)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        bottomBar = {
            Column(modifier = Modifier.navigationBarsPadding()) {
                // Persistent Mini Player (visible on tabs other than full Studio Player)
                if (currentPiece != null && currentTab != StudioTab.PLAYER) {
                    MiniPlayerBar(
                        piece = currentPiece,
                        isPlaying = isPlaying,
                        currentTimeMs = currentTimeMs,
                        totalTimeMs = totalTimeMs,
                        isLooping = isLooping,
                        onTogglePlay = { viewModel.togglePlayPause() },
                        onToggleLoop = { viewModel.toggleLoop() },
                        onBarClicked = { viewModel.setTab(StudioTab.PLAYER) }
                    )
                }

                NavigationBar(
                    containerColor = DarkSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("symphonia_navigation_bar")
                ) {
                    val tabs = listOf(
                        StudioTab.CREATE to Icons.Default.AutoAwesome,
                        StudioTab.PLAYER to Icons.Default.GraphicEq,
                        StudioTab.HARMONY to Icons.Default.Layers,
                        StudioTab.LYRICS to Icons.Default.Mic,
                        StudioTab.STYLE to Icons.Default.Tune,
                        StudioTab.LIBRARY to Icons.Default.LibraryMusic
                    )

                    tabs.forEach { (tab, icon) ->
                        val selected = currentTab == tab
                        NavigationBarItem(
                            selected = selected,
                            onClick = { viewModel.setTab(tab) },
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = tab.label,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = tab.label,
                                    fontSize = 10.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = DarkBackground,
                                selectedTextColor = GoldPrimary,
                                indicatorColor = GoldPrimary,
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                StudioTab.CREATE -> CreateScreen(viewModel = viewModel)
                StudioTab.PLAYER -> StudioPlayerScreen(viewModel = viewModel)
                StudioTab.HARMONY -> HarmonyMixerScreen(viewModel = viewModel)
                StudioTab.LYRICS -> LyricComposerScreen(viewModel = viewModel)
                StudioTab.STYLE -> StyleLabScreen(viewModel = viewModel)
                StudioTab.LIBRARY -> LibraryScreen(viewModel = viewModel)
            }
        }
    }
}
