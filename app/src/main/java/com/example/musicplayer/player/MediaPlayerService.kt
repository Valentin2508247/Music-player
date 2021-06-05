package com.example.musicplayer.player

import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.media.MediaPlayer
import android.media.MediaPlayer.*
import android.media.session.MediaSessionManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.RemoteException
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.musicplayer.activities.MainActivity
import com.example.musicplayer.database.Song
import com.example.musicplayer.firebase.FirebaseConsts
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.io.IOException


class MediaPlayerService : Service(), OnCompletionListener,
    OnPreparedListener, OnErrorListener, OnSeekCompleteListener, OnInfoListener,
    OnBufferingUpdateListener, OnAudioFocusChangeListener {
    private val TAG = "MediaPlayerService2"

    // notification channel id
    private val CHANNEL_ID = "action.CHANNEL_ID"
    private var notificationManager: NotificationManager? = null

    private var mediaPlayer: MediaPlayer? = null
    private lateinit var audioManager: AudioManager

    private var audioList: ArrayList<Audio>? = null
    private var audioIndex = -1
    private var activeAudio : Audio? = null
    //an object of the currently playing audio

    private var songList: ArrayList<Song>? = null
    private var songIndex = -1
    private var activeSong : Song? = null
    //an object of the currently playing song


    //path to the audio file
    private var mediaFile: String? = null
    private var resumePosition = 0

    //Handle incoming phone calls
    private var ongoingCall = false
    private var phoneStateListener: PhoneStateListener? = null
    private var telephonyManager: TelephonyManager? = null

    val ACTION_PLAY = "com.valdioveliu.valdio.audioplayer.ACTION_PLAY"
    val ACTION_PAUSE = "com.valdioveliu.valdio.audioplayer.ACTION_PAUSE"
    val ACTION_PREVIOUS = "com.valdioveliu.valdio.audioplayer.ACTION_PREVIOUS"
    val ACTION_NEXT = "com.valdioveliu.valdio.audioplayer.ACTION_NEXT"
    val ACTION_STOP = "com.valdioveliu.valdio.audioplayer.ACTION_STOP"

    //MediaSession
    private var mediaSessionManager: MediaSessionManager? = null
    private var mediaSession: MediaSessionCompat? = null
    private var transportControls: MediaControllerCompat.TransportControls? = null

    //AudioPlayer notification ID
    private val NOTIFICATION_ID = 101

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            //Load data from SharedPreferences
            val storage = StorageUtil(applicationContext)
//            audioList = storage.loadAudio()
//            audioIndex = storage.loadAudioIndex()
//            if (audioIndex != -1 && audioIndex < audioList!!.size) {
//                //index is in a valid range
//                activeAudio = audioList!![audioIndex]
//            } else {
//                stopSelf()
//            }

            songList = storage.loadSong()
            songIndex = storage.loadSongIndex()
            if (songIndex != -1 && songIndex < songList!!.size) {
                //index is in a valid range
                activeSong = songList!![songIndex]
            } else {
                stopSelf()
            }

//            if (activeSong == null){
//                Toast.makeText(this, "Songs have not been loaded yet", Toast.LENGTH_LONG).show()
//                stopSelf()
//            }

        } catch (e: java.lang.NullPointerException) {
            stopSelf()
        }

        //Request audio focus
        if (requestAudioFocus() == false) {
            //Could not gain focus
            stopSelf()
        }
        if (mediaSessionManager == null) {
            try {
                initMediaSession()
                initMediaPlayer()
            } catch (e: RemoteException) {
                e.printStackTrace()
                stopSelf()
            }
            buildNotification(PlaybackStatus.PLAYING)
        }

        //Handle Intent action from MediaSession.TransportControls
        handleIncomingActions(intent)
        return super.onStartCommand(intent, flags, startId)
    }

    @Throws(RemoteException::class)
    private fun initMediaSession() {
        if (mediaSessionManager != null)
            return  //mediaSessionManager exists
        mediaSessionManager = getSystemService(MEDIA_SESSION_SERVICE) as MediaSessionManager
        // Create a new MediaSession
        mediaSession = MediaSessionCompat(applicationContext, "AudioPlayer")
        //Get MediaSessions transport controls
        transportControls = mediaSession!!.controller.transportControls
        //set MediaSession -> ready to receive media commands
        mediaSession!!.setActive(true)
        //indicate that the MediaSession handles transport control commands
        // through its MediaSessionCompat.Callback.
        mediaSession!!.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)

        //Set mediaSession's MetaData
        updateMetaData()

        // Attach Callback to receive MediaSession updates
        mediaSession!!.setCallback(object : MediaSessionCompat.Callback() {
            // Implement callbacks
            override fun onPlay() {
                super.onPlay()
                Log.d(TAG, "mediaSession onPlay")
                resumeMedia()
                buildNotification(PlaybackStatus.PLAYING)
            }

            override fun onPause() {
                super.onPause()
                Log.d(TAG, "mediaSession onPause")
                pauseMedia()
                buildNotification(PlaybackStatus.PAUSED)
            }

            override fun onSkipToNext() {
                super.onSkipToNext()
                Log.d(TAG, "mediaSession onSkipToNext")
                skipToNext()
                updateMetaData()
                buildNotification(PlaybackStatus.PLAYING)
            }

            override fun onSkipToPrevious() {
                super.onSkipToPrevious()
                Log.d(TAG, "mediaSession onSkipToPrevious")
                skipToPrevious()
                updateMetaData()
                buildNotification(PlaybackStatus.PLAYING)
            }

            override fun onStop() {
                super.onStop()
                Log.d(TAG, "mediaSession onStop")
                removeNotification()
                //Stop the service
                stopSelf()
            }

            override fun onSeekTo(position: Long) {
                super.onSeekTo(position)
            }
        })
    }

    @RequiresApi(26)
    private fun createNotificationChannel() {
        if (notificationManager == null)
            notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (notificationManager!!.getNotificationChannel(CHANNEL_ID) == null) {
            val notificationChannel = NotificationChannel(CHANNEL_ID,
                    "Music player",
                    NotificationManager.IMPORTANCE_HIGH)

            notificationChannel.description = "Music player"

            notificationChannel.enableLights(false)
            notificationChannel.enableVibration(false)
            notificationChannel.setShowBadge(false)

            notificationManager!!.createNotificationChannel(notificationChannel)
        }
    }

    private fun buildNotification(playbackStatus: PlaybackStatus) {
        Log.d(TAG, "buildNotification")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }

        var notificationAction = R.drawable.ic_media_pause //needs to be initialized
        var play_pauseAction: PendingIntent? = null

        //Build a new notification according to the current state of the MediaPlayer
        if (playbackStatus === PlaybackStatus.PLAYING) {
            notificationAction = R.drawable.ic_media_pause
            //create the pause action
            play_pauseAction = playbackAction(1)
        } else if (playbackStatus === PlaybackStatus.PAUSED) {
            notificationAction = R.drawable.ic_media_play
            //create the play action
            play_pauseAction = playbackAction(0)
        }
        val largeIcon = BitmapFactory.decodeResource(
            resources,
            R.drawable.ic_media_play
        ) //replace with your own image

        // Create a new Notification
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)

        notificationBuilder.
        setShowWhen(false) // Set the Notification style
                .setStyle(
                        androidx.media.app.NotificationCompat.MediaStyle() // Attach our MediaSession token
                                .setMediaSession(mediaSession!!.sessionToken) // Show our playback controls in the compact notification view.
                                .setShowActionsInCompactView(0, 1, 2)
                ) // Set the Notification color
                //.setColor(resources.getColor(R.color.background_light)) // Set the large and small icons
                .setLargeIcon(largeIcon)
                .setSmallIcon(R.drawable.stat_sys_headset) // Set Notification content information
                .setContentText(activeSong!!.songName) // TODO: playlist name
                .setContentTitle(activeSong!!.performer)
                .setContentInfo(activeSong!!.songName) // Add playback actions
                .addAction(R.drawable.ic_media_previous, "previous", playbackAction(3))
                .addAction(notificationAction, "pause", play_pauseAction)
                .addAction(
                        R.drawable.ic_media_next,
                        "next",
                        playbackAction(2)
                ) as NotificationCompat.Builder
        if (activeSong!!.imageUrl != null)
        {
            var largeIcon = StorageUtil.bitmapFromUrl(activeSong!!.imageUrl!!)
            largeIcon?.let {
                notificationBuilder.setLargeIcon(it)
            }
        }
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).notify(
            NOTIFICATION_ID,
            notificationBuilder.build()
        )
    }

    private fun removeNotification() {
        Log.d(TAG, "removeNotification")
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }

    private fun handleIncomingActions(playbackAction: Intent?) {
        if (playbackAction == null || playbackAction.action == null) return
        val actionString = playbackAction.action
        if (actionString.equals(ACTION_PLAY, ignoreCase = true)) {
            transportControls!!.play()
        } else if (actionString.equals(ACTION_PAUSE, ignoreCase = true)) {
            transportControls!!.pause()
        } else if (actionString.equals(ACTION_NEXT, ignoreCase = true)) {
            transportControls!!.skipToNext()
        } else if (actionString.equals(ACTION_PREVIOUS, ignoreCase = true)) {
            transportControls!!.skipToPrevious()
        } else if (actionString.equals(ACTION_STOP, ignoreCase = true)) {
            transportControls!!.stop()
        }
    }

    private fun playbackAction(actionNumber: Int): PendingIntent? {
        val playbackAction = Intent(this, MediaPlayerService::class.java)
        when (actionNumber) {
            0 -> {
                // Play
                playbackAction.action = ACTION_PLAY
                return PendingIntent.getService(this, actionNumber, playbackAction, 0)
            }
            1 -> {
                // Pause
                playbackAction.action = ACTION_PAUSE
                return PendingIntent.getService(this, actionNumber, playbackAction, 0)
            }
            2 -> {
                // Next track
                playbackAction.action = ACTION_NEXT
                return PendingIntent.getService(this, actionNumber, playbackAction, 0)
            }
            3 -> {
                // Previous track
                playbackAction.action = ACTION_PREVIOUS
                return PendingIntent.getService(this, actionNumber, playbackAction, 0)
            }
            else -> {
            }
        }
        return null
    }

    private fun updateMetaData() {
        Log.d(TAG, "updateMetaData. Audio: $activeAudio}")
        val albumArt = BitmapFactory.decodeResource(
            resources,
            R.drawable.ic_media_play // TODO: change icon
        ) //replace with medias albumArt
        // Update the current metadata
        mediaSession!!.setMetadata(
                MediaMetadataCompat.Builder()
                        .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)
                        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, activeSong!!.performer)
                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, activeSong!!.songName)
                        .build()
