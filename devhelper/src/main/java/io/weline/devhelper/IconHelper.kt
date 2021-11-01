package io.weline.devhelper

import net.sdvn.cmapi.global.Constants
import net.sdvn.cmapi.util.CommonUtils

object IconHelper {
    @JvmStatic
    fun getIconByeDevClass(devClass: Int, isOnline: Boolean = true, isHigh: Boolean = true): Int {
        val manufacturer = CommonUtils.getManufacturer(devClass)
        val dt: Int = CommonUtils.getDeviceType(devClass)
        val dst: Int = CommonUtils.getDeviceSubType(devClass)
        return if (DevTypeHelper.isNas(devClass)) {
            return when (dt) {
                Constants.DT_ANDROID -> when (manufacturer) {
                    Manufacturer.BeeLink -> R.drawable.icon_device_gs
                    Manufacturer.E -> when (dst) {
                        DevSubType.E_A3 -> R.drawable.icon_device_a3
                        DevSubType.E_A5,
                        DevSubType.E_A5II -> R.drawable.icon_device_a5
                        DevSubType.E_A6 -> R.drawable.icon_device_a6
                        DevSubType.E_A7,
                        DevSubType.E_A7Pro -> R.drawable.icon_device_a7
                        DevSubType.E_A8 -> R.drawable.icon_device_a8
                        DevSubType.E_A9,
                        DevSubType.E_A9Pro,
                        DevSubType.E_A9II -> R.drawable.icon_device_a9
                        DevSubType.E_A10,
                        DevSubType.E_A10Pro -> R.drawable.icon_device_a10
                        DevSubType.E_A11 -> R.drawable.icon_device_a11
                        DevSubType.E_A13 -> R.drawable.icon_device_a13
                        DevSubType.E_A15 -> R.drawable.icon_device_a15
                        DevSubType.E_A95 -> R.drawable.icon_device_c100
                        else -> R.drawable.icon_device_wz
                    }
                    else -> R.drawable.icon_devices_android_tv
                }
                Constants.DT_NAS ->
                    when (manufacturer) {
                        Manufacturer.E ->
                            R.drawable.icon_device_x6
                        Manufacturer.Izzbie,
                        Manufacturer.Cifernet,
                        Manufacturer.Memenet ->
                            R.drawable.icon_device_m3
                        else -> R.drawable.icon_device_wz
                    }

                Constants.DT_OpenWRT ->
                    when (manufacturer) {
                        Manufacturer.Zdzkj -> {
                            R.drawable.icon_device_e1
                        }
                        Manufacturer.Memenet -> when (dst) {
                            DevSubType.M7X3 -> {
                                R.drawable.icon_device_m3
                            }
                            DevSubType.M8X4 -> {
                                R.drawable.icon_device_m8
                            }
                            DevSubType.M8X5 -> {
                                R.drawable.icon_device_m8
                            }
                            DevSubType.M8X2 -> {
                                R.drawable.icon_device_e1
                            }
                            else -> R.drawable.icon_device_wz
                        }
                        else -> R.drawable.icon_device_wz
                    }

                else -> R.drawable.icon_device_wz
            }
        } else {
            if (manufacturer == 0) {
                //osType = devClass
                when (devClass) {
                    Constants.OT_OSX,
                    Constants.OT_MINIPC -> R.drawable.icon_devices_macpc
                    Constants.OT_LINUX -> R.drawable.icon_devices_linuxpc
                    Constants.OT_WINDOWS -> R.drawable.icon_devices_winpc
                    Constants.OT_ANDROID -> R.drawable.icon_devices_androidphone
                    Constants.OT_IOS -> R.drawable.icon_devices_ios
                    Constants.OT_AMBARELLA_CAMERA -> R.drawable.icon_camera
                    Constants.OT_BS_WIFI -> R.drawable.icon_router
                    Constants.OT_NANOPI_M1,
                    Constants.OT_NANOPI_M2,
                    Constants.OT_NANOPI_NEO -> R.drawable.icon_node
                    Constants.OT_M1_STATION,
                    Constants.OT_ONESPACE -> R.drawable.icon_device_m3
                    else -> R.drawable.icon_device_wz
                }
            } else {
                when (dt) {
                    Constants.DT_MACOS -> R.drawable.icon_devices_macpc
                    Constants.DT_LINUX -> R.drawable.icon_devices_linuxpc
                    Constants.DT_WINDOWS -> R.drawable.icon_devices_winpc
                    Constants.DT_ANDROID -> R.drawable.icon_devices_androidphone
                    Constants.DT_IOS -> R.drawable.icon_devices_ios
                    Constants.DT_ROUTER -> R.drawable.icon_router
                    Constants.DT_NAS -> R.drawable.icon_device_m3
                    Constants.DT_CAMERA -> R.drawable.icon_camera
                    Constants.DT_SN -> R.drawable.icon_node
                    Constants.DT_OpenWRT -> R.drawable.icon_openwrt
                    else -> R.drawable.icon_device_wz
                }
            }
        }
    }

