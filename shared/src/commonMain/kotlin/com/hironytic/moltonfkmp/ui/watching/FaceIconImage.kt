package com.hironytic.moltonfkmp.ui.watching

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import coil3.compose.AsyncImage
import com.hironytic.moltonfkmp.story.FaceIcon
import moltonf_kmp.shared.generated.resources.Res
import moltonf_kmp.shared.generated.resources.face01
import moltonf_kmp.shared.generated.resources.face02
import moltonf_kmp.shared.generated.resources.face03
import moltonf_kmp.shared.generated.resources.face04
import moltonf_kmp.shared.generated.resources.face05
import moltonf_kmp.shared.generated.resources.face06
import moltonf_kmp.shared.generated.resources.face07
import moltonf_kmp.shared.generated.resources.face08
import moltonf_kmp.shared.generated.resources.face09
import moltonf_kmp.shared.generated.resources.face10
import moltonf_kmp.shared.generated.resources.face11
import moltonf_kmp.shared.generated.resources.face12
import moltonf_kmp.shared.generated.resources.face13
import moltonf_kmp.shared.generated.resources.face14
import moltonf_kmp.shared.generated.resources.face15
import moltonf_kmp.shared.generated.resources.face16
import moltonf_kmp.shared.generated.resources.face17
import moltonf_kmp.shared.generated.resources.face18
import moltonf_kmp.shared.generated.resources.face19
import moltonf_kmp.shared.generated.resources.face20
import moltonf_kmp.shared.generated.resources.face99
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

private fun builtInFaceIconResource(fileName: String): DrawableResource? = when (fileName.lowercase()) {
    "face01.jpg" -> Res.drawable.face01
    "face02.jpg" -> Res.drawable.face02
    "face03.jpg" -> Res.drawable.face03
    "face04.jpg" -> Res.drawable.face04
    "face05.jpg" -> Res.drawable.face05
    "face06.jpg" -> Res.drawable.face06
    "face07.jpg" -> Res.drawable.face07
    "face08.jpg" -> Res.drawable.face08
    "face09.jpg" -> Res.drawable.face09
    "face10.jpg" -> Res.drawable.face10
    "face11.jpg" -> Res.drawable.face11
    "face12.jpg" -> Res.drawable.face12
    "face13.jpg" -> Res.drawable.face13
    "face14.jpg" -> Res.drawable.face14
    "face15.jpg" -> Res.drawable.face15
    "face16.jpg" -> Res.drawable.face16
    "face17.jpg" -> Res.drawable.face17
    "face18.jpg" -> Res.drawable.face18
    "face19.jpg" -> Res.drawable.face19
    "face20.jpg" -> Res.drawable.face20
    "face99.jpg" -> Res.drawable.face99
    else -> null
}

@Composable
fun FaceIconImage(icon: FaceIcon?, contentDescription: String?, modifier: Modifier = Modifier) {
    when (icon) {
        is FaceIcon.BuiltIn -> {
            val resource = builtInFaceIconResource(icon.fileName)
            if (resource != null) {
                Image(painterResource(resource), contentDescription, modifier)
            }
        }

        is FaceIcon.Remote -> AsyncImage(model = icon.url, contentDescription = contentDescription, modifier = modifier)

        null -> {}
    }
}
