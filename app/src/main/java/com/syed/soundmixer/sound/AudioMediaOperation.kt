package com.syed.soundmixer.sound

import java.io.*
import java.nio.ByteBuffer
import java.nio.ByteOrder

//https://gist.github.com/DrustZ/d3d3fc8fcc1067433db4dd3079f8d187
class AudioMediaOperation {
    companion object {
        fun mergeAudios(
            selection: Array<String>,
            outPath: String,
            onSuccess: () -> Unit,
            onFailure: () -> Unit
        ) {
            var recorderSampleRate = 0
            try {
                DataOutputStream(BufferedOutputStream(FileOutputStream(outPath))).use { amplifyOutputStream ->
                    val mergeFilesStream = Array(selection.size) { index ->
                        DataInputStream(
                            BufferedInputStream(FileInputStream(selection[index]))
                        )
                    }
                    val sizes = LongArray(selection.size) { index ->
                        val file = File(selection[index])
                        (file.length() - 44) / 2
                    }

                    for (i in selection.indices) {
                        val stream = mergeFilesStream[i]
                        if (i == selection.size - 1) {
                            stream.skip(24)
                            val sampleRt = ByteArray(4)
                            stream.read(sampleRt)
                            val bbInt = ByteBuffer.wrap(sampleRt).order(ByteOrder.LITTLE_ENDIAN)
                            recorderSampleRate = bbInt.int
                            stream.skip(16)
                        } else {
                            stream.skip(44)
                        }
                    }

                    for (b in selection.indices) {
                        for (i in 0 until sizes[b].toInt()) {
                            val dataBytes = ByteArray(2)
                            try {
                                dataBytes[0] = mergeFilesStream[b].readByte()
                                dataBytes[1] = mergeFilesStream[b].readByte()
                            } catch (e: EOFException) {
                                amplifyOutputStream.close()
                            }
                            val dataInShort =
                                ByteBuffer.wrap(dataBytes).order(ByteOrder.LITTLE_ENDIAN).short
                            val dataInFloat = dataInShort.toFloat() / 37268.0f

                            val outputSample = (dataInFloat * 37268.0f).toInt().toShort()
                            val dataFin = ByteArray(2)
                            dataFin[0] = (outputSample.toInt() and 0xff).toByte()
                            dataFin[1] = (outputSample.toInt() shr 8 and 0xff).toByte()
                            amplifyOutputStream.write(dataFin, 0, 2)
                        }
                    }

                    for (i in selection.indices) {
                        mergeFilesStream[i].close()
                    }
                }
            } catch (e: FileNotFoundException) {
                onFailure.invoke()
                e.printStackTrace()
            } catch (e: IOException) {
                onFailure.invoke()
                e.printStackTrace()
            }

            var size: Long = 0
            try {
                FileInputStream(outPath).use { fileSize ->
                    size = fileSize.channel.size()
                }
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            val recorderBpp = 16
            val dataSize = size + 36
            val byteRate = (recorderBpp * recorderSampleRate) / 8
            val longSampleRate = recorderSampleRate.toLong()
            val header = ByteArray(44)

            header[0] = 'R'.code.toByte()  // RIFF/WAVE header
            header[1] = 'I'.code.toByte()
            header[2] = 'F'.code.toByte()
            header[3] = 'F'.code.toByte()
            header[4] = (dataSize and 0xff).toByte()
            header[5] = ((dataSize shr 8) and 0xff).toByte()
            header[6] = ((dataSize shr 16) and 0xff).toByte()
            header[7] = ((dataSize shr 24) and 0xff).toByte()
            header[8] = 'W'.code.toByte()
            header[9] = 'A'.code.toByte()
            header[10] = 'V'.code.toByte()
            header[11] = 'E'.code.toByte()
            header[12] = 'f'.code.toByte()  // 'fmt ' chunk
            header[13] = 'm'.code.toByte()
            header[14] = 't'.code.toByte()
            header[15] = ' '.code.toByte()
            header[16] = 16  // 4 bytes: size of 'fmt ' chunk
            header[17] = 0
            header[18] = 0
            header[19] = 0
            header[20] = 1  // format = 1
            header[21] = 0
            header[22] = 1.toByte()
            header[23] = 0
            header[24] = (longSampleRate and 0xff).toByte()
            header[25] = ((longSampleRate shr 8) and 0xff).toByte()
            header[26] = ((longSampleRate shr 16) and 0xff).toByte()
            header[27] = ((longSampleRate shr 24) and 0xff).toByte()
            header[28] = (byteRate and 0xff).toByte()
            header[29] = ((byteRate shr 8) and 0xff).toByte()
            header[30] = ((byteRate shr 16) and 0xff).toByte()
            header[31] = ((byteRate shr 24) and 0xff).toByte()
            header[32] = (recorderBpp / 8).toByte()  // block align
            header[33] = 0
            header[34] = recorderBpp.toByte()  // bits per sample
            header[35] = 0
            header[36] = 'd'.code.toByte()
            header[37] = 'a'.code.toByte()
            header[38] = 't'.code.toByte()
            header[39] = 'a'.code.toByte()
            header[40] = (size and 0xff).toByte()
            header[41] = ((size shr 8) and 0xff).toByte()
            header[42] = ((size shr 16) and 0xff).toByte()
            header[43] = ((size shr 24) and 0xff).toByte()

            try {
                RandomAccessFile(outPath, "rw").use { rFile ->
                    rFile.seek(0)
                    rFile.write(header)
                    onSuccess.invoke()
                }
            } catch (e: FileNotFoundException) {
                onFailure.invoke()
                e.printStackTrace()
            } catch (e: IOException) {
                onFailure.invoke()
                e.printStackTrace()
            }
        }
    }
}