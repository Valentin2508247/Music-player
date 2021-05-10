package com.example.musicplayer.activities

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.musicplayer.R
import com.example.musicplayer.firebase.FirebaseConsts
import com.example.musicplayer.firebase.SongsFirebase
import com.example.musicplayer.fragments.BlankFragment
import com.example.musicplayer.fragments.PlaylistListFragment
import com.example.musicplayer.fragments.ScrollingFragment
import com.example.musicplayer.fragments.SongListFragment
import com.example.musicplayer.player.Audio
import com.example.musicplayer.player.MediaPlayerService
import com.example.musicplayer.player.StorageUtil
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class MainActivity : AppCompatActivity(), PopupMenu.OnMenuItemClickListener {
    private val TAG = "MainActivity2"

    lateinit var mAuth: FirebaseAuth
    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var firebaseStorage: FirebaseStorage
    lateinit var currentUser: FirebaseUser
    lateinit var viewPager: ViewPager2
    lateinit var pagesAdapter: ScreenSlidePagerAdapter
    lateinit var bottomNavigation: BottomNavigationView

    // Music player service
    private var player: MediaPlayerService? = null
    private var serviceBound = false
    var audioList: ArrayList<Audio>? = null
    //Binding this Client to the AudioPlayer Service
    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as MediaPlayerService.LocalBinder
            player = binder.service
            serviceBound = true
            Toast.makeText(this@MainActivity, "Service Bound", Toast.LENGTH_SHORT).show()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            serviceBound = false
        }
    }


//    @Inject
//    lateinit var mDatabase: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.my_toolbar))

        //(application as MyApplication).appComponent.inject(this)
        firebaseDatabase = FirebaseDatabase.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
        mAuth = FirebaseAuth.getInstance()

        val songsFirebase = SongsFirebase(firebaseDatabase, firebaseStorage)


        initViews()

        loadAudio();
        //play the first audio in the ArrayList




        // firebase loading songs
//        val path = FirebaseConsts.songsRef
//        val maybe: Maybe<MutableList<Song>> = songsFirebase.readSongs(path)
//        val disposable = maybe.subscribe({
//                // onSuccess
//                Log.d(TAG, "onSuccess")
//                for (song: Song? in it){
//                    Log.d(TAG, "$song")
//                }
//            },
//            {
//                // onError
//                Log.d(TAG, "onError")
//                Log.d(TAG, it.message)
//                it.printStackTrace()
//            },
//            {
//                // onComplete
//                Log.d(TAG, "onComplete")
//            })


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

    private fun initViews() {
        viewPager = findViewById(R.id.view_pager)
        bottomNavigation = findViewById(R.id.bottom_navigation)
        setupViewPager()
        setupBottomNavigation()
        findViewById<Button>(R.id.load_songs_button).setOnClickListener {
//            GlobalScope.launch {
//                val songs = songsFirebase.readSongsUsingCoroutines(FirebaseConsts.songsDatabaseRef)
////                songs?.let {
////                    for (song in it)
////                        Log.d(TAG, "$song")
////                }
//            }
            audioList?.let{
                playAudio(1)
            }

//            val intent = Intent(this@MainActivity, NowPlayingActivity::class.java)
//            startActivity(intent)
        }
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

    private fun setupViewPager(){
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
    }

    private fun setupBottomNavigation(){
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
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        if (menu is MenuBuilder) (menu as MenuBuilder).setOptionalIconsVisible(true)
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.action_create_playlist -> {
                Toast.makeText(this, "Create playlist", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, CreatePlaylistActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_upload_song -> {
                Toast.makeText(this, "Upload song", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, UploadSongActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putBoolean("ServiceState", serviceBound)
        super.onSaveInstanceState(savedInstanceState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        serviceBound = savedInstanceState.getBoolean("ServiceState")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (serviceBound) {
            unbindService(serviceConnection)
            //service is active
            player!!.stopSelf()
        }
    }

    private fun playAudio(audioIndex: Int) {
        //Check is service is active
        if (!serviceBound) {
            //Store Serializable audioList to SharedPreferences
            val storage = StorageUtil(applicationContext)
            storage.storeAudio(audioList)
            storage.storeAudioIndex(audioIndex)
            val playerIntent = Intent(this, MediaPlayerService::class.java)
            startService(playerIntent)
            bindService(playerIntent, serviceConnection, BIND_AUTO_CREATE)
        } else {
            //Store the new audioIndex to SharedPreferences
            val storage = StorageUtil(applicationContext)
            storage.storeAudioIndex(audioIndex)

            //Service is active
            //Send a broadcast to the service -> PLAY_NEW_AUDIO
            val broadcastIntent = Intent(NowPlayingActivity.Broadcast_PLAY_NEW_AUDIO)
            sendBroadcast(broadcastIntent)
        }
    }

    private fun loadAudio() {
        val contentResolver = contentResolver
        val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0"
        val sortOrder = MediaStore.Audio.Media.TITLE + " ASC"
        val cursor: Cursor? = contentResolver.query(uri, null, selection, null, sortOrder)
        if (cursor != null && cursor.getCount() > 0) {
            audioList = ArrayList()
            while (cursor.moveToNext()) {
                val data: String = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                val title: String = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                val album: String = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM))
                val artist: String = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))

                // Save to audioList
                audioList!!.add(Audio(data, title, album, artist))
            }
        }
        cursor?.close()
    }
}