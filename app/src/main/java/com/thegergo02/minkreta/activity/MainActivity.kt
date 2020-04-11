package com.thegergo02.minkreta.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.thegergo02.minkreta.ApiHandler
import com.thegergo02.minkreta.KretaDate
import com.thegergo02.minkreta.R
import com.thegergo02.minkreta.controller.MainController
import com.thegergo02.minkreta.data.Student
import com.thegergo02.minkreta.data.homework.StudentHomework
import com.thegergo02.minkreta.data.homework.TeacherHomework
import com.thegergo02.minkreta.data.message.MessageDescriptor
import com.thegergo02.minkreta.data.timetable.SchoolClass
import com.thegergo02.minkreta.data.timetable.SchoolDay
import com.thegergo02.minkreta.data.timetable.Test
import com.thegergo02.minkreta.ui.*
import com.thegergo02.minkreta.view.MainView
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.time.DayOfWeek
import java.time.LocalDateTime


class MainActivity : AppCompatActivity(), MainView {
    private lateinit var controller: MainController
    private lateinit var cachedStudent: Student
    private lateinit var itemHolders: Map<Tab, LinearLayout>
    private lateinit var tabButtons: Map<Tab, Button>

    private var canClick = true

    private lateinit var accessToken: String
    private lateinit var refreshToken: String
    private lateinit var instituteCode: String

    private var isHomeworkNeeded = false
    private var homeworkIds = mutableListOf<Int>()
    private var studentHomeworkList = mutableListOf<StudentHomework>()
    private var studentHomeworkSize = 0
    private var teacherHomeworkList = mutableListOf<TeacherHomework>()
    private var teacherHomeworkSize = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        controller = MainController(this, ApiHandler(this))

        val sharedPref = getSharedPreferences("com.thegergo02.minkreta.auth", Context.MODE_PRIVATE) ?: return

        val storedAccessToken = sharedPref.getString("accessToken", null)
        val storedRefreshToken = sharedPref.getString("refreshToken", null)
        val storedInstituteCode = sharedPref.getString("instituteCode", null)

