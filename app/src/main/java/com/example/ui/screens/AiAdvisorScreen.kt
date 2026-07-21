package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.ParsedReceiptResult
import com.example.ui.components.GlassCard
import com.example.ui.viewmodel.AiAdvisorUiState
import java.io.InputStream
import java.util.Locale

@Composable
fun AiAdvisorScreen(
    state: AiAdvisorUiState,
    onRunAudit: () -> Unit,
    onScanReceipt: (Bitmap) -> Unit,
    onConfirmScannedReceipt: (ParsedReceiptResult) -> Unit,
    onDismissReceipt: () -> Unit
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    onScanReceipt(bitmap)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .testTag("ai_advisor_screen")
    ) {
        // Header
        Text(
            text = "INTELLIGENCE",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Kai AI Strategist",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Hero Intelligence Card
        GlassCard(
            modifier = Modifier.padding(bottom = 16.dp),
            testTag = "ai_hero_card"
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "GEMINI HIGH THINKING ENGINE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Automated Strategic Wealth Audit",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Leverage Gemini 3.1 Pro high reasoning to evaluate cash flow efficiency, spot spend leakages, and craft tailored investment steps.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onRunAudit,
                        enabled = !state.isLoading,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("run_audit_btn"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reasoning...")
                        } else {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Run Audit")
                        }
                    }

                    OutlinedButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        enabled = !state.isLoading,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("scan_receipt_btn"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Scan Receipt")
                    }
                }
            }
        }

        // Scanned Receipt Modal / Card
        state.scannedReceiptResult?.let { scanned ->
            GlassCard(
                modifier = Modifier.padding(bottom = 16.dp),
                testTag = "scanned_receipt_card"
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AUTO-EXTRACTED RECEIPT",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.Green
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = scanned.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Amount: $${String.format(Locale.US, "%,.2f", scanned.amount)} • ${scanned.category.name}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onConfirmScannedReceipt(scanned) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("confirm_receipt_tx_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Save Entry")
                        }

                        OutlinedButton(
                            onClick = onDismissReceipt,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("dismiss_receipt_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Dismiss")
                        }
                    }
                }
            }
        }

        // Error message if any
        state.errorMessage?.let { err ->
            GlassCard(
                modifier = Modifier.padding(bottom = 16.dp),
                testTag = "ai_error_card"
            ) {
                Text(
                    text = err,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Red
                )
            }
        }

        // Audit Output Report Display Card
        GlassCard(
            modifier = Modifier.padding(bottom = 16.dp),
            testTag = "ai_audit_report_card"
        ) {
            Column {
                Text(
                    text = "AI FINANCIAL STRATEGY REPORT",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (state.analysisReport != null) {
                    Text(
                        text = state.analysisReport,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 24.sp
                    )
                } else {
                    Text(
                        text = "Tap 'Run Audit' above to generate a high-reasoning financial strategy powered by Gemini AI.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}
