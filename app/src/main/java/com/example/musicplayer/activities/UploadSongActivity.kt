package com.example.musicplayer.activities

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.musicplayer.R
import com.example.musicplayer.database.AppDatabase
import com.example.musicplayer.database.Song
import com.example.musicplayer.firebase.SongsFirebase
import com.example.musicplayer.repositories.SongsRepository
import com.example.musicplayer.view_models.UploadSongViewModel
import com.example.musicplayer.view_models.UploadSongViewModelFactory
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.jaiselrahman.filepicker.activity.FilePickerActivity
import com.jaiselrahman.filepicker.config.Configurations
import com.jaiselrahman.filepicker.model.MediaFile


class UploadSongActivity : AppCompatActivity() {
    private val TAG = "UploadSongActivity2"

    private lateinit var viewModel: UploadSongViewModel
    private lateinit var repository: SongsRepository
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var mDatabase: AppDatabase

    private lateinit var image: ImageView
    private lateinit var songName: EditText
    private lateinit var songPerformer: EditText
    private lateinit var btnPickImage: Button
    private lateinit var btnPickSong: Button
    private lateinit var btnUploadPlaylist: Button

    companion object{
        val codeMusic = 102
        val codeImage = 101
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_song)

        initViews()
        initDependencies()

        viewModel = ViewModelProvider(this, UploadSongViewModelFactory(repository))
                .get(UploadSongViewModel::class.java)

        viewModel.imagePath.observe(this){
            it?.let { path->
                Log.d(TAG, "Observe. Value $it")
                //image.setImageURI()

                Glide.with(this)
                        .load(path)
                        .into(image)
                image.setImageBitmap(BitmapFactory.decodeFile(path));
//                val imgFile = File(path);
//                if(imgFile.exists())
//                {
//                    Log.d(TAG, "File $path exists")
//                    image.setImageURI(Uri.fromFile(imgFile));
//                }
            }
        }

    }

    private fun initDependencies() {
        firebaseDatabase = FirebaseDatabase.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
        mDatabase = AppDatabase.getDatabase(this)
        repository = SongsRepository(mDatabase.songDao(), mDatabase.likesDao(), SongsFirebase(firebaseDatabase, firebaseStorage))
    }

    private fun initViews() {
        songName = findViewById(R.id.et_create_playlist_name)
        songPerformer = findViewById(R.id.et_upload_song_performer)
        image = findViewById(R.id.iv_create_playlist_image)
        btnPickImage = findViewById(R.id.btn_pick_playlist_image)
        btnPickImage.setOnClickListener {
            // TODO: Camera permissions
            pickImage()
        }

        btnPickSong = findViewById(R.id.btn_pick_song)
        btnPickSong.setOnClickListener {
            pickSong()
        }

        btnUploadPlaylist = findViewById(R.id.btn_create_playlist)
        btnUploadPlaylist.setOnClickListener {
            uploadSong()
        }
    }

    private fun pickImage(){
        val intent = Intent(this, FilePickerActivity::class.java)
        intent.putExtra(FilePickerActivity.CONFIGS, Configurations.Builder()
                .setCheckPermission(true)
                .setShowImages(true)
                .setShowVideos(false)
                .enableImageCapture(true)
                .setMaxSelection(1)
                .setSkipZeroSizeFiles(true)
                .build())
        startActivityForResult(intent, codeImage)
    }

    private fun pickSong(){
        val intent = Intent(this, FilePickerActivity::class.java)
        intent.putExtra(FilePickerActivity.CONFIGS, Configurations.Builder()
                .setCheckPermission(true)
                .setShowAudios(true)
                .setShowImages(false)
                .setShowVideos(false)
                .enableImageCapture(false)
                .setMaxSelection(1)
                .setSkipZeroSizeFiles(true)
                .build())
        startActivityForResult(intent, codeMusic)
    }

    private fun uploadSong(){
        if (viewModel.isImageFileSelected.value == null || viewModel.isImageFileSelected.value == false)
        {
            Toast.makeText(this, "Image needed", Toast.LENGTH_SHORT).show()
            return
        }
        if (viewModel.isSongFileSelected.value == null || viewModel.isSongFileSelected.value == false)
        {
            Toast.makeText(this, "Music file needed", Toast.LENGTH_SHORT).show()
            return
        }

        val name = songName.text.toString()
        val performer = songPerformer.text.toString()
        val song = Song("", name, performer, null, null, null, null)
        viewModel.uploadSong(song)
        mDatabase.songDao().insertSong(song)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && data != null){
            val list: List<MediaFile> = data.getParcelableArrayListExtra(FilePickerActivity.MEDIA_FILES)
            val path = list[0].path
            //val path = list[0].name
            when (requestCode){
                codeImage -> {
                    Toast.makeText(this, "Path: $path", Toast.LENGTH_LONG).show()
                    viewModel.imagePath.value = path
                    viewModel.isImageFileSelected.value = true
                }
                codeMusic -> {
                    Toast.makeText(this, "Path: $path", Toast.LENGTH_LONG).show()
                    viewModel.songPath.value = path
                    viewModel.isSongFileSelected.value = true
                }
            }
        }

    }
}