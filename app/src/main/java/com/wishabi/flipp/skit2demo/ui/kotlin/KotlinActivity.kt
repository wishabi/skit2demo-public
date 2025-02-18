package com.wishabi.flipp.com.wishabi.flipp.skit2demo.ui.kotlin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.wishabi.flipp.skit2demo.databinding.ActivityKotlinBinding

class KotlinActivity : AppCompatActivity() {

    private lateinit var binding: ActivityKotlinBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKotlinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.storefrontCtaFromKotlin.setOnClickListener { _ ->
            val intent = Intent(this, StorefrontKotlinActivity::class.java)
            startActivity(intent)
        }
    }
}