package com.example.musicplayer.activities

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.musicplayer.R
import com.example.musicplayer.database.AppDatabase
import com.example.musicplayer.firebase.SongsFirebase
import com.example.musicplayer.player.Audio
import com.example.musicplayer.player.MediaPlayerService
import com.example.musicplayer.player.MediaPlayerService.LocalBinder
import com.example.musicplayer.player.StorageUtil
import com.example.musicplayer.repositories.SongsRepository
import com.example.musicplayer.view_models.NowPlayingViewModel
import com.example.musicplayer.view_models.NowPlayingViewModelFactory
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage


class NowPlayingActivity : AppCompatActivity() {
    private final val TAG = "NowPlayingActivity2"

    private var player: MediaPlayerService? = null
    var serviceBound = false
    var audioList: ArrayList<Audio>? = null
    companion object{
        const val Broadcast_PLAY_NEW_AUDIO = "com.example.musicplayer.activities.PlayNewAudio"
    }
// Change to your package name


    private lateinit var viewModel: NowPlayingViewModel
    private lateinit var mDatabase: AppDatabase
    private lateinit var repository: SongsRepository
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var firebaseStorage: FirebaseStorage


    private lateinit var button: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_now_playing)

        firebaseDatabase = FirebaseDatabase.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
        mDatabase = AppDatabase.getDatabase(this)
        repository = SongsRepository(
            mDatabase.songDao(), SongsFirebase(
                firebaseDatabase,
                firebaseStorage
            )
        )
        viewModel = ViewModelProvider(this, NowPlayingViewModelFactory(repository))
                .get(NowPlayingViewModel::class.java)


        viewModel.songsLiveData.observe(this){ songs ->
            Log.d(TAG, "Observing livedata")
            try {

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        button = findViewById(R.id.button2)
        button.setOnClickListener {
            //playAudio("https://upload.wikimedia.org/wikipedia/commons/6/6c/Grieg_Lyric_Pieces_Kobold.ogg");
            // TODO: clicks on button
//            try {
//                mPlayerAdapter!!.setCurrentSong(song, songs)
//                mPlayerAdapter!!.initMediaPlayer()
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
        }

        loadAudio();
        //play the first audio in the ArrayList
        audioList?.let{
            playAudio(1)
        }

    }

    //Binding this Client to the AudioPlayer Service
    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as LocalBinder
            player = binder.service
            serviceBound = true
            Toast.makeText(this@NowPlayingActivity, "Service Bound", Toast.LENGTH_SHORT).show()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            serviceBound = false
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