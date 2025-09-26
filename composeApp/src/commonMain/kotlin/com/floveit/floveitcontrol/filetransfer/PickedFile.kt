package com.floveit.floveitcontrol.filetransfer




data class PickedFile(
    val bytes: ByteArray,
    val displayName: String,
    val mime: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as PickedFile

        if (!bytes.contentEquals(other.bytes)) return false
        if (displayName != other.displayName) return false
        if (mime != other.mime) return false

        return true
    }

    override fun hashCode(): Int {
        var result = bytes.contentHashCode()
        result = 31 * result + displayName.hashCode()
        result = 31 * result + mime.hashCode()
        return result
    }
}