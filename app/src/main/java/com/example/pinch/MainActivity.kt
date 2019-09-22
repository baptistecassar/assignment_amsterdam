package com.example.pinch

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.pinch.data.service.GamesApiClient
import com.example.pinch.ui.main.GameListFragment

class MainActivity : AppCompatActivity() {

    companion object {
        private val TAG = GamesApiClient::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            startFragment(GameListFragment.newInstance())
        }
    }

    /**
     * on back press, return when it's possible
     */
    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 1) {
            super.onBackPressed()
        } else {
            finish()
        }
    }

    /**
     * if user presses on home button go back
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * starts a fragment and adding it into back stack
     */
    fun startFragment(fragment: Fragment) {
        val backStateName = fragment.javaClass.simpleName
        Log.v(TAG, "startFragment : $backStateName")
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.container, fragment, backStateName)
        fragmentTransaction.addToBackStack(backStateName)
        fragmentTransaction.commit()
    }
}