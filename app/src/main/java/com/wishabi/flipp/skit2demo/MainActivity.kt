package com.wishabi.flipp.skit2demo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.wishabi.flipp.com.wishabi.flipp.skit2demo.ui.kotlin.KotlinActivity
import com.wishabi.flipp.skit2demo.databinding.ActivityMainBinding
import com.wishabi.flipp.skit2demo.ui.java.JavaActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        binding.kotlinActivity.setOnClickListener { _ ->
            val intent = Intent(this, KotlinActivity::class.java)
            startActivity(intent)
        }

        binding.javaActivity.setOnClickListener { _ ->
            val intent = Intent(this, JavaActivity::class.java)
            startActivity(intent)
        }
    }
}