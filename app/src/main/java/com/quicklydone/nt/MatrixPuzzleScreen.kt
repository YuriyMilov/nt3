import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
//import com.quicklydone.nt.Board

@Composable
fun MatrixPuzzleScreen(onBack: () -> Unit = {}) {
    var board by remember { mutableStateOf(Board()) }
    var grid by remember { mutableStateOf(board.getGrid()) }
    var message by remember { mutableStateOf("Goal:\n⬛⬛⬛⬛\n⬛⬜⬜⬛\n⬛⬜⬜⬛\n⬛⬛⬛⬛") }

    fun update() {
        grid = board.getGrid()
        message = if (board.isSolved()) "🎉 Congratulations! 🎉"
        else "Goal:\n⬛⬛⬛⬛\n⬛⬜⬜⬛\n⬛⬜⬜⬛\n⬛⬛⬛⬛"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            // Кнопка назад
            Button(onClick = onBack, modifier = Modifier.padding(4.dp)) {
                Text("Back to Menu")
            }

            // Цель и scramble
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = message,
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(4.dp)
                )
                Button(
                    onClick = { board.scramble(); update() },
                    modifier = Modifier.padding(4.dp)
                ) {
                    Text("Scramble")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Матрица 4x4
            Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                for (r in 0 until 4) {
                    Row {
                        for (c in 0 until 4) {
                            val color = if (grid[r][c]) Color.White else Color.DarkGray
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .padding(2.dp)
                                    .background(color, shape = RoundedCornerShape(6.dp))
                                    .border(1.dp, Color.Gray, RoundedCornerShape(6.dp))
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Кнопки вращения
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(0.6f)
                ) {
                    Button(onClick = { board.rotateTopLeft(); update() }) { Text("🔵↻") }
                    Button(onClick = { board.rotateTopRight(); update() }) { Text("🔴↻") }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(0.6f)
                ) {
                    Button(onClick = { board.rotateBottomLeft(); update() }) { Text("🟢↻") }
                    Button(onClick = { board.rotateBottomRight(); update() }) { Text("🟣↻") }
                }
            }
        }
    }
}
// --- Твоя существующая головоломка ---
class Board(private val n: Int = 4) {
    private val grid = Array(n) { Array(n) { false } }

    init {
        resetSolved()
    }

    fun resetSolved() {
        for (r in 0 until n) for (c in 0 until n) grid[r][c] = false
        // 2x2 белые в центре
        for (r in 1..2) for (c in 1..2) grid[r][c] = true
    }

    private fun rotate3x3(r0: Int, c0: Int) {
        val indices = listOf(
            0 to 0, 0 to 1, 0 to 2,
            1 to 2, 2 to 2, 2 to 1,
            2 to 0, 1 to 0
        )
        val temp = indices.map { (i, j) -> grid[r0 + i][c0 + j] }.toMutableList()
        temp.add(0, temp.removeAt(temp.size - 1))
        for ((k, pos) in indices.withIndex()) {
            val (i, j) = pos
            grid[r0 + i][c0 + j] = temp[k]
        }
    }

    fun rotateTopLeft() = rotate3x3(0, 0)
    fun rotateTopRight() = rotate3x3(0, n - 3)
    fun rotateBottomLeft() = rotate3x3(n - 3, 0)
    fun rotateBottomRight() = rotate3x3(n - 3, n - 3)

    fun isSolved(): Boolean = (1..2).all { r -> (1..2).all { c -> grid[r][c] } }

    fun scramble(moves: Int = 10) {
        val ops =
            listOf(::rotateTopLeft, ::rotateTopRight, ::rotateBottomLeft, ::rotateBottomRight)
        repeat(moves) { ops.random()() }
    }

    fun getGrid(): Array<Array<Boolean>> = Array(n) { r -> Array(n) { c -> grid[r][c] } }
}
