package com.hironytic.moltonfkmp.story

sealed interface FaceIcon {
    data class BuiltIn(val fileName: String) : FaceIcon
    data class Remote(val url: String) : FaceIcon
}

// Built-in face icons: face01.jpg - face20.jpg, face99.jpg
private val builtInFaceIconRegex = Regex("""^face(0[1-9]|1[0-9]|20|99)\.jpg$""", RegexOption.IGNORE_CASE)

// Some lands' image hosts have moved since these archives were made.
private val relocatedBaseURIs = mapOf(
    "http://www.wolfg.x0.com/" to "http://ninjinix.x0.com/wolfg/",
    "http://ninjin002.x0.com/wolff/" to "http://ninjinix.x0.com/wolff/",
)

private fun relocateBaseURI(baseURI: String): String = relocatedBaseURIs[baseURI] ?: baseURI

private fun toFaceIcon(baseURI: String, iconURI: String): FaceIcon {
    val fileName = iconURI.substringAfterLast('/')
    if (builtInFaceIconRegex.matches(fileName)) {
        return FaceIcon.BuiltIn(fileName.lowercase())
    }
    return FaceIcon.Remote(resolveURI(relocateBaseURI(baseURI), iconURI))
}

fun resolveAvatarFaceIcon(story: Story, avatarId: String): FaceIcon? {
    val faceIconURI = story.avatarList.find { it.avatarId == avatarId }?.faceIconURI ?: return null
    return toFaceIcon(story.baseURI, faceIconURI)
}

fun resolveGraveIcon(story: Story): FaceIcon = toFaceIcon(story.baseURI, story.graveIconURI)

private val absoluteURIRegex = Regex("""^[a-zA-Z][a-zA-Z0-9+.-]*://.*""")

/**
 * Resolves [relative] against [base], following the usual rules for relative URL
 * resolution (absolute / protocol-relative / root-relative / path-relative), without
 * relying on any platform-specific URI class.
 */
fun resolveURI(base: String, relative: String): String {
    if (absoluteURIRegex.matches(relative)) {
        return relative
    }

    val schemeEnd = base.indexOf("://")
    if (schemeEnd < 0) {
        return relative
    }
    val scheme = base.substring(0, schemeEnd)

    if (relative.startsWith("//")) {
        return "$scheme:$relative"
    }

    val afterScheme = base.substring(schemeEnd + 3)
    val authorityEnd = afterScheme.indexOf('/').let { if (it < 0) afterScheme.length else it }
    val authority = afterScheme.substring(0, authorityEnd)
    val basePath = afterScheme.substring(authorityEnd)

    val resultPath = if (relative.startsWith("/")) {
        relative
    } else {
        val baseDir = basePath.substringBeforeLast('/', "")
        "$baseDir/$relative"
    }

    return "$scheme://$authority${normalizePath(resultPath)}"
}

private fun normalizePath(path: String): String {
    val segments = path.split('/')
    val result = mutableListOf<String>()
    for (segment in segments) {
        when (segment) {
            ".", "" -> {}
            ".." -> if (result.isNotEmpty()) result.removeAt(result.size - 1)
            else -> result.add(segment)
        }
    }
    val normalized = result.joinToString("/")
    return if (path.startsWith("/")) "/$normalized" else normalized
}