        if (storedAccessToken != null && storedRefreshToken != null && storedInstituteCode != null) {
            accessToken = storedAccessToken
            refreshToken = storedRefreshToken
            instituteCode = storedInstituteCode
        } else {
            sendToLogin()
        }
        initializeActivity()
    }

    private fun initializeActivity() {
        controller.getStudent(accessToken, refreshToken, instituteCode)
        showProgress()
        setupHolders()
        setupClickListeners()
    }

    private fun setupHolders() {
        itemHolders = mutableMapOf<Tab, LinearLayout>(
            Tab.Evaluations to eval_holder_ll,
            Tab.Notes to note_holder_ll,
            Tab.Absences to abs_holder_ll,
            Tab.Homework to homework_holder_ll,
            Tab.Timetable to timetable_holder_ll,
            Tab.Messages to messages_holder_ll,
            Tab.Tests to tests_holder_ll
        )
        tabButtons = mutableMapOf<Tab, Button>(
            Tab.Evaluations to evals_btt,
            Tab.Notes to notes_btt,
            Tab.Absences to abs_btt,
            Tab.Homework to homework_btt,
            Tab.Timetable to timetable_btt,
            Tab.Messages to messages_btt,
            Tab.Tests to tests_btt
        )
    }
    private fun setupClickListeners() {
        name_tt.setOnClickListener {
            if (canClick) {
                if (details_ll.visibility == View.GONE) {
                    hideDetails()
                    val nameDetailsTextView = TextView(this)
                    nameDetailsTextView.text =
                        "(${cachedStudent.id}, ${cachedStudent.schoolYearId}) \n" +
                                "Place Of Birth: ${cachedStudent.placeOfBirth} \n" +
                                "Mother's name: ${cachedStudent.mothersName} \n" +
                                "AddressDataList: ${cachedStudent.addressDataList} \n" +
                                "DateOfBirthUTC: ${cachedStudent.DateOfBirthUtc} \n" +
                                "InstituteName: ${cachedStudent.instituteName} \n" +
                                "InstituteCode: ${cachedStudent.instituteCode} \n" +
                                "Lessons: ${cachedStudent.lessons} \n" +
                                "Events: ${cachedStudent.events}"
                    nameDetailsTextView.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.colorText
                        )
                    )
                    details_ll.addView(nameDetailsTextView)
                    showDetails()
                } else {
                    hideDetails()
                }
            }
        }

        tabButtons[Tab.Evaluations]?.setOnClickListener {
            if (canClick) {
                switchTab(Tab.Evaluations)
            }
        }
        tabButtons[Tab.Notes]?.setOnClickListener {
            if (canClick) {
                switchTab(Tab.Notes)
            }
        }
        tabButtons[Tab.Absences]?.setOnClickListener {
            if (canClick) {
                switchTab(Tab.Absences)
            }
        }
        tabButtons[Tab.Homework]?.setOnClickListener {
            if (canClick) {
                if (itemHolders[Tab.Homework]?.visibility == View.GONE) {
                    showProgress()
                    isHomeworkNeeded = true
                    startTimetableRequest()
                } else {
                    switchTab(Tab.Homework)
                }
            }
        }
        tabButtons[Tab.Timetable]?.setOnClickListener {
            if (canClick) {
                if (itemHolders[Tab.Timetable]?.visibility == View.GONE) {
                    showProgress()
                    startTimetableRequest()
                } else {
                    switchTab(Tab.Timetable)
                }
            }
        }
        tabButtons[Tab.Messages]?.setOnClickListener {
            if (canClick) {
                if (itemHolders[Tab.Messages]?.visibility == View.GONE) {
                    showProgress()
                    controller.getMessageList(accessToken)
                } else {
                    switchTab(Tab.Messages)
                }
            }
        }
        tabButtons[Tab.Tests]?.setOnClickListener {
            if (canClick) {
                if (itemHolders[Tab.Tests]?.visibility == View.GONE) {
                    showProgress()
                    controller.getTests(accessToken, instituteCode, KretaDate(1970), KretaDate())
                } else {
                    switchTab(Tab.Tests)
                }
            }
        }
    }

    override fun hideProgress() {
        loading_bar.visibility = View.GONE
        name_tt.visibility = View.VISIBLE
        canClick = true
    }
    override fun showProgress() {
        loading_bar.visibility = View.VISIBLE
        name_tt.visibility = View.GONE
        canClick = false
    }

    override fun setStudent(student: Student) {
        cachedStudent = student
        refreshUI()
    }

    override fun displayError(error: String) {
        val errorSnack = Snackbar.make(main_cl, error, Snackbar.LENGTH_LONG)
        errorSnack.view.setBackgroundColor(ContextCompat.getColor(this,
            R.color.colorError
        ))
        errorSnack.show()
    }

    private fun showDetails() {
        details_ll.visibility = View.VISIBLE
        scroll_view.smoothScrollTo(0, 0)
    }
    private fun hideDetails() {
        details_ll.visibility = View.GONE
        details_ll.removeAllViews()
    }

    enum class Tab {
        Evaluations,
        Notes,
        Absences,
        Homework,
        Timetable,
        Messages,
        Tests
    }
    private fun closeTabs(exception: Tab? = null) {
        for (tabHolder in itemHolders) {
            if (tabHolder.key != exception) {
                tabHolder.value.visibility = View.GONE
                tabButtons[tabHolder.key]?.setBackgroundColor(ContextCompat.getColor(this,
                    R.color.colorButtonUnselected
                ))
            }
        }
        hideDetails()
    }
    private fun switchTab(newTab: Tab) {
        closeTabs(newTab)
        val tabHolder = itemHolders[newTab]
        val tabButton = tabButtons[newTab]
        if (tabHolder != null && tabButton != null) {
            if (tabHolder.visibility == View.GONE) {
                tabHolder.visibility = View.VISIBLE
                tabButton.setBackgroundColor(ContextCompat.getColor(this,
                    R.color.colorButtonSelected
                ))
            } else {
                tabHolder.visibility = View.GONE
                tabButton.setBackgroundColor(ContextCompat.getColor(this,
                    R.color.colorButtonUnselected
                ))
            }
        }
    }

    override fun generateTimetable(timetable: Map<SchoolDay, List<SchoolClass>>) {
        if (isHomeworkNeeded) {
            populateHomeworkIds(timetable)
            isHomeworkNeeded = false
        } else {
            TimetableUI.generateTimetable(this, timetable,
                itemHolders[Tab.Timetable], details_ll, ::showDetails, ::hideDetails, controller)
            switchTab(Tab.Timetable)
            hideProgress()
        }
    }
    private fun populateHomeworkIds(timetable: Map<SchoolDay, List<SchoolClass>>) {
        for (schoolClassList in timetable.values) {
            for (schoolClass in schoolClassList) {
                if (schoolClass.teacherHomeworkId != null) {
                    homeworkIds.add(schoolClass.teacherHomeworkId)
                }
            }
        }
        controller.getHomework(accessToken, instituteCode, homeworkIds)
    }
    override fun collectStudentHomework(homeworkList: List<StudentHomework?>?) {
        if (homeworkList != null) {
            for (homework in homeworkList) {
                if (homework != null) {
                    studentHomeworkList.add(homework)
                }
            }
        }
        studentHomeworkSize++
        if (homeworkIds.size == studentHomeworkSize) {
            homeworkCollectionDone()
        }
    }
    override fun collectTeacherHomework(homework: TeacherHomework?) {
        if (homework != null) {
            teacherHomeworkList.add(homework)
        }
        teacherHomeworkSize++
        if (homeworkIds.size == teacherHomeworkSize) {
            homeworkCollectionDone()
        }
    }
    fun homeworkCollectionDone() {
        if (homeworkIds.size == studentHomeworkSize && homeworkIds.size == teacherHomeworkSize) {
            itemHolders[Tab.Homework]?.removeAllViews()
            HomeworkUI.generateTeacherHomework(this, teacherHomeworkList, itemHolders[Tab.Homework], details_ll, ::showDetails, ::hideDetails)
            HomeworkUI.generateStudentHomework(this, studentHomeworkList, itemHolders[Tab.Homework], details_ll, ::showDetails, ::hideDetails)
            switchTab(Tab.Homework)
            homeworkIds = mutableListOf()
            studentHomeworkList = mutableListOf()
            studentHomeworkSize = 0
            teacherHomeworkList = mutableListOf()
            teacherHomeworkSize = 0
            hideProgress()
        }
    }

    override fun generateMessageDescriptors(messages: List<MessageDescriptor>) {
        MessageUI.generateMessageDescriptors(this, messages, itemHolders[Tab.Messages], controller, accessToken)
        switchTab(Tab.Messages)
        hideProgress()
    }
    override fun generateMessage(message: MessageDescriptor) {
        MessageUI.generateMessage(this, message.message, details_ll, ::showDetails, ::hideDetails)
    }

    override fun generateTests(tests: List<Test>) {
        TestUI.generateTests(this, tests, itemHolders[Tab.Tests], details_ll, ::showDetails, ::hideDetails)
        switchTab(Tab.Tests)
        hideProgress()
    }

    private fun refreshUI() {
        showProgress()
        closeTabs()
        name_tt.visibility = View.VISIBLE
        name_tt.text = cachedStudent.name
        EvaluationUI.generateEvaluations(this, cachedStudent,
            itemHolders[Tab.Evaluations], details_ll, ::showDetails, ::hideDetails)
        NotesUI.generateNotes(this, cachedStudent,
            itemHolders[Tab.Notes], details_ll, ::showDetails, ::hideDetails)
        AbsencesUI.generateAbsences(this, cachedStudent,
            itemHolders[Tab.Absences], details_ll, ::showDetails, ::hideDetails)
        hideProgress()
    }

    override fun triggerRefreshToken() {
        controller.refreshToken(instituteCode, refreshToken)
    }
    override fun refreshToken(tokens: JSONObject) {
        val mainIntent = Intent(this, MainActivity::class.java)
        val sharedPref = getSharedPreferences("com.thegergo02.minkreta.auth", Context.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            putString("accessToken", tokens["access_token"].toString())
            putString("refreshToken", tokens["refresh_token"].toString())
            commit()
        }
        initializeActivity()
    }

    override fun sendToLogin() {
        val loginIntent = Intent(this, LoginActivity::class.java)
        startActivity(loginIntent)
        finish()
    }

    private fun startTimetableRequest() {
        val firstDay = LocalDateTime.now().with(DayOfWeek.MONDAY)
        val startDate = KretaDate(firstDay)
        val endDate = KretaDate(firstDay.plusDays(6))
        controller.getTimetable(
            accessToken,
            instituteCode,
            startDate,
            endDate
        )
    }
}
