package com.app.music.activity

import android.Manifest.permission
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.app.music.R
import com.app.music.adapter.MusicAdapter
import com.app.music.databinding.ActivityMusicBinding
import com.app.music.interfaces.MusicInterface
import com.app.music.model.Music
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.DexterError
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class MusicActivity : AppCompatActivity() {

    private lateinit var m: ActivityMusicBinding
    private val musicList: ArrayList<Music> = ArrayList()
    private var adapter: MusicAdapter? = null

    ///////////////////////////////////////////////////////////////////////////
    // Override Methods
    ///////////////////////////////////////////////////////////////////////////

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        m = DataBindingUtil.setContentView(this, R.layout.activity_music)
        init()
    }

    override fun onBackPressed() {
        if (musicList.isNotEmpty()) {
            if (musicList[currentPos].nowPlaying) {
                mp.stop()
                musicList[currentPos].nowPlaying = false
                adapter!!.notifyItemChanged(currentPos)
            } else {
                musicList.clear()
                m.rvMusic.adapter = null
            }
        } else {
            finish()
        }
        super.onBackPressed()
    }

    ///////////////////////////////////////////////////////////////////////////
    // Custom Methods
    ///////////////////////////////////////////////////////////////////////////

    private fun init() {
        adapter = MusicAdapter(this@MusicActivity, musicList, cb)
        m.rvMusic.adapter = adapter

        m.imgClose.setOnClickListener {
            if (musicList.isNotEmpty()) {
                if (musicList[currentPos].nowPlaying) {
                    mp.stop()
                    musicList[currentPos].nowPlaying = false
                    adapter!!.notifyItemChanged(currentPos)
                } else {
                    musicList.clear()
                    m.rvMusic.adapter = null
                }
            } else {
                finish()
            }
        }

        m.imgAudio.setOnClickListener {
            validatePermission()
        }
    }

    private fun validatePermission() {
        // Below line is use to request the number of permissions which are required in our app.
        Dexter.withContext(this)
            .withPermissions(
                // Below is the list of permissions
                permission.READ_EXTERNAL_STORAGE,
                // After adding permissions we are calling an with listener method.
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(multiplePermissionsReport: MultiplePermissionsReport) {
                    // this method is called when all permissions are granted
                    if (multiplePermissionsReport.areAllPermissionsGranted()) {
                        // Do you work here
                        selectMusic()
                    }
                    // check for permanent denial of any permission
                    if (multiplePermissionsReport.isAnyPermissionPermanentlyDenied) {
                        // Permission is denied permanently, we will show user a dialog message.
                        showSettingsDialog()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    list: List<PermissionRequest>, permissionToken: PermissionToken
                ) {
                    // This method is called when user grants some permission and denies some of them.
                    permissionToken.continuePermissionRequest()
                }
            }).withErrorListener { error: DexterError? ->
                // We are displaying a toast message for error message.
                Toast.makeText(
                    applicationContext,
                    "Error occurred! - > $error ",
                    Toast.LENGTH_SHORT
                ).show()
                // Below line is use to run the permissions on same thread and to check the permissions.
            }.onSameThread().check()
    }

    private fun showSettingsDialog() {
        val builder = AlertDialog.Builder(this@MusicActivity)
        builder.setTitle(getString(R.string.permissions))
        builder.setMessage(getString(R.string.grant_permissions))
        builder.setPositiveButton(getString(R.string.go_to_setting)) { dialog: DialogInterface, _: Int ->
            dialog.cancel()
        }
        builder.setNegativeButton(getString(R.string.cancel)) { dialog: DialogInterface, _: Int -> dialog.cancel() }
        builder.show()
    }

    private fun selectMusic() {
        val filesIntent = Intent(Intent.ACTION_GET_CONTENT)
        filesIntent.addCategory(Intent.CATEGORY_OPENABLE)
        filesIntent.type = "audio/*" // Use image/* for photos, etc.

        resultLauncher.launch(filesIntent)
    }

    @SuppressLint("DefaultLocale")
    fun getDuration(uriOfFile: Uri): String {
        val mp = MediaPlayer.create(this, uriOfFile)
        val duration = mp.duration
        mp.release()
        return String.format(
            "%d min, %d sec",
            TimeUnit.MILLISECONDS.toMinutes(duration.toLong()),
            TimeUnit.MILLISECONDS.toSeconds(duration.toLong()) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration.toLong()))
        )
    }

    ///////////////////////////////////////////////////////////////////////////
    // Callbacks
    ///////////////////////////////////////////////////////////////////////////

    @SuppressLint("Range")
    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val uri = result.data?.data
                if (uri != null) {
                    val uriString: String = uri.toString()
                    val myFile = File(uriString)
                    // val path: String = myFile.absolutePath
                    var displayName: String? = null
                    if (uriString.startsWith("content://")) {
                        var cursor: Cursor? = null
                        try {
                            cursor =
                                this@MusicActivity.contentResolver.query(
                                    uri,
                                    null,
                                    null,
                                    null,
                                    null
                                )
                            if (cursor != null && cursor.moveToFirst()) {
                                displayName =
                                    cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                            }
                        } finally {
                            cursor?.close()
                        }
                    } else if (uriString.startsWith("file://")) {
                        displayName = myFile.name
                    }
                    musicList.add(
                        Music(
                            displayName, uri, getDuration(uri)
                        )
                    )
                    adapter?.notifyItemInserted(musicList.size - 1)
                }
            }
        }

    private lateinit var mp: MediaPlayer
    private fun play(context: Context, music: Music) {
        try {
            mp = MediaPlayer()
            music.uri?.let { mp.setDataSource(context, it) }
            mp.prepare()
            mp.start()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private var currentPos = -1
    private val cb: MusicInterface = MusicInterface { music, pos ->
        if (currentPos != -1) {
            mp.stop()
            musicList[currentPos].nowPlaying = false
            adapter!!.notifyItemChanged(currentPos)
        }
        if (pos == currentPos) {
            musicList[currentPos].nowPlaying = musicList[currentPos].nowPlaying
            adapter!!.notifyItemChanged(currentPos)
            currentPos = if (musicList[currentPos].nowPlaying) pos else -1
        } else {
            currentPos = pos
            musicList[pos].nowPlaying = true
            adapter!!.notifyItemChanged(currentPos)
        }
        if (currentPos != -1 && musicList[currentPos].nowPlaying) {
            play(this@MusicActivity, music)
        }
    }
}