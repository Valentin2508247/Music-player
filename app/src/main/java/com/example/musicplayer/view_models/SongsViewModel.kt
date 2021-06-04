package com.example.musicplayer.view_models

import android.util.Log
import androidx.lifecycle.*
import com.example.musicplayer.database.AppDatabase
import com.example.musicplayer.database.Likes
import com.example.musicplayer.database.Playlist
import com.example.musicplayer.database.Song
import com.example.musicplayer.firebase.FirebaseConsts
import com.example.musicplayer.fragments.strategy.SongsSource
import com.example.musicplayer.repositories.PlaylistRepository
import com.example.musicplayer.repositories.SongsRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class SongsViewModel(private val repository: SongsRepository, playlistId: String?, songsSource: SongsSource, playlistRepository: PlaylistRepository): ViewModel() {
    private val TAG: String = "SongsViewModel"

    lateinit var likesLiveData: LiveData<Likes>
    lateinit var songLiveData: LiveData<List<Song>>
    var playlist: Playlist? = null

    init {
        playlistId?.let {
            playlist = playlistRepository.playlistDao.getPlaylist(it)
        }

        when (songsSource){
            is SongsSource.AllSongs ->
                songLiveData = repository.songDao.getAllSongs()
                //songLiveData = getAllSongs()
            is SongsSource.LikedSongs ->
                //songLiveData = repository.songDao.getLikedSongs()
            {
                val allSongs = repository.songDao.getAllSongs()
                val likes = repository.songDao.getLikes()
                songLiveData = Transformations.map(allSongs){
                    val result = it.filter { song ->
                        likes.songs!!.containsKey(song.id)
                    }
                    result
                }
            }
            is SongsSource.PlaylistSongs -> {
                //TODO()
                playlist?.songs?.let {
                    val ids = it.keys.toList()
                    songLiveData = repository.songDao.getSongsByIds(ids)
                }
            }
        }
        //songLiveData = getAllSongs()
        likesLiveData = getLikes()
        Log.d(TAG, "Updating songs")
        //loadLikes()

        //updateSongs()

//        else{
//            val list = playlist.songs?.keys?.toList()
//            list?.let {
//
//                songLiveData = repository.songDao.getSongsByIds(list)
//            }
//            // TODO: show message to add songs if no songs in playlist
//        }
    }

    private fun updateSongs()
    {
        viewModelScope.async(Dispatchers.IO) {
            val songs = repository.songsFirebase.readSongsUsingCoroutines(FirebaseConsts.songsDatabaseRef)
            songs?.let {
                Log.d(TAG, "Update songs. Count: ${it.size}")
                repository.songDao.deleteAllSongs()
                repository.songDao.insertAllSongs(songs)
            }
        }
    }

    private fun loadLikes() {
        viewModelScope.launch(Dispatchers.IO) {
            val user = FirebaseAuth.getInstance().currentUser
            val userId = user.uid
            val likes = repository.songsFirebase.loadLikes("${FirebaseConsts.likesDatabaseRef}/${userId}")
        }
    }

    fun uploadLikes(likes: HashMap<String, Boolean>){
        GlobalScope.launch (Dispatchers.IO){
            val user = FirebaseAuth.getInstance().currentUser
            val userId = user.uid
            repository.songsFirebase.uploadLikes(likes, "${FirebaseConsts.likesDatabaseRef}/${userId}")
        }
    }

    fun saveToLocal(likes: HashMap<String, Boolean>){
        viewModelScope.launch (Dispatchers.IO) {
            val myLikes = Likes("qwerty", likes)
            repository.likesDao.insertAndDelete(myLikes)
        }
    }

    private fun getAllSongs(): LiveData<List<Song>> {
        return repository.songDao.getAllSongs()
    }

    private fun getLikes(): LiveData<Likes>
    {
        return repository.likesDao.getAllLikes()
    }
}

class SongsViewModelFactory(private val repository: SongsRepository, private val playlistId: String? = null, private val songsSource: SongsSource,
                            private val playlistRepository: PlaylistRepository)
    : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SongsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SongsViewModel(repository, playlistId, songsSource, playlistRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}