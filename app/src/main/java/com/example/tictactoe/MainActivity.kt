   package com.example.tictactoe

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

   class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        /*
        binding = ResultProfileBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

         */



        val intent = SettingActivity.newIntent(this@MainActivity)
        startActivity(intent)
    }
}

