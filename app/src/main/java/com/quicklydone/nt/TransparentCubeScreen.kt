import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TransparentCubeScreen(onBack: () -> Unit = {}) {
    var cube by remember { mutableStateOf(ColoredCubeModel()) }
    var faces by remember { mutableStateOf(cube.getFaces()) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Верхний ряд чекбоксов
        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.padding(bottom = 2.dp)) {
            listOf(
                "front" to "🟥",
                "top" to "⬜",
                "right" to "🟦"
            ).forEach { (side, emoji) ->
                var checked by remember { mutableStateOf(true) }
                Checkbox(
                    checked = checked,
                    onCheckedChange = {
                        checked = it
                        cube.setHidden(side, !it)
                        faces = cube.getFaces()
                    },
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                Text(emoji, fontSize = 22.sp)
            }
        }

// Нижний ряд чекбоксов
        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.padding(bottom = 2.dp)) {
            listOf(
                "left" to "🟩",
                "bottom" to "🟨",
                "back" to "🟧"
            ).forEach { (side, emoji) ->
                var checked by remember { mutableStateOf(true) }
                Checkbox(
                    checked = checked,
                    onCheckedChange = {
                        checked = it
                        cube.setHidden(side, !it)
                        faces = cube.getFaces()
                    },
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                Text(emoji, fontSize = 22.sp)
            }
        }

        // Куб
        // Куб
        Canvas(modifier = Modifier.size(500.dp)) {  // было 400.dp, стало 800.dp
            val cx = size.width / 2
            val cy = size.height / 2
            val cell = 160f  // было 80f, стало 160f
            val cos30 = 0.866f
            val sin30 = 0.5f

            val drawOrder = listOf("back", "left", "bottom", "top", "right", "front")
            drawOrder.forEach { side ->
                if (!cube.isHidden(side)) {
                    faces.filter { it.side == side }.forEach { face ->
                        // Градиент прозрачности для передних граней
                        val fill = if (face.isFront) {
                            Brush.radialGradient(
                                colors = listOf(
                                    face.color.copy(alpha = 0.0f),
                                    face.color.copy(alpha = 0.9f),
                                    face.color.copy(alpha = 1.0f)
                                ),
                                center = face.pathCenter(cell, cx, cy, cos30, sin30),
                                radius = cell
                            )
                        } else {
                            SolidColor(face.color.copy(alpha = 0.9f))
                        }
                        drawPath(face.path(cell, cx, cy, cos30, sin30), fill)
                        drawPath(
                            face.path(cell, cx, cy, cos30, sin30),
                            Color.Black,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                        )
                    }
                }
            }
        }


        //Spacer(Modifier.height(16.dp))

        // Кнопка "Back"
        Button(onClick = onBack) {
            Text("Back")
        }
        //Spacer(Modifier.height(16.dp))
    }
}

// ----- данные одной мини-грани -----
data class MiniFace(
    val x: Int, val y: Int, val z: Int,
    val color: Color,
    val side: String,
    val isFront: Boolean
) {
    fun path(cell: Float, cx: Float, cy: Float, cos30: Float, sin30: Float): Path {
        fun iso(x: Float, y: Float, z: Float) =
            Offset(cx + (x - z) * cos30 * cell, cy + (x + z) * sin30 * cell - y * cell)

        return when (side) {
            "front" -> makePath(
                iso(x.toFloat(), y.toFloat(), 2f),
                iso((x + 1).toFloat(), y.toFloat(), 2f),
                iso((x + 1).toFloat(), (y + 1).toFloat(), 2f),
                iso(x.toFloat(), (y + 1).toFloat(), 2f)
            )
            "back" -> makePath(
                iso(x.toFloat(), y.toFloat(), 0f),
                iso((x + 1).toFloat(), y.toFloat(), 0f),
                iso((x + 1).toFloat(), (y + 1).toFloat(), 0f),
                iso(x.toFloat(), (y + 1).toFloat(), 0f)
            )
            "left" -> makePath(
                iso(0f, y.toFloat(), z.toFloat()),
                iso(0f, y.toFloat(), (z + 1).toFloat()),
                iso(0f, (y + 1).toFloat(), (z + 1).toFloat()),
                iso(0f, (y + 1).toFloat(), z.toFloat())
            )
            "right" -> makePath(
                iso(2f, y.toFloat(), z.toFloat()),
                iso(2f, y.toFloat(), (z + 1).toFloat()),
                iso(2f, (y + 1).toFloat(), (z + 1).toFloat()),
                iso(2f, (y + 1).toFloat(), z.toFloat())
            )
            "top" -> makePath(
                iso(x.toFloat(), 2f, z.toFloat()),
                iso((x + 1).toFloat(), 2f, z.toFloat()),
                iso((x + 1).toFloat(), 2f, (z + 1).toFloat()),
                iso(x.toFloat(), 2f, (z + 1).toFloat())
            )
            "bottom" -> makePath(
                iso(x.toFloat(), 0f, z.toFloat()),
                iso((x + 1).toFloat(), 0f, z.toFloat()),
                iso((x + 1).toFloat(), 0f, (z + 1).toFloat()),
                iso(x.toFloat(), 0f, (z + 1).toFloat())
            )
            else -> Path()
        }
    }

    private fun makePath(tl: Offset, tr: Offset, br: Offset, bl: Offset) = Path().apply {
        moveTo(tl.x, tl.y)
        lineTo(tr.x, tr.y)
        lineTo(br.x, br.y)
        lineTo(bl.x, bl.y)
        close()
    }

    // Исправленная функция для центра грани
    fun pathCenter(cell: Float, cx: Float, cy: Float, cos30: Float, sin30: Float): Offset {
        val p = path(cell, cx, cy, cos30, sin30)
        val bounds = p.getBounds()
        return bounds.center // <-- Используем center вместо centerX/centerY
    }
}



// ----- модель куба -----
class ColoredCubeModel {
    private var faces = mutableListOf<MiniFace>()
    private val hiddenSides = mutableSetOf<String>()

    init {
        generateCube()
    }

    private fun generateCube() {
        val sides = listOf(
            "front" to Color.Red,                        // 🟥
            "back" to Color(0xFFFF8400),          // 🟧 Orange
            "top" to Color.White,                        // ⬜
            "bottom" to Color.Yellow,                    // 🟨
            "left" to Color.Green,                       // 🟩
            "right" to Color.Blue                        // 🟦
        )
        faces.clear()

        for ((side, color) in sides) {
            val isFront = side in listOf("front", "right", "top")
            for (i in 0..1) for (j in 0..1) {
                val (x, y, z) = when (side) {
                    "front" -> Triple(i, j, 1)
                    "back" -> Triple(i, j, 0)
                    "right" -> Triple(1, j, i)
                    "left" -> Triple(0, j, i)
                    "top" -> Triple(i, 1, j)
                    "bottom" -> Triple(i, 0, j)
                    else -> Triple(0, 0, 0)
                }
                faces += MiniFace(x, y, z, color, side, isFront)
            }
        }
    }

    fun setHidden(sideName: String, hidden: Boolean) {
        if (hidden) hiddenSides.add(sideName) else hiddenSides.remove(sideName)
    }

    fun isHidden(sideName: String) = hiddenSides.contains(sideName)

    fun getFaces() = faces
}
