package com.dddqmmx.util;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class AudioMixer {
    public static void mix(File voiceFile, File instrumentFile, File outputFile, float voiceVolume, float instrumentVolume) throws Exception {
        if (outputFile == null || outputFile.isDirectory()) {
            throw new IllegalArgumentException("The output audio file must be a valid file path");
        }
        AudioInputStream mixAIS = null;
        try {
            AudioInputStream voiceAIS = AudioSystem.getAudioInputStream(voiceFile);
            AudioInputStream instrumentAIS = AudioSystem.getAudioInputStream(instrumentFile);
            AudioFormat format = voiceAIS.getFormat();
            mixAIS = mixAudioStreams(voiceAIS, instrumentAIS, format, voiceVolume, instrumentVolume);
            AudioSystem.write(mixAIS, AudioFileFormat.Type.WAVE, outputFile);
        } finally {
            if (mixAIS != null) {
                mixAIS.close();
            }
        }
    }

    private static AudioInputStream adjustVolume(AudioInputStream audioInputStream, float volume) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = audioInputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(applyVolume(buffer, volume), 0, bytesRead);
        }
        byte[] mixedData = byteArrayOutputStream.toByteArray();
        ByteArrayInputStream mixedStream = new ByteArrayInputStream(mixedData);
        return new AudioInputStream(mixedStream, audioInputStream.getFormat(), mixedData.length / audioInputStream.getFormat().getFrameSize());
    }

    private static byte[] applyVolume(byte[] audioData, float volume) {
        for (int i = 0; i < audioData.length; i += 2) {
            short sample = (short) (((audioData[i + 1] & 0xff) << 8) | (audioData[i] & 0xff));
            sample = (short) (sample * volume);
            audioData[i] = (byte) (sample & 0xff);
            audioData[i + 1] = (byte) ((sample >> 8) & 0xff);
        }
        return audioData;
    }

    private static AudioInputStream mixAudioStreams(AudioInputStream voiceAIS, AudioInputStream instrumentAIS, AudioFormat format, float voiceVolume, float instrumentVolume) throws IOException {
        voiceAIS = convertToTargetFormat(voiceAIS, format);
        instrumentAIS = convertToTargetFormat(instrumentAIS, format);
        byte[] audioData1 = getAudioData(voiceAIS);
        byte[] audioData2 = getAudioData(instrumentAIS);
        int maxLength = Math.max(audioData1.length, audioData2.length);
        byte[] mixedData = new byte[maxLength];
        for (int i = 0; i < maxLength; i++) {
            int sample1 = i < audioData1.length ? audioData1[i] : 0;
            int sample2 = i < audioData2.length ? audioData2[i] : 0;
            int mixedSample = (int) (sample1 * voiceVolume + sample2 * instrumentVolume) / 2;
            mixedData[i] = (byte) mixedSample;
        }
        ByteArrayInputStream mixedStream = new ByteArrayInputStream(mixedData);
        return new AudioInputStream(mixedStream, format, mixedData.length / format.getFrameSize());
    }

    private static byte[] getAudioData(AudioInputStream audioStream) throws IOException {
        byte[] buffer = new byte[audioStream.available()];
        audioStream.read(buffer);
        return buffer;
    }
    private static AudioInputStream convertToTargetFormat(AudioInputStream audioStream, AudioFormat targetFormat) {
        AudioFormat sourceFormat = audioStream.getFormat();
        if (!sourceFormat.equals(targetFormat)) {
            return AudioSystem.getAudioInputStream(targetFormat, audioStream);
        }
        return audioStream;
    }

}

