package com.hironytic.moltonfkmp.story

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

private fun story(baseURI: String, graveIconURI: String, avatarList: List<Avatar>) = Story(
    villageFullName = "村",
    baseURI = baseURI,
    landId = "wolfg",
    graveIconURI = graveIconURI,
    periods = emptyList(),
    avatarList = avatarList,
)

class FaceIconUtilsTest {
    @Test
    fun resolveAvatarFaceIcon_returnsBuiltIn_forKnownFileNames() {
        val s = story(
            baseURI = "https://example.com/village/",
            graveIconURI = "img/grave.png",
            avatarList = listOf(Avatar("gerd", "ゲルト", "ゲルト", "img/face01.jpg")),
        )
        val icon = assertIs<FaceIcon.BuiltIn>(resolveAvatarFaceIcon(s, "gerd"))
        assertEquals("face01.jpg", icon.fileName)
    }

    @Test
    fun resolveAvatarFaceIcon_returnsRemote_forCustomFileNames_resolvedAgainstBaseURI() {
        val s = story(
            baseURI = "https://example.com/village/",
            graveIconURI = "img/grave.png",
            avatarList = listOf(Avatar("gerd", "ゲルト", "ゲルト", "img/custom.jpg")),
        )
        val icon = assertIs<FaceIcon.Remote>(resolveAvatarFaceIcon(s, "gerd"))
        assertEquals("https://example.com/village/img/custom.jpg", icon.url)
    }

    @Test
    fun resolveAvatarFaceIcon_returnsNull_whenAvatarHasNoFaceIcon() {
        val s = story(
            baseURI = "https://example.com/village/",
            graveIconURI = "img/grave.png",
            avatarList = listOf(Avatar("gerd", "ゲルト", "ゲルト", null)),
        )
        assertNull(resolveAvatarFaceIcon(s, "gerd"))
    }

    @Test
    fun resolveGraveIcon_resolvesAgainstBaseURI() {
        val s = story(
            baseURI = "https://example.com/village/",
            graveIconURI = "img/grave.png",
            avatarList = emptyList(),
        )
        val icon = assertIs<FaceIcon.Remote>(resolveGraveIcon(s))
        assertEquals("https://example.com/village/img/grave.png", icon.url)
    }

    @Test
    fun resolveAvatarFaceIcon_relocatesKnownMovedHosts() {
        val s = story(
            baseURI = "http://www.wolfg.x0.com/",
            graveIconURI = "img/grave.png",
            avatarList = listOf(Avatar("gerd", "ゲルト", "ゲルト", "img/custom.jpg")),
        )
        val icon = assertIs<FaceIcon.Remote>(resolveAvatarFaceIcon(s, "gerd"))
        assertEquals("http://ninjinix.x0.com/wolfg/img/custom.jpg", icon.url)
    }

    @Test
    fun resolveURI_passesThroughAbsoluteURIs() {
        assertEquals(
            "https://other.example.com/x.png",
            resolveURI("https://example.com/a/b/", "https://other.example.com/x.png"),
        )
    }

    @Test
    fun resolveURI_resolvesRelativePathAgainstBaseDirectory() {
        assertEquals("https://example.com/a/b/x.png", resolveURI("https://example.com/a/b/", "x.png"))
        assertEquals("https://example.com/a/x.png", resolveURI("https://example.com/a/b", "x.png"))
    }

    @Test
    fun resolveURI_resolvesRootRelativePath() {
        assertEquals("https://example.com/x.png", resolveURI("https://example.com/a/b/", "/x.png"))
    }

    @Test
    fun resolveURI_normalizesDotSegments() {
        assertEquals("https://example.com/a/x.png", resolveURI("https://example.com/a/b/", "../x.png"))
    }
}
