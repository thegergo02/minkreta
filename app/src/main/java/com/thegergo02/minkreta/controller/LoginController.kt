package com.thegergo02.minkreta.controller

import com.android.volley.*
import com.thegergo02.minkreta.kreta.KretaError
import com.thegergo02.minkreta.kreta.KretaRequests
import com.thegergo02.minkreta.kreta.data.Institute
import com.thegergo02.minkreta.view.LoginView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class LoginController(private var loginView: LoginView?, private val apiHandler: KretaRequests)
    : KretaRequests.OnInstitutesResult, KretaRequests.OnTokensResult {

    private val apiLinkHelper = ApiLinkHelper(apiHandler)

    fun getInstitutes() {
        val parentListener = this
        GlobalScope.launch {
            apiHandler.getInstitutes(parentListener, apiLinkHelper.getCurrentApiLink())
        }
    }

    fun getTokens(userName: String, password: String, instituteUrl: String, instituteCode: String) {
        val parentListener = this
        GlobalScope.launch {
            apiHandler.getTokens(parentListener, userName, password, instituteUrl, instituteCode)
        }
    }

    override fun onInstitutesSuccess(institutes: List<Institute>) {
        loginView?.setInstitutes(institutes)
        loginView?.hideProgress()
    }
    override fun onInstitutesError(error: KretaError) {
        loginView?.displayError(error.errorString)
        loginView?.hideProgress()
    }

    override fun onTokensSuccess(tokens: Map<String, String>) {
        loginView?.setTokens(tokens)
        loginView?.hideProgress()
    }
    override fun onTokensError(error: KretaError) {
        loginView?.displayError(error.errorString)
        loginView?.hideProgress()
    }
}
