package com.thegergo02.minkreta.kreta.data.sub

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.thegergo02.minkreta.kreta.KretaDate

@JsonClass(generateAdapter = true)
class Evaluation(
    @Json(name = "Uid")  val uid: String,
    @Json(name = "Mod") val mode: Nature?,
    @Json(name = "SulySzazalekErteke") val weight: Int?,
    @Json(name = "SzovegesErtek") val textValue: String?,
    @Json(name = "SzovegesErtekRovidNev") val textValueShort: String?,
    @Json(name = "SzamErtek") val numberValue: Int?,
    @Json(name = "LattamozasDatuma") val seen: KretaDate?,
    @Json(name = "KeszitesDatuma") val creatingDate: KretaDate,
    @Json(name = "RogzitesDatuma") val postDate: KretaDate,
    @Json(name = "Jelleg") val nature: String?,
    @Json(name = "ErtekFajta") val valueType: Nature?,
    @Json(name = "Tipus") val type: Nature?,
    @Json(name = "Tantargy") val subject: Subject,
    @Json(name = "ErtekeloTanarNeve") val teacher: String,
    @Json(name = "Tema") val theme: String?//,
    //@Json(name = "OsztalyCsoport") val classGroup: String?
): Comparable<Evaluation> {
    companion object {
        fun sortTypeFromString(str: String): SortType {
            val stringToSortType = mapOf(
                "Creating time" to SortType.CreatingDate,
                "Form" to SortType.Nature,
                "Value" to SortType.TextValue,
                "Mode" to SortType.Mode,
                "Subject" to SortType.Subject,
                "Teacher" to SortType.Teacher
            )
            return stringToSortType[str] ?: SortType.CreatingDate
        }
    }

    enum class SortType(val lambda: (it: Evaluation) -> Comparable<*>) {
        CreatingDate({it.creatingDate}),
        Nature({it.nature ?: ""}),
        TextValue({it.textValue ?: ""}),
        Mode({it.mode?.name ?: ""}),
        Subject({it.subject.name}),
        Teacher({it.teacher})
    }

    override fun toString(): String {
        if (nature == "Diligence" || nature == "Deportment") {
            return  "$nature ($teacher) \n" +
                    "${creatingDate?.toFormattedString(KretaDate.KretaDateFormat.DATETIME)}"
        }
        return  "${subject.name} ($teacher) \n" +
                "$textValue ($weight%) \n" +
                "${theme ?: ""} \n" +
                "${creatingDate?.toFormattedString(KretaDate.KretaDateFormat.DATETIME)}"
    }
    fun toDetailedString(): String {
        return  "${subject.name} ($teacher)\n" +
                "$textValue ($weight%) \n" +
                "${type?.name} \n" +
                "$nature \n" +
                "${theme ?: ""} \n" +
                "${creatingDate?.toFormattedString(KretaDate.KretaDateFormat.DATETIME)}"
    }
    override fun compareTo(other: Evaluation): Int {
        return this.creatingDate.compareTo(other.creatingDate)
    }
}