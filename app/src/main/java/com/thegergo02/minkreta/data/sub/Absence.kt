package com.thegergo02.minkreta.data.sub

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Absence (
    @Json(name = "AbsenceId") val id: Int?,
    @Json(name = "Type") val type: String?,
    @Json(name = "TypeName") val typeName: String?,
    @Json(name = "Mode") val mode: String?,
    @Json(name = "ModeName") val modeName: String?,
    @Json(name = "Subject") val subject: String?,
    @Json(name = "SubjectCategory") val subjectCategory: String?,
    @Json(name = "SubjectCategoryName") val subjectCategoryName: String?,
    @Json(name = "DelayTimeMinutes") val delayTime: Int?,
    @Json(name = "Teacher") val teacher: String?,
    @Json(name = "LessonStartTime") val lessonStartTime: String?,
    @Json(name = "NumberOfLessons") val numberOfLessons: Int?,
    @Json(name = "CreatingTime") val creatingTime: String?,
    @Json(name = "JustificationState") val justificationState: String?,
    @Json(name = "JustificationStateName") val justificationStateName: String?,
    @Json(name = "JustificationType") val justificationType: String?,
    @Json(name = "JustificationTypeName") val justificationTypeName: String?,
    @Json(name = "SeenByTutelaryUTC") val seenByTutelaryUtc: String?,
    @Json(name = "OsztalyCsoportUid") val classGroupUid: String?
)