package com.thegergo02.minkreta.ui

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.thegergo02.minkreta.R
import com.thegergo02.minkreta.kreta.data.Student

class AbsencesUI {
    companion object {
        private fun getColorFromJustification(ctx: Context, justificationState: String?): Int {
            return when (justificationState) {
                "Justified" -> R.color.colorAbsJustified
                else -> R.color.colorAbsUnjustified
            }
        }

        fun generateAbsences(ctx: Context, cachedStudent: Student, absenceHolder: LinearLayout?, detailsLL: LinearLayout, showDetails: () -> Unit, hideDetails: () -> Unit) {
            if (cachedStudent.absences != null) {
                for (abs in cachedStudent.absences) {
                    val text = abs.toString()
                    val absOnClickListener = {
                            _: View ->
                        val absDetailsTextView = TextView(ctx)
                        absDetailsTextView.text = abs.toDetailedString()
                        absDetailsTextView.setTextColor(ContextCompat.getColor(ctx, R.color.colorText))
                        listOf(absDetailsTextView)
                    }
                    val absenceButton =
                        UIHelper.generateButton(ctx, text, absOnClickListener, showDetails, hideDetails, detailsLL, getColorFromJustification(ctx, abs.justificationState))
                    absenceHolder?.addView(absenceButton)
                }
            }
        }
    }
}