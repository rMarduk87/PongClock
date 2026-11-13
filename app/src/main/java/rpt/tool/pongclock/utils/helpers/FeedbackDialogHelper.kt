package rpt.tool.pongclock.utils.helpers

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.view.LayoutInflater
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import rpt.tool.pongclock.R
import rpt.tool.pongclock.databinding.DialogFeedbackBinding
import rpt.tool.pongclock.utils.AppUtils
import rpt.tool.pongclock.utils.manager.SharedPreferencesManager
import androidx.core.net.toUri

object FeedbackDialogHelper {

    fun showFeedbackDialog(context: Context) {
        val binding = DialogFeedbackBinding.inflate(LayoutInflater.from(context))
        val (mainColor, bgColor) = AppUtils.getModeColors(context)

        val isGlitch = SharedPreferencesManager.glitch == 1
        val isNoGravity = SharedPreferencesManager.noGravity == 1
        val isBlueprint = SharedPreferencesManager.bluePrint == 1
        val isPacman = SharedPreferencesManager.pacManMode == 1
        val isMatrix = SharedPreferencesManager.mode == 1
        val isFuturistic = SharedPreferencesManager.futuristic == 1

        val accentColor = when {
            isFuturistic -> context.getColor(R.color.neon_pink)
            isGlitch -> context.getColor(R.color.shocking_pink)
            isNoGravity -> context.getColor(R.color.acid_purple)
            isPacman -> context.getColor(R.color.red)
            else -> mainColor
        }

        val strokeColor = when {
            isBlueprint -> mainColor
            isGlitch -> mainColor
            isPacman -> context.getColor(R.color.pacman_maze)
            else -> mainColor
        }

        val backgroundDrawable = GradientDrawable().apply {
            setColor(bgColor)
            val cornerRadius = when {
                isMatrix || isPacman || isBlueprint -> 0f
                isFuturistic -> 24f
                else -> 16f
            }
            setCornerRadius(cornerRadius)
            val strokeWidth = if (isBlueprint) 2 else 3
            setStroke(strokeWidth, strokeColor)
        }
        binding.feedbackContainer.background = backgroundDrawable

        // Typography styling
        val isMonospace = isMatrix || isPacman || isBlueprint || SharedPreferencesManager.classic == 1
        if (isMonospace) {
            binding.dialogTitle.typeface = Typeface.MONOSPACE
            binding.dialogSubtitle.typeface = Typeface.MONOSPACE
            binding.inputSubject.typeface = Typeface.MONOSPACE
            binding.inputMessage.typeface = Typeface.MONOSPACE
            binding.btnCancel.typeface = Typeface.MONOSPACE
            binding.btnSend.typeface = Typeface.MONOSPACE
        }

        binding.dialogTitle.setTextColor(mainColor)
        binding.dialogSubtitle.setTextColor(mainColor)
        binding.dialogSubtitle.alpha = 0.85f

        // TextInputs styling
        val textColors = ColorStateList.valueOf(mainColor)
        binding.inputSubject.setTextColor(mainColor)
        binding.inputMessage.setTextColor(mainColor)

        val boxColorStateList = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_focused),
                intArrayOf(-android.R.attr.state_focused)
            ),
            intArrayOf(
                accentColor,
                strokeColor
            )
        )

        binding.inputLayoutSubject.setBoxStrokeColorStateList(boxColorStateList)
        binding.inputLayoutSubject.defaultHintTextColor = textColors
        binding.inputLayoutSubject.hintTextColor = ColorStateList.valueOf(accentColor)

        binding.inputLayoutMessage.setBoxStrokeColorStateList(boxColorStateList)
        binding.inputLayoutMessage.defaultHintTextColor = textColors
        binding.inputLayoutMessage.hintTextColor = ColorStateList.valueOf(accentColor)

        if (isMatrix || isPacman || isBlueprint) {
            binding.inputLayoutSubject.setBoxCornerRadii(0f, 0f, 0f, 0f)
            binding.inputLayoutMessage.setBoxCornerRadii(0f, 0f, 0f, 0f)
            binding.btnCancel.cornerRadius = 0
            binding.btnSend.cornerRadius = 0
        } else if (isFuturistic) {
            binding.inputLayoutSubject.setBoxCornerRadii(16f, 16f, 16f, 16f)
            binding.inputLayoutMessage.setBoxCornerRadii(16f, 16f, 16f, 16f)
            binding.btnCancel.cornerRadius = 24
            binding.btnSend.cornerRadius = 24
        }

        // Button colors
        binding.btnCancel.setTextColor(mainColor)
        binding.btnCancel.strokeColor = ColorStateList.valueOf(mainColor)
        binding.btnCancel.rippleColor = ColorStateList.valueOf(mainColor).withAlpha(40)

        binding.btnSend.backgroundTintList = ColorStateList.valueOf(accentColor)
        val sendTextColor = if (accentColor == Color.WHITE || accentColor == context.getColor(R.color.pacman_yellow)) {
            Color.BLACK
        } else {
            Color.WHITE
        }
        binding.btnSend.setTextColor(sendTextColor)

        // Build Dialog
        val dialog = MaterialAlertDialogBuilder(context)
            .setView(binding.root)
            .setBackground(GradientDrawable().apply { setColor(Color.TRANSPARENT) })
            .create()

        binding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        binding.btnSend.setOnClickListener {
            val userSubject = binding.inputSubject.text?.toString()?.trim().orEmpty()
            val userMessage = binding.inputMessage.text?.toString()?.trim().orEmpty()

            val finalSubject = if (userSubject.isNotEmpty()) {
                "[PongClock] $userSubject"
            } else {
                context.getString(R.string.feedback_email_subject_default)
            }

            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = "mailto:".toUri()
                putExtra(Intent.EXTRA_EMAIL,
                    arrayOf(context.getString(R.string.email)))
                putExtra(Intent.EXTRA_SUBJECT, finalSubject)
                putExtra(Intent.EXTRA_TEXT, userMessage)
            }

            try {
                context.startActivity(Intent.createChooser(emailIntent, context.getString(R.string.feedback_title)))
                dialog.dismiss()
            } catch (e: Exception) {
                Toast.makeText(context, context.getString(R.string.feedback_no_email_app), Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }
}
