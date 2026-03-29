import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.*
import kotlin.math.*
import com.quicklydone.nt.Cycle
import com.quicklydone.nt.R

//data class Cycle(val ids: List<Int>)


@Composable
fun GhostCycleScreen(
    onBack: () -> Unit = {}
) {
    val size = 3
    val count = size * size

    val cycles = listOf(
        Cycle(listOf(1,2,3,6,9,8,7,4)),
        Cycle(listOf(2,5,8)),
        Cycle(listOf(4,5,6)),
        Cycle(listOf(1,5,9)),
        Cycle(listOf(3,5,7))
    )

    var perm by remember { mutableStateOf(List(count) { it + 1 }) }

    var draggingId by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }

    var candidateCycle by remember { mutableStateOf<Cycle?>(null) }
    var grabbedCycles by remember { mutableStateOf<List<Cycle>>(emptyList()) }

    val cellSize = 78.dp
    val gap = 6.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0f1113))
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = onBack) {
                    Image(
                    painter = painterResource(id = R.drawable.home),
                    contentDescription = null,
                    contentScale = ContentScale.Fit, // Fit, Crop, FillHeight и т.д.
                    modifier = Modifier.size(32.dp)
                    )
                }
                Button(onClick = { perm = perm.shuffled() }) {
                    Text("Scramble")
                }
                Button(onClick = { perm = List(count) { it + 1 } }) {
                    Text("Reset")
                }


            }

            Spacer(modifier = Modifier.height(12.dp))

            // Здесь будет твоя доска / Grid
        }

        Box(
            modifier = Modifier
                .size((cellSize * size) + gap * (size - 1))
                .align(Alignment.Center)
        ) {

            for (pos in 1..count) {
                val id = perm[pos - 1]

                val row = (pos - 1) / size
                val col = (pos - 1) % size

                val offsetX = col * (cellSize + gap)
                val offsetY = row * (cellSize + gap)

                val isDragging = draggingId == id

                // 🔹 НОВОЕ: подсветка
                val isInCandidate = candidateCycle?.ids?.contains(pos) == true
                val isInGrabbed = grabbedCycles.any { it.ids.contains(pos) }

                Box(
                    modifier = Modifier
                        .offset(offsetX, offsetY)
                        .offset {
                            if (isDragging) {
                                IntOffset(
                                    dragOffset.x.roundToInt(),
                                    dragOffset.y.roundToInt()
                                )
                            } else IntOffset.Zero
                        }
                        .size(cellSize)
                        .zIndex(if (isDragging) 1f else 0f)
                        .background(Color(0xFF26292b), RoundedCornerShape(10.dp))
                        .then(
                            when {
                                isInCandidate -> Modifier.border(
                                    3.dp,
                                    Color(0xFF00E5FF),
                                    RoundedCornerShape(10.dp)
                                )
                                isInGrabbed -> Modifier.border(
                                    1.dp,
                                    Color(0x55FFFFFF),
                                    RoundedCornerShape(10.dp)
                                )
                                else -> Modifier
                            }
                        )
                        .pointerInput(perm) {
                            detectDragGestures(

                                onDragStart = {
                                    draggingId = id
                                    dragOffset = Offset.Zero

                                    val posIndex = perm.indexOf(id) + 1
                                    grabbedCycles = cycles.filter {
                                        it.ids.contains(posIndex)
                                    }
                                },

                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragOffset += dragAmount

                                    val dx = dragOffset.x
                                    val dy = dragOffset.y

                                    if (draggingId != null) {
                                        candidateCycle = chooseCycle(
                                            grabbedCycles,
                                            perm,
                                            draggingId!!,
                                            dx,
                                            dy,
                                            size
                                        )
                                    }
                                },

                                onDragEnd = {
                                    val idGrab = draggingId
                                    if (candidateCycle != null && idGrab != null) {
                                        val cycle = candidateCycle!!
                                        val ids = cycle.ids
                                        val len = ids.size

                                        // индекс тянутой клетки в цикле
                                        val idxGrab = ids.indexOfFirst { perm[it - 1] == idGrab }

                                        // функция для координат по позиции
                                        fun pos(i: Int): Pair<Float, Float> {
                                            val x = ((i - 1) % size).toFloat()
                                            val y = ((i - 1) / size).toFloat()
                                            return x to y
                                        }

                                        // текущая позиция перетаскиваемой клетки
                                        val (gx, gy) = pos(ids[idxGrab])
                                        val dragX = gx + dragOffset.x / cellSize.toPx()
                                        val dragY = gy + dragOffset.y / cellSize.toPx()

                                        // найти ближайшую позицию в цикле к текущей точке drag
                                        var nearest = 0
                                        var minDist = Float.MAX_VALUE
                                        ids.forEachIndexed { i, posId ->
                                            val (x, y) = pos(posId)
                                            val d = hypot(x - dragX, y - dragY)
                                            if (d < minDist) {
                                                minDist = d
                                                nearest = i
                                            }
                                        }

                                        // вычисляем сдвиг по циклу
                                        val shift = (nearest - idxGrab + len) % len

                                        if (shift != 0) {
                                            perm = rotateCycle(perm, cycle, idGrab, shift)
                                        }
                                    }

                                    // сброс состояния drag
                                    draggingId = null
                                    dragOffset = Offset.Zero
                                    candidateCycle = null
                                    grabbedCycles = emptyList()
                                }


                            )
                        }
                ) {
                    Stripe(id)
                }
            }
        }
    }
}



fun rotateCycle(
    perm: List<Int>,
    cycle: Cycle,
    grabbedId: Int,
    shift: Int
): List<Int> {
    val ids = cycle.ids
    val len = ids.size

    val newPerm = perm.toMutableList()

    for (i in ids.indices) {
        val fromIndex = (i - shift + len) % len
        newPerm[ids[i] - 1] = perm[ids[fromIndex] - 1]
    }

    return newPerm
}

@Composable
fun Stripe(id: Int) {
    Box(Modifier.fillMaxSize()) {
        @Composable
        fun stripe(alignment: Alignment, w: Float, h: Float) {
            Box(Modifier.fillMaxSize()) {
                Box(
                    Modifier
                        .align(alignment)
                        .fillMaxWidth(w)
                        .fillMaxHeight(h)
                        .background(Color.LightGray)
                )
            }
        }

        when (id) {
            1 -> stripe(Alignment.BottomEnd, 0.5f, 0.5f)
            2 -> stripe(Alignment.BottomCenter, 1f, 0.5f)
            3 -> stripe(Alignment.BottomStart, 0.5f, 0.5f)
            4 -> stripe(Alignment.CenterEnd, 0.5f, 1f)
            5 -> stripe(Alignment.Center, 1f, 1f)
            6 -> stripe(Alignment.CenterStart, 0.5f, 1f)
            7 -> stripe(Alignment.TopEnd, 0.5f, 0.5f)
            8 -> stripe(Alignment.TopCenter, 1f, 0.5f)
            9 -> stripe(Alignment.TopStart, 0.5f, 0.5f)
        }
    }
}