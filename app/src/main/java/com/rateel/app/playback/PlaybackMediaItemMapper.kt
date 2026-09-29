package com.rateel.app.playback

import android.net.Uri
import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.rateel.app.domain.model.PlaybackItem

object PlaybackMediaItemMapper {
    fun toMediaItem(item: PlaybackItem): MediaItem {
        val uri = item.localUri ?: item.remoteUri
            ?: throw IllegalArgumentException("Playback item has no media URI")
        return MediaItem.Builder()
            .setMediaId(item.id)
            .setUri(uri)
            .apply { item.mimeType?.let(::setMimeType) }
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(item.title)
                    .setArtist(item.subtitle)
                    .setArtworkUri(item.artwork?.let(Uri::parse))
                    .setIsPlayable(true)
                    .setExtras(Bundle().apply {
                        putBoolean("rateel.isLive", item.isLive)
                        putString("rateel.sourceId", item.sourceId)
                    })
                    .build(),
            ).build()
    }
}
