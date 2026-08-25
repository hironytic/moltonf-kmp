package com.hironytic.moltonfkmp.story

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull

/**
 * A hand-authored village that follows the structure of a real Jindolf XmlScheme archive
 * (element/attribute shapes, avatar roster) but contains only original, fictional dialogue.
 */
private val SAMPLE_XML = """
    <?xml version="1.0" encoding="UTF-8" ?>
    <village
      xmlns="http://jindolf.sourceforge.jp/xml/ns/501"
      xml:lang="ja-JP"
      xml:base="https://example.com/moltonf-test/"
      fullName="G999 テストの村" vid="999"
      state="gameover" isValid="true"
      landName="人狼BBS:G国" formalName="人狼BBS:G"
      landId="wolfg" landPrefix="G"
      graveIconURI="img/grave.png"
      generator="MoltonfTestFixture 1.0"
    >

    <avatarList>
    <avatar avatarId="gerd" fullName="楽天家 ゲルト" shortName="ゲルト" faceIconURI="img/face01.jpg" />
    <avatar avatarId="thomas" fullName="木こり トーマス" shortName="トーマス" faceIconURI="img/face05.jpg" />
    <avatar avatarId="otto" fullName="パン屋 オットー" shortName="オットー" faceIconURI="img/face12.jpg" />
    <avatar avatarId="simon" fullName="負傷兵 シモン" shortName="シモン" faceIconURI="img/face20.jpg" />
    <avatar avatarId="liesa" fullName="少女 リーザ" shortName="リーザ" faceIconURI="img/face09.jpg" />
    <avatar avatarId="clara" fullName="司書 クララ" shortName="クララ" faceIconURI="img/face19.jpg" />
    </avatarList>

    <period type="prologue" day="0" nextCommitDay="--01-02" commitTime="00:00:00+09:00" sourceURI="https://example.com/p0">
    <startEntry><li>プロローグが始まりました。</li></startEntry>
    <onStage entryNo="1" avatarId="gerd"><li>ゲルトが登場した。</li></onStage>
    <onStage entryNo="2" avatarId="thomas"><li>トーマスが登場した。</li></onStage>
    <onStage entryNo="3" avatarId="otto"><li>オットーが登場した。</li></onStage>
    <onStage entryNo="4" avatarId="simon"><li>シモンが登場した。</li></onStage>
    <onStage entryNo="5" avatarId="liesa"><li>リーザが登場した。</li></onStage>
    <onStage entryNo="6" avatarId="clara"><li>クララが登場した。</li></onStage>
    <startMirror><li>1日目が始まりました。</li></startMirror>
    <openRole>
    <li>役職が公開されました。</li>
    <roleHeads role="innocent" heads="3" />
    <roleHeads role="wolf" heads="2" />
    <roleHeads role="seer" heads="1" />
    </openRole>
    <talk type="public" avatarId="gerd" xname="mes1" time="09:00:00+09:00">
    <li>おはよう<rawdata encoding="UTF-8" hexBin="1a">␚</rawdata>、みんな。</li>
    <li>よろしくね。</li>
    </talk>
    </period>

    <period type="progress" day="1" nextCommitDay="--01-03" commitTime="20:00:00+09:00" sourceURI="https://example.com/p1">
    <talk type="public" avatarId="thomas" xname="mes2" time="10:00:00.5+09:00">
    <li>おはよう。</li>
    </talk>
    <talk type="wolf" avatarId="otto" xname="mes3" time="10:05:00+09:00">
    <li>今晩シモンを襲おう。</li>
    </talk>
    <talk type="private" avatarId="liesa" xname="mes4" time="10:10:00+09:00">
    <li>独り言。</li>
    </talk>
    <murdered><li>シモンが無残な姿で発見された。</li><avatarRef avatarId="simon" /></murdered>
    <startAssault><li>最初の襲撃が発生しました。</li></startAssault>
    <assault byWhom="otto" target="simon" xname="mes5" time="23:59:00+09:00"><li>ヒュー。</li></assault>
    <judge byWhom="clara" target="thomas"><li>占い結果。</li></judge>
    <guard byWhom="liesa" target="gerd"><li>護衛結果。</li></guard>
    <survivor>
    <li>生存者は以下の通り。</li>
    <avatarRef avatarId="gerd" />
    <avatarRef avatarId="thomas" />
    <avatarRef avatarId="otto" />
    <avatarRef avatarId="liesa" />
    <avatarRef avatarId="clara" />
    </survivor>
    <counting victim="thomas">
    <li>投票結果。</li>
    <vote byWhom="gerd" target="thomas" />
    <vote byWhom="liesa" target="thomas" />
    <vote byWhom="otto" target="gerd" />
    </counting>
    <counting2>
    <li>投票結果その2。</li>
    <vote byWhom="gerd" target="thomas" />
    <vote byWhom="liesa" target="thomas" />
    </counting2>
    <execution victim="thomas">
    <li>処刑結果。</li>
    <nominated avatarId="thomas" count="2" />
    <nominated avatarId="gerd" count="1" />
    </execution>
    <talk type="grave" avatarId="thomas" xname="mes6" time="20:30:00+09:00">
    <li>成仏できない。</li>
    </talk>
    </period>

    <period type="epilogue" day="2" nextCommitDay="--01-04" commitTime="21:00:00+09:00" sourceURI="https://example.com/p2">
    <winWolf><li>人狼陣営の勝利。</li></winWolf>
    <playerList>
    <li>プレイヤー一覧。</li>
    <playerInfo playerId="player-gerd" avatarId="gerd" survive="true" role="innocent" />
    <playerInfo playerId="player-thomas" avatarId="thomas" survive="false" role="innocent" />
    <playerInfo playerId="player-otto" avatarId="otto" survive="true" role="wolf" />
    <playerInfo playerId="player-simon" avatarId="simon" survive="false" role="hunter" />
    <playerInfo playerId="player-liesa" avatarId="liesa" survive="true" role="seer" uri="https://example.com/liesa" />
    <playerInfo playerId="player-clara" avatarId="clara" survive="true" role="wolf" />
    </playerList>
    <talk type="public" avatarId="gerd" xname="mes7" time="21:30:00+09:00">
    <li>お疲れ様でした。</li>
    </talk>
    </period>

    </village>
""".trimIndent()

