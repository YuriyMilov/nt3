package com.quicklydone.nt

import GhostCycleScreen
import BackToMenuScreen
import GhostCyclePuzzle
import HexagonPuzzleScreen
import TransparentCubeScreen
import MatrixPuzzleScreen
import SixteenScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var currentScreen by remember { mutableStateOf("menu") }

            when (currentScreen) {
                "menu" -> MainMenuScreen { selected ->
                    currentScreen = selected
                }

                "matrix" -> MatrixPuzzleScreen(onBack = { currentScreen = "menu" })
                "rotation" -> TransparentCubeScreen(onBack = { currentScreen = "menu" })
                "lightsOut" -> HexagonPuzzleScreen(onBack = { currentScreen = "menu" })
                "111" -> GhostCycleScreen(onBack = { currentScreen = "menu" })
                "222" -> SixteenScreen(onBack = { currentScreen = "menu" })
                "333" -> GhostCyclePuzzle(onBack = { currentScreen = "menu" })
            }
        }
    }

    // --- Главное меню ---
    @Composable
    fun MainMenuScreen(onSelectPuzzle: (String) -> Unit) {
        // Создаём объект состояния прокрутки
        val scrollState = rememberScrollState()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .verticalScroll(scrollState) // Делаем колонку прокручиваемой
                    .padding(vertical = 16.dp) // Немного отступов сверху и снизу
            ) {
                Text(
                    "Puzzle Collection",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )

                val puzzles = listOf(
                    //"111" to "puzzle 1",
                    //"222" to "puzzle 16",
                    //"333" to "puzzle 3",
                    "matrix" to "Matrix",
                    "lightsOut" to "Hexagon",
                    "rotation" to "Cube 2×2"


                   // "333" to "Back to Menu",

                )


                // Генерируем кнопку

                Button(onClick = { onSelectPuzzle("111") }) {
                    Image(
                        painter = painterResource(id = R.drawable.goal_icon),
                        contentDescription = null,
                        contentScale = ContentScale.Fit, // Fit, Crop, FillHeight и т.д.
                        modifier = Modifier.size(128.dp)
                    )
                }

                Button(onClick = { onSelectPuzzle("333") }) {
                    Image(
                        painter = painterResource(id = R.drawable.im4),
                        contentDescription = null,
                        contentScale = ContentScale.Fit, // Fit, Crop, FillHeight и т.д.
                        modifier = Modifier.size(128.dp)
                    )
                }
                Button(onClick = { onSelectPuzzle("222") }) {
                    Image(
                        painter = painterResource(id = R.drawable.im16),
                        contentDescription = null,
                        contentScale = ContentScale.Fit, // Fit, Crop, FillHeight и т.д.
                        modifier = Modifier.size(128.dp)
                    )
                }

                // Генерируем кнопки динамически
                for ((key, label) in puzzles) {

                    Button(
                        onClick = { onSelectPuzzle(key) },
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth(0.8f) // Кнопки чуть уже экрана
                    ) {
                        Text(label)
                    }
                }
            }
        }
    }
}