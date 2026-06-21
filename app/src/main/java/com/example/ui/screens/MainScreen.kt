package com.example.ui.screens

import android.os.Environment
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qnn.ModelManager
import com.example.qnn.QnnBridge
import com.example.ui.QnnViewModel
import com.example.ui.theme.*
import java.io.File
import kotlinx.coroutines.launch

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: QnnViewModel,
    onNavigateToDocs: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val localModels by viewModel.localModels.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()
    val selectedPreset by viewModel.selectedPreset.collectAsState()
    val selectedBackend by viewModel.selectedBackend.collectAsState()
    val qnnLogs by viewModel.qnnLogs.collectAsState()
    val rootState by viewModel.rootState.collectAsState()
    val parsedMetadata by viewModel.parsedMetadata.collectAsState()
    val contextWindow by viewModel.contextWindow.collectAsState()
    val streamingOutput by viewModel.streamingOutput.collectAsState()
    val blobStatuses by viewModel.blobStatuses.collectAsState()

    var showModelPicker by remember { mutableStateOf(false) }
    var showPresetPicker by remember { mutableStateOf(false) }
    var showInstallPanel by remember { mutableStateOf(false) }
    var sourceDirPathInput by remember { mutableStateOf("/sdcard/moonstone_vendor_blobs_13_miui14") }
    var forceVendorInstall by remember { mutableStateOf(false) }

    val logsListState = rememberLazyListState()

    // Auto-scroll logic for log terminal
    LaunchedEffect(qnnLogs.size) {
        if (qnnLogs.isNotEmpty()) {
            logsListState.animateScrollToItem(qnnLogs.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "QNN-LLM-Auto",
                            fontWeight = FontWeight.Bold,
                            color = ElegantLavender,
                            fontSize = 20.sp,
                            letterSpacing = (-0.5).sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (rootState?.isRooted == true) LogSuccessGreen else LogWarningOrange)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (rootState?.isRooted == true) "ROOT ACCESS ACTIVE" else "LOCAL SANDBOX FALLBACK",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (rootState?.isRooted == true) LogSuccessGreen else LogWarningOrange,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToDocs) {
                        Icon(
                            imageVector = Icons.Default.Book,
                            contentDescription = "Справочник",
                            tint = ElegantLavender
                        )
                    }
                    IconButton(onClick = { viewModel.checkForRoot() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Проверить Root",
                            tint = ElegantLavender
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepCharcoalBg)
            )
        },
        containerColor = DeepCharcoalBg
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // 1. Core Qualcomm NPU Auto-Detection Card
            item {
                val isNpuActive = viewModel.isNpuAvailable
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isNpuActive) LogSuccessGreen.copy(alpha = 0.08f) else SnapdragonRed.copy(alpha = 0.06f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .border(
                            1.dp,
                            if (isNpuActive) LogSuccessGreen.copy(alpha = 0.4f) else SnapdragonRed.copy(alpha = 0.4f),
                            RoundedCornerShape(16.dp)
                        )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(if (isNpuActive) LogSuccessGreen else SnapdragonRed),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isNpuActive) Icons.Default.Check else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = DeepCharcoalBg,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = if (isNpuActive) "NPU + GPU + CPU АКТИВИРОВАНО" else "NPU НЕ НАЙДЕН",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = if (isNpuActive) LogSuccessGreen else SnapdragonRed,
                                        letterSpacing = (-0.2).sp
                                    )
                                    Text(
                                        text = if (isNpuActive) "Snapdragon Tensor Core (HTP+GPU) готов к инференсу" else "Режим: GPU + CPU только (софтверный fallback)",
                                        fontSize = 11.sp,
                                        color = LightSlateText
                                    )
                                }
                            }

                            // Mode indicator on the top right
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isNpuActive) LogSuccessGreen.copy(alpha = 0.2f) else SnapdragonRed.copy(alpha = 0.2f))
                                    .padding(vertical = 4.dp, horizontal = 8.dp)
                            ) {
                                Text(
                                    text = if (isNpuActive) "NPU РЕЖИМ" else "GPU+CPU РЕЖИМ",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isNpuActive) LogSuccessGreen else SnapdragonRed,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        
                        // Detailed file list to showcase precise Snapdragon verification
                        Text(
                            text = "Статус системных библиотек Qualcomm (/vendor/*):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CozyPureWhite,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DeepCharcoalBg.copy(alpha = 0.6f))
                                .padding(8.dp)
                        ) {
                            blobStatuses.forEach { status ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (status.exists) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                            contentDescription = null,
                                            tint = if (status.exists) LogSuccessGreen else SnapdragonRed,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = status.filename,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp,
                                            color = if (status.exists) CozyPureWhite else MutedSlateText
                                        )
                                    }
                                    Text(
                                        text = if (status.exists) "FOUND" else "MISSING",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (status.exists) LogSuccessGreen else SnapdragonRed
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Controls: Rescan and Clear Cache buttons
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { viewModel.scanSystemBlobs() },
                                colors = ButtonDefaults.buttonColors(containerColor = SlateOutline),
                                modifier = Modifier.weight(1.3f),
                                contentPadding = PaddingValues(vertical = 4.dp, horizontal = 4.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp), tint = HexagonTeal)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Перепроверить blobs", fontSize = 11.sp, color = HexagonTeal)
                            }

                            Button(
                                onClick = { viewModel.clearCompilationCache() },
                                colors = ButtonDefaults.buttonColors(containerColor = SlateOutline),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 4.dp, horizontal = 4.dp)
                            ) {
                                Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(14.dp), tint = SnapdragonRed)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Очистить кэш", fontSize = 11.sp, color = SnapdragonRed)
                            }
                        }
                    }
                }
            }

            // 2. Root Capabilities Card
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateCardBg),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (rootState?.isRooted == true) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (rootState?.isRooted == true) LogSuccessGreen else LogWarningOrange,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (rootState?.isRooted == true) "ROOT ДОСТУПЕН via su" else "СИМУЛЯЦИЯ / NO ROOT",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (rootState?.isRooted == true) LogSuccessGreen else LogWarningOrange
                                )
                            }
                            Button(
                                onClick = { showInstallPanel = !showInstallPanel },
                                colors = ButtonDefaults.buttonColors(containerColor = SlateOutline),
                                modifier = Modifier.testTag("toggle_install_btn"),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (showInstallPanel) "Скрыть панель" else "Копировать Blobs",
                                    fontSize = 11.sp,
                                    color = HexagonTeal,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (rootState?.isRooted == false) {
                            Text(
                                text = "Приложение работает в режиме Sandbox (без Root). Драйверы Qualcomm считываются из доступных папок без копирования в /vendor/.",
                                fontSize = 12.sp,
                                color = MutedSlateText,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        } else {
                            Text(
                                text = "Версия superuser: ${rootState?.suVersion}. Права суперпользователя активны. Доступна прошивка напрямую в /vendor/lib64/.",
                                fontSize = 12.sp,
                                color = LightSlateText,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                    }
                }
            }

            // Expanding Vendor copy Panel
            item {
                AnimatedVisibility(visible = showInstallPanel) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .border(1.dp, SnapdragonRed.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                        colors = CardDefaults.cardColors(containerColor = SlateCardBg)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "ПАНЕЛЬ УСТАНОВКИ QNN BLOBS",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SnapdragonRed,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Укажите абсолютный путь к moonstone_vendor_blobs на SD-карте устройства:",
                                fontSize = 12.sp,
                                color = LightSlateText
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = sourceDirPathInput,
                                onValueChange = { sourceDirPathInput = it },
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    color = Color.White
                                ),
                                placeholder = { Text("/sdcard/...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("source_path_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SnapdragonRed,
                                    unfocusedBorderColor = SlateOutline
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { forceVendorInstall = !forceVendorInstall }
                            ) {
                                Checkbox(
                                    checked = forceVendorInstall,
                                    onCheckedChange = { forceVendorInstall = it },
                                    colors = CheckboxDefaults.colors(checkedColor = SnapdragonRed)
                                )
                                Text(
                                    text = "Прямая запись в /vendor/lib64 (требует Unlock)",
                                    fontSize = 12.sp,
                                    color = LightSlateText
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    viewModel.installVendorBlobs(sourceDirPathInput, forceVendorInstall)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("copy_blobs_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = SnapdragonRed),
                                enabled = !viewModel.isInstallingBlobs
                            ) {
                                if (viewModel.isInstallingBlobs) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.FileCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Запустить копирование весов и драйверов", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Config Panel (Backend, presets, clear cache)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateCardBg)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "ПАРАМЕТРЫ СИСТЕМЫ",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = HexagonTeal,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Backend selection Dropdown
                        Text("Аппаратный ускоритель QNN Backend:", fontSize = 13.sp, color = MutedSlateText)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            QnnBridge.Backend.values().forEach { backend ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(4.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selectedBackend == backend) HexagonTeal.copy(alpha = 0.2f) else SlateOutline)
                                        .border(
                                            1.dp,
                                            if (selectedBackend == backend) HexagonTeal else SlateOutline,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { viewModel.setBackend(backend) }
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = when (backend) {
                                                QnnBridge.Backend.HTP -> Icons.Default.Bolt
                                                QnnBridge.Backend.GPU -> Icons.Default.Memory
                                                QnnBridge.Backend.CPU -> Icons.Default.Settings
                                            },
                                            contentDescription = null,
                                            tint = if (selectedBackend == backend) HexagonTeal else Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = backend.id,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (selectedBackend == backend) HexagonTeal else Color.White
                                        )
                                    }
                                }
                            }
                        }

                        Text(
                            text = selectedBackend.detail,
                            fontSize = 11.sp,
                            color = MutedSlateText,
                            modifier = Modifier.padding(top = 6.dp, start = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Model selection File selector & Presets
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Выбранная LLM-модель (GGUF/weights):", fontSize = 13.sp, color = MutedSlateText)
                                Text(
                                    text = selectedModel?.name ?: "Модель не выбрана",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (selectedModel != null) Color.White else SnapdragonRed
                                )
                            }
                            Row {
                                Button(
                                    onClick = { showModelPicker = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = SlateOutline),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Обзор", fontSize = 12.sp, color = HexagonTeal)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Button(
                                    onClick = { viewModel.createDemoGguf("qwen2_1_5b_instruct_q5_k_m.gguf") },
                                    colors = ButtonDefaults.buttonColors(containerColor = SlateOutline),
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Демо", fontSize = 12.sp, color = BrightAmber)
                                }
                            }
                        }

                        // Model Metadata details if parsed
                        parsedMetadata?.let { meta ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(DeepCharcoalBg)
                                    .padding(8.dp)
                            ) {
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("Архитектура: ${meta.architecture}", fontSize = 11.sp, color = LightSlateText, fontFamily = FontFamily.Monospace)
                                    Text("Размер: ${meta.parametersCount}", fontSize = 11.sp, color = LightSlateText, fontFamily = FontFamily.Monospace)
                                }
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                                    Text("Квантование: ${meta.quantization}", fontSize = 11.sp, color = LightSlateText, fontFamily = FontFamily.Monospace)
                                    Text("Контекст: ${meta.contextLength}", fontSize = 11.sp, color = HexagonTeal, fontFamily = FontFamily.Monospace)
                                }
                                meta.additionalInfo.forEach { (k, v) ->
                                    Text("$k: $v", fontSize = 10.sp, color = MutedSlateText, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Clear cache & Init Engine buttons
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { viewModel.clearCompilationCache() },
                                colors = ButtonDefaults.buttonColors(containerColor = SlateOutline),
                                modifier = Modifier.weight(1f).padding(end = 4.dp).testTag("clear_cache_btn"),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Сброс кэша", fontSize = 12.sp, color = SnapdragonRed)
                            }

                            Button(
                                onClick = { viewModel.initQnnEngine() },
                                colors = ButtonDefaults.buttonColors(containerColor = HexagonTeal),
                                modifier = Modifier.weight(1f).padding(start = 4.dp).testTag("init_engine_btn"),
                                enabled = !viewModel.isInitializingQnn
                            ) {
                                if (viewModel.isInitializingQnn) {
                                    CircularProgressIndicator(color = DeepCharcoalBg, modifier = Modifier.size(16.dp))
                                } else {
                                    Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp), tint = DeepCharcoalBg)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (viewModel.isQnnInitialized) "Перезапустить JNI" else "Активировать NPU",
                                        color = DeepCharcoalBg,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Realtime Prompt & Generation UI
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateCardBg)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "ИНФЕРЕНС LLM-МОДЕЛИ",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = HexagonTeal,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = viewModel.inputPrompt,
                            onValueChange = { viewModel.inputPrompt = it },
                            label = { Text("Input Prompt") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("prompt_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = HexagonTeal,
                                unfocusedBorderColor = SlateOutline
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { viewModel.runInferenceQuery() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("run_inference_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = HexagonTeal),
                            enabled = !viewModel.isGeneratingState
                        ) {
                            if (viewModel.isGeneratingState) {
                                CircularProgressIndicator(color = DeepCharcoalBg, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Inference running...", color = DeepCharcoalBg, fontWeight = FontWeight.Bold)
                            } else {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = DeepCharcoalBg)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Запустить инференс", color = DeepCharcoalBg, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Progress and TPS details if generating
                        if (viewModel.isGeneratingState || viewModel.totalTokensGenerated > 0) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Скорость NPU: ${String.format("%.1f", viewModel.currentSpeedTokensPerSec)} токенов/сек",
                                    fontSize = 12.sp,
                                    color = HexagonTeal,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${viewModel.totalTokensGenerated} токенов сгенерировано",
                                    fontSize = 12.sp,
                                    color = CozyPureWhite
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = viewModel.generationProgressPercentage,
                                color = HexagonTeal,
                                trackColor = SlateOutline,
                                modifier = Modifier.fillMaxWidth().height(4.dp)
                            )
                        }

                        // Text Stream Response Viewer
                        if (streamingOutput.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Вывод (Streaming Tokens):", fontSize = 12.sp, color = MutedSlateText)
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DeepCharcoalBg)
                                    .border(1.dp, SlateOutline, RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = streamingOutput,
                                    fontSize = 14.sp,
                                    color = CozyPureWhite,
                                    lineHeight = 22.sp
                                )
                            }
                        }
                    }
                }
            }

            // Diagnostic Logger section at the bottom (Terminal simulation)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .height(280.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateCardBg)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Terminal, contentDescription = null, tint = HexagonTeal, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "QNN ТЕРМИНАЛ / ЛОГИ Diagnostic Console",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CozyPureWhite,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Text(
                                text = "Очистить",
                                fontSize = 12.sp,
                                color = SnapdragonRed,
                                modifier = Modifier
                                    .clickable { viewModel.clearSystemConsole() }
                                    .padding(4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        // List of console log rows
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(6.dp))
                                .background(DeepCharcoalBg)
                                .padding(8.dp)
                        ) {
                            if (qnnLogs.isEmpty()) {
                                Text(
                                    text = "Системные логи пусты. Инициализируйте контекст или начните копирование blobs/weights.",
                                    fontSize = 12.sp,
                                    color = MutedSlateText,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            } else {
                                LazyColumn(
                                    state = logsListState,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(qnnLogs) { logMsg ->
                                        val color = when (logMsg.level) {
                                            QnnViewModel.LogLevel.INFO -> LogInfoBlue
                                            QnnViewModel.LogLevel.SUCCESS -> LogSuccessGreen
                                            QnnViewModel.LogLevel.WARNING -> LogWarningOrange
                                            QnnViewModel.LogLevel.ERROR -> LogErrorRed
                                        }
                                        val prefix = when (logMsg.level) {
                                            QnnViewModel.LogLevel.INFO -> "[INFO]"
                                            QnnViewModel.LogLevel.SUCCESS -> "[OK]"
                                            QnnViewModel.LogLevel.WARNING -> "[WARN]"
                                            QnnViewModel.LogLevel.ERROR -> "[ERR]"
                                        }
                                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                                            Text(
                                                text = "$prefix ",
                                                color = color,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Text(
                                                text = logMsg.text,
                                                color = Color.White.copy(alpha = 0.9f),
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace,
                                                lineHeight = 15.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal File Picker Dialog (Simulation since we have no actual file explorer popup on direct device compile)
    if (showModelPicker) {
        AlertDialog(
            onDismissRequest = { showModelPicker = false },
            title = { Text("Выберите модель с накопителя") },
            text = {
                Column {
                    Text(
                        text = "Сканирование папок /sdcard/Download и /data ...",
                        fontSize = 13.sp,
                        color = MutedSlateText,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    if (localModels.isEmpty()) {
                        Text(
                            text = "Файлов .gguf / .safetensors не найдено.",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = SnapdragonRed
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Нажмите 'Создать Демо' в окне настроек для мгновенного развертывания симуляционной модели.",
                            fontSize = 12.sp,
                            color = LightSlateText
                        )
                    } else {
                        LazyColumn(modifier = Modifier.height(200.dp)) {
                            items(localModels) { file ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable {
                                            viewModel.selectModel(file)
                                            showModelPicker = false
                                        },
                                    colors = CardDefaults.cardColors(containerColor = SlateOutline)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Dataset, contentDescription = null, tint = HexagonTeal)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(file.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            Text(file.parent ?: "/", fontSize = 10.sp, color = MutedSlateText)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showModelPicker = false }) {
                    Text("Закрыть", color = HexagonTeal)
                }
            },
            containerColor = SlateCardBg,
            titleContentColor = Color.White,
            textContentColor = LightSlateText
        )
    }
}

// Extension to avoid compilation error on extra properties if present
private var Modifier.modifier2: Modifier
    get() = this
    set(value) {}