   fun getIcByeDevClassSimple(devClass: Int):Int {
       val manufacturer = CommonUtils.getManufacturer(devClass)
       val dt: Int = CommonUtils.getDeviceType(devClass)
       val dst: Int = CommonUtils.getDeviceSubType(devClass)
    //   return if (DevTypeHelper.isNas(devClass)) {
           return when (dt) {
               Constants.DT_ANDROID -> when (manufacturer) {
                   Manufacturer.BeeLink -> R.drawable.ic_original_gs_king_x
                   Manufacturer.E -> when (dst) {
                       DevSubType.E_A15 -> R.drawable.ic_original_a15
                       else -> R.drawable.icon_device_unknown_simple
                   }
                   else -> R.drawable.icon_device_tv_simple
               }
               Constants.DT_NAS ->
                   when (manufacturer) {
                       Manufacturer.E ->
                           R.drawable.icon_device_x6_simple
                       Manufacturer.Izzbie,
                       Manufacturer.Cifernet,
                       Manufacturer.Memenet ->
                           R.drawable.ic_original_m3
                       else -> R.drawable.icon_device_unknown_simple
                   }

               Constants.DT_OpenWRT ->
                   when (manufacturer) {
                       Manufacturer.Zdzkj -> {
                           R.drawable.ic_original_m8x2
                       }
                       Manufacturer.Memenet -> when (dst) {
//                           DevSubType.M7X3 -> {
//                               R.drawable.icon_device_m3_simple
//                           }
                           DevSubType.M8X2 -> {
                               R.drawable.ic_original_m8x2
                           }
                           DevSubType.M8X4 -> {
                               R.drawable.ic_original_m8x4
                           }
                           DevSubType.M8X5->{
                               R.drawable.ic_original_t2_pro
                           }
                           else -> R.drawable.icon_device_unknown_simple
                       }
                       else -> R.drawable.icon_device_unknown_simple
                   }

               else -> R.drawable.icon_device_unknown_simple
           }
       }
//       else {
//           if (manufacturer == 0) {
//               //osType = devClass
//               when (devClass) {
//                   Constants.OT_OSX,
//                   Constants.OT_MINIPC -> R.drawable.icon_device_mac_simple
//                   Constants.OT_LINUX -> R.drawable.icon_device_linux_simple
//                   Constants.OT_WINDOWS -> R.drawable.icon_device_win_simple
//                   Constants.OT_ANDROID -> R.drawable.icon_device_andriod_simple
//                   Constants.OT_IOS -> R.drawable.icon_device_ios_simple
//                   Constants.OT_AMBARELLA_CAMERA -> R.drawable.icon_device_camera_simple
//                   Constants.OT_BS_WIFI -> R.drawable.icon_device_router_simple
//                   Constants.OT_NANOPI_M1,
//                   Constants.OT_NANOPI_M2,
//                   Constants.OT_NANOPI_NEO -> R.drawable.icon_device_nodes_simple
//                   Constants.OT_M1_STATION,
//                   Constants.OT_ONESPACE -> R.drawable.icon_device_m3_simple
//                   else -> R.drawable.icon_device_unknown_simple
//               }
//           } else {
//               when (dt) {
//                   Constants.DT_MACOS -> R.drawable.icon_device_mac_simple
//                   Constants.DT_LINUX -> R.drawable.icon_device_linux_simple
//                   Constants.DT_WINDOWS -> R.drawable.icon_device_win_simple
//                   Constants.DT_ANDROID -> R.drawable.icon_device_andriod_simple
//                   Constants.DT_IOS -> R.drawable.icon_device_ios_simple
//                   Constants.DT_ROUTER -> R.drawable.icon_device_router_simple
//                   Constants.DT_NAS -> R.drawable.icon_device_m3_simple
//                   Constants.DT_CAMERA -> R.drawable.icon_device_camera_simple
//                   Constants.DT_SN -> R.drawable.icon_device_nodes_simple
//                   Constants.DT_OpenWRT -> R.drawable.icon_device_openwrt_simple
//                   else -> R.drawable.icon_device_unknown_simple
//               }
//           }
//       }




