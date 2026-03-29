import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.*
import kotlin.math.*
import com.quicklydone.nt.Cycle

@Composable
fun SixteenScreen(
    onBack: () -> Unit = {}
) {
    val size = 4
    val count = size * size

    val cycles = listOf(
        Cycle(listOf(1,2,3,4,8,12,16,15,14,13,9,5)),
        Cycle(listOf(6,7,11,10)),

        Cycle(listOf(2,6,10,14)),
        Cycle(listOf(3,7,11,15)),

        Cycle(listOf(5,6,7,8)),
        Cycle(listOf(9,10,11,12)),

        Cycle(listOf(1,6,11,16)),
        Cycle(listOf(4,7,10,13))
    )

    var perm by remember { mutableStateOf(List(count) { it + 1 }) }

    val visible = remember {
        mutableStateListOf<Int>().apply {
            addAll((1..count).toList())
        }
    }

    var draggingId by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var candidateCycle by remember { mutableStateOf<Cycle?>(null) }
    var grabbedCycles by remember { mutableStateOf<List<Cycle>>(emptyList()) }

    val cellSize = 70.dp
    val gap = 6.dp

    val density = LocalDensity.current
    val cellPx: Float = with(density) { cellSize.toPx() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0f1113))
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // --- TOP: Scramble / Reset ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = { perm = perm.shuffled() }) {
                    Text("Scramble")
                }
                Button(onClick = { perm = List(count) { it + 1 } }) {
                    Text("Reset")
                }
                Button(onClick = onBack) {
                    Text("Back")
                }

            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- MIDDLE: BOARD ---
            Box(
                modifier = Modifier
                    .size((cellSize * size) + gap * (size - 1))
                    .align(Alignment.CenterHorizontally)
            ) {
                for (pos in 1..count) {
                    val id = perm[pos - 1]

                    val row = (pos - 1) / size
                    val col = (pos - 1) % size

                    val offsetX = col * (cellSize + gap)
                    val offsetY = row * (cellSize + gap)

                    val isDragging = draggingId == id
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
                                        draggingId?.let {
                                            candidateCycle = chooseCycle(
                                                grabbedCycles,
                                                perm,
                                                it,
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

                                            val idxGrab = ids.indexOfFirst { perm[it - 1] == idGrab }

                                            fun pos(i: Int): Pair<Float, Float> {
                                                val x = ((i - 1) % size).toFloat()
                                                val y = ((i - 1) / size).toFloat()
                                                return x to y
                                            }

                                            val (gx, gy) = pos(ids[idxGrab])
                                            val dragX = gx + dragOffset.x / cellPx
                                            val dragY = gy + dragOffset.y / cellPx

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
                                                perm = rotateCycle(perm, cycle, shift)
                                            }
                                        }

                                        draggingId = null
                                        dragOffset = Offset.Zero
                                        candidateCycle = null
                                        grabbedCycles = emptyList()
                                    }
                                )
                            }
                    ) {
                        if (visible.contains(id)) {
                            Text(
                                id.toString(),
                                color = Color.White,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- BOTTOM: HIDE PANEL + BACK BUTTON ---
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                for (row in 0 until size) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        for (col in 0 until size) {
                            val value = row * size + col + 1
                            val isChecked = visible.contains(value)

                            Column(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .border(
                                        width = if (isChecked) 2.dp else 1.dp,
                                        color = if (isChecked) Color.White else Color.Gray,
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .padding(4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(value.toString(), fontSize = 12.sp)

                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = {
                                        if (it) visible.add(value)
                                        else visible.remove(value)
                                    }
                                )
                            }
                        }
                    }
                }

            }
        }
    }









}

// --- LOGIC ---

fun chooseCycle(
    cycles: List<Cycle>,
    perm: List<Int>,
    grabbedId: Int,
    dx: Float,
    dy: Float,
    size: Int
): Cycle? {
    if (hypot(dx, dy) < 6f) return null

    val angle = atan2(dy, dx)
    var best: Cycle? = null
    var bestDiff = Float.MAX_VALUE

    cycles.forEach { cycle ->
        val ids = cycle.ids
        val idx = ids.indexOfFirst { perm[it - 1] == grabbedId }

        val neighbors = listOf(
            (idx + 1) % ids.size,
            (idx - 1 + ids.size) % ids.size
        )

        neighbors.forEach { i ->
            val from = ids[idx]
            val to = ids[i]
            val fx = (from - 1) % size
            val fy = (from - 1) / size
            val tx = (to - 1) % size
            val ty = (to - 1) / size
            val vx = (tx - fx).toFloat()
            val vy = (ty - fy).toFloat()
            val a = atan2(vy, vx)

            var diff = abs(a - angle)
            diff = min(diff, abs(diff - 2 * PI.toFloat()))
            val proj = cos(diff) * hypot(dx, dy)
            if (proj < 14f) return@forEach

            if (diff < bestDiff) {
                bestDiff = diff
                best = cycle
            }
        }
    }

    return best
}

fun rotateCycle(
    perm: List<Int>,
    cycle: Cycle,
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