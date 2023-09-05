package com.dddqmmx;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dddqmmx.util.*;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.OfflineAudio;
import net.mamoe.mirai.utils.ExternalResource;
import top.yumbo.util.music.MusicEnum;
import top.yumbo.util.music.musicImpl.netease.NeteaseCloudMusicInfo;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.io.SequenceInputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandProcessor {
    static boolean isLocked = false;
    static final NeteaseCloudMusicInfo musicInfo = new NeteaseCloudMusicInfo();
    static final Map<Long, JSONArray> userSongs = new HashMap<>();
    static {
        PropertiesUtil.load();
        MusicEnum.setBASE_URL_163Music(PropertiesUtil.properties.getProperty("baseUrl163Music"));
    }
    static void processSpkListCommand(GroupMessageEvent event, String message) {
        String regUserId = "speakerList";
        if (message.equals(regUserId)){
            MessageChainBuilder messageChain = new MessageChainBuilder().append("可选说话人:\n");
            for (int i = 0; i < PropertiesUtil.spkList.size(); i++){
                messageChain.append(PropertiesUtil.spkList.getJSONObject(i).getString("name"));
                if (!(i == PropertiesUtil.spkList.size()-1)){
                    messageChain.append(",");
                }
            }
            event.getSubject().sendMessage(messageChain.build());
        }
    }
    static void processCoverCommand(GroupMessageEvent event, String message) {
        String regUserId = "cover (\\d+) s (.*) t (\\d+)";
        Matcher matcher = Pattern.compile(regUserId).matcher(message);
        if (matcher.find()) {
            if (!isLocked){
                new Thread(()->{
                    try {
                        isLocked = true;
                        for (int i = 0; i < PropertiesUtil.spkList.size(); i++){
                            if(matcher.group(2).equals(PropertiesUtil.spkList.getJSONObject(i).getString("name"))){
                                event.getSubject().sendMessage(new MessageChainBuilder()
                                        .append("开始推理,稍等")
                                        .build());
                                int songId = Integer.parseInt(matcher.group(1))-1;
                                JSONObject songInfo = userSongs.get(event.getSender().getId()).getJSONObject(songId);
                                String maxBrLevel = songInfo.getJSONObject("privilege").getString("maxBrLevel");
                                System.out.println(maxBrLevel);
                                JSONObject songUrlParameter = new JSONObject();
                                songUrlParameter.put("id",songInfo.get("id"));
                                songUrlParameter.put("level",maxBrLevel);
                                setNeteaseCloudMusicCookie();
                                String songUrl = musicInfo.songUrl(songUrlParameter).getJSONArray("data").getJSONObject(0).getString("url");
                                String fileName = null;
                                try {
                                    fileName = FileUtil.downloadFile(songUrl, PropertiesUtil.properties.getProperty("uvr5Path")+"/audio");
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                                File instrumentFile = vocalAndBackgroundMusicSeparation(fileName);
                                File workingDirectory = new File(PropertiesUtil.properties.getProperty("soVitsSvc"));
                                PythonScript pythonScript = new PythonScript(
                                        workingDirectory,
                                        PropertiesUtil.properties.getProperty("pythonPath"),
                                        "inference_main.py"+ PropertiesUtil.spkList.getJSONObject(i).getString("parameter") +" -n \"test.wav\" -t " + matcher.group(3) + " --wav_format \"wav\"");
                                String directoryPath = PropertiesUtil.properties.getProperty("soVitsSvc")+"/results";
                                processResults(event, instrumentFile, directoryPath);
                            }
                        }
                    }finally {
                        isLocked = false;
                    }
                }).start();
            }else{
                event.getSubject().sendMessage(new MessageChainBuilder()
                        .append("已有推理任务在运行，等推理完再试")
                        .build());
            }
        }
    }

    public static File vocalAndBackgroundMusicSeparation(String fileName){
        File workingDirectory = new File(PropertiesUtil.properties.getProperty("uvr5Path"));
        PythonScript pythonScript = new PythonScript(
                workingDirectory,
                PropertiesUtil.properties.getProperty("pythonPath"),
                "infer_uvr5.py -device cuda -model_path uvr5_weights/HP5_only_main_vocal.pth -audio_path audio/"+fileName+" -save_path opt -is_half true -model_params 4band_v2 -format wav");
        String directoryPath = workingDirectory.getPath()+"/opt";
        File instrumentFile = FileUtil.findLatestFileStartingWith(directoryPath, "instrument_"+fileName);
        File vocalFile = FileUtil.findLatestFileStartingWith(directoryPath, "vocal_"+fileName);
        File wavPath = new File(PropertiesUtil.properties.getProperty("soVitsSvc")+"/raw/test.wav");
        try {
            Files.move(vocalFile.toPath(), wavPath.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return instrumentFile;
    }

    private static void processResults(GroupMessageEvent event, File instrumentFile, String directoryPath) {
        File directory = new File(directoryPath);
        File[] files = directory.listFiles();

        if (files != null && files.length > 0) {
            // Sort files by last modified time
            Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
            System.out.println(files[0].getPath());
            System.out.println(instrumentFile.getPath());
            File mixed = new File(SoVitsSvcCover.INSTANCE.getDataFolder().getPath() + "/mixed.wav");
            try {
                AudioMixer.mix(files[0],instrumentFile,mixed,1f,0.3f);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            sendAudioFile(event, mixed);
        } else {
            System.out.println("目录中没有文件。");
        }
    }

    private static void sendAudioFile(GroupMessageEvent event, File file) {
        try (ExternalResource externalResource = ExternalResource.create(file)) {
            OfflineAudio offlineAudio = event.getGroup().uploadAudio(externalResource);
            event.getGroup().sendMessage(offlineAudio);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    static void processSearchCommand(GroupMessageEvent event, String message) {
        String regUserId = "search (.*) page (\\d+)";
        Matcher matcher = Pattern.compile(regUserId).matcher(message);
        if (matcher.find()) {
            JSONObject searchKeywords = new JSONObject();
            searchKeywords.put("keywords", matcher.group(1));
            searchKeywords.put("limit", 8);

            // Parse the page number from the regex match
            int pageNumber = Integer.parseInt(matcher.group(2));
            int offset = (pageNumber - 1) * 8; // Assuming 8 results per page

            searchKeywords.put("offset", offset);
            final JSONObject searchResult = musicInfo.cloudsearch(searchKeywords);
            JSONArray songs = searchResult.getJSONObject("result").getJSONArray("songs");
            StringBuilder resultStringBuilder = new StringBuilder();
            resultStringBuilder.append("搜索结果:\n");
            userSongs.put(event.getSender().getId(),songs);
            for (int i = 0; i < songs.size(); i++) {
                resultStringBuilder.append(i+1);
                resultStringBuilder.append(". ");
                JSONObject songInfo = songs.getJSONObject(i);
                resultStringBuilder.append(songInfo.get("name"));
                resultStringBuilder.append(" - ");

                JSONArray artists = songInfo.getJSONArray("ar");
                for (int j = 0; j < artists.size(); j++) {
                    JSONObject artistInfo = artists.getJSONObject(j);
                    if (j != 0) {
                        resultStringBuilder.append(",");
                    }
                    resultStringBuilder.append(artistInfo.get("name"));
                }
                resultStringBuilder.append("\n");
            }
            resultStringBuilder.append("AI翻唱使用:cover [序号] s [说话人] t [音高]\n想要获取说话人发送:speakerList");
            event.getSubject().sendMessage(new MessageChainBuilder()
                    .append(resultStringBuilder.toString())
                    .build());
        }

    }


    //我用的这个sdk是真他妈的垃圾我日还不如我自己写一个
    //反正这是个测试顺便写的项目,就先这样吧后期也不打算维护了
    public static void setNeteaseCloudMusicCookie(){
        String neteaseCloudMusicCookie = String.valueOf(PropertiesUtil.properties.get("neteaseCloudMusicCookie"));
        musicInfo.setCookieString(NeteaseCloudMusicUtil.convertCookieString(neteaseCloudMusicCookie));
        int accountType = new JSONObject(musicInfo.loginStatus()).getJSONObject("data").getJSONObject("account").getInteger("type");
        if (accountType == 1000){
            JSONObject parameter = new JSONObject();
            parameter.put("phone",PropertiesUtil.properties.get("neteasePhone"));
            parameter.put("password",PropertiesUtil.properties.get("neteasePassword"));
            String cookie = musicInfo.loginCellphone(parameter).getString("cookie");
            PropertiesUtil.properties.put("neteaseCloudMusicCookie",cookie);
            PropertiesUtil.save();
            musicInfo.setCookieString(NeteaseCloudMusicUtil.convertCookieString(cookie));
        }
        System.out.println(musicInfo.loginStatus());
    }

}
