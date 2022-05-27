package com.example.codegeneration.model

import com.example.codegeneration.model.Vector.Companion.distance
import kotlin.math.acos
import kotlin.math.pow

class Triangle(var point: MutableList<Vector>) {
    private var side: MutableList<Double> = mutableListOf()

    private fun setSides() {
        side.clear()
        side.add(distance(point[0], point[1]))
        side.add(distance(point[1], point[2]))
        side.add(distance(point[2], point[0]))
    }

    // Returns the angle from the vertex
    fun getAngle(vertex: Int): Double {
        setSides()

        //Law of cosines
        return acos(
            (side[(vertex + 2) % 3].pow(2.0) - side[vertex].pow(2.0) - side[(vertex + 1) % 3].pow(
                2.0
            )) / (-2 * side[vertex] * side[(vertex + 1) % 3])
        )
    }
}