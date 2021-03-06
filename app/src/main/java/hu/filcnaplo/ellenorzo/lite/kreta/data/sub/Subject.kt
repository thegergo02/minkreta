package hu.filcnaplo.ellenorzo.lite.kreta.data.sub

import androidx.room.Embedded
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class Subject(
    @Json(name = "Uid") val uid: String,
    @Embedded(prefix = "subject_nature_category_") @Json(name = "Kategoria") val category: Nature,
    @Json(name = "Nev") val name: String
) {
    override fun toString(): String {
        return name
    }
}
