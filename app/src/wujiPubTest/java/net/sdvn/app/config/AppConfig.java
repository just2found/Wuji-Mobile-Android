package net.sdvn.app.config;

import net.linkmate.app.BuildConfig;

public interface AppConfig {

    //izzbie config
    String CONFIG_PARTID = "Y1DMATNYSMZPOKC3R8NJ";
    String CONFIG_APPID = "CN6SDL3H5K4UL55YP77L";
    int CONFIG_DEV_CLASS = 196870;
    //    String host = "app.memenet.net";
    String host = BuildConfig.host;
    //    String host_cn = "app.memenet.net";
    String host_cn = BuildConfig.host_cn;
    //    String host_us = "as.izzbie.com";
    String host_us = BuildConfig.host_us;

    String schema = "https";
    boolean DEBUG = BuildConfig.DEBUG;

    int port = 8445;

    //app.memenet.net ssl
    String ssl_app_file_name_1 = "Certum_Trusted_Network_CA.cer";
    String ssl_app_file_name_2 = "WoTrus_DV_SSL_CA.cer";
    //app.memenet.net ssl
    String ssl_home_file_name_1 = "Certum_Trusted_Network_CA.cer";
    String ssl_home_file_name_2 = "WoTrus_DV_SSL_CA.cer";
}