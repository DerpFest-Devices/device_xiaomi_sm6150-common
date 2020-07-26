# Copyright (C) 2009 The Android Open Source Project
# Copyright (c) 2011, The Linux Foundation. All rights reserved.
# Copyright (C) 2017-2020 The LineageOS Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import common
import re

def FullOTA_InstallEnd(info):
  input_zip = info.input_zip
  OTA_UpdateFirmware(info)
  OTA_InstallEnd(info, input_zip)
  return

def IncrementalOTA_InstallEnd(info):
  input_zip = info.target_zip
  OTA_UpdateFirmware(info)
  OTA_InstallEnd(info, input_zip)
  return

def OTA_UpdateFirmware(info):
  info.script.AppendExtra('ui_print("Flashing firmware images");')
  info.script.AppendExtra('package_extract_file("install/firmware-update/abl.elf", "/dev/block/bootdevice/by-name/abl.elf");')
  info.script.AppendExtra('package_extract_file("install/firmware-update/aop.mbn", "/dev/block/bootdevice/by-name/aop.mbn");')
  info.script.AppendExtra('package_extract_file("install/firmware-update/BTFM.bin", "/dev/block/bootdevice/by-name/BTFM.bin");')
  info.script.AppendExtra('package_extract_file("install/firmware-update/cmnlib64.mbn", "/dev/block/bootdevice/by-name/cmnlib64.mbn");')
  info.script.AppendExtra('package_extract_file("install/firmware-update/cmnlib.mbn", "/dev/block/bootdevice/by-name/cmnlib.mbn");')
  info.script.AppendExtra('package_extract_file("install/firmware-update/devcfg.mbn", "/dev/block/bootdevice/by-name/devcfg.mbn");')
  info.script.AppendExtra('package_extract_file("install/firmware-update/dspso.bin", "/dev/block/bootdevice/by-name/dspso.bin");')
  info.script.AppendExtra('package_extract_file("install/firmware-update/firmware.mk", "/dev/block/bootdevice/by-name/firmware.mk");')
  info.script.AppendExtra('package_extract_file("install/firmware-update/hyp.mbn", "/dev/block/bootdevice/by-name/hyp.mbn");')
  info.script.AppendExtra('package_extract_file("install/firmware-update/imagefv.elf", "/dev/block/bootdevice/by-name/imagefv.elf");')
  info.script.AppendExtra('package_extract_file("install/firmware-update/km4.mbn", "/dev/block/bootdevice/by-name/km4.mbn");')
  info.script.AppendExtra('package_extract_file("install/firmware-update/logo.img", "/dev/block/bootdevice/by-name/logo.img");')
  info.script.AppendExtra('package_extract_file("install/firmware-update/multi_image.mbn", "/dev/block/bootdevice/by-name/multi_image.mbn");')
  info.script.AppendExtra('package_extract_file("install/firmware-update/NON-HLOS.bin", "/dev/block/bootdevice/by-name/NON-HLOS.bin");')
  info.script.AppendExtra('package_extract_file("install/firmware-update/qupv3fw.elf", "/dev/block/bootdevice/by-name/qupv3fw.elf");')
  info.script.AppendExtra('package_extract_file("install/firmware-update/storsec.mbn", "/dev/block/bootdevice/by-name/storsec.mbn");')
  info.script.AppendExtra('package_extract_file("install/firmware-update/tz.mbn", "/dev/block/bootdevice/by-name/tz.mbn");')
  info.script.AppendExtra('package_extract_file("install/firmware-update/uefi_sec.mbn", "/dev/block/bootdevice/by-name/uefi_sec.mbn");')
  info.script.AppendExtra('package_extract_file("install/firmware-update/xbl_config.elf", "/dev/block/bootdevice/by-name/xbl_config.elf");')
  info.script.AppendExtra('package_extract_file("install/firmware-update/xbl.elf", "/dev/block/bootdevice/by-name/xbl.elf");')

def AddImage(info, input_zip, basename, dest):
  name = basename
  data = input_zip.read("IMAGES/" + basename)
  common.ZipWriteStr(info.output_zip, name, data)
  info.script.Print("Patching {} image unconditionally...".format(dest.split('/')[-1]))
  info.script.AppendExtra('package_extract_file("%s", "%s");' % (name, dest))

def OTA_InstallEnd(info, input_zip):
  AddImage(info, input_zip, "vbmeta.img", "/dev/block/bootdevice/by-name/vbmeta")
  AddImage(info, input_zip, "vbmeta_system.img", "/dev/block/bootdevice/by-name/vbmeta_system")
  AddImage(info, input_zip, "dtbo.img", "/dev/block/bootdevice/by-name/dtbo")
  return
