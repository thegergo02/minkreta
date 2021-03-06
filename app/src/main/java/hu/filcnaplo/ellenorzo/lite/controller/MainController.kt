package hu.filcnaplo.ellenorzo.lite.controller

import android.app.DownloadManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import hu.filcnaplo.ellenorzo.lite.controller.cache.CacheHandler
import hu.filcnaplo.ellenorzo.lite.controller.cache.CacheType
import hu.filcnaplo.ellenorzo.lite.kreta.*
import hu.filcnaplo.ellenorzo.lite.kreta.data.message.MessageDescriptor
import hu.filcnaplo.ellenorzo.lite.kreta.data.timetable.SchoolClass
import hu.filcnaplo.ellenorzo.lite.kreta.data.timetable.SchoolDay
import hu.filcnaplo.ellenorzo.lite.kreta.data.timetable.Test
import hu.filcnaplo.ellenorzo.lite.kreta.data.homework.Homework
import hu.filcnaplo.ellenorzo.lite.kreta.data.homework.HomeworkComment
import hu.filcnaplo.ellenorzo.lite.kreta.data.message.Attachment
import hu.filcnaplo.ellenorzo.lite.kreta.data.message.LongerMessageDescriptor
import hu.filcnaplo.ellenorzo.lite.kreta.data.sub.Absence
import hu.filcnaplo.ellenorzo.lite.kreta.data.sub.Evaluation
import hu.filcnaplo.ellenorzo.lite.kreta.data.sub.Note
import hu.filcnaplo.ellenorzo.lite.kreta.data.sub.Notice
import hu.filcnaplo.ellenorzo.lite.view.MainView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainController(ctx: Context, private var mainView: MainView?, accessToken: String, refreshToken: String, instituteCode: String)
    :
    KretaRequests.OnEvaluationListResult,
    KretaRequests.OnRefreshTokensResult,
    KretaRequests.OnTimetableResult,
    KretaRequests.OnMessageListResult,
    KretaRequests.OnMessageResult,
    KretaRequests.OnTestListResult,
    KretaRequests.OnHomeworkListResult,
    KretaRequests.OnStudentDetailsResult,
    KretaRequests.OnNoteListResult,
    KretaRequests.OnAbsenceListResult,
    KretaRequests.OnHomeworkCommentListResult,
    KretaRequests.OnSendHomeworkCommentResult,
    KretaRequests.OnNoticeListResult,
    KretaRequests.OnTrashMessageResult,
    ConnectivityManager.NetworkCallback()
{

    private val apiHandler = KretaRequests(ctx, this, accessToken, refreshToken, instituteCode)
    private val cacheHandler = CacheHandler(ctx)
    private var refreshTryCount = 0
    private val maxRefreshRetry = 3

    private fun requestCacheOrExternal(type: CacheType, unique: String, external: () -> Unit, cache: () -> Unit) {
        if (cacheHandler.shouldUseCache(type, unique)) {
            cache()
        } else {
            GlobalScope.launch {
                cacheHandler.requestedExternalSource(type, unique)
                external()
            }
        }
    }
    
    fun getEvaluationList() {
        requestCacheOrExternal(CacheType.EvaluationList, "", { apiHandler.getEvaluationList(this) }, { cacheHandler.getEvaluationListCache(this) })
    }

    fun getTimetable(fromDate: KretaDate, toDate: KretaDate) {
        val parentListener = this
        GlobalScope.launch {
            apiHandler.getTimetable(parentListener, fromDate, toDate)
        }
    }

    fun getMessageList(type: MessageDescriptor.Type) {
        requestCacheOrExternal(CacheType.MessageList, type.toString(), { apiHandler.getMessageList(this, type) }, { cacheHandler.getMessageListCache(this, type) })
    }
    fun getMessage(messageId: Int) {
        val parentListener = this
        GlobalScope.launch {
            apiHandler.getMessage(parentListener, messageId)
        }
    }
    fun downloadAttachment(downloadManager: DownloadManager, attachment: Attachment) {
        GlobalScope.launch {
            apiHandler.downloadAttachment(downloadManager, attachment)
        }
    }
    fun setMessageRead(messageId: Int, isRead: Boolean = true) {
        GlobalScope.launch {
            apiHandler.setMessageRead(messageId, isRead)
        }
    }

    fun sendHomeworkComment(homeworkUid: String, text: String) {
        val parentListener = this
        GlobalScope.launch {
            apiHandler.sendHomeworkComment(parentListener, homeworkUid, text)
        }
    }

    fun getTestList() {
        val parentListener = this
        GlobalScope.launch {
            apiHandler.getTestList(parentListener)
        }
    }

    fun getNoteList() {
        requestCacheOrExternal(CacheType.MessageList, "", { apiHandler.getNoteList(this) }, { cacheHandler.getNoteListCache(this) })
    }

    fun getNoticeList() {
        val parentListener = this
        GlobalScope.launch {
            apiHandler.getNoticeList(parentListener)
        }
    }

    fun getHomeworkList(fromDate: KretaDate) {
        val parentListener = this
        GlobalScope.launch {
            apiHandler.getHomeworkList(parentListener, fromDate)
        }
    }

    fun getHomeworkCommentList(homeworkUid: String) {
        val parentListener = this
        GlobalScope.launch {
            apiHandler.getHomeworkCommentList(parentListener, homeworkUid)
        }
    }

    fun getAbsenceList() {
        requestCacheOrExternal(CacheType.AbsenceList, "", { apiHandler.getAbsenceList(this) }, { cacheHandler.getAbsenceListCache(this) })
    }

    fun getStudentDetails() {
        val parentListener = this
        GlobalScope.launch {
            apiHandler.getStudentDetails(parentListener)
        }
    }

    fun trashMessage(messageId: Int, isTrashed: Boolean) {
        val parentListener = this
        GlobalScope.launch {
            apiHandler.trashMessage(parentListener, messageId, isTrashed)
        }
    }
    
    fun getTokens(): List<String> {
        return listOf(apiHandler.accessToken, apiHandler.refreshToken, apiHandler.instituteCode)
    }
    
    override fun onEvaluationListSuccess(evals: List<Evaluation>) {
        if (!cacheHandler.isCachedReturn(CacheType.EvaluationList))
            cacheHandler.cacheEvaluationList(evals)
        mainView?.generateEvaluationList(evals)
    }
    override fun onEvaluationListError(error: KretaError) {
        mainView?.displayError(ControllerHelper.getErrorString(error.reason, ControllerHelper.ControllerOrigin.Main, ControllerHelper.RequestOrigin.EvaluationList))
        mainView?.hideProgress()
    }

    override fun onTimetableSuccess(timetable: MutableList<SchoolClass>) {
        mainView?.generateTimetable(timetable)
    }
    override fun onTimetableError(error: KretaError) {
        mainView?.displayError(ControllerHelper.getErrorString(error.reason, ControllerHelper.ControllerOrigin.Main, ControllerHelper.RequestOrigin.Timetable))
        mainView?.hideProgress()
        mainView?.generateTimetable(null)
    }

    override fun onMessageListSuccess(messageList: List<MessageDescriptor>) {
        if (!cacheHandler.isCachedReturn(CacheType.MessageList))
            cacheHandler.cacheMessageList(messageList, MessageDescriptor.MessageDescriptorTypeConverter().fromStringToType(messageList[0].type ?: ""))
        mainView?.generateMessageDescriptors(messageList.reversed())
    }
    override fun onMessageListError(error: KretaError) {
        mainView?.displayError(ControllerHelper.getErrorString(error.reason, ControllerHelper.ControllerOrigin.Main, ControllerHelper.RequestOrigin.MessageList))
        mainView?.generateMessageDescriptors(listOf())
        mainView?.hideProgress()
    }

    override fun onMessageSuccess(message: LongerMessageDescriptor) {
        mainView?.generateMessage(message)
    }
    override fun onMessageError(error: KretaError) {
        mainView?.displayError(ControllerHelper.getErrorString(error.reason, ControllerHelper.ControllerOrigin.Main, ControllerHelper.RequestOrigin.Message))
        mainView?.hideProgress()
    }

    override fun onRefreshTokensSuccess(tokens: Map<String, String>) {
        mainView?.refreshToken(tokens)
        mainView?.hideProgress()
    }
    override fun onRefreshTokensError(error: KretaError) {
        if (maxRefreshRetry <= refreshTryCount) {
            mainView?.sendToLogin()
        }
    }

    override fun onTestListSuccess(testList: List<Test>) {
        mainView?.generateTestList(testList)
    }
    override fun onTestListError(error: KretaError) {
        mainView?.displayError(ControllerHelper.getErrorString(error.reason, ControllerHelper.ControllerOrigin.Main, ControllerHelper.RequestOrigin.TestList))
        mainView?.hideProgress()
    }

    override fun onNoteListSuccess(noteList: List<Note>) {
        if (!cacheHandler.isCachedReturn(CacheType.NoteList))
            cacheHandler.cacheNoteList(noteList)
        mainView?.generateNoteList(noteList)
    }
    override fun onNoteListError(error: KretaError) {
        mainView?.displayError(ControllerHelper.getErrorString(error.reason, ControllerHelper.ControllerOrigin.Main, ControllerHelper.RequestOrigin.NoteList))
        mainView?.hideProgress()
    }

    override fun onNoticeListSuccess(notices: List<Notice>) {
        mainView?.generateNoticeList(notices)
    }
    override fun onNoticeListError(error: KretaError) {
        mainView?.displayError(ControllerHelper.getErrorString(error.reason, ControllerHelper.ControllerOrigin.Main, ControllerHelper.RequestOrigin.NoticeList))
        mainView?.hideProgress()
    }

    override fun onHomeworkListSuccess(homeworks: List<Homework>) {
        mainView?.generateHomeworkList(homeworks)
    }
    override fun onHomeworkListError(error: KretaError) {
        mainView?.displayError(ControllerHelper.getErrorString(error.reason, ControllerHelper.ControllerOrigin.Main, ControllerHelper.RequestOrigin.HomeworkList))
        mainView?.hideProgress()
    }

    override fun onHomeworkCommentListSuccess(homeworkComments: List<HomeworkComment>) {
        mainView?.generateHomeworkCommentList(homeworkComments)
    }
    override fun onHomeworkCommentListError(error: KretaError) {
        mainView?.displayError(ControllerHelper.getErrorString(error.reason, ControllerHelper.ControllerOrigin.Main, ControllerHelper.RequestOrigin.HomeworkCommentList))
        mainView?.hideProgress()
    }

    override fun onAbsenceListSuccess(absenceList: List<Absence>) {
        if (!cacheHandler.isCachedReturn(CacheType.AbsenceList))
            cacheHandler.cacheAbsenceList(absenceList)
        mainView?.generateAbsenceList(absenceList)
    }
    override fun onAbsenceListError(error: KretaError) {
        mainView?.displayError(ControllerHelper.getErrorString(error.reason, ControllerHelper.ControllerOrigin.Main, ControllerHelper.RequestOrigin.AbsenceList))
        mainView?.hideProgress()
    }

    override fun onStudentDetailsSuccess(studentDetails: StudentDetails) {
        mainView?.generateStudentDetails(studentDetails)
    }
    override fun onStudentDetailsError(error: KretaError) {
        //TODO: OMG VERY UGLY CAN BE RECURSIVE COULD DOS THE SERVER FUCK FUCK FUCK FIX IT SOMEONE PLS I NEED TO DO IT AAAAA
        getStudentDetails()
    }

    override fun onSendHomeworkSuccess(homeworkUid: String) {
        mainView?.refreshCommentList(homeworkUid)
    }
    override fun onSendHomeworkError(error: KretaError) {
        mainView?.displayError(ControllerHelper.getErrorString(error.reason, ControllerHelper.ControllerOrigin.Main, ControllerHelper.RequestOrigin.SendHomework))
        mainView?.hideProgress()
    }

    override fun onTrashMessageSuccess(messageId: Int, isTrashed: Boolean) {
        //TODO: Translation
        mainView?.displaySuccess("Message ($messageId) ${if (isTrashed) "has been trashed" else "has been recovered"}!")
        getMessageList(if (isTrashed) MessageDescriptor.Type.Trash else MessageDescriptor.Type.Inbox)
    }

    override fun onTrashMessageError(error: KretaError) {
        mainView?.displayError(ControllerHelper.getErrorString(error.reason, ControllerHelper.ControllerOrigin.Main, ControllerHelper.RequestOrigin.TrashMessage))
        mainView?.hideProgress()
    }

    override fun onAvailable(network: Network) {
        //TODO: Translation
        mainView?.displaySuccess("A network is available!")
    }

    override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
        cacheHandler.networkCapabilities = networkCapabilities
    }

    override fun onLost(network: Network) {
        //TODO: Translation
        mainView?.displayError("Lost connection to the internet!")
        cacheHandler.networkCapabilities = null
    }
}
