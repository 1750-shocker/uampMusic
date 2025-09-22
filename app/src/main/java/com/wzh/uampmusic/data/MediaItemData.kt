package com.wzh.uampmusic.data

import android.net.Uri

/**
 * Data class representing a media item for UI display
 * 
 * @param mediaId Unique identifier for the media item
 * @param title Primary title of the media item
 * @param subtitle Secondary text (artist, album, etc.)
 * @param albumArtUri URI for the album artwork
 * @param browsable Whether this item can be browsed (folder) or played (file)
 * @param playbackRes Resource ID for playback state icon (default: 0)
 */
data class MediaItemData(
    val mediaId: String,
    val title: String,
    val subtitle: String,
    val albumArtUri: Uri,
    val browsable: Boolean,
    val playbackRes: Int = 0
)