//        mediaSession!!.setMetadata(
//            MediaMetadataCompat.Builder()
//                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)
//                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, activeAudio!!.artist)
//                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, activeAudio!!.album)
//                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, activeAudio!!.title)
//                .build()
        )
    }

    private fun skipToNext() {
//        if (audioIndex == audioList!!.size - 1) {
//            //if last in playlist
//            audioIndex = 0
//            activeAudio = audioList!![audioIndex]
//        } else {
//            //get next in playlist
//            activeAudio = audioList!![++audioIndex]
//        }

        if (songIndex == songList!!.size - 1) {
            //if last in playlist
            songIndex = 0
            activeSong = songList!![songIndex]
        } else {
            //get next in playlist
            activeSong = songList!![++songIndex]
        }

        //Update stored index
        StorageUtil(applicationContext).storeSongIndex(songIndex)
        stopMedia()
        //reset mediaPlayer
        mediaPlayer!!.reset()
        initMediaPlayer()
    }

    private fun skipToPrevious() {
//        if (audioIndex == 0) {
//            //if first in playlist
//            //set index to the last of audioList
//            audioIndex = audioList!!.size - 1
//            activeAudio = audioList!![audioIndex]
//        } else {
//            //get previous in playlist
//            activeAudio = audioList!![--audioIndex]
//        }

        if (songIndex == 0) {
            //if first in playlist
            //set index to the last of audioList
            songIndex = songList!!.size - 1
            activeSong = songList!![songIndex]
        } else {
            //get previous in playlist
            activeSong = songList!![--songIndex]
        }

        //Update stored index
//        StorageUtil(applicationContext).storeAudioIndex(audioIndex)
        StorageUtil(applicationContext).storeSongIndex(songIndex)
        stopMedia()
        //reset mediaPlayer
        mediaPlayer!!.reset()
        initMediaPlayer()
    }

    //Handle incoming phone calls
    private fun callStateListener() {
        // Get the telephony manager
        telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        //Starting listening for PhoneState changes
        phoneStateListener = object : PhoneStateListener() {
            override fun onCallStateChanged(state: Int, incomingNumber: String) {
                when (state) {
                    TelephonyManager.CALL_STATE_OFFHOOK, TelephonyManager.CALL_STATE_RINGING -> if (mediaPlayer != null) {
                        pauseMedia()
                        ongoingCall = true
                    }
                    TelephonyManager.CALL_STATE_IDLE ->                   // Phone idle. Start playing.
                        if (mediaPlayer != null) {
                            if (ongoingCall) {
                                ongoingCall = false
                                resumeMedia()
                            }
                        }
                }
            }
        }
        // Register the listener with the telephony manager
        // Listen for changes to the device call state.
        telephonyManager!!.listen(
            phoneStateListener,
            PhoneStateListener.LISTEN_CALL_STATE
        )
    }

    //Becoming noisy
    private val becomingNoisyReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            //pause audio on ACTION_AUDIO_BECOMING_NOISY
            pauseMedia()
            buildNotification(PlaybackStatus.PAUSED)
        }
    }

    private fun registerBecomingNoisyReceiver() {
        //register after getting audio focus
        val intentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        registerReceiver(becomingNoisyReceiver, intentFilter)
    }

    private val playNewAudio: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