class ArchiveTest {
    @Test
    fun parsesVillageAttributes() {
        val story = parseStory(SAMPLE_XML)
        assertEquals(1, story.version)
        assertEquals("G999 テストの村", story.villageFullName)
        assertEquals("https://example.com/moltonf-test/", story.baseURI)
        assertEquals("wolfg", story.landId)
        assertEquals("img/grave.png", story.graveIconURI)
        assertEquals(6, story.avatarList.size)
        assertEquals(3, story.periods.size)
    }

    @Test
    fun parsesAvatarList() {
        val story = parseStory(SAMPLE_XML)
        val gerd = story.avatarList.first { it.avatarId == "gerd" }
        assertEquals("楽天家 ゲルト", gerd.fullName)
        assertEquals("ゲルト", gerd.shortName)
        assertEquals("img/face01.jpg", gerd.faceIconURI)
    }

    @Test
    fun assignsSequentialTalkNoToPublicTalksOnly() {
        val story = parseStory(SAMPLE_XML)
        val talks = story.periods.flatMap { it.elements }.filterIsInstance<Talk>()
        val publicTalks = talks.filter { it.talkType == TalkType.PUBLIC }
        assertEquals(listOf(1, 2, 3), publicTalks.map { it.talkNo })

        val wolfTalk = talks.first { it.talkType == TalkType.WOLF }
        assertNull(wolfTalk.talkNo)
        val privateTalk = talks.first { it.talkType == TalkType.PRIVATE }
        assertNull(privateTalk.talkNo)
        val graveTalk = talks.first { it.talkType == TalkType.GRAVE }
        assertNull(graveTalk.talkNo)
    }

