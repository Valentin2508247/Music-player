package com.example.musicplayer.activities

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.musicplayer.R
import com.example.musicplayer.fragments.BlankFragment
import com.example.musicplayer.fragments.PlaylistListFragment
import com.example.musicplayer.fragments.ScrollingFragment
import com.example.musicplayer.fragments.SongListFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


class MainActivity : AppCompatActivity(), PopupMenu.OnMenuItemClickListener {
    lateinit var mAuth: FirebaseAuth
    lateinit var currentUser: FirebaseUser
    lateinit var viewPager: ViewPager2
    lateinit var pagesAdapter: ScreenSlidePagerAdapter
    lateinit var bottomNavigation: BottomNavigationView

    //lateinit var bottomNavigationListener: BottomNavigationView.OnNavigationItemSelectedListener
//    @Inject
//    lateinit var mDatabase: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //(application as MyApplication).appComponent.inject(this)

        viewPager = findViewById(R.id.view_pager)
        bottomNavigation = findViewById(R.id.bottom_navigation)
        pagesAdapter = ScreenSlidePagerAdapter(this)
        viewPager.adapter = pagesAdapter

        class ViewPager2PageChangeCallback() : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                //Toast.makeText(this@MainActivity, "Position: $position", Toast.LENGTH_SHORT).show()

                val item_id = when (position) {
                    0 -> R.id.scrolling_menu_item
                    1 -> R.id.playlists_menu_item
                    2 -> R.id.songs_menu_item
                    else -> R.id.scrolling_menu_item
                }

                bottomNavigation.selectedItemId = item_id
            }
        }
        viewPager.registerOnPageChangeCallback(ViewPager2PageChangeCallback())


        val bottomNavigationListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.scrolling_menu_item -> {
                    viewPager.currentItem = 0
                    return@OnNavigationItemSelectedListener true
                }
                R.id.playlists_menu_item -> {
                    viewPager.currentItem = 1
                    return@OnNavigationItemSelectedListener true
                }
                R.id.songs_menu_item -> {
                    viewPager.currentItem = 2
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        }
        bottomNavigation.setOnNavigationItemSelectedListener(bottomNavigationListener)

        mAuth = FirebaseAuth.getInstance()


//        // SnackBar example
//        class MyUndoListener : View.OnClickListener {
//            override fun onClick(v: View) {
//                // Code to undo the user's last action
//
//            }
//        }
//        val mySnackbar = Snackbar.make(
//                findViewById(R.id.myCoordinatorLayout),
//                "Hello world",
//                Snackbar.LENGTH_LONG
//        ).setAction("UNDO", MyUndoListener())
//                .show()
//
//
//        // Alert example
//        AlertDialog.Builder(this)
//            .setTitle("Test dialog")
//            .setMessage("Hello world from dialog")
//            .setPositiveButton("Ok"){ dialogInterface: DialogInterface, i: Int ->
//
//            }
//            .setNegativeButton("Cancel"){ dialogInterface: DialogInterface, i: Int ->
//
//            }
//            .show()
//    }
//
//    fun showPopup(view: View){
//        PopupMenu(this, view).apply {
//            // MainActivity implements OnMenuItemClickListener
//            setOnMenuItemClickListener(this@MainActivity)
//            inflate(R.menu.popup_menu)
//            show()
//        }
    }

    override fun onStart() {
        super.onStart()

        if (mAuth.currentUser != null){
            currentUser = mAuth.currentUser
        }
        else{
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.popup_delete -> {
                //archive(item)
                true
            }
            R.id.popup_edit -> {
                //delete(item)
                true
            }
            else -> false
        }
    }

    override fun onBackPressed() {
        if (viewPager.currentItem == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed()
        } else {
            // Otherwise, select the previous step.
            viewPager.currentItem = viewPager.currentItem - 1
        }
    }

    inner class ScreenSlidePagerAdapter(activity: MainActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = 3


        override fun createFragment(position: Int): Fragment =
            when (position)
            {
                0 -> {
                    ScrollingFragment()
                }
                1 -> {
                    PlaylistListFragment()
                }
                2 -> {
                    SongListFragment()
                }
                else -> BlankFragment()
            }
    }
}