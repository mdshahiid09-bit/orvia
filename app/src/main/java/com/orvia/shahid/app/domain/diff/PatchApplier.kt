package com.orvia.shahid.app.domain.diff

enum class DiffLineType {
    UNCHANGED,
    ADDED,
    DELETED
}

data class DiffLine(
    val type: DiffLineType,
    val text: String,
    val oldLineNumber: Int? = null,
    val newLineNumber: Int? = null
)

data class DiffResult(
    val lines: List<DiffLine>,
    val additions: Int,
    val deletions: Int,
    val isIdentical: Boolean
)

object PatchApplier {

    /**
     * Computes a line-by-line diff between original and modified text.
     */
    fun computeDiff(original: String, modified: String): DiffResult {
        val origLines = if (original.isEmpty()) emptyList() else original.lines()
        val modLines = if (modified.isEmpty()) emptyList() else modified.lines()

        if (origLines == modLines) {
            val lines = origLines.mapIndexed { idx, line ->
                DiffLine(DiffLineType.UNCHANGED, line, idx + 1, idx + 1)
            }
            return DiffResult(lines, 0, 0, true)
        }

        val n = origLines.size
        val m = modLines.size
        val dp = Array(n + 1) { IntArray(m + 1) }

        for (i in 0 until n) {
            for (j in 0 until m) {
                if (origLines[i] == modLines[j]) {
                    dp[i + 1][j + 1] = dp[i][j] + 1
                } else {
                    dp[i + 1][j + 1] = maxOf(dp[i + 1][j], dp[i][j + 1])
                }
            }
        }

        val resultLines = mutableListOf<DiffLine>()
        var i = n
        var j = m
        var additions = 0
        var deletions = 0

        val temp = mutableListOf<DiffLine>()
        while (i > 0 || j > 0) {
            if (i > 0 && j > 0 && origLines[i - 1] == modLines[j - 1]) {
                temp.add(DiffLine(DiffLineType.UNCHANGED, origLines[i - 1], i, j))
                i--
                j--
            } else if (j > 0 && (i == 0 || dp[i][j - 1] >= dp[i - 1][j])) {
                temp.add(DiffLine(DiffLineType.ADDED, modLines[j - 1], null, j))
                additions++
                j--
            } else if (i > 0 && (j == 0 || dp[i][j - 1] < dp[i - 1][j])) {
                temp.add(DiffLine(DiffLineType.DELETED, origLines[i - 1], i, null))
                deletions++
                i--
            }
        }

        resultLines.addAll(temp.reversed())
        return DiffResult(resultLines, additions, deletions, additions == 0 && deletions == 0)
    }

    /**
     * Applies a search-and-replace patch or full content update.
     */
    fun applyPatch(original: String, patchOrNewContent: String): String {
        val searchReplaceRegex = Regex(
            "<<<<<<< SEARCH\\r?\\n([\\s\\S]*?)=======\\r?\\n([\\s\\S]*?)>>>>>>> REPLACE",
            RegexOption.MULTILINE
        )

        val matches = searchReplaceRegex.findAll(patchOrNewContent).toList()
        if (matches.isNotEmpty()) {
            var patched = original
            for (match in matches) {
                val search = match.groupValues[1]
                val replace = match.groupValues[2]
                if (patched.contains(search)) {
                    patched = patched.replaceFirst(search, replace)
                }
            }
            return patched
        }

        return patchOrNewContent
    }
}
