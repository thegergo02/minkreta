package hu.filcnaplo.ellenorzo.lite.authenticator

import android.accounts.AbstractAccountAuthenticator
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import hu.filcnaplo.ellenorzo.lite.R
import hu.filcnaplo.ellenorzo.lite.activity.LoginActivity
import hu.filcnaplo.ellenorzo.lite.kreta.KretaError
import hu.filcnaplo.ellenorzo.lite.kreta.KretaRequests


class KretaAuthenticator(val ctx: Context): AbstractAccountAuthenticator(ctx) {
    private val apiHandler = KretaRequests(ctx)

    private fun getLoginIntentBundle(accountType: String?, authTokenType: String?, response: AccountAuthenticatorResponse?): Bundle {
        val intent = Intent(ctx, LoginActivity::class.java)
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, accountType)
        intent.putExtra(ctx.getString(R.string.key_auth_type), authTokenType)
        intent.putExtra(ctx.getString(R.string.key_is_adding_new_account), true)
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
        val bundle = Bundle()
        bundle.putParcelable(AccountManager.KEY_INTENT, intent)
        return bundle
    }

    override fun getAuthToken(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        authTokenType: String?,
        options: Bundle?
    ): Bundle? {
        Log.w("AUTHFIX-getauthtoken", "getAuthToken started")
        val accountManager = AccountManager.get(ctx)
        Log.w("AUTHFIX-getauthtoken", "got accountmanager")
        var authToken = if (options?.getBoolean(ctx.getString(R.string.key_is_refresh_token), false) == true) null else accountManager.peekAuthToken(account, authTokenType)
        Log.w("AUTHFIX-getauthtoken", "tried getting authtoken back, got: $authToken")
        val returnAuthToken = { refreshToken: String? ->
            Log.w("AUTHFIX-getauthtoken", "we return authtoken returnAuthToken: $refreshToken")
            val result = Bundle()
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account?.name)
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account?.type)
            result.putString(AccountManager.KEY_AUTHTOKEN, authToken)
            result.putString(ctx.getString(R.string.key_refresh_token), refreshToken)
            result
        }
        val returnGetToken = { instituteCode: String ->
            Log.w("AUTHFIX-getauthtoken", "returnGetToken: $instituteCode")
            apiHandler.getTokens(object : KretaRequests.OnTokensResult {
                override fun onTokensSuccess(tokens: Map<String, String>) {
                    authToken = tokens["access_token"]
                    Log.w("AUTHFIX-getauthtoken", "returnGetToken: success: $authToken")
                    response?.onResult(returnAuthToken(tokens["refresh_token"]))
                }

                override fun onTokensError(error: KretaError) {
                    Log.w("AUTHFIX-getauthtoken", "returnGetToken: error: ${error.reason.toString()}")
                    response?.onResult(getLoginIntentBundle(account?.type, authTokenType, response))
                }
            }, account?.name ?: "", accountManager.getPassword(account), instituteCode)
            null
        }
        if (!authToken.isNullOrEmpty()) {
            Log.w("AUTHFIX-getauthtoken", "authToken not empty or null: $authToken")
            return returnAuthToken(accountManager.getUserData(account, ctx.getString(R.string.key_refresh_token)))
        }
        val refreshToken = options?.getString(ctx.getString(R.string.key_refresh_token))
        val instituteCode = options?.getString(ctx.getString(R.string.key_institute_code))
        Log.w("AUTHFIX-getauthtoken", "refresh: $refreshToken | institute: $instituteCode")
        if (refreshToken != null && instituteCode != null) {
            apiHandler.refreshToken = refreshToken
            apiHandler.instituteCode = instituteCode
            apiHandler.refreshToken(object : KretaRequests.OnRefreshTokensResult {
                override fun onRefreshTokensSuccess(tokens: Map<String, String>) {
                    authToken = tokens["access_token"]
                    Log.w("AUTHFIX-getauthtoken", "refreshed token: $authToken")
                    response?.onResult(returnAuthToken(tokens["refresh_token"]))
                }

                override fun onRefreshTokensError(error: KretaError) {
                    Log.w("AUTHFIX-getauthtoken", "failed to refresh token: ${error.reason}")
                    returnGetToken(instituteCode)
                }
            })
        } else if (instituteCode != null) {
            return returnGetToken(instituteCode)
        }
        response?.onResult(getLoginIntentBundle(account?.type, authTokenType, response))
        return null
    }

    override fun addAccount(
        response: AccountAuthenticatorResponse?,
        accountType: String?,
        authTokenType: String?,
        requiredFeatures: Array<out String>?,
        options: Bundle?
    ): Bundle? {
        return getLoginIntentBundle(accountType, authTokenType, response)
    }

    override fun getAuthTokenLabel(authTokenType: String?): String? {
        return "Full access to your ${ctx.getString(R.string.kreta_account_name)} account"
    }

    override fun confirmCredentials(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        options: Bundle?
    ): Bundle? {
        return null
    }

    override fun editProperties(
        response: AccountAuthenticatorResponse?,
        accountType: String?
    ): Bundle? {
        return null
    }

    override fun hasFeatures(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        features: Array<out String>?
    ): Bundle? {
        return null
    }

    override fun updateCredentials(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        authTokenType: String?,
        options: Bundle?
    ): Bundle? {
        return null
    }
}