package com.thegergo02.minkreta.ui

import android.app.ActionBar
import android.content.Context
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.thegergo02.minkreta.R
import com.thegergo02.minkreta.activity.MessageActivity
import com.thegergo02.minkreta.controller.MainController
import com.thegergo02.minkreta.kreta.KretaDate
import com.thegergo02.minkreta.kreta.data.message.Attachment
import com.thegergo02.minkreta.kreta.data.message.Message
import com.thegergo02.minkreta.kreta.data.message.MessageDescriptor
import com.thegergo02.minkreta.ui.manager.RefreshableData


class MessageUI {
    companion object {
        fun generateMessage(
            ctx: Context,
            message: Message,
            detailsLL: LinearLayout,
            downloadAttachment: (attachment: Attachment) -> Unit,
            showDetails: () -> Unit,
            hideDetails: () -> Unit,
            themeHelper: ThemeHelper
        ) {
            UIHelper.wrapIntoDetails({ _, _ ->
                val subjectTextView = TextView(ctx)
                subjectTextView.text = "${message.subject}"
                subjectTextView.setTextColor(themeHelper.getColorFromAttributes(R.attr.colorText))
                val senderTextView = TextView(ctx)
                senderTextView.text = "${message.senderName} (${message.senderRole}) \n" +
                        message.sendDate.toFormattedString(KretaDate.KretaDateFormat.DATETIME)
                val htmlString = if (message.text != null) {
                    UIHelper.formatHtml(
                        UIHelper.decodeHtml(message.text),
                        themeHelper.getColorFromAttributes(R.attr.colorBackground),
                        themeHelper.getColorFromAttributes(R.attr.colorText)
                    )
                } else {
                    ""
                }
                val messageWebView = UIHelper.generateWebView(ctx, htmlString)
                val attachmentLinearLayout = LinearLayout(ctx)
                for (attachment in message.attachments) {
                    val onAttachmentClickListener = { _: View, _: RefreshableData ->
                        downloadAttachment(attachment)
                        listOf<View>()
                    }
                    val attachmentButton =
                        UIHelper.generateButton(ctx, attachment.fileName, onAttachmentClickListener)
                    attachmentButton.setBackgroundColor(themeHelper.getColorFromAttributes(R.attr.colorAccent))
                    val params = ActionBar.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    params.setMargins(10, 0, 10, 0)
                    attachmentButton.layoutParams = params
                    attachmentLinearLayout.addView(attachmentButton)
                }
                listOf(subjectTextView, messageWebView, attachmentLinearLayout, senderTextView)
            }, RefreshableData(""), showDetails, hideDetails, detailsLL)(View(ctx))
        }
    }
}