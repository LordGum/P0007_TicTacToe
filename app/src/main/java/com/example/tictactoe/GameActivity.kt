package com.example.tictactoe

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.os.SystemClock
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tictactoe.SettingsActivity.Companion.PREF_LEVEL
import com.example.tictactoe.SettingsActivity.Companion.PREF_RULES
import com.example.tictactoe.SettingsActivity.Companion.PREF_SOUND
import com.example.tictactoe.databinding.ActivityGameBinding

class GameActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGameBinding

    private lateinit var gameField: Array<Array<String>>

    private lateinit var mediaPlayer: MediaPlayer

    private lateinit var animation: Animation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGameBinding.inflate(layoutInflater)

        binding.crossGame.setOnClickListener {
            onBackPressed()
        }

        binding.menuGame.setOnClickListener {
            showPopupMenu()
        }

        binding.imageView1.setOnClickListener {
            makeStepToUser(0, 0)
        }

        binding.imageView2.setOnClickListener {
            makeStepToUser(0, 1)
        }

        binding.imageView3.setOnClickListener {
            makeStepToUser(0, 2)
        }

        binding.imageView4.setOnClickListener {
            makeStepToUser(1, 0)
        }

        binding.imageView5.setOnClickListener {
            makeStepToUser(1, 1)
        }

        binding.imageView6.setOnClickListener {
            makeStepToUser(1, 2)
        }

        binding.imageView7.setOnClickListener {
            makeStepToUser(2, 0)
        }

        binding.imageView8.setOnClickListener {
            makeStepToUser(2, 1)
        }

        binding.imageView9.setOnClickListener {
            makeStepToUser(2, 2)
        }

        setContentView(binding.root)

        val time = intent.getLongExtra(MainActivity.EXTRA_TIME, 0L)
        val gameField = intent.getStringExtra(MainActivity.EXTRA_GAME_FIELD)

        if (gameField != null && time != 0L && gameField != "") {
            restartGame(time, gameField)
        } else {
            initGameField()
        }

        mediaPlayer = MediaPlayer.create(this, R.raw.music)
        mediaPlayer.isLooping = true
        val settingsInfo = getCurrentSettings()
        setVolumeMediaPlayer(settingsInfo.sound)

        binding.chronometer.start()
        mediaPlayer.start()

        animation = AnimationUtils.loadAnimation(this, R.anim.image_scale)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }

    override fun onStop() {
        super.onStop()
        mediaPlayer.release()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == POPUP_MENU) {
            if (resultCode == RESULT_OK) {
                mediaPlayer = MediaPlayer.create(this, R.raw.music)
                mediaPlayer.isLooping = true
                val settingsInfo = getCurrentSettings()
                setVolumeMediaPlayer(settingsInfo.sound)

                binding.chronometer.start()
                mediaPlayer.start()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun setVolumeMediaPlayer(soundValue: Int){
        val volume = soundValue / 100.0
        mediaPlayer.setVolume(volume.toFloat(), volume.toFloat())
    }

    private fun restartGame(time: Long, gameField: String) {
        binding.chronometer.base = SystemClock.elapsedRealtime() - time

        this.gameField = arrayOf()

        val rows = gameField.split("\n")

        for (row in rows) {
            val columns = row.split(";")
            this.gameField += columns.toTypedArray()
        }

        this.gameField.forEachIndexed { indexRow, columns ->
            columns.forEachIndexed { indexColumn, cell ->
                makeGameFieldUI("$indexRow$indexColumn", cell)
            }
        }
    }

    private fun convertGameFieldToString(): String {
        val tmpArray = arrayListOf<String>()
        gameField.forEach { tmpArray.add(it.joinToString(separator = ";")) }
        return tmpArray.joinToString(separator = "\n")
    }

    private fun saveGame(time: Long, gameField: String) {
        getSharedPreferences("game", MODE_PRIVATE).edit().apply {
            putLong(PREF_TIME, time)
            putString(PREF_GAME_FIELD, gameField)
            apply()
        }
    }

    private fun initGameField() {
        gameField = arrayOf()

        for (i in 0..2) {
            var array = arrayOf<String>()
            for (j in 0..2) {
                array += " "
            }
            gameField += array
        }
    }

    private fun makeStepToUser(row: Int, column: Int) {
        if (isEmptyField(row, column)) {
            userMakeStep(row, column, PLAYER_SYMBOL)

            if (checkGameField(row, column, PLAYER_SYMBOL)) {
                showGameStatus(STATUS_WIN_PLAYER)
            } else if (!isFilledGameField()) {
                val stepOfAI = makeStepToAI()

                if (checkGameField(stepOfAI.row, stepOfAI.column, BOT_SYMBOL)) {
                    showGameStatus(STATUS_WIN_BOT)
                } else if (isFilledGameField()) {
                    showGameStatus(STATUS_DRAW)
                }
            } else {
                showGameStatus(STATUS_DRAW)
            }
        } else {
            Toast.makeText(this, "Поле уже заполнено", Toast.LENGTH_SHORT).show()
        }
    }

    private fun makeStepToAI(): CellGameField {
        val settingsInfo = getCurrentSettings()
        return when (settingsInfo.level) {
            0 -> makeStepOfAIEasyLvl()
            1 -> makeStepOfAIMediumLvl()
            2 -> makeStepOfAIHardLvl()
            else -> CellGameField(0, 0)
        }
    }

    private fun makeStepOfAIEasyLvl(): CellGameField {
        var randomRow = 0
        var randomColumn = 0

        do {
            randomRow = (0..2).random()
            randomColumn = (0..2).random()
        } while (!isEmptyField(randomRow, randomColumn))

        makeStep(randomRow, randomColumn, BOT_SYMBOL)

        return CellGameField(randomRow, randomColumn)
    }

    private fun makeStepOfAIMediumLvl(): CellGameField {
        var bestScore = Double.NEGATIVE_INFINITY
        var moveCell = CellGameField(0, 0)

        var board = gameField.map { it.clone() }.toTypedArray()

        board.forEachIndexed { indexRow, columns ->
            columns.forEachIndexed { indexColumn, cell ->
                if (board[indexRow][indexColumn] == " ") {
                    board[indexRow][indexColumn] = BOT_SYMBOL
                    val score = minimax(board, false)
                    board[indexRow][indexColumn] = " "
                    if (score > bestScore) {
                        bestScore = score
                        moveCell = CellGameField(indexRow, indexColumn)
                    }
                }
            }
        }

        makeStep(moveCell.row, moveCell.column, BOT_SYMBOL)

        return moveCell
    }

    private fun makeStepOfAIHardLvl(): CellGameField {
        var bestScore = Double.NEGATIVE_INFINITY
        var moveCell = CellGameField(0, 0)

        var board = gameField.map { it.clone() }.toTypedArray()

        board.forEachIndexed { indexRow, columns ->
            columns.forEachIndexed { indexColumn, cell ->
                if (board[indexRow][indexColumn] == " ") {
                    board[indexRow][indexColumn] = BOT_SYMBOL
                    val score = minimax(board, false)
                    board[indexRow][indexColumn] = " "
                    if (score > bestScore) {
                        bestScore = score
                        moveCell = CellGameField(indexRow, indexColumn)
                    }
                }
            }
        }

        makeStep(moveCell.row, moveCell.column, BOT_SYMBOL)

        return moveCell
    }

    private fun minimax(board: Array<Array<String>>, isMaximizing: Boolean): Double {
        val result = checkWinner(board)
        result?.let {
            return scores[result]!!
        }

        if (isMaximizing) {
            var bestScore = Double.NEGATIVE_INFINITY
            board.forEachIndexed { indexRow, columns ->
                columns.forEachIndexed { indexColumn, cell ->
                    if (board[indexRow][indexColumn] == " ") {
                        board[indexRow][indexColumn] = BOT_SYMBOL
                        val score = minimax(board, false)
                        board[indexRow][indexColumn] = " "
                        if (score > bestScore) {
                            bestScore = score
                        }
                    }
                }
            }
            return bestScore
        } else {
            var bestScore = Double.POSITIVE_INFINITY
            board.forEachIndexed { indexRow, columns ->
                columns.forEachIndexed { indexColumn, cell ->
                    if (board[indexRow][indexColumn] == " ") {
                        board[indexRow][indexColumn] = PLAYER_SYMBOL
                        val score = minimax(board, true)
                        board[indexRow][indexColumn] = " "
                        if (score < bestScore) {
                            bestScore = score
                        }
                    }
                }
            }
            return bestScore
        }
    }

    private fun checkWinner(board: Array<Array<String>>): Int? {
        var countRowsUser = 0
        var countRowsAI = 0
        var countLeftDiagonalUser = 0
        var countLeftDiagonalAL = 0
        var countRightDiagonalUser = 0
        var countRightDiagonalAI = 0

        board.forEachIndexed { indexRow, columns ->
            if (columns.all { it == PLAYER_SYMBOL })
                return STATUS_WIN_PLAYER
            else if (columns.all { it == BOT_SYMBOL })
                return STATUS_WIN_BOT

            countRowsUser = 0
            countRowsAI = 0

            columns.forEachIndexed { indexColumn, cell ->
                if (board[indexColumn][indexRow] == PLAYER_SYMBOL)
                    countRowsUser++
                else if (board[indexColumn][indexRow] == BOT_SYMBOL)
                    countRowsAI++

                if (indexRow == indexColumn && board[indexRow][indexColumn] == PLAYER_SYMBOL)
                    countLeftDiagonalUser++
                else if (indexRow == indexColumn && board[indexRow][indexColumn] == BOT_SYMBOL)
                    countLeftDiagonalAL++

                if (indexRow == 2 - indexColumn && board[indexRow][indexColumn] == PLAYER_SYMBOL)
                    countRightDiagonalUser++
                else if (indexRow == 2 - indexColumn && board[indexRow][indexColumn] == BOT_SYMBOL)
                    countRightDiagonalAI++
            }

            if (countRowsUser == 3 || countLeftDiagonalUser == 3 || countRightDiagonalUser == 3)
                return STATUS_WIN_PLAYER
            else if (countRowsAI == 3 || countLeftDiagonalAL == 3 || countRightDiagonalAI == 3)
                return STATUS_WIN_BOT
        }

        board.forEach {
            if (it.find { it == " " } != null)
                return null
        }

        return STATUS_DRAW
    }

    private fun getCurrentSettings(): SettingsActivity.SettingInfo {
        this.getSharedPreferences("game", MODE_PRIVATE).apply {

            val sound = getInt(PREF_SOUND, 100)
            val level = getInt(PREF_LEVEL, 1)
            val rules = getInt(PREF_RULES, 7)

            return SettingsActivity.SettingInfo(sound, level, rules)
        }
    }

    data class CellGameField(val row: Int, val column: Int)

    private fun isEmptyField(row: Int, column: Int): Boolean {
        return gameField[row][column] == " "
    }

    private fun makeStep(row: Int, column: Int, symbol: String) {
        gameField[row][column] = symbol

        makeGameFieldUI("$row$column", symbol)
    }
    private fun userMakeStep(row: Int, column: Int, symbol: String) {
        gameField[row][column] = symbol

        makeUserStepUI("$row$column", symbol)
    }

    private fun makeGameFieldUI(position: String, symbol: String) {
        val drawable = when (symbol) {
            PLAYER_SYMBOL -> R.drawable.cross
            BOT_SYMBOL -> R.drawable.zero
            else -> return
        }

        when (position) {
            "00" -> {
                binding.imageView1.setImageResource(drawable)
            }
            "01" -> {
                binding.imageView2.setImageResource(drawable)
            }
            "02" -> {
                binding.imageView3.setImageResource(drawable)
            }
            "10" -> {
                binding.imageView4.setImageResource(drawable)
            }
            "11" -> {
                binding.imageView5.setImageResource(drawable)
            }
            "12" -> {
                binding.imageView6.setImageResource(drawable)
            }
            "20" -> {
                binding.imageView7.setImageResource(drawable)
            }
            "21" -> {
                binding.imageView8.setImageResource(drawable)
            }
            "22" -> {
                binding.imageView9.setImageResource(drawable)
            }
        }
    }

    private fun makeUserStepUI(position: String, symbol: String) {
        val drawable = when (symbol) {
            PLAYER_SYMBOL -> R.drawable.cross
            BOT_SYMBOL -> R.drawable.zero
            else -> return
        }

        when (position) {
            "00" -> {
                binding.imageView1.setImageResource(drawable)
                binding.imageView1.startAnimation(animation)
            }

            "01" -> {
                binding.imageView2.setImageResource(drawable)
                binding.imageView2.startAnimation(animation)
            }

            "02" -> {
                binding.imageView3.setImageResource(drawable)
                binding.imageView3.startAnimation(animation)
            }

            "10" -> {
                binding.imageView4.setImageResource(drawable)
                binding.imageView4.startAnimation(animation)
            }

            "11" -> {
                binding.imageView5.setImageResource(drawable)
                binding.imageView5.startAnimation(animation)
            }

            "12" -> {
                binding.imageView6.setImageResource(drawable)
                binding.imageView6.startAnimation(animation)
            }

            "20" -> {
                binding.imageView7.setImageResource(drawable)
                binding.imageView7.startAnimation(animation)
            }

            "21" -> {
                binding.imageView8.setImageResource(drawable)
                binding.imageView8.startAnimation(animation)
            }

            "22" -> {
                binding.imageView9.setImageResource(drawable)
                binding.imageView9.startAnimation(animation)
            }
        }
    }

    private fun checkGameField(x: Int, y: Int, symbol: String): Boolean {
        var col = 0
        var row = 0
        var diag = 0
        var rdiag = 0
        val n = gameField.size

        for (i in 0..2) {
            if (gameField[x][i] == symbol)
                col++
            if (gameField[i][y] == symbol)
                row++
            if (gameField[i][i] == symbol)
                diag++
            if (gameField[i][n - i - 1] == symbol)
                rdiag++
        }

        val settings = getCurrentSettings()
        return when (settings.rules) {
            1 -> {
                col == n
            }
            2 -> {
                row == n
            }
            3 -> {
                col == n || row == n
            }
            4 -> {
                diag == n || rdiag == n
            }
            5 -> {
                col == n || diag == n || rdiag == n
            }
            6 -> {
                row == n || diag == n || rdiag == n
            }
            7 -> {
                col == n || row == n || diag == n || rdiag == n
            }
            else -> {
                false
            }
        }
    }

    private fun isFilledGameField(): Boolean {
        gameField.forEach { strings ->
            if (strings.find { it == " " } != null)
                return false
        }
        return true
    }

    private fun showGameStatus(status: Int) {
        binding.chronometer.stop()

        val dialog = Dialog(this@GameActivity, R.style.Theme_TicTacToe)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.argb(50, 0, 0, 0)))
        dialog.setContentView(R.layout.dialog_popup_game_status)
        dialog.setCancelable(true)

        when (status) {
            STATUS_WIN_BOT -> {
                dialog.findViewById<TextView>(R.id.dialog_text).text = "Вы проиграли!"
                dialog.findViewById<ImageView>(R.id.dialog_image)
                    .setImageResource(R.drawable.lose)
            }
            STATUS_WIN_PLAYER -> {
                dialog.findViewById<TextView>(R.id.dialog_text).text = "Вы выиграли!"
                dialog.findViewById<ImageView>(R.id.dialog_image)
                    .setImageResource(R.drawable.win)
            }
            STATUS_DRAW -> {
                dialog.findViewById<TextView>(R.id.dialog_text).text = "Ничья!"
                dialog.findViewById<ImageView>(R.id.dialog_image)
                    .setImageResource(R.drawable.draw)
            }
        }

        dialog.findViewById<TextView>(R.id.dialog_button).setOnClickListener {
            dialog.hide()
            onBackPressed()
        }
        dialog.show()
    }

    private fun showPopupMenu() {
        binding.chronometer.stop()

        val elapsedMillis = SystemClock.elapsedRealtime() - binding.chronometer.base

        val dialog = Dialog(this@GameActivity, R.style.Theme_TicTacToe)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.argb(50, 0, 0, 0)))
        dialog.setContentView(R.layout.dialog_popup_menu)
        dialog.setCancelable(true)

        dialog.findViewById<TextView>(R.id.dialog_back).setOnClickListener {
            dialog.hide()
            binding.chronometer.base = SystemClock.elapsedRealtime() - elapsedMillis
            binding.chronometer.start()
        }
        dialog.findViewById<TextView>(R.id.dialog_setting).setOnClickListener {
            dialog.hide()
            val intent = Intent(this, SettingsActivity::class.java)
            startActivityForResult(intent, POPUP_MENU)
        }
        dialog.findViewById<TextView>(R.id.dialog_save_and_exit).setOnClickListener {
            saveGame(elapsedMillis, convertGameFieldToString())
            dialog.hide()
            onBackPressed()
        }

        dialog.show()
    }

    companion object {
        const val STATUS_WIN_PLAYER = 1
        const val STATUS_WIN_BOT = 2
        const val STATUS_DRAW = 3
        const val POPUP_MENU = 235

        val scores = hashMapOf(
            Pair(STATUS_WIN_PLAYER, -1.0), Pair(STATUS_WIN_BOT, 1.0), Pair(STATUS_DRAW, 0.0)
        )

        const val PLAYER_SYMBOL = "X"
        const val BOT_SYMBOL = "0"

        const val PREF_TIME = "pref_time"
        const val PREF_GAME_FIELD = "pref_game_field"
    }
}