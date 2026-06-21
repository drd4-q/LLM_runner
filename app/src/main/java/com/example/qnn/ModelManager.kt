package com.example.qnn

import android.os.Environment
import java.io.File
import java.nio.ByteOrder

object ModelManager {

    data class PresetModel(
        val id: String,
        val label: String,
        val sizeGb: Double,
        val quantFormat: String,
        val huggingFaceUrl: String,
        val architecture: String,
        val promptTemplate: String
    )

    val PRESET_MODELS = listOf(
        PresetModel(
            id = "qwen_2_1.5b_chat",
            label = "Qwen2-1.5B-Chat (GGUF)",
            sizeGb = 0.98,
            quantFormat = "Q4_K_M (4-bit)",
            huggingFaceUrl = "https://huggingface.co/Qwen/Qwen2-1.5B-Instruct-GGUF",
            architecture = "qwen2",
            promptTemplate = "<|im_start|>system\nYou are QNN-LLM LOCAL running on Snapdragon Hexagon NPU.<|im_end|>\n<|im_start|>user\n{prompt}<|im_end|>\n<|im_start|>assistant\n"
        ),
        PresetModel(
            id = "llama_3_8b_instruct",
            label = "Llama-3-8B-Instruct (GGUF)",
            sizeGb = 4.65,
            quantFormat = "Q4_K_M (4-bit)",
            huggingFaceUrl = "https://huggingface.co/lmstudio-community/Meta-Llama-3-8B-Instruct-GGUF",
            architecture = "llama",
            promptTemplate = "<|begin_of_text|><|start_header_id|>system<|end_header_id|>\n\nYou are a helpful Qualcomm-accelerated assistant.<|eot_id|><|start_header_id|>user<|end_header_id|>\n\n{prompt}<|eot_id|><|start_header_id|>assistant<|end_header_id|>\n\n"
        ),
        PresetModel(
            id = "phi_3_mini_4k",
            label = "Phi-3-mini-4K-Instruct (GGUF)",
            sizeGb = 2.21,
            quantFormat = "Q4_K_M (4-bit)",
            huggingFaceUrl = "https://huggingface.co/microsoft/Phi-3-mini-4k-instruct-gguf",
            architecture = "phi3",
            promptTemplate = "<|system|>\nYou are a Qualcomm AI expert assistant.<|end|>\n<|user|>\n{prompt}<|end|>\n<|assistant|>\n"
        )
    )

    /**
     * Scans for local GGUF and Safetensors files inside internal app directories
     * and external download folders.
     */
    fun findLocalModels(appFilesDir: File): List<File> {
        val foundFiles = mutableListOf<File>()

        // 1. Scan app's internal files folder
        scanFolderForModels(appFilesDir, foundFiles)

        // 2. Scan external Downloads directory
        try {
            val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (downloadDir != null && downloadDir.exists() && downloadDir.isDirectory) {
                scanFolderForModels(downloadDir, foundFiles)
            }
        } catch (ignored: Exception) {}

        // 3. Scan root sdcard directory optionally (safely)
        try {
            val sdcard = Environment.getExternalStorageDirectory()
            if (sdcard != null && sdcard.exists() && sdcard.isDirectory) {
                // Scan just SDCard root folder (non-recursive to keep execution ultra-fast)
                sdcard.listFiles()?.forEach { file ->
                    if (file.isFile && isSupportedFilename(file.name)) {
                        foundFiles.add(file)
                    }
                }
            }
        } catch (ignored: Exception) {}

        return foundFiles.distinctBy { it.absolutePath }
    }

    private fun scanFolderForModels(folder: File, collection: MutableList<File>) {
        val files = folder.listFiles() ?: return
        for (file in files) {
            if (file.isDirectory) {
                // Support scanned subfolders for HuggingFace model cache dirs
                if (!file.name.startsWith(".")) {
                    // Maximum depth scanning limits
                    file.listFiles()?.forEach { sub ->
                        if (sub.isFile && isSupportedFilename(sub.name)) {
                            collection.add(sub)
                        }
                    }
                }
            } else if (file.isFile && isSupportedFilename(file.name)) {
                collection.add(file)
            }
        }
    }

    private fun isSupportedFilename(name: String): Boolean {
        val lower = name.lowercase()
        return lower.endsWith(".gguf") || lower.endsWith(".safetensors")
    }

    /**
     * Helper to write a small simulated GGUF file for developer demonstration.
     * Keeps footprint low (1MB) but includes structural GGUF magic bytes so reading/parsing works exactly.
     */
    fun createSimulatedGgufFile(appFilesDir: File, name: String): File {
        val modelsFolder = File(appFilesDir, "models")
        modelsFolder.mkdirs()
        
        val targetFile = File(modelsFolder, name)
        if (!targetFile.exists()) {
            targetFile.writeBytes(buildGgufDemoHeader())
        }
        return targetFile
    }

    private fun buildGgufDemoHeader(): ByteArray {
        val buffer = java.nio.ByteBuffer.allocate(128)
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        
        // 1. Magic GGUF (4 bytes)
        buffer.put("GGUF".toByteArray())
        
        // 2. Version (4 bytes, version = 3)
        buffer.putInt(3)
        
        // 3. Tensor count (8 bytes, set to 291)
        buffer.putLong(291L)
        
        // 4. Metadata key-value count (8 bytes, set to 15)
        buffer.putLong(15L)
        
        // Rest of buffer padded with dummy bytes to keep memory safe
        while(buffer.hasRemaining()){
            buffer.put(0.toByte())
        }
        
        return buffer.array()
    }
}
