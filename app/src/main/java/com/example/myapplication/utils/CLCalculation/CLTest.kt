package com.example.myapplication.utils.CLCalculation;

fun main() {
    val positions =
        arrayOf(doubleArrayOf(35.75195,51.53178), doubleArrayOf(35.73276,51.51766), doubleArrayOf(35.73753,51.48329))
    val distances = doubleArrayOf(0.032248504,0.022957212,0.023119044)

    val result =
        CircularLaterationCalculator.lateration(
            positions,
            distances
        )
    println("Estimated position: x = " + result[0] + ", y = " + result[1])
}