package com.example.pinch

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.pinch.ui.main.GameListFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, GameListFragment.newInstance())
                .commitNow()
        }
    }

}
