package com.josejordan.memorygame

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
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

    private fun flipCard(imageView: ImageView, memoryCard: MemoryCard, onAnimationEnd: () -> Unit) {
        val flipFrontToBack = ObjectAnimator.ofFloat(imageView, "rotationY", 0f, -90f)
        flipFrontToBack.duration = 250
        flipFrontToBack.interpolator = AccelerateInterpolator()

        val flipBackToFront = ObjectAnimator.ofFloat(imageView, "rotationY", 90f, 0f)
        flipBackToFront.duration = 250
        flipBackToFront.interpolator = DecelerateInterpolator()

        flipFrontToBack.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                imageView.setImageResource(
                    if (imageView.rotationY == 0f) R.drawable.card_back else memoryCard.imageResource
                )
                imageView.rotationY = 90f
            }
        })

        flipBackToFront.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                onAnimationEnd()
            }
        })

        val set = AnimatorSet()
        set.playSequentially(flipFrontToBack, flipBackToFront)
        set.start()
    }


    private fun flipCardBack(memoryCard: MemoryCard, onAnimationEnd: (() -> Unit)? = null) {
        val index = memoryCards.indexOf(memoryCard)
        val memoryCardViewHolder =
            binding.rvMemoryCards.findViewHolderForAdapterPosition(index) as MemoryCardAdapter.MemoryCardViewHolder?
        val memoryCardImageView =
            memoryCardViewHolder?.itemView?.findViewById<ImageView>(R.id.iv_memory_card_image)

        if (memoryCardImageView != null) {
            flipCard(memoryCardImageView, memoryCard) {
                memoryCard.isFaceUp = !memoryCard.isFaceUp
                onAnimationEnd?.invoke()
            }
        } else {
            memoryCard.isFaceUp = !memoryCard.isFaceUp
            onAnimationEnd?.invoke()
        }
    }


    private fun processCardPair(index: Int, memoryCardImageView: ImageView) {
        val selectedCard = memoryCards[index]
        val previousCard = memoryCards[selectedCardIndex!!]

        if (selectedCard.identifier == previousCard.identifier && index != selectedCardIndex) {
            Toast.makeText(this, "¡Has encontrado un par!", Toast.LENGTH_SHORT).show()
            selectedCard.isMatched = true
            previousCard.isMatched = true
            selectedCardIndex = null
            updateScore(100)

            if (isGameOver()) {
                Toast.makeText(this, "¡Fin de la partida!", Toast.LENGTH_SHORT).show()
                Handler(Looper.getMainLooper()).postDelayed({
                    Toast.makeText(this, "Comenzando una nueva partida", Toast.LENGTH_SHORT)
                        .show()
                    restartGame()
                }, 2000)
            }
        } else {
            Toast.makeText(this, "No es un par, sigue intentándolo", Toast.LENGTH_SHORT).show()
            cardsLocked = true

            Handler(Looper.getMainLooper()).postDelayed({
                flipCardBack(previousCard) {
                    val previousSelectedCardIndex = selectedCardIndex
                    selectedCardIndex = null
                    if (previousSelectedCardIndex != null) {
                        updateCardView(previousSelectedCardIndex)
                    }
                }

                flipCardBack(selectedCard) {
                    updateCardView(index)
                }

                cardsLocked = false
            }, 1000)


        }
    }

    private fun flipSecondCard(index: Int) {
        val selectedCard = memoryCards[index]
        val memoryCardViewHolder =
            binding.rvMemoryCards.findViewHolderForAdapterPosition(index) as MemoryCardAdapter.MemoryCardViewHolder
        val memoryCardImageView =
            memoryCardViewHolder.itemView.findViewById<ImageView>(R.id.iv_memory_card_image)
        flipCard(memoryCardImageView, selectedCard) {
            selectedCard.isFaceUp = true
            updateCardView(index)
            processCardPair(
                index,
                memoryCardImageView
            ) // Pasa la variable memoryCardImageView como argumento
        }
    }

    private fun flipFirstCard(index: Int) {
        selectedCardIndex = index
        val memoryCardViewHolder =
            binding.rvMemoryCards.findViewHolderForAdapterPosition(index) as MemoryCardAdapter.MemoryCardViewHolder
        val memoryCardImageView =
            memoryCardViewHolder.itemView.findViewById<ImageView>(R.id.iv_memory_card_image)
        val selectedCard = memoryCards[index]
        flipCard(memoryCardImageView, selectedCard) {
            selectedCard.isFaceUp = true
            updateCardView(index)
        }
    }

    private fun onCardClicked(index: Int) {
        if (cardsLocked) return
        val selectedCard = memoryCards[index]
        if (selectedCard.isFaceUp || selectedCard.isMatched) return
        if (selectedCardIndex == null) {
            flipFirstCard(index)
        } else {
            flipSecondCard(index)
        }
    }


}



