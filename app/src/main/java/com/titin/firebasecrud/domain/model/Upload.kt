package com.titin.firebasecrud.domain.model

data class Upload(
    var imgName: String = "",
    var imgUrl: String = "",
    var author: String = "",
    var creationDate: String = "",
    var key: String? = null
) {
    init {
        imgName = if (imgName.trim().isEmpty()) "Sin nombre" else imgName.trim()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Upload) return false

        return key == other.key &&
                imgName == other.imgName &&
                imgUrl == other.imgUrl &&
                author == other.author &&
                creationDate == other.creationDate
    }

    override fun hashCode(): Int {
        var result = imgName.hashCode()
        result = 31 * result + imgUrl.hashCode()
        result = 31 * result + author.hashCode()
        result = 31 * result + creationDate.hashCode()
        result = 31 * result + (key?.hashCode() ?: 0)
        return result
    }
}