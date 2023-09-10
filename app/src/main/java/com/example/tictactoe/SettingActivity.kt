package com.example.tictactoe

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SeekBar
import com.example.tictactoe.databinding.ActivitySettingBinding

class SettingActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingBinding

    private var currentSound = 0
    private var currentLevel = 0
    private var currentRules = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingBinding.inflate(layoutInflater)
/*
        binding.previousSet.setOnClickListener{

        }
        binding.nextSet.setOnClickListener{

        }

        binding.seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                TODO("Not yet implemented")
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                TODO("Not yet implemented")
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                TODO("Not yet implemented")
            }
        })
        binding.checkBox1Set.setOnClickListener{

        }
        binding.checkBox2Set.setOnClickListener{

        }
        binding.checkBox3Set.setOnClickListener{

        }

 */
        setContentView(binding.root)
    }

    companion object {
        fun newIntent(context: Context, time: Long, gameField: String): Intent {
            val intent =  Intent(context, SettingActivity::class.java)
            intent.putExtra(EXTRA_TIME, time)
            intent.putExtra(EXTRA_GAME_FIELD, gameField)
            return intent
        }

        private const val EXTRA_TIME = "Time"
        private const val EXTRA_GAME_FIELD = "GameField"
    }
}