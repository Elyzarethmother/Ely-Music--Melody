package com.example.audio

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import com.example.model.OrchestralPiece
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

object AudioExporter {

    private const val TAG = "AudioExporter"
    const val DEFAULT_SAMPLE_RATE = OrchestralSynthesizer.SAMPLE_RATE

    /**
     * Converts raw 16-bit PCM samples into a standard WAV byte array with a 44-byte RIFF header.
     */
    fun createWavBytes(
        pcmData: ShortArray,
        sampleRate: Int = DEFAULT_SAMPLE_RATE,
        numChannels: Int = 1
    ): ByteArray {
        val byteRate = sampleRate * numChannels * 2
        val blockAlign = numChannels * 2
        val dataSize = pcmData.size * 2
        val totalSize = 36 + dataSize

        val buffer = ByteBuffer.allocate(44 + dataSize)
        buffer.order(ByteOrder.LITTLE_ENDIAN)

        // RIFF chunk descriptor
        buffer.put("RIFF".toByteArray(Charsets.US_ASCII))
        buffer.putInt(totalSize)
        buffer.put("WAVE".toByteArray(Charsets.US_ASCII))

        // "fmt " sub-chunk
        buffer.put("fmt ".toByteArray(Charsets.US_ASCII))
        buffer.putInt(16) // Subchunk1Size for PCM
        buffer.putShort(1.toShort()) // AudioFormat 1 = Linear PCM
        buffer.putShort(numChannels.toShort())
        buffer.putInt(sampleRate)
        buffer.putInt(byteRate)
        buffer.putShort(blockAlign.toShort())
        buffer.putShort(16.toShort()) // BitsPerSample

        // "data" sub-chunk
        buffer.put("data".toByteArray(Charsets.US_ASCII))
        buffer.putInt(dataSize)

        // Raw 16-bit PCM data
        for (sample in pcmData) {
            buffer.putShort(sample)
        }

        return buffer.array()
    }

    /**
     * Exports the song to the app's cache/files directory ready for sharing or playing.
     * Can export as .mp3 or .wav.
     */
    fun exportSongFile(
        context: Context,
        piece: OrchestralPiece,
        pcmData: ShortArray,
        extension: String = "mp3"
    ): File? {
        return try {
            val audioDir = File(context.cacheDir, "audio").apply { if (!exists()) mkdirs() }
            val sanitizedTitle = piece.title
                .replace("[^a-zA-Z0-9_-]".toRegex(), "_")
                .take(30)
                .ifBlank { "symphonia_song" }
            val fileName = "${sanitizedTitle}_${piece.variationLabel.replace(" ", "")}.$extension"
            val file = File(audioDir, fileName)

            val wavBytes = createWavBytes(pcmData)
            FileOutputStream(file).use { fos ->
                fos.write(wavBytes)
                fos.flush()
            }
            file
        } catch (e: Exception) {
            Log.e(TAG, "Failed to export song file", e)
            null
        }
    }

    /**
     * Saves the final audio file directly into the device's public Music folder
     * so it shows up in media players and file managers.
     */
    fun saveToPublicMusic(
        context: Context,
        piece: OrchestralPiece,
        pcmData: ShortArray,
        extension: String = "mp3"
    ): Uri? {
        val sanitizedTitle = piece.title
            .replace("[^a-zA-Z0-9 _-]".toRegex(), " ")
            .trim()
            .ifBlank { "Symphonia Piece" }
        val displayName = "$sanitizedTitle.$extension"
        val mimeType = if (extension.equals("mp3", ignoreCase = true)) "audio/mpeg" else "audio/wav"

        return try {
            val wavBytes = createWavBytes(pcmData)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Audio.Media.DISPLAY_NAME, displayName)
                    put(MediaStore.Audio.Media.MIME_TYPE, mimeType)
                    put(MediaStore.Audio.Media.TITLE, sanitizedTitle)
                    put(MediaStore.Audio.Media.ARTIST, "Symphonia Studio")
                    put(MediaStore.Audio.Media.ALBUM, "AI Orchestral & Vocal Creations")
                    put(MediaStore.Audio.Media.RELATIVE_PATH, "${Environment.DIRECTORY_MUSIC}/Symphonia")
                    put(MediaStore.Audio.Media.IS_PENDING, 1)
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { os: OutputStream ->
                        os.write(wavBytes)
                        os.flush()
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.Audio.Media.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                }
                uri
            } else {
                val musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
                val symphoniaDir = File(musicDir, "Symphonia").apply { if (!exists()) mkdirs() }
                val targetFile = File(symphoniaDir, displayName)
                FileOutputStream(targetFile).use { fos ->
                    fos.write(wavBytes)
                    fos.flush()
                }
                Uri.fromFile(targetFile)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save song to public Music collection", e)
            null
        }
    }

    /**
     * Builds an Android Intent for sharing the exported song file.
     */
    fun createShareIntent(context: Context, file: File, piece: OrchestralPiece): Intent {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        return Intent(Intent.ACTION_SEND).apply {
            type = if (file.name.endsWith(".mp3", ignoreCase = true)) "audio/mpeg" else "audio/wav"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "${piece.title} (Symphonia AI Studio)")
            putExtra(
                Intent.EXTRA_TEXT,
                "🎵 Listen to \"${piece.title}\" — composed with AI lyrics & music in Symphonia Studio (${piece.styleDescription})!"
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
