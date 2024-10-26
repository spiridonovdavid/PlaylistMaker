package com.example.playlistmaker.sharing.data

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.playlistmaker.R
import com.example.playlistmaker.sharing.domain.api.SharingRepository
import com.example.playlistmaker.sharing.model.EmailData

class SharingRepositoryImpl(private val context: Context) : SharingRepository {

    override fun getShareAppLink(): String {
        return context.getString(R.string.share_text)
    }

    override fun getTermsLink(): String {
        return context.getString(R.string.policy_text)
    }

    override fun getSupportEmailData(): EmailData {
        return EmailData(
            email = context.getString(R.string.email),
            subject = context.getString(R.string.email_subject),
            body = context.getString(R.string.email_text)
        )
    }

    override fun shareApp(appLink: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, appLink)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Поделиться через"))
    }

    @SuppressLint("QueryPermissionsNeeded")
    override fun openTerms(termsLink: String) {
        val linkIntent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(termsLink)
        }
        if (linkIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(linkIntent)
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    override fun openSupport(emailData: EmailData) {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(emailData.email))
            putExtra(Intent.EXTRA_SUBJECT, emailData.subject)
            putExtra(Intent.EXTRA_TEXT, emailData.body)
        }
        if (emailIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(emailIntent)
        }
    }
}
