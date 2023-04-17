package com.josejordan.memorygame

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.josejordan.memorygame.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var memoryCards = mutableListOf<MemoryCard>()
    private var selectedCardIndex: Int? = null
    private var score = 0
    private var cardsLocked = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configura la barra de acción
        supportActionBar?.title = "Juego de Memoria"
        // Carga imágenes y crea cartas
        setupGame()
        // Configura el RecyclerView
        binding.rvMemoryCards.layoutManager = GridLayoutManager(this, 4)
        // Crea el adaptador y configúralo en el RecyclerView
        binding.rvMemoryCards.adapter =
            MemoryCardAdapter(memoryCards) { index -> onCardClicked(index) }
    }

    private fun setupGame() {
        val images = mutableListOf(
            Pair(1, R.drawable.card_image_1),
            Pair(2, R.drawable.card_image_2),
            Pair(3, R.drawable.card_image_3),
            Pair(4, R.drawable.card_image_4),
            Pair(5, R.drawable.card_image_5),
            Pair(6, R.drawable.card_image_6),
            Pair(7, R.drawable.card_image_7),
            Pair(8, R.drawable.card_image_8),
            Pair(9, R.drawable.card_image_9),
            Pair(10, R.drawable.card_image_10),
            // Agrega más imágenes y sus identificadores aquí
        )

        val imagePairs =
            (images + images).toMutableList() // Duplica la lista de imágenes y conviértela en MutableList
        imagePairs.shuffle() // Baraja las imágenes

        // Crea las cartas
        memoryCards.clear()
        for ((identifier, image) in imagePairs) {
            memoryCards.add(
                MemoryCard(
                    identifier,
                    isFaceUp = false,
                    isMatched = false,
                    imageResource = image
                )
            )
        }
    }

    private fun updateScore(points: Int) {
        // Actualiza la puntuación
        score += points // Añade los puntos a la puntuación
        binding.tvScore.text =
            getString(R.string.score_text, score)// Actualiza la vista de la puntuación
    }

    private fun updateCardView(index: Int) {
        // Actualiza la vista de la carta específica
        (binding.rvMemoryCards.adapter as MemoryCardAdapter).notifyItemChanged(index)//
    }

    private fun isGameOver(): Boolean {// Comprueba si el juego ha terminado
        // Comprueba si todas las cartas están emparejadas
        return memoryCards.all { it.isMatched }
    }

    private fun restartGame() {
        score = 0 // Reinicia la puntuación
        updateScore(0)// Actualiza la puntuación
        setupGame() // Configura el juego de nuevo
        // Reinicia el estado de las cartas
        memoryCards.forEachIndexed { index, card ->
            card.isFaceUp = false // Voltea la carta
            card.isMatched = false // Desempareja la carta
            (binding.rvMemoryCards.adapter as MemoryCardAdapter).notifyItemChanged(index) // Actualiza la vista de la carta específica
        }
    }

    private fun onCardClicked(index: Int) {
        if (cardsLocked) return // Ignora los clics si las cartas están bloqueadas
        val selectedCard = memoryCards[index] // Obtiene la carta seleccionada
        if (selectedCard.isFaceUp || selectedCard.isMatched) return// Si la carta ya está volteada o emparejada, no hagas nada
        if (selectedCardIndex == null) {// Si no hay ninguna carta seleccionada, selecciona esta carta
            selectedCardIndex = index // Guarda el índice de la carta seleccionada
            selectedCard.isFaceUp = true // Voltea la carta
            updateCardView(index)  // Actualiza la vista de la carta específica
        } else { // Si hay una carta seleccionada, comprueba si es un par
            val previousCard = memoryCards[selectedCardIndex!!] // Obtiene la carta anterior
            selectedCard.isFaceUp = true // Voltea la carta
            updateCardView(index)// Actualiza la vista de la carta específica

            // Comprueba si es un par
            if (selectedCard.identifier == previousCard.identifier && index != selectedCardIndex) {
                Toast.makeText(this, "¡Has encontrado un par!", Toast.LENGTH_SHORT).show()
                selectedCard.isMatched = true // Marca la carta actual como emparejada
                previousCard.isMatched = true // Marca la carta anterior como emparejada
                selectedCardIndex = null// Reinicia el índice de la carta seleccionada
                updateScore(100)  // Aumenta la puntuación y actualiza el marcador

                // Comprueba si el juego ha terminado
                if (isGameOver()) {
                    Toast.makeText(this, "¡Fin de la partida!", Toast.LENGTH_SHORT).show()
                    Handler(Looper.getMainLooper()).postDelayed({ // Espera 2 segundos antes de reiniciar el juego
                        Toast.makeText(this, "Comenzando una nueva partida", Toast.LENGTH_SHORT)
                            .show()
                        restartGame() // Reinicia el juego
                    }, 2000) // Espera 2 segundos antes de reiniciar el juego
                }
                // Si no es un par, vuelve a ocultar las cartas
            } else {
                Toast.makeText(this, "No es un par, sigue intentándolo", Toast.LENGTH_SHORT).show()
                cardsLocked = true // Bloquea las cartas antes de la acción diferida
                // Espera 1 segundo antes de ocultar las cartas
                Handler(Looper.getMainLooper()).postDelayed({
                    previousCard.isFaceUp = false // Voltea la carta anterior
                    selectedCard.isFaceUp = false // Voltea la carta actual

                    //updateCardView(selectedCardIndex!!) // Actualiza la vista de la carta anterior

                    if (selectedCardIndex != null) {
                        updateCardView(selectedCardIndex!!) // Actualiza la vista de la carta anterior
                    }

                    updateCardView(index) // Actualiza la vista de la carta actual
                    selectedCardIndex = null // Reinicia el índice de la carta seleccionada
                    cardsLocked = false // Desbloquea las cartas después de la acción diferida
                }, 1000) // 1000ms = 1 segundo de retraso
            }
        }
    }
}


