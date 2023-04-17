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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Juego de Memoria"

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

        val imagePairs = (images + images).toMutableList() // Duplica la lista de imágenes y conviértela en MutableList
        imagePairs.shuffle() // Baraja las imágenes

        for ((identifier, image) in imagePairs) {
            memoryCards.add(MemoryCard(identifier,
                isFaceUp = false,
                isMatched = false,
                imageResource = image
            ))
        }

        binding.rvMemoryCards.layoutManager = GridLayoutManager(this, 4)
        binding.rvMemoryCards.adapter = MemoryCardAdapter(memoryCards) { index -> onCardClicked(index) }
    }

    private fun updateScore(points: Int) {
        score += points
        binding.tvScore.text = getString(R.string.score_text, score)
    }

    private fun updateCardView(index: Int) {
        (binding.rvMemoryCards.adapter as MemoryCardAdapter).notifyItemChanged(index)
    }
    private fun isGameOver(): Boolean {
        return memoryCards.all { it.isMatched }
    }

    private fun restartGame() {
        // Reinicia la puntuación
        score = 0
        updateScore(0)

        // Baraja las imágenes y reinicia el estado de las cartas
        memoryCards.shuffle()

        memoryCards.forEachIndexed { index, card ->
            card.isFaceUp = false
            card.isMatched = false

            // Actualiza la vista de la carta específica
            (binding.rvMemoryCards.adapter as MemoryCardAdapter).notifyItemChanged(index)
        }

    }

    private fun onCardClicked(index: Int) {
        val selectedCard = memoryCards[index]

        // Si la carta ya está volteada o emparejada, no hagas nada
        if (selectedCard.isFaceUp || selectedCard.isMatched) return

        if (selectedCardIndex == null) {
            selectedCardIndex = index
            selectedCard.isFaceUp = true
            updateCardView(index)
        } else {
            val previousCard = memoryCards[selectedCardIndex!!]
            selectedCard.isFaceUp = true
            updateCardView(index)

            if (selectedCard.identifier == previousCard.identifier && index != selectedCardIndex) {
                Toast.makeText(this, "¡Has encontrado un par!", Toast.LENGTH_SHORT).show()
                selectedCard.isMatched = true
                previousCard.isMatched = true
                selectedCardIndex = null
                // Aumenta la puntuación y actualiza el marcador
                updateScore(100)

                if (isGameOver()) {
                    Toast.makeText(this, "¡Fin de la partida!", Toast.LENGTH_SHORT).show()
                    Handler(Looper.getMainLooper()).postDelayed({
                        Toast.makeText(this, "Comenzando una nueva partida", Toast.LENGTH_SHORT).show()
                        restartGame()
                    }, 2000) // Espera 2 segundos antes de reiniciar el juego
                }

            } else {
                Toast.makeText(this, "No es un par, sigue intentándolo", Toast.LENGTH_SHORT).show()
                Handler(Looper.getMainLooper()).postDelayed({
                    previousCard.isFaceUp = false
                    selectedCard.isFaceUp = false
                    updateCardView(selectedCardIndex!!)
                    updateCardView(index)
                    selectedCardIndex = null
                }, 1000) // 1000ms = 1 segundo de retraso
            }
        }
    }


}


