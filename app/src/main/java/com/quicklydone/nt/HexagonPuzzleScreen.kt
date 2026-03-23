import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.*

@Composable
fun HexagonPuzzleScreen(onBack: () -> Unit = {}) {
    val side = 110f
    val h = sqrt(3f) / 2f * side
    val rows = listOf(5, 7, 7, 5)
    val rowOrder = listOf(0, 2, 1, 3)
    val invertRow = listOf(false, true, true, false)
    val topY = -h * (rows.size - 1) / 2f

    // 6 цветов по 4 треугольника каждого
    val colorA = Color(0xFFD32F2F) // красный
    val colorB = Color(0xFFFFEB3B) // желтый
    val colorC = Color(0xFF4CAF50) // зеленый
    val colorD = Color(0xFF2196F3) // синий
    val colorE = Color(0xFFFF9800) // оранжевый
    val colorF = Color(0xFF9C27B0) // фиолетовый

    // эталонная цель — 6 цветов, симметричная
    val halfPattern = listOf(colorA, colorB, colorC, colorD, colorE, colorF,
        colorF, colorE, colorD, colorC, colorB, colorA)
    val goalColors = remember { (halfPattern + halfPattern.reversed()) }

    data class Triangle(val id: Int, val points: List<Offset>, val color: MutableState<Color>)
    data class Hub(val x: Float, val y: Float, val tris: List<Int>)

    val initialColors = remember { goalColors.shuffled().toMutableList() }

    val triangles = remember {
        val list = mutableStateListOf<Triangle>()
        var id = 0
        for (rIdx in rowOrder.indices) {
            val r = rowOrder[rIdx]
            val count = rows[r]
            val y = topY + rIdx * h
            val startX = -side * (count - 1) / 4f
            for (c in 0 until count) {
                val x = startX + c * (side / 2f)
                val up = ((rIdx + c + if (invertRow[rIdx]) 1 else 0) % 2 == 0)
                val pts = if (up) listOf(
                    Offset(x, y - h / 2f),
                    Offset(x - side / 2f, y + h / 2f),
                    Offset(x + side / 2f, y + h / 2f)
                ) else listOf(
                    Offset(x, y + h / 2f),
                    Offset(x - side / 2f, y - h / 2f),
                    Offset(x + side / 2f, y - h / 2f)
                )
                val colorState = mutableStateOf(initialColors.getOrElse(id) { Color.Gray })
                list.add(Triangle(id, pts, colorState))
                id++
            }
        }
        list
    }

    val hubs = remember {
        val vertexMap = mutableMapOf<Pair<Int, Int>, MutableList<Int>>()
        fun roundKey(x: Float, y: Float) = (x * 100).roundToInt() to (y * 100).roundToInt()
        triangles.forEach { tri -> tri.points.forEach { p -> vertexMap.getOrPut(roundKey(p.x, p.y)) { mutableListOf() }.add(tri.id) } }
        val list = mutableStateListOf<Hub>()
        vertexMap.filter { it.value.size == 6 }.forEach { (key, listIds) ->
            list.add(Hub(key.first / 100f, key.second / 100f, listIds.toList()))
        }
        list
    }

    var canvasSize by remember { mutableStateOf(IntSize(1, 1)) }

    fun scrambleBoard() {
        val shuffled = goalColors.shuffled()
        triangles.forEachIndexed { i, tri -> tri.color.value = shuffled.getOrElse(i) { tri.color.value } }
    }

    fun rotateHubClockwise(hub: Hub) {
        val trisWithAngles = hub.tris.map { id ->
            val tri = triangles.first { it.id == id }
            val cx = tri.points.map { it.x }.average().toFloat()
            val cy = tri.points.map { it.y }.average().toFloat()
            tri to atan2(cy - hub.y, cx - hub.x)
        }.sortedByDescending { it.second }

        val colors = trisWithAngles.map { it.first.color.value }
        val rotated = colors.drop(1) + colors.first()
        trisWithAngles.forEachIndexed { idx, pair -> pair.first.color.value = rotated[idx] }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.TopCenter
    ) {

        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Text(
                text = "Click the black arrows to rotate clockwise. Collect the daisy.",
                color = Color.White,
                modifier = Modifier.padding(6.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            )


            {


                Button(onClick = { scrambleBoard() }) { Text("Scramble") }
                Button(onClick = onBack) { Text("Back to Menu") }
            }

            /*       Spacer(Modifier.height(6.dp))

        Text(
            text = "Нажимай на чёрные стрелки — вращение по часовой. Собери ромашку.",
            color = Color.White,
            modifier = Modifier.padding(6.dp)
        )
*/
            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier.fillMaxWidth().aspectRatio(1f).background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .onSizeChanged { canvasSize = it }
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                val w = canvasSize.width.toFloat().coerceAtLeast(1f)
                                val hPx = canvasSize.height.toFloat().coerceAtLeast(1f)
                                val minDim = min(w, hPx)
                                val maxCoord = triangles.flatMap { it.points }.maxOfOrNull { max(abs(it.x), abs(it.y)) } ?: 1f
                                val scale = (minDim * 0.94f / 2f) / maxCoord
                                val cx = w / 2f
                                val cy = hPx / 2f

                                val clicked = hubs.minByOrNull { hub ->
                                    hypot(cx + hub.x * scale - offset.x, cy + hub.y * scale - offset.y)
                                }

                                if (clicked != null) {
                                    val hx = cx + clicked.x * scale
                                    val hy = cy + clicked.y * scale
                                    if (hypot(hx - offset.x, hy - offset.y) <= 48f) rotateHubClockwise(clicked)
                                }
                            }
                        }
                ) {
                    val w = size.width
                    val hPx = size.height
                    val minDim = min(w, hPx)
                    val maxCoord = triangles.flatMap { it.points }.maxOfOrNull { max(abs(it.x), abs(it.y)) } ?: 1f
                    val scale = (minDim * 0.94f / 2f) / maxCoord
                    val cx = size.width / 2f
                    val cy = size.height / 2f

                    triangles.forEach { tri ->
                        val path = Path().apply {
                            moveTo(cx + tri.points[0].x * scale, cy + tri.points[0].y * scale)
                            lineTo(cx + tri.points[1].x * scale, cy + tri.points[1].y * scale)
                            lineTo(cx + tri.points[2].x * scale, cy + tri.points[2].y * scale)
                            close()
                        }
                        drawPath(path, tri.color.value, style = Fill)
                    }

                    val arrowStroke = max(6f, 0.022f * minDim)
                    hubs.forEach { hub ->
                        val hx = cx + hub.x * scale
                        val hy = cy + hub.y * scale
                        val r = 28f * (scale / (side / 110f))
                        drawArc(
                            color = Color.Black,
                            startAngle = 210f,
                            sweepAngle = 120f,
                            useCenter = false,
                            topLeft = Offset(hx - r, hy - r),
                            size = Size(r * 2f, r * 2f),
                            style = Stroke(width = arrowStroke, cap = StrokeCap.Round)
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Мини-цель с поворотом на 90°
            Box(
                modifier = Modifier.fillMaxWidth(0.45f).aspectRatio(1f).background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = size.minDimension / 2f
                    val petalColors = listOf(colorA, colorB, colorC, colorD, colorE, colorF)

                    this.rotate(degrees = 90f, pivot = center) { // поворот на 90°
                        for (i in 0 until 6) {
                            val angle = Math.toRadians((60.0 * i - 30.0))
                            val p1 = center
                            val p2 = Offset(
                                center.x + radius * cos(angle).toFloat(),
                                center.y + radius * sin(angle).toFloat()
                            )
                            val p3 = Offset(
                                center.x + radius * cos(angle + Math.toRadians(60.0)).toFloat(),
                                center.y + radius * sin(angle + Math.toRadians(60.0)).toFloat()
                            )
                            val path = Path().apply {
                                moveTo(p1.x, p1.y)
                                lineTo(p2.x, p2.y)
                                lineTo(p3.x, p3.y)
                                close()
                            }
                            drawPath(path, petalColors[i], style = Fill)
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
        }
    }
}
