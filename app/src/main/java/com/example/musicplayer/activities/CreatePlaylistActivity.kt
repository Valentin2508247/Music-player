package com.example.musicplayer.activities

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.musicplayer.R
import com.example.musicplayer.database.AppDatabase
import com.example.musicplayer.database.Playlist
import com.example.musicplayer.firebase.PlaylistsFirebase
import com.example.musicplayer.repositories.PlaylistRepository
import com.example.musicplayer.view_models.CreatePlaylistViewModel
import com.example.musicplayer.view_models.CreatePlaylistViewModelFactory
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.jaiselrahman.filepicker.activity.FilePickerActivity
import com.jaiselrahman.filepicker.config.Configurations
import com.jaiselrahman.filepicker.model.MediaFile


class CreatePlaylistActivity : AppCompatActivity() {
    private val TAG = "CreatePlaylistActivity2"

    private lateinit var viewModel: CreatePlaylistViewModel
    private lateinit var repository: PlaylistRepository
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var mDatabase: AppDatabase

    private lateinit var image: ImageView
    private lateinit var playlistName: EditText
    private lateinit var btnPickImage: Button
    private lateinit var btnUploadPlaylist: Button
    private lateinit var checkBox: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_playlist)

        initViews()
        initDependencies()

        viewModel = ViewModelProvider(this, CreatePlaylistViewModelFactory(repository))
        .get(CreatePlaylistViewModel::class.java)

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
        repository = PlaylistRepository(mDatabase.playlistDao(), PlaylistsFirebase(firebaseDatabase, firebaseStorage))
    }

    private fun initViews() {
        playlistName = findViewById(R.id.et_create_playlist_name)
        image = findViewById(R.id.iv_create_playlist_image)
        btnPickImage = findViewById(R.id.btn_pick_playlist_image)
        btnPickImage.setOnClickListener {
            // TODO: Camera permissions
            pickImage()
        }
        checkBox = findViewById(R.id.share_checkbox)
        btnUploadPlaylist = findViewById(R.id.btn_create_playlist)
        btnUploadPlaylist.setOnClickListener {
            uploadPlaylist()
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
        startActivityForResult(intent, 101)
    }

    private fun uploadPlaylist(){
        if (viewModel.isFileSelected.value == null || viewModel.isFileSelected.value == false)
            Toast.makeText(this, "Image needed", Toast.LENGTH_SHORT).show()
        val isShared = checkBox.isChecked
        val name = playlistName.text.toString()
        val playlist = Playlist("", name, null, null, null)
        viewModel.uploadPlaylist(playlist, isShared)
        mDatabase.playlistDao().insertPlaylist(playlist)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && data != null){
            val list: List<MediaFile> = data.getParcelableArrayListExtra(FilePickerActivity.MEDIA_FILES)
            val path = list[0].path
            //val path = list[0].name
            when (requestCode){
                101 -> {
                    Toast.makeText(this, "Path: $path", Toast.LENGTH_LONG).show()
                    viewModel.imagePath.value = path
                    viewModel.isFileSelected.value = true
                }
            }
        }

    }
}