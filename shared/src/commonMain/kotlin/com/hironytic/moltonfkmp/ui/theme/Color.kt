package com.hironytic.moltonfkmp.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Tailwind CSS (v4) palette entries used by moltonf-web, so both apps share the same colors.
 */
internal object Palette {
    val Black = Color(0xFF000000)
    val White = Color(0xFFFFFFFF)

    val Gray300 = Color(0xFFD1D5DC)
    val Gray400 = Color(0xFF99A1AF)
    val Gray500 = Color(0xFF6A7282)
    val Gray600 = Color(0xFF4A5565)
    val Gray700 = Color(0xFF364153)
    val Gray800 = Color(0xFF1E2939)
    val Gray900 = Color(0xFF101828)

    /** `primary-500` customized in moltonf-web's app.css; used for the focus ring of inputs. */
    val Primary500 = Color(0xFFFE795D)

    val Red500 = Color(0xFFFB2C36)
    val Red600 = Color(0xFFE7000B)
    val Red700 = Color(0xFFC10007)
    val Red800 = Color(0xFF9F0712)
}

/**
 * Colors which have no Material 3 counterpart: the ones used to draw the story itself
 * (talks, story events and so on), taken from moltonf-web.
 */
object MoltonfColors {
    /** Background of a talk balloon and the color of the tail. */
    val talkPublic = Color(0xFFFFFFFF)
    val talkWolf = Color(0xFFFF7777)
    val talkPrivate = Color(0xFF939393)
    val talkGrave = Color(0xFF9FB7CF)

    /** Text color on a talk balloon. */
    val onTalk = Color(0xFF000000)

    /** Link color to another talk, drawn on a talk balloon. */
    val talkLinkPublic = Color(0xFFFF0000)
    val talkLinkWolf = Color(0xFFFFFFFF)
    val talkLinkPrivate = Color(0xFFFFFFFF)
    val talkLinkGrave = Color(0xFF0000FF)

    val avatarName = Color(0xFFDDDDDD)
    val talkTime = Color(0xFF666666)

    val eventAnnounce = Color(0xFFDDDDDD)
    val eventOrder = Color(0xFFFF4444)
    val eventExtra = Color(0xFF888888)

    val moltonfMessage = Color(0xFFF5E8B2)
}
