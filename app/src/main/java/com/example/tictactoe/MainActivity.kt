   package com.example.tictactoe

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.tictactoe.databinding.ActivityMainBinding

   class MainActivity : AppCompatActivity() {
       private lateinit var binding: ActivityMainBinding
       override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)

        binding.startButtonMain.setOnClickListener {
            val data = getInfoGame()
            val intent = GameActivity.newIntent(this@MainActivity,data.time, data.gameField)
            startActivity(intent)
        }
        binding.settingsMain.setOnClickListener {
            val data = getInfoGame()
            val intent = SettingActivity.newIntent(this@MainActivity, data.time, data.gameField)
            startActivity(intent)
        }
        binding.continueGameMain.setOnClickListener {
            val data = getInfoGame()
            val intent = GameActivity.newIntent(this@MainActivity, data.time, data.gameField)
            startActivity(intent)
        }
        setContentView(binding.root)
       }

       data class InfoGame(val time: Long, val gameField: String){}

       private fun getInfoGame(): InfoGame {
           with(getSharedPreferences(getString(R.string.preference_file_key), MODE_PRIVATE)) {
               val time = getLong("time", 0L)
               val gameField = getString("gameField", "")

               return if(gameField != null) {
                   InfoGame(time, gameField)
               } else {
                   InfoGame(0L, "")
               }
           }
       }


}

