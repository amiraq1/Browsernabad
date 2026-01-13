package com.nabdh.browser

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nabdh.browser.ui.theme.*
import org.mozilla.geckoview.GeckoView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserScreen(
    viewModel: BrowserViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var urlText by remember(state.url) { mutableStateOf(state.url) }
    
    // حالة النافذة المنبثقة
    val sheetState = rememberModalBottomSheetState()
    
    if (state.showSummarySheet) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.closeSummary() },
            sheetState = sheetState,
            containerColor = Color(0xFF1A1A1A), // لون رمادي غامق جداً
            contentColor = TextPrimary
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .padding(bottom = 32.dp)
            ) {
                // رأس النافذة
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("✨", fontSize = 24.sp)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "نبض AI",
                        style = MaterialTheme.typography.titleLarge,
                        color = NabdhPulseRed
                    )
                }
                
                Spacer(Modifier.height(24.dp))

                if (state.isSummarizing) {
                    // تأثير تحميل
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = NabdhPulseRed,
                        trackColor = Color.Transparent
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("جاري قراءة الصفحة وتحليل المحتوى...", color = Color.Gray)
                } else {
                    // النص الملخص
                    Text(
                        text = state.summaryResult ?: "فشل التلخيص",
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 28.sp
                    )
                }
            }
        }
    }

    Scaffold(
        containerColor = NabdhBlack,
        bottomBar = {
            // شريط التحكم السفلي العائم (Avant-Garde Style)
            Surface(
                color = NabdhSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp) // لجعله عائماً
                    .height(60.dp),
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 12.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // حقل العنوان (Minimalist)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .background(Color(0xFF252525), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        BasicTextField(
                            value = urlText,
                            onValueChange = { urlText = it },
                            textStyle = TextStyle(color = TextPrimary, fontSize = 14.sp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                            keyboardActions = KeyboardActions(onGo = { viewModel.loadUrl(urlText) }),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // زر التلخيص (قلب نبض)
                    IconButton(onClick = { viewModel.summarizePage() }) {
                        Text("✨", fontSize = 20.sp)
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. محرك العرض
            AndroidView(
                factory = { context ->
                    GeckoView(context).apply {
                        setSession(viewModel.session)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // 2. شريط التقدم النبضي (Gradient Pulse)
            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(NabdhPulseRed, Color.Transparent)
                            )
                        )
                )
            }
        }
    }
}
