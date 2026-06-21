package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun DocScreen(onBack: () -> Unit) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("QNN Reference & Mappings", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "Назад",
                            tint = HexagonTeal
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepCharcoalBg,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = DeepCharcoalBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            Text(
                text = "QUALCOMM AI ENGINE DIRECT (QNN)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = SnapdragonRed,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Документация и маппинг файлов",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Introduction Card
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCardBg),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = HexagonTeal)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Что такое QNN SDK?",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = HexagonTeal
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Qualcomm Neural Network (QNN) SDK — это специализированный низкоуровневый инструмент от Qualcomm для прямого программирования NPU (Hexagon DSP/HTP) и GPU Adreno. Программа QNN-LLM-Auto использует библиотеки QNN для прямого инференса больших языковых моделей (LLM) без посредников в лице Android NNAPI.",
                        fontSize = 14.sp,
                        color = LightSlateText,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // File Mapping Section
            Text(
                text = "СПИСОК И ТАРГЕТЫ КОПИРОВАНИЯ БЛОБОВ",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = HexagonTeal,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(8.dp))

            MappingItem(
                fileName = "com.qti.feature2.ml.so",
                targetPath = "/vendor/lib64/com.qti.feature2.ml.so",
                description = "Основной системный ML плагин для MIUI/HyperOS, отвечающий за аппаратное связывание сокет-портов NPU."
            )

            MappingItem(
                fileName = "libQnnSystem.so",
                targetPath = "/vendor/lib64/libQnnSystem.so",
                description = "Системное ядро QNN API. Обеспечивает аллокацию контекста, чтение унифицированных графов в памяти."
            )

            MappingItem(
                fileName = "libQnnIr.so",
                targetPath = "/vendor/lib64/libQnnIr.so",
                description = "Промежуточный компилятор графов. Преобразует GGUF слои тензоров во внутренний формат QNN IR."
            )

            MappingItem(
                fileName = "libQnnGpu.so",
                targetPath = "/vendor/lib64/libQnnGpu.so",
                description = "Библиотека прямого инференса на GPU Adreno через расширенные шейдеры OpenCL."
            )

            MappingItem(
                fileName = "unified_kbcs_64.bin",
                targetPath = "/vendor/gpu/kbc/unified_kbcs_64.bin",
                description = "Скомпилированные шейдерные ядра KBC для расчёта матриц свертки на GPU Adreno."
            )

            MappingItem(
                fileName = "unified_ksqs.bin",
                targetPath = "/vendor/gpu/kbc/unified_ksqs.bin",
                description = "Конфигурация квантования весов и распределения тензоров по кластерам GPU."
            )

            MappingItem(
                fileName = "sequence_manifest.bin",
                targetPath = "/vendor/gpu/kbc/sequence_manifest.bin",
                description = "Манифест вызова цепочек операций для Adreno GPU."
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Magisk and locks card
            Text(
                text = "ОБХОД ОГРАНИЧЕНИЙ ЧТЕНИЯ/ЗАПИСИ (DM-VERITY)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = SnapdragonRed,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCardBg),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Terminal, contentDescription = null, tint = SnapdragonRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Проблема Read-Only в Android 15",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = SnapdragonRed
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "В Android 13/14/15 раздел /vendor защищен суперблоком шифрования dm-verity и примонтирован как строго Read-Only. Direct remount (mount -o rw,remount) может вернуть ошибку Permission Denied даже с SU.",
                        fontSize = 14.sp,
                        color = LightSlateText
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Решение через Magisk Module (Bind Mount):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = CozyPureWhite
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        color = DeepCharcoalBg,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "# Создайте Magisk модуль с маппингом:\n" +
                                        "mkdir -p /data/adb/modules/qnn_overlay/system/vendor/lib64\n" +
                                        "cp {blobs} /data/adb/modules/qnn_overlay/system/vendor/lib64/\n" +
                                        "chmod -R 755 /data/adb/modules/qnn_overlay",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = HexagonTeal
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "* Приложение QNN-LLM-Runner автоматически определяет, заблокирован ли раздел, и позволяет использовать 'Local App Sandbox' режим, который эмулирует загрузку библиотек из памяти приложения, сохраняя полную функциональность.",
                        fontSize = 13.sp,
                        color = MutedSlateText
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun MappingItem(
    fileName: String,
    targetPath: String,
    description: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SlateCardBg),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = fileName,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Surface(
                color = DeepCharcoalBg,
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = targetPath,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = HexagonTeal,
                    modifier = Modifier.padding(6.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                fontSize = 13.sp,
                color = LightSlateText,
                lineHeight = 18.sp
            )
        }
    }
}
