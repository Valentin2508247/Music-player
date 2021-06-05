package com.example.musicplayer.activities

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.ActivityInfo
import android.database.Cursor
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.musicplayer.R
import com.example.musicplayer.database.AppDatabase
import com.example.musicplayer.database.Playlist
import com.example.musicplayer.database.Song
import com.example.musicplayer.firebase.SongsFirebase
import com.example.musicplayer.fragments.PlaylistListFragment
import com.example.musicplayer.fragments.SongListFragment
import com.example.musicplayer.player.Audio
import com.example.musicplayer.player.MediaPlayerService
import com.example.musicplayer.player.StorageUtil
import com.example.musicplayer.repositories.SongsRepository
import com.example.musicplayer.view_models.MainActivityViewModel
import com.example.musicplayer.view_models.MainActivityViewModelFactory
import com.example.musicplayer.workers.UpdatePlaylistWorker
import com.example.musicplayer.workers.UpdateSongsWorker
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage


class MainActivity : AppCompatActivity(), PopupMenu.OnMenuItemClickListener, SongListFragment.SongItemClickListener,
        PlaylistListFragment.PlaylistItemClickListener {
    private val TAG = "MainActivity2"


    private lateinit var viewModel: MainActivityViewModel
    private lateinit var mAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var mDatabase: AppDatabase
    private lateinit var repository: SongsRepository
    private lateinit var currentUser: FirebaseUser
    private lateinit var viewPager: ViewPager2
    private  lateinit var pagesAdapter: ScreenSlidePagerAdapter
    private lateinit var bottomNavigation: BottomNavigationView

    // Music player service
    private var player: MediaPlayerService? = null
    private var serviceBound = false
    var audioList: ArrayList<Audio>? = null
    private val songList: ArrayList<Song> = ArrayList()
    companion object{
        const val Broadcast_PLAY_NEW_AUDIO = "com.example.musicplayer.activities.PlayNewAudio"
    }
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

        Log.d(TAG, "Main activity onCreate")


        //(application as MyApplication).appComponent.inject(this)
        firebaseDatabase = FirebaseDatabase.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
        mDatabase = AppDatabase.getDatabase(this)
        repository = SongsRepository(mDatabase.songDao(), mDatabase.likesDao(), SongsFirebase(firebaseDatabase, firebaseStorage))
        mAuth = FirebaseAuth.getInstance()

        viewModel = ViewModelProvider(this, MainActivityViewModelFactory(repository))
                .get(MainActivityViewModel::class.java)



        initViews()
        scheduleTasks()
        //loadAudio()
        //play the first audio in the ArrayList


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
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

        supportActionBar!!.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.my_black)))
        viewPager = findViewById(R.id.view_pager)
        bottomNavigation = findViewById(R.id.bottom_navigation)
        setupViewPager()
        setupBottomNavigation()
    }

    private fun scheduleTasks() {
        var workManager = WorkManager.getInstance(this)

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()


        val uploadSongsWorkRequest = OneTimeWorkRequestBuilder<UpdateSongsWorker>()
            .setConstraints(constraints)
            .build()

        val uploadPlaylistsWorkRequest = OneTimeWorkRequestBuilder<UpdatePlaylistWorker>()
            .setConstraints(constraints)
            .build()

//        val uploadSongsWorkRequest = PeriodicWorkRequestBuilder<UpdateSongsWorker>(24, TimeUnit.DAYS)
//            .setConstraints(constraints)
//            .build()

        workManager.enqueue(uploadSongsWorkRequest)
        workManager.enqueue(uploadPlaylistsWorkRequest)
        //UpdateSongsIntentService.startActionUpdateSongs(applicationContext, "param1", "param2")

//        appComponent = DaggerApplicationComponent
//                .builder()
//                .build()
    }

    override fun onStart() {
        super.onStart()

        Log.d(TAG, "Main activity onStart")
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
        override fun getItemCount(): Int = 4


        override fun createFragment(position: Int): Fragment =
            when (position)
            {
                0 -> {
                    PlaylistListFragment()
                }
                1 -> {
                    PlaylistListFragment()
                }
                2 -> {
//                    0 -> AllSongs()
//                    1 -> PlaylistSongs()
//                    2 -> LikedSongs()
                    SongListFragment.newInstance(1, 2)
                }
                3 -> {
                    SongListFragment.newInstance(1, 0)
                }
                else -> PlaylistListFragment()
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
                    0 -> R.id.shared_playlists_menu_item
                    1 -> R.id.playlists_menu_item
                    2 -> R.id.liked_menu_item
                    3 -> R.id.songs_menu_item
                    else -> R.id.shared_playlists_menu_item
                }
                bottomNavigation.selectedItemId = item_id
            }
        }
        viewPager.registerOnPageChangeCallback(ViewPager2PageChangeCallback())
    }

    private fun setupBottomNavigation(){
        val bottomNavigationListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.shared_playlists_menu_item -> {
                    viewPager.currentItem = 0
                    return@OnNavigationItemSelectedListener true
                }
                R.id.playlists_menu_item -> {
                    viewPager.currentItem = 1
                    return@OnNavigationItemSelectedListener true
                }
                R.id.liked_menu_item -> {
                    viewPager.currentItem = 2
                    return@OnNavigationItemSelectedListener true
                }
                R.id.songs_menu_item -> {
                    viewPager.currentItem = 3
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
            R.id.action_sign_out -> {
                Toast.makeText(this, "Sign out", Toast.LENGTH_SHORT).show()
                mAuth.signOut()
                finish()
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

    private fun playSong(songIndex: Int) {
        //Check is service is active
        if (!serviceBound) {
            //Store Serializable audioList to SharedPreferences
            val storage = StorageUtil(applicationContext)
            storage.storeSong(songList)
            storage.storeSongIndex(songIndex)
            val playerIntent = Intent(this, MediaPlayerService::class.java)
            startService(playerIntent)
            bindService(playerIntent, serviceConnection, BIND_AUTO_CREATE)
        } else {
            //Store the new audioIndex to SharedPreferences
            val storage = StorageUtil(applicationContext)
            storage.storeSongIndex(songIndex)

            //Service is active
            //Send a broadcast to the service -> PLAY_NEW_AUDIO
            val broadcastIntent = Intent(Broadcast_PLAY_NEW_AUDIO)
            sendBroadcast(broadcastIntent)
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
            val broadcastIntent = Intent(Broadcast_PLAY_NEW_AUDIO)
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

    override fun songItemClick(song: Song) {
        songList.add(song)
    }

    override fun playSong(pos: Int, songs: List<Song>) {
        //Check is service is active

        if (!serviceBound) {
            //Store Serializable audioList to SharedPreferences
            val storage = StorageUtil(applicationContext)
            storage.storeSong(ArrayList(songs))
            storage.storeSongIndex(pos)
            val playerIntent = Intent(this, MediaPlayerService::class.java)
            startService(playerIntent)
            bindService(playerIntent, serviceConnection, BIND_AUTO_CREATE)
        } else {
            //Store the new audioIndex to SharedPreferences
            val storage = StorageUtil(applicationContext)
            storage.clearCachedSongPlaylist()
            storage.storeSong(ArrayList(songs))
            storage.storeSongIndex(pos)

            //Service is active
            //Send a broadcast to the service -> PLAY_NEW_AUDIO
            val broadcastIntent = Intent(Broadcast_PLAY_NEW_AUDIO)
            sendBroadcast(broadcastIntent)
        }
    }

    override fun playlistItemClick(playlist: Playlist) {
        viewModel.playlist.value = playlist
        openPlaylistActivity(playlist.id)
    }

    private fun openPlaylistActivity(playlistId: String){
        intent = Intent(this, PlaylistSongsActivity::class.java)
        intent.putExtra(SongListFragment.ARG_PLAYLIST, playlistId)
        startActivity(intent)
    }
}