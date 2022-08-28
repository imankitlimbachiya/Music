package com.app.music.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.music.R
import com.app.music.databinding.ItemMusicBinding
import com.app.music.interfaces.MusicInterface
import com.app.music.model.Music

class MusicAdapter(
    ctx: Context?, musicList: ArrayList<Music>?, cb: MusicInterface?
) : RecyclerView.Adapter<RecyclerView.ViewHolder?>() {

    private var musicList: ArrayList<Music>? = null
    private var cb: MusicInterface? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return VhAvailable(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_music, parent, false
            )
        )
    }

    override fun onBindViewHolder(m: RecyclerView.ViewHolder, position: Int) {
        val music = musicList!![position]
        val available: VhAvailable?
        available = m as VhAvailable

        available.b.lblTitle.text = music.title
        available.b.lblTime.text = music.time

        available.b.root.setOnClickListener {
            cb!!.onPlayPause(music, position)
        }

        available.b.imgPlay.visibility  = if (!music.nowPlaying ) View.VISIBLE else View.GONE
        available.b.imgPause.visibility  = if (music.nowPlaying ) View.VISIBLE else View.GONE
    }

    override fun getItemCount(): Int {
        return if (musicList == null) 0 else musicList!!.size
    }

    internal class VhAvailable(b: ItemMusicBinding) : RecyclerView.ViewHolder(b.root) {

        val b: ItemMusicBinding

        init {
            this.b = b
        }
    }

    init {
        if (ctx != null) {
            this.musicList = musicList
            this.cb = cb
        }
    }
}