    @Test
    fun eachElementGetsAUniqueSyntheticId() {
        val story = parseStory(SAMPLE_XML)
        val ids = story.periods.flatMap { it.elements }.map { it.elementId }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun scalesFractionalSecondsToMilliseconds() {
        val story = parseStory(SAMPLE_XML)
        val talks = story.periods.flatMap { it.elements }.filterIsInstance<Talk>()
        val fractionalTalk = talks.first { it.xname == "mes2" }
        // 10:00:00.5 -> 10h + 0.5s = 36_000_000ms + 500ms
        assertEquals(36_000_500, fractionalTalk.time)

        val wholeSecondTalk = talks.first { it.xname == "mes1" }
        // 09:00:00 -> 9h
        assertEquals(32_400_000, wholeSecondTalk.time)
    }

    @Test
    fun concatenatesRawdataTextInsideLi() {
        val story = parseStory(SAMPLE_XML)
        val talks = story.periods.flatMap { it.elements }.filterIsInstance<Talk>()
        val talkWithRawdata = talks.first { it.xname == "mes1" }
        assertEquals("おはよう␚、みんな。", talkWithRawdata.messageLines.first())
    }

    @Test
    fun parsesEventsWithSubElements() {
        val story = parseStory(SAMPLE_XML)
        val elements = story.periods.flatMap { it.elements }

        val openRole = elements.filterIsInstance<OpenRole>().single()
        assertEquals(mapOf(Role.INNOCENT to 3, Role.WOLF to 2, Role.SEER to 1), openRole.roleHeads)

        val murdered = elements.filterIsInstance<Murdered>().single()
        assertEquals(listOf("simon"), murdered.avatarIds)

        val survivor = elements.filterIsInstance<Survivor>().single()
        assertEquals(listOf("gerd", "thomas", "otto", "liesa", "clara"), survivor.avatarIds)

        val counting = elements.filterIsInstance<Counting>().single()
        assertEquals("thomas", counting.victim)
        assertEquals(mapOf("gerd" to "thomas", "liesa" to "thomas", "otto" to "gerd"), counting.votes)

        val counting2 = elements.filterIsInstance<Counting2>().single()
        assertEquals(mapOf("gerd" to "thomas", "liesa" to "thomas"), counting2.votes)

        val execution = elements.filterIsInstance<Execution>().single()
        assertEquals("thomas", execution.victim)
        assertEquals(mapOf("thomas" to 2, "gerd" to 1), execution.nominated)

        val playerList = elements.filterIsInstance<PlayerList>().single()
        assertEquals(6, playerList.players.size)
        val liesaPlayer = playerList.players.first { it.avatarId == "liesa" }
        assertEquals(Role.SEER, liesaPlayer.role)
        assertEquals(true, liesaPlayer.survive)
        assertEquals("https://example.com/liesa", liesaPlayer.uri)

        val judge = elements.filterIsInstance<Judge>().single()
        assertEquals("clara", judge.byWhom)
        assertEquals("thomas", judge.target)

        val assault = elements.filterIsInstance<Assault>().single()
        assertEquals("otto", assault.byWhom)
        assertEquals("simon", assault.target)
        assertEquals(86_340_000, assault.time)
    }

    @Test
    fun periodTypesAreParsedStrictly() {
        val story = parseStory(SAMPLE_XML)
        assertEquals(
            listOf(PeriodType.PROLOGUE, PeriodType.PROGRESS, PeriodType.EPILOGUE),
            story.periods.map { it.type },
        )
    }

    @Test
    fun throwsOnMissingRequiredAttribute() {
        val invalidXml = """
            <village xml:base="https://example.com/" landId="wolfg" graveIconURI="img/grave.png">
            <avatarList/>
            </village>
        """.trimIndent()
        assertFailsWith<InvalidArchiveException> { parseStory(invalidXml) }
    }

    @Test
    fun throwsOnUnknownEnumValue() {
        val invalidXml = """
            <village fullName="村" xml:base="https://example.com/" landId="wolfg" graveIconURI="img/grave.png">
            <avatarList/>
            <period type="bogus" day="0"><talk type="public" avatarId="gerd" xname="mes1" time="00:00:00" /></period>
            </village>
        """.trimIndent()
        assertFailsWith<InvalidArchiveException> { parseStory(invalidXml) }
    }

    @Test
    fun rootElementTypeIsStoryElementSealedHierarchy() {
        val story = parseStory(SAMPLE_XML)
        val firstElement = story.periods.first().elements.first()
        assertIs<StartEntry>(firstElement)
    }
}
