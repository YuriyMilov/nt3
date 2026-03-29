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

@Composable
fun GhostCyclePuzzle(
    onBack: () -> Unit = {}
) {
    val size = 4
    val count = size * size

    val cycles = listOf(
        Cycle(listOf(1,2,3,4,8,12,16,15,14,13,9,5)),  // outer
        Cycle(listOf(6,7,11,10)),                     // inner
        Cycle(listOf(2,6,10,14)),                     // verticals
        Cycle(listOf(3,7,11,15)),
        Cycle(listOf(5,6,7,8)),                       // horizontals
        Cycle(listOf(9,10,11,12)),
        Cycle(listOf(1,6,11,16)),                     // diagonals
        Cycle(listOf(4,7,10,13))
    )

    var perm by remember { mutableStateOf(List(count) { it + 1 }) }

    var draggingId by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var ghostShift by remember { mutableStateOf(0) }

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
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                  Button(onClick = onBack) {
                    Image(
                        painter = painterResource(id = R.drawable.home),
                        contentDescription = null,
                        contentScale = ContentScale.Fit, // Fit, Crop, FillHeight и т.д.
                        modifier = Modifier.size(32.dp)
                    )
                }
                Button(onClick = { perm = perm.shuffled() }) { Text("Scramble") }
                Button(onClick = { perm = List(count) { it + 1 } }) { Text("Reset") }

            }
            Spacer(Modifier.height(12.dp))
        }

        Box(
            modifier = Modifier
                .size((cellSize * size) + gap * (size - 1))
                .align(Alignment.Center)
        ) {

            // Основные клетки
            for (pos in 1..count) {
                val id = perm[pos - 1]
                val row = (pos - 1) / size
                val col = (pos - 1) % size

                val isDragging = draggingId == id
                val isInCandidate = candidateCycle?.ids?.contains(pos) == true
                val isInGrabbed = grabbedCycles.any { it.ids.contains(pos) }

                Box(
                    modifier = Modifier
                        .offset(col * (cellSize + gap), row * (cellSize + gap))
                        .offset {
                            if (isDragging) IntOffset(
                                dragOffset.x.roundToInt(),
                                dragOffset.y.roundToInt()
                            ) else IntOffset.Zero
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
                                    ghostShift = 0
                                    val posIndex = perm.indexOf(id) + 1
                                    grabbedCycles = cycles.filter { it.ids.contains(posIndex) }
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragOffset += dragAmount
                                    if (draggingId != null) {
                                        candidateCycle = chooseCycle(
                                            grabbedCycles,
                                            perm,
                                            draggingId!!,
                                            dragOffset.x,
                                            dragOffset.y,
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

                                        val idxGrab = ids.indexOfFirst { perm[it - 1] == idGrab }
                                        if (idxGrab != -1) { // безопасно
                                            fun pos(i: Int): Pair<Float, Float> {
                                                val x = ((i - 1) % size).toFloat()
                                                val y = ((i - 1) / size).toFloat()
                                                return x to y
                                            }

                                            val (gx, gy) = pos(ids[idxGrab])
                                            val dragX = gx + dragOffset.x / cellSize.toPx()
                                            val dragY = gy + dragOffset.y / cellSize.toPx()

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

                                            val shift = (nearest - idxGrab + len) % len
                                            if (shift != 0) {
                                                perm = rotateCycle4(perm, cycle, idGrab, shift)
                                            }
                                        }
                                    }

                                    draggingId = null
                                    dragOffset = Offset.Zero
                                    ghostShift = 0
                                    candidateCycle = null
                                    grabbedCycles = emptyList()
                                }
                            )
                        }
                ) {
                    Stripe4(id)
                }
            }

            // 🔹 Ghost-клетка поверх всех остальных
            // 🔹 Ghost-клетка для самой тянутой клетки
            draggingId?.let { dragId ->
                val cycle = candidateCycle
                if (cycle != null) {
                    val idxGrab = cycle.ids.indexOfFirst { perm[it - 1] == dragId }
                    if (idxGrab != -1) {
                        val len = cycle.ids.size
                        val idxInCycle = (idxGrab + ghostShift + len) % len
                        val actualPos = cycle.ids[idxInCycle] - 1
                        val row = (cycle.ids[idxInCycle] - 1) / size
                        val col = (cycle.ids[idxInCycle] - 1) % size

                        Box(
                            modifier = Modifier
                                .offset(col * (cellSize + gap), row * (cellSize + gap))
                                .offset { IntOffset(dragOffset.x.roundToInt(), dragOffset.y.roundToInt()) }
                                .size(cellSize)
                                .zIndex(100f) // 🔹 поверх всех
                                .background(Color(0xFF5A5FFF), RoundedCornerShape(10.dp)) // более яркий
                                .border(3.dp, Color.Yellow, RoundedCornerShape(10.dp)) // яркий бордер
                        ) {
                            Stripe4(dragId)
                        }
                    }
                }
            }
        }
    }
}

// отдельная функция для сдвига цикла 4x4
fun rotateCycle4(
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

// отдельный Stripe для 4x4
@Composable
fun Stripe4(id: Int) {
    Box(Modifier.fillMaxSize()) {
        @Composable
        fun stripe4(alignment: Alignment, w: Float, h: Float) {
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

            1 -> stripe4(Alignment.BottomEnd, 0.5f, 0.5f)
            4 -> stripe4(Alignment.BottomStart, 0.5f, 0.5f)
            13 -> stripe4(Alignment.TopEnd, 0.5f, 0.5f)
            16 -> stripe4(Alignment.TopStart, 0.5f, 0.5f)
            2,3 -> stripe4(Alignment.BottomCenter, 1f, 0.5f)
            5,9 -> stripe4(Alignment.CenterEnd, 0.5f, 1f)
            6,7,10,11 -> stripe4(Alignment.Center, 1f, 1f)
            8,12 -> stripe4(Alignment.CenterStart, 0.5f, 1f)
            14,15 -> stripe4(Alignment.TopCenter, 1f, 0.5f)
            else -> stripe4(Alignment.Center, 0.5f, 0.5f)
        }
    }
}