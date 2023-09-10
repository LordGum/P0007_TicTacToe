package com.example.tictactoe

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class GameActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
    }

    companion object {
        fun newIntent(context: Context, time: Long, gameField: String): Intent {
            val intent = Intent(context, GameActivity::class.java)
            intent.putExtra(EXTRA_TIME, time)
            intent.putExtra(EXTRA_GAME_FIELD, gameField)
            return intent
        }

        private const val EXTRA_TIME = "Time"
        private const val EXTRA_GAME_FIELD = "GameField"
    }
}