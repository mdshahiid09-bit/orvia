package com.orvia.shahid.app.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.orvia.shahid.app.R
import com.orvia.shahid.app.domain.diff.DiffLineType
import com.orvia.shahid.app.domain.diff.DiffResult
import com.orvia.shahid.app.ui.theme.DiffAddGreen
import com.orvia.shahid.app.ui.theme.DiffAddGreenBg
import com.orvia.shahid.app.ui.theme.DiffRemoveRed
import com.orvia.shahid.app.ui.theme.DiffRemoveRedBg

@Composable
fun DiffViewerDialog(
    diffResult: DiffResult,
    onAccept: () -> Unit,
    onDiscard: () -> Unit
) {
    Dialog(onDismissRequest = onDiscard) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF140F26))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.master_prompt_diff_title),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Row {
                        Text(
                            text = "+${diffResult.additions}",
                            color = DiffAddGreen,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "-${diffResult.deletions}",
                            color = DiffRemoveRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Diff Lines List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp)
                        .background(Color(0xFF0C0917), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFF2B2248), RoundedCornerShape(8.dp))
                        .padding(6.dp)
                ) {
                    items(diffResult.lines) { line ->
                        val (bgColor, textColor, prefix) = when (line.type) {
                            DiffLineType.ADDED -> Triple(DiffAddGreenBg, Color(0xFF81C784), "+ ")
                            DiffLineType.DELETED -> Triple(DiffRemoveRedBg, Color(0xFFE57373), "- ")
                            DiffLineType.UNCHANGED -> Triple(Color.Transparent, Color(0xFFB0AEC0), "  ")
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(bgColor)
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 2.dp, horizontal = 4.dp)
                        ) {
                            Text(
                                text = prefix + line.text,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = textColor,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDiscard,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(stringResource(R.string.reject_changes), color = Color.LightGray)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = onAccept,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DiffAddGreen)
                    ) {
                        Text(stringResource(R.string.accept_changes), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
