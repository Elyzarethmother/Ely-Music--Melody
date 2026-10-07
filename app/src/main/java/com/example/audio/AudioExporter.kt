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
     * Builds an ID3v2.3 tag containing Title, Artist, Album, Genre, and embedded song lyrics.
     */
    fun buildId3v2Tag(piece: OrchestralPiece): ByteArray {
        val frames = mutableListOf<ByteArray>()

        fun makeTextFrame(id: String, text: String): ByteArray {
            val textBytes = text.toByteArray(Charsets.UTF_8)
            val frameSize = textBytes.size + 1 // 1 byte for encoding (0x03 = UTF-8)
            val frame = ByteBuffer.allocate(10 + frameSize)
            frame.order(ByteOrder.BIG_ENDIAN)
            frame.put(id.toByteArray(Charsets.US_ASCII))
            frame.putInt(frameSize)
            frame.putShort(0) // flags
            frame.put(3.toByte()) // UTF-8 encoding
            frame.put(textBytes)
            return frame.array()
        }

        frames.add(makeTextFrame("TIT2", piece.title))
        frames.add(makeTextFrame("TPE1", "Symphonia Studio"))
        frames.add(makeTextFrame("TALB", "AI Music & Lyrics Compositions"))
        frames.add(makeTextFrame("TCON", piece.styleDescription.take(30)))

        if (piece.lyrics.isNotBlank()) {
            val lyricBytes = piece.lyrics.toByteArray(Charsets.UTF_8)
            val frameSize = 1 + 3 + 1 + lyricBytes.size
            val frame = ByteBuffer.allocate(10 + frameSize)
            frame.order(ByteOrder.BIG_ENDIAN)
            frame.put("USLT".toByteArray(Charsets.US_ASCII))
            frame.putInt(frameSize)
            frame.putShort(0)
            frame.put(3.toByte()) // UTF-8
            frame.put("eng".toByteArray(Charsets.US_ASCII))
            frame.put(0.toByte()) // empty content descriptor terminator
            frame.put(lyricBytes)
            frames.add(frame.array())
        }

        val totalFramesSize = frames.sumOf { it.size }
        val header = ByteBuffer.allocate(10 + totalFramesSize)
        header.order(ByteOrder.BIG_ENDIAN)
        header.put("ID3".toByteArray(Charsets.US_ASCII))
        header.put(3.toByte()) // ID3v2.3
        header.put(0.toByte())
        header.put(0.toByte()) // flags

        // 4-byte syncsafe integer
        header.put(((totalFramesSize shr 21) and 0x7F).toByte())
        header.put(((totalFramesSize shr 14) and 0x7F).toByte())
        header.put(((totalFramesSize shr 7) and 0x7F).toByte())
        header.put((totalFramesSize and 0x7F).toByte())

        for (f in frames) {
            header.put(f)
        }

        return header.array()
    }

    /**
     * Builds complete audio file bytes with optional ID3 tags.
     */
    fun createAudioBytes(
        piece: OrchestralPiece,
        pcmData: ShortArray,
        asMp3: Boolean = true
    ): ByteArray {
        val wavBytes = createWavBytes(pcmData)
        if (!asMp3) return wavBytes

        // For MP3, attach ID3v2 metadata header before audio container
        val id3 = buildId3v2Tag(piece)
        val combined = ByteArray(id3.size + wavBytes.size)
        System.arraycopy(id3, 0, combined, 0, id3.size)
        System.arraycopy(wavBytes, 0, combined, id3.size, wavBytes.size)
        return combined
    }

    /**
     * Exports the song to the app's cache directory ready for sharing or playing.
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

            val audioBytes = createAudioBytes(piece, pcmData, asMp3 = extension.equals("mp3", ignoreCase = true))
            FileOutputStream(file).use { fos ->
                fos.write(audioBytes)
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
            val audioBytes = createAudioBytes(piece, pcmData, asMp3 = extension.equals("mp3", ignoreCase = true))

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Audio.Media.DISPLAY_NAME, displayName)
                    put(MediaStore.Audio.Media.MIME_TYPE, mimeType)
                    put(MediaStore.Audio.Media.TITLE, sanitizedTitle)
                    put(MediaStore.Audio.Media.ARTIST, "Symphonia Studio")
                    put(MediaStore.Audio.Media.ALBUM, "AI Music & Lyrics Compositions")
                    put(MediaStore.Audio.Media.RELATIVE_PATH, "${Environment.DIRECTORY_MUSIC}/Symphonia")
                    put(MediaStore.Audio.Media.IS_PENDING, 1)
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { os: OutputStream ->
                        os.write(audioBytes)
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
                    fos.write(audioBytes)
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
