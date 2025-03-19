/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2021-2023 Fcitx5 for Android Contributors
 */

package org.fcitx.fcitx5.android.input.candidates

import android.content.Context
import android.widget.LinearLayout
import org.fcitx.fcitx5.android.data.theme.Theme
import org.fcitx.fcitx5.android.input.AutoScaleTextView
import org.fcitx.fcitx5.android.input.keyboard.CustomGestureView
import org.fcitx.fcitx5.android.utils.pressHighlightDrawable
import splitties.views.dsl.core.Ui
import splitties.views.dsl.core.add
import splitties.views.dsl.core.lParams
import splitties.views.dsl.core.matchParent
import splitties.views.dsl.core.view
import splitties.views.dsl.core.wrapContent
import splitties.views.gravityCenter

class CandidateItemUi(override val ctx: Context, theme: Theme) : Ui {

    val text = view(::AutoScaleTextView) {
        scaleMode = AutoScaleTextView.Mode.Proportional
        textSize = 20f // sp
        isSingleLine = true
        gravity = gravityCenter
        setTextColor(theme.candidateTextColor)
    }

    // Add Hint TextView for Multi-lingual hint support
    val hintText = view(::AutoScaleTextView) {
        scaleMode = AutoScaleTextView.Mode.Proportional
        textSize = 12f // sp, 細啲作為hint
        isSingleLine = true
        gravity = gravityCenter
        setTextColor(theme.candidateTextColor) // hint 淡色啲
    }

    // 包裝字同Hint，用 LinearLayout 上下排列
    private val textContainer = view(::LinearLayout) {
        orientation = LinearLayout.VERTICAL
        gravity = gravityCenter

        add(text, lParams(wrapContent, wrapContent))
        add(hintText, lParams(wrapContent, wrapContent))
    }

    override val root = view(::CustomGestureView) {
        background = pressHighlightDrawable(theme.keyPressHighlightColor)

        /**
         * candidate long press feedback is handled by [org.fcitx.fcitx5.android.input.candidates.horizontal.HorizontalCandidateComponent.showCandidateActionMenu]
         */
        longPressFeedbackEnabled = false

        add(textContainer, lParams(wrapContent, matchParent) {
            gravity = gravityCenter
        })
    }
}
