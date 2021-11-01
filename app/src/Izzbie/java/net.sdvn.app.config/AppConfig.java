package net.sdvn.app.config;


import net.linkmate.app.BuildConfig;

public interface AppConfig{
    //test访问地址
//    String host = "test.memenet.net";
    //LinkMate config
//    String CONFIG_PARTID = "SDVN0F9S2RFYTCYA5SP";
//    String CONFIG_APPID = "MEMENETJC5E8AA91D7SS";
//    int CONFIG_DEV_CLASS = 131329;
//    String host = "app.memenet.net";
    //izzbie config
    String CONFIG_PARTID = "Y1DMATNYSMZPOKC3R8NJ";
    String CONFIG_APPID = "CN6SDL3H5K4UL55YP77L";
    int CONFIG_DEV_CLASS = 196865;
    String host = "as.izzbie.com";

    String schema = "https";
    boolean DEBUG = BuildConfig.DEBUG;


    String host_cn = "app.memenet.net";
    String host_us = "as.izzbie.com";
    int port = 8445;

    //app.memenet.net ssl
    String ssl_app_file_name_1 = "Certum_Trusted_Network_CA.cer";
    String ssl_app_file_name_2 = "WoTrus_DV_SSL_CA.cer";
    //app.memenet.net ssl
    String ssl_home_file_name_1 = "Certum_Trusted_Network_CA.cer";
    String ssl_home_file_name_2 = "WoTrus_DV_SSL_CA.cer";
}