//            //Get the new media index form SharedPreferences
//            audioIndex = StorageUtil(applicationContext).loadAudioIndex()
//            Log.d(TAG, "onReceive in playNewAudio BroadCast receiver. AudioIndex: $audioIndex")
//            if (audioIndex != -1 && audioIndex < audioList!!.size) {
//                //index is in a valid range
//                activeAudio = audioList!![audioIndex]
//            } else {
//                stopSelf()
//            }
//
//            //A PLAY_NEW_AUDIO action received
//            //reset mediaPlayer to play the new Audio
//            stopMedia()
//            mediaPlayer!!.reset()
//            initMediaPlayer()
//            updateMetaData()
//            buildNotification(PlaybackStatus.PLAYING)
            //Get the new media index form SharedPreferences

            songIndex = StorageUtil(applicationContext).loadSongIndex()
            Log.d(TAG, "onReceive in playNewAudio BroadCast receiver. SongIndex: $songIndex")
            if (songIndex != -1 && songIndex < songList!!.size) {
                //index is in a valid range
                activeSong = songList!![songIndex]
            } else {
                stopSelf()
            }

            //A PLAY_NEW_AUDIO action received
            //reset mediaPlayer to play the new Audio
            stopMedia()
            mediaPlayer!!.reset()
            initMediaPlayer()
            updateMetaData()
            buildNotification(PlaybackStatus.PLAYING)
        }
    }

    private fun register_playNewAudio() {
        //Register playNewMedia receiver
        val filter = IntentFilter(MainActivity.Broadcast_PLAY_NEW_AUDIO)
        registerReceiver(playNewAudio, filter)
    }

    override fun onCreate() {
        super.onCreate()
        // Perform one-time setup procedures

        // Manage incoming phone calls during playback.
        // Pause MediaPlayer on incoming call,
        // Resume on hangup.
        callStateListener()
        //ACTION_AUDIO_BECOMING_NOISY -- change in audio outputs -- BroadcastReceiver
        registerBecomingNoisyReceiver()
        //Listen for new Audio to play -- BroadcastReceiver
        register_playNewAudio()
    }

    private fun initMediaPlayer() {
        mediaPlayer = MediaPlayer()
        //Set up MediaPlayer event listeners
        mediaPlayer!!.setOnCompletionListener(this)
        mediaPlayer!!.setOnErrorListener(this)
        mediaPlayer!!.setOnPreparedListener(this)
        mediaPlayer!!.setOnBufferingUpdateListener(this)
        mediaPlayer!!.setOnSeekCompleteListener(this)
        mediaPlayer!!.setOnInfoListener(this)
        //Reset so that the MediaPlayer is not pointing to another data source
        mediaPlayer!!.reset()
        mediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
        try {
            // Set the data source to the mediaFile location
            //mediaPlayer!!.setDataSource(activeAudio!!.data);
            mediaPlayer!!.setDataSource(activeSong!!.songUrl)
        } catch (e: IOException) {
            e.printStackTrace()
            stopSelf()
        } catch (e: NullPointerException){
            Toast.makeText(this, "Songs have not been loaded yet", Toast.LENGTH_LONG).show()
            stopSelf()
            return

//            if (activeSong == null){
//                Log.d(TAG, "Active song is null")
//                e.printStackTrace()
//                stopSelf()
//            }
//            else {
//                //activeSong.songUrl =
//                runBlocking {
//                    //val path = "${FirebaseConsts.songMusicStorage}/${activeSong!!.songPath}"
//                    Log.d(TAG, activeSong!!.songPath)
//                    val reference = FirebaseStorage.getInstance().reference.child("${FirebaseConsts.songMusicStorage}/${activeSong!!.songPath}")
//                    val url = reference.downloadUrl.await().toString()
//                    //Log.d(TAG, "Url: $url")
//                    activeSong!!.songUrl = url
//                    mediaPlayer!!.setDataSource(activeSong!!.songUrl)
//                }
//            }
        }
        mediaPlayer!!.prepareAsync()
    }

    private fun playMedia() {
        if (!mediaPlayer!!.isPlaying) {
            mediaPlayer!!.start()
        }
    }

    private fun stopMedia() {
        if (mediaPlayer == null)
            return
        if (mediaPlayer!!.isPlaying) {
            mediaPlayer!!.stop()
        }
    }

    private fun pauseMedia() {
        if (mediaPlayer!!.isPlaying) {
            mediaPlayer!!.pause()
            resumePosition = mediaPlayer!!.currentPosition
        }
    }

    private fun resumeMedia() {
        if (!mediaPlayer!!.isPlaying) {
            mediaPlayer!!.seekTo(resumePosition)
            mediaPlayer!!.start()
        }
    }

    // Binder given to clients
    private val iBinder: IBinder = LocalBinder()
    override fun onBind(intent: Intent): IBinder? {
        return iBinder
    }

    override fun onBufferingUpdate(mp: MediaPlayer, percent: Int) {
        //Invoked indicating buffering status of
        //a media resource being streamed over the network.
    }

    override fun onCompletion(mp: MediaPlayer) {
        //Invoked when playback of a media source has completed.
        Log.d(TAG, "Media player on completion")
        stopMedia();

        //stopSelf()
        skipToNext()
    // TODO: play next song
    }

    //Handle errors
    override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        //Invoked when there has been an error during an asynchronous operation.
        when (what) {
            MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK ->
                Log.d("MediaPlayer Error", "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK $extra")
            MEDIA_ERROR_SERVER_DIED ->
                Log.d("MediaPlayer Error", "MEDIA ERROR SERVER DIED $extra")
            MEDIA_ERROR_UNKNOWN ->
                Log.d("MediaPlayer Error", "MEDIA ERROR UNKNOWN $extra")
        }
        return false
    }

    override fun onInfo(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        //Invoked to communicate some info.
        return false
    }

    override fun onPrepared(mp: MediaPlayer) {
        //Invoked when the media source is ready for playback.
        playMedia();
    }

    override fun onSeekComplete(mp: MediaPlayer) {
        //Invoked indicating the completion of a seek operation.
    }

    override fun onAudioFocusChange(focusState: Int) {
        //Invoked when the audio focus of the system is updated.
        when (focusState) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                // resume playback
                if (mediaPlayer == null)
                    initMediaPlayer()
                else if (!mediaPlayer!!.isPlaying)
                    mediaPlayer!!.start()
                mediaPlayer!!.setVolume(1.0f, 1.0f)
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (mediaPlayer!!.isPlaying)
                    mediaPlayer!!.stop()
                mediaPlayer!!.release()
                mediaPlayer = null
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ->             // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (mediaPlayer!!.isPlaying)
                    mediaPlayer!!.pause()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK ->             // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (mediaPlayer!!.isPlaying)
                    mediaPlayer!!.setVolume(0.1f, 0.1f)
        }
    }

    private fun requestAudioFocus(): Boolean {
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val result = audioManager.requestAudioFocus(
            this,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN
        )
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        //Could not gain focus
    }

    private fun removeAudioFocus(): Boolean {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                audioManager.abandonAudioFocus(this)
    }


    override fun onDestroy() {
        super.onDestroy()
        if (mediaPlayer != null) {
            stopMedia()
            mediaPlayer!!.release()
        }
        removeAudioFocus()
        //Disable the PhoneStateListener
        if (phoneStateListener != null) {
            telephonyManager!!.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
        }
        removeNotification()

        //unregister BroadcastReceivers
        unregisterReceiver(becomingNoisyReceiver)
        unregisterReceiver(playNewAudio)

        //clear cached playlist
        StorageUtil(applicationContext).clearCachedAudioPlaylist()
    }



    inner class LocalBinder : Binder() {
        val service: MediaPlayerService
            get() = this@MediaPlayerService
    }
}