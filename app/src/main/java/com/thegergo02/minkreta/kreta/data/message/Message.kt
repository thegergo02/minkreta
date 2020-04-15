package com.thegergo02.minkreta.kreta.data.message

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.thegergo02.minkreta.kreta.KretaDate

@JsonClass(generateAdapter = true)
data class Message(
    @Json(name = "azonosito") val id: Int?,
    @Json(name = "kuldesDatum") val sendDate: KretaDate?,
    @Json(name = "feladoNev") val senderName: String?,
    @Json(name = "feladoTitulus") val senderRole: String?,
    @Json(name = "szoveg") val text: String?,
    @Json(name = "targy") val subject: String?,
    @Json(name = "cimzettLista") val receiverList: List<Receiver>?//,
    //@Json(name = "csatolmanyok") val attachments: String?
    //TODO: FIX ATTACHMENTS
)