package com.app.music.model

import android.net.Uri

class Music {

    var title: String? = null
    var uri: Uri? = null
    var nowPlaying = false
    var time: String = ""

    constructor()

    constructor(title: String?, uri: Uri?, time: String) {
        this.title = title
        this.uri = uri
        this.time = time
    }
}