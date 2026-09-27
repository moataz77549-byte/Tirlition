package com.rateel.app.playback
/** Boundary for the future Media3-backed playback service. UI/ViewModels must depend on this boundary, not a player instance. */
interface PlaybackController{fun play(contentId:String);fun pause();fun stop()}
