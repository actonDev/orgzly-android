package com.orgzly.android.ui.views

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.appcompat.widget.AppCompatEditText
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import com.orgzly.BuildConfig
import com.orgzly.android.util.LogUtils
import java.util.regex.Pattern

class EditTextWithMarkup : AppCompatEditText {
    private var vibrator: Vibrator? = null
    constructor(context: Context) : super(context) {
        vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        addTextChangedListener(textWatcher)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        addTextChangedListener(textWatcher)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        addTextChangedListener(textWatcher)
    }

    fun vibratePhone(ms: Long, amplitude: Int) {
        val vib = this.vibrator;
        if(vib != null) {
            if (Build.VERSION.SDK_INT >= 26) {
                vib.vibrate(VibrationEffect.createOneShot(ms, amplitude))
            } else {
                vib.vibrate(ms)
            }
        }
    }

    fun vibrateNewLine() {
        vibratePhone(100, 100)
    }

    fun vibrateSpace() {
        vibratePhone(50, 50)
    }

    private val textWatcher: TextWatcher = object: TextWatcher {
        private var nextCheckboxPosition = -1
        private var nextCheckboxIndent: String = ""

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            if (BuildConfig.LOG_DEBUG) LogUtils.d(TAG, s, "Start", start, "Count", count, "After", after)

            if (s.length == start || s[start] == '\n') { // End of string or line
                var startOfLine = s.lastIndexOf("\n", start - 1)
                if (startOfLine < 0) {
                    startOfLine = 0
                } else {
                    startOfLine++
                }

                val line = s.substring(startOfLine, start)

                val p = Pattern.compile("^(\\s*)-\\s+\\[[ X]]")
                val m = p.matcher(line)
                if (m.find()) {
                    nextCheckboxPosition = start + 1
                    nextCheckboxIndent = m.group(1)
                }
            }
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            if (BuildConfig.LOG_DEBUG) LogUtils.d(TAG, s, "Start", start, "Before", before, "Count", count)

            if (nextCheckboxPosition != -1 && before == 0 && count == 1 && start < s.length && s[start] == '\n') {
                // All set
            } else {
                nextCheckboxPosition = -1
            }
            val lastCharIndex = start + count - 1;
            if(lastCharIndex >= 0 && count >=1) {
                val lastChar = s.get(lastCharIndex)
                if (BuildConfig.LOG_DEBUG) LogUtils.d(TAG, "Last char", lastChar)
                if (lastChar == ' ') {
                    if (BuildConfig.LOG_DEBUG) LogUtils.d(TAG, "SPACE")
                    vibrateSpace()
                }
                if (lastChar == '\n') {
                    if (BuildConfig.LOG_DEBUG) LogUtils.d(TAG, "NEW LINE")
                    vibrateNewLine()
                }
            }
        }

        override fun afterTextChanged(s: Editable) {
            if (nextCheckboxPosition != -1) {
                s.replace(nextCheckboxPosition, nextCheckboxPosition, "$nextCheckboxIndent- [ ] ")
                nextCheckboxPosition = -1
                nextCheckboxIndent = ""
            }
        }
    }

    companion object {
        private val TAG = EditTextWithMarkup::class.java.name
    }
}