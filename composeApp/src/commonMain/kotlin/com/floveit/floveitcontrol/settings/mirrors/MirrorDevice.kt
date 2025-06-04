package com.floveit.floveitcontrol.settings.mirrors

import kotlinx.serialization.Serializable


@Serializable
data class MirrorDevice(
    val name: String,
    val id:   String
)