    @JvmStatic
    fun getIconByeDevClassSimple(devClass: Int, isOnline: Boolean = true, isHigh: Boolean = true): Int {
        val manufacturer = CommonUtils.getManufacturer(devClass)
        val dt: Int = CommonUtils.getDeviceType(devClass)
        val dst: Int = CommonUtils.getDeviceSubType(devClass)
        return if (DevTypeHelper.isNas(devClass)) {
            return when (dt) {
                Constants.DT_ANDROID -> when (manufacturer) {
                    Manufacturer.BeeLink -> R.drawable.icon_device_gs_simple
                    Manufacturer.E -> when (dst) {
                        DevSubType.E_A3 -> R.drawable.icon_device_a3_simple
                        DevSubType.E_A5,
                        DevSubType.E_A5II -> R.drawable.icon_device_a5_simple
                        DevSubType.E_A6 -> R.drawable.icon_device_a6_simple
                        DevSubType.E_A7,
                        DevSubType.E_A7Pro -> R.drawable.icon_device_a7_simple
                        DevSubType.E_A8 -> R.drawable.icon_device_a8_simple
                        DevSubType.E_A9,
                        DevSubType.E_A9Pro,
                        DevSubType.E_A9II -> R.drawable.icon_device_a9_simple
                        DevSubType.E_A10,
                        DevSubType.E_A10Pro -> R.drawable.icon_device_a10_simple
                        DevSubType.E_A11 -> R.drawable.icon_device_a11_simple
                        DevSubType.E_A13 -> R.drawable.icon_device_a13_simple
                        DevSubType.E_A15 -> R.drawable.icon_device_a15_simple
                        DevSubType.E_A95 -> R.drawable.icon_device_c100_simple
                        else -> R.drawable.icon_device_unknown_simple
                    }
                    else -> R.drawable.icon_device_tv_simple
                }
                Constants.DT_NAS ->
                    when (manufacturer) {
                        Manufacturer.E ->
                            R.drawable.icon_device_x6_simple
                        Manufacturer.Izzbie,
                        Manufacturer.Cifernet,
                        Manufacturer.Memenet ->
                            R.drawable.icon_device_m3_simple
                        else -> R.drawable.icon_device_unknown_simple
                    }

                Constants.DT_OpenWRT ->
                    when (manufacturer) {
                        Manufacturer.Zdzkj -> {
                            R.drawable.icon_device_e1_simple
                        }
                        Manufacturer.Memenet -> when (dst) {
                            DevSubType.M7X3 -> {
                                R.drawable.icon_device_m3_simple
                            }

                            DevSubType.M8X2 -> {
                                R.drawable.icon_device_e1_simple
                            }

                            DevSubType.M8X4 -> {
                                R.drawable.icon_device_m8_simple
                            }
                            DevSubType.M8X5 -> {
                                R.drawable.icon_device_m8_simple
                            }
                            else -> R.drawable.icon_device_unknown_simple
                        }
                        else -> R.drawable.icon_device_unknown_simple
                    }

                else -> R.drawable.icon_device_unknown_simple
            }
        } else {
            if (manufacturer == 0) {
                //osType = devClass
                when (devClass) {
                    Constants.OT_OSX,
                    Constants.OT_MINIPC -> R.drawable.icon_device_mac_simple
                    Constants.OT_LINUX -> R.drawable.icon_device_linux_simple
                    Constants.OT_WINDOWS -> R.drawable.icon_device_win_simple
                    Constants.OT_ANDROID -> R.drawable.icon_device_andriod_simple
                    Constants.OT_IOS -> R.drawable.icon_device_ios_simple
                    Constants.OT_AMBARELLA_CAMERA -> R.drawable.icon_device_camera_simple
                    Constants.OT_BS_WIFI -> R.drawable.icon_device_router_simple
                    Constants.OT_NANOPI_M1,
                    Constants.OT_NANOPI_M2,
                    Constants.OT_NANOPI_NEO -> R.drawable.icon_device_nodes_simple
                    Constants.OT_M1_STATION,
                    Constants.OT_ONESPACE -> R.drawable.icon_device_m3_simple
                    else -> R.drawable.icon_device_unknown_simple
                }
            } else {
                when (dt) {
                    Constants.DT_MACOS -> R.drawable.icon_device_mac_simple
                    Constants.DT_LINUX -> R.drawable.icon_device_linux_simple
                    Constants.DT_WINDOWS -> R.drawable.icon_device_win_simple
                    Constants.DT_ANDROID -> R.drawable.icon_device_andriod_simple
                    Constants.DT_IOS -> R.drawable.icon_device_ios_simple
                    Constants.DT_ROUTER -> R.drawable.icon_device_router_simple
                    Constants.DT_NAS -> R.drawable.icon_device_m3_simple
                    Constants.DT_CAMERA -> R.drawable.icon_device_camera_simple
                    Constants.DT_SN -> R.drawable.icon_device_nodes_simple
                    Constants.DT_OpenWRT -> R.drawable.icon_device_openwrt_simple
                    else -> R.drawable.icon_device_unknown_simple
                }
            }
        }
    }


}