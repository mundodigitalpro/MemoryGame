package com.josejordan.memorygame
data class MemoryCard(
    val identifier: Int,
    val imageResource: Int, // Agrega este campo para almacenar el recurso de la imagen
    var isFaceUp: Boolean = false,
    var isMatched: Boolean = false
)
