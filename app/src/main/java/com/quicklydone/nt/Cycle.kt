package com.quicklydone.nt

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.min

public final data class Cycle(
    public final val ids: List<Int>
)

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

            if (abs(vx) > 0 && abs(vy) > 0) diff *= 0.5f

            if (diff < bestDiff) {
                bestDiff = diff
                best = cycle
            }
        }
    }

    return best
}

fun rotateCycle(perm: List<Int>, cycle: Cycle, shift: Int): List<Int> {
    val ids = cycle.ids
    val len = ids.size
    val newPerm = perm.toMutableList()
    for(i in ids.indices){
        val from = (i-shift+len)%len
        newPerm[ids[i]-1] = perm[ids[from]-1]
    }
    return newPerm
}
