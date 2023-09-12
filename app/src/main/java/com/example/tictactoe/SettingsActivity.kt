package com.example.tictactoe

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import com.example.tictactoe.databinding.ActivitySettingBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var settingsBinding: ActivitySettingBinding

    private var currentLevel : Int = 0
    private var currentVolumeSound: Int = 0
    private var currentRules: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        settingsBinding = ActivitySettingBinding.inflate(layoutInflater)

        val currentSettings = getCurrentSettings()

        currentLevel = currentSettings.level
        currentVolumeSound = currentSettings.sound
        currentRules = currentSettings.rules

        if(currentLevel == 0){
            settingsBinding.previousSet.visibility = View.INVISIBLE
        } else if (currentLevel == 2) {
            settingsBinding.nextSet.visibility = View.INVISIBLE
        }

        settingsBinding.levelInGameSet.text = resources.getStringArray(R.array.game_level)[currentLevel]
        settingsBinding.seekBar.progress = currentVolumeSound


        when(currentSettings.rules){
            1 -> settingsBinding.checkBoxHorizontal.toggle()
            2 -> settingsBinding.checkBoxVertical.toggle()
            3 -> {
                settingsBinding.checkBoxHorizontal.toggle()
                settingsBinding.checkBoxVertical.toggle()
            }
            4 -> settingsBinding.checkBoxDiagonal.toggle()
            5 -> {
                settingsBinding.checkBoxDiagonal.toggle()
                settingsBinding.checkBoxHorizontal.toggle()
            }
            6 -> {
                settingsBinding.checkBoxDiagonal.toggle()
                settingsBinding.checkBoxVertical.toggle()
            }
            7 -> {
                settingsBinding.checkBoxHorizontal.toggle()
                settingsBinding.checkBoxVertical.toggle()
                settingsBinding.checkBoxDiagonal.toggle()
            }
        }

        settingsBinding.previousSet.setOnClickListener {
            currentLevel--

            if(currentLevel == 0){
                settingsBinding.previousSet.visibility = View.INVISIBLE
            } else if (currentLevel == 1) {
                settingsBinding.nextSet.visibility = View.VISIBLE
            }

            updateLevel(currentLevel)
            settingsBinding.levelInGameSet.text = resources.getStringArray(R.array.game_level)[currentLevel]
        }

        settingsBinding.nextSet.setOnClickListener {
            currentLevel++

            if(currentLevel == 2){
                settingsBinding.nextSet.visibility = View.INVISIBLE
            } else if (currentLevel == 1) {
                settingsBinding.previousSet.visibility = View.VISIBLE
            }

            updateLevel(currentLevel)
            settingsBinding.levelInGameSet.text = resources.getStringArray(R.array.game_level)[currentLevel]
        }

        settingsBinding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, progress: Int, p2: Boolean) {
                currentVolumeSound = progress
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(p0: SeekBar?) {
                updateVolumeSound(currentVolumeSound)
            }

        })

        settingsBinding.checkBoxHorizontal.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                currentRules += 1
            } else {
                currentRules -= 1
            }

            updateRules(currentRules)
        }

        settingsBinding.checkBoxVertical.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                currentRules += 2
            } else {
                currentRules -= 2
            }

            updateRules(currentRules)
        }

        settingsBinding.checkBoxDiagonal.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                currentRules += 4
            } else {
                currentRules -= 4
            }

            updateRules(currentRules)
        }

        settingsBinding.backButtonSet.setOnClickListener {
            setResult(RESULT_OK)
            onBackPressed()
        }

        setContentView(settingsBinding.root)
    }

    private fun updateVolumeSound(volume: Int){
        getSharedPreferences("game", MODE_PRIVATE).edit().apply{
            putInt(PREF_SOUND, volume)
            apply()
        }
        setResult(RESULT_OK)
    }

    private fun updateLevel(level: Int){
        getSharedPreferences("game", MODE_PRIVATE).edit().apply {
            putInt(PREF_LEVEL, level)
            apply()
        }
        setResult(RESULT_OK)
    }

    private fun updateRules(rules: Int){
        getSharedPreferences("game", MODE_PRIVATE).edit().apply {
            putInt(PREF_RULES, rules)
            apply()
        }
        setResult(RESULT_OK)
    }

    private fun getCurrentSettings(): SettingInfo {
        this.getSharedPreferences("game", MODE_PRIVATE).apply {

            val sound = getInt(PREF_SOUND, 100)
            val level = getInt(PREF_LEVEL, 1)
            val rules = getInt(PREF_RULES, 7)

            return SettingInfo(sound, level, rules)
        }
    }

    data class SettingInfo(val sound: Int, val level: Int, val rules: Int)

    companion object {
        const val PREF_SOUND = "my.tick_tac_toe.SOUND"
        const val PREF_LEVEL = "my.tick_tac_toe.LEVEL"
        const val PREF_RULES = "my.tick_tac_toe.RULES"
    }
}