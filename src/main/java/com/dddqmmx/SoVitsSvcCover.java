package com.dddqmmx;

import com.dddqmmx.util.PropertiesUtil;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.EventChannel;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events.GroupMessageEvent;

public final class SoVitsSvcCover extends JavaPlugin {
    public static final SoVitsSvcCover INSTANCE=new SoVitsSvcCover();

    private SoVitsSvcCover(){
            super(new JvmPluginDescriptionBuilder("com.dddqmmx.cover","0.1.0")
                    .name("so-vits-svc-cover")
                    .info("这是一个使用so-vits-svc进行ai翻唱的插件")
                    .author("dddqmmx")
                    .build());
    }

    @Override
    public void onEnable(){
        getLogger().info("Plugin loaded!");
        EventChannel<Event> eventChannel = GlobalEventChannel.INSTANCE.parentScope(this);
        eventChannel.subscribeAlways(GroupMessageEvent.class, event -> {
            String message = event.getMessage().contentToString();
            CommandProcessor.processCoverCommand(event, message);
            CommandProcessor.processSearchCommand(event, message);
            CommandProcessor.processSpkListCommand(event, message);
        });
    }



}