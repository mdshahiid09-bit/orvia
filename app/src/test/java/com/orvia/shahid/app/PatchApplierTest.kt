package com.orvia.shahid.app

import com.orvia.shahid.app.domain.diff.DiffLineType
import com.orvia.shahid.app.domain.diff.PatchApplier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PatchApplierTest {

    @Test
    fun testComputeDiffIdenticalText() {
        val original = "Line 1\nLine 2\nLine 3"
        val modified = "Line 1\nLine 2\nLine 3"

        val diff = PatchApplier.computeDiff(original, modified)
        assertTrue(diff.isIdentical)
        assertEquals(0, diff.additions)
        assertEquals(0, diff.deletions)
        assertEquals(3, diff.lines.size)
        assertTrue(diff.lines.all { it.type == DiffLineType.UNCHANGED })
    }

    @Test
    fun testComputeDiffWithAdditionsAndDeletions() {
        val original = "Line 1\nLine 2\nLine 3"
        val modified = "Line 1\nLine 2 (Updated)\nLine 3\nLine 4"

        val diff = PatchApplier.computeDiff(original, modified)
        assertFalse(diff.isIdentical)
        assertTrue(diff.additions >= 2) // "Line 2 (Updated)" and "Line 4"
        assertTrue(diff.deletions >= 1) // "Line 2"
    }

    @Test
    fun testApplySearchReplacePatch() {
        val original = """
            fun main() {
                println("Old Hello")
            }
        """.trimIndent()

        val patch = """
            <<<<<<< SEARCH
                println("Old Hello")
            =======
                println("New Hello World")
            >>>>>>> REPLACE
        """.trimIndent()

        val result = PatchApplier.applyPatch(original, patch)
        assertTrue(result.contains("println(\"New Hello World\")"))
        assertFalse(result.contains("println(\"Old Hello\")"))
    }

    @Test
    fun testApplyFullContentPatch() {
        val original = "Old content"
        val newContent = "Brand new updated content"

        val result = PatchApplier.applyPatch(original, newContent)
        assertEquals("Brand new updated content", result)
    }
}
