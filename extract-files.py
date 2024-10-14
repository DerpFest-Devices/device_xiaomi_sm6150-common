#!/usr/bin/env -S PYTHONPATH=../../../tools/extract-utils python3
#
# SPDX-FileCopyrightText: 2024 The LineageOS Project
# SPDX-License-Identifier: Apache-2.0
#

from extract_utils.file import File
from extract_utils.fixups_blob import (
    BlobFixupCtx,
    blob_fixup,
    blob_fixups_user_type,
)
from extract_utils.fixups_lib import (
    lib_fixup_remove,
    lib_fixups,
    lib_fixups_user_type,
)
from extract_utils.main import (
    ExtractUtils,
    ExtractUtilsModule,
)
from extract_utils.utils import run_cmd

namespace_imports = [
    'device/xiaomi/sm6150-common',
    'hardware/qcom-caf/sm8150',
    'hardware/qcom-caf/wlan',
    'hardware/xiaomi',
    'vendor/qcom/opensource/commonsys/display',
    'vendor/qcom/opensource/commonsys-intf/display',
    'vendor/qcom/opensource/dataservices',
    'vendor/qcom/opensource/display',
]

blob_fixups: blob_fixups_user_type = {
    ('vendor/lib64/mediadrm/libwvdrmengine.so', 'vendor/lib64/libwvhidl.so'): blob_fixup()
        .add_needed('libcrypto_shim.so'),
    ('vendor/bin/mlipayd@1.1', 'vendor/lib64/libmlipay.so', 'vendor/lib64/libmlipay@1.1.so'): blob_fixup()
        .remove_needed('vendor.xiaomi.hardware.mtdservice@1.0.so'),
    'vendor/etc/camera/camxoverridesettings.txt': blob_fixup()
        .regex_replace(r'0x10080', '0')
        .regex_replace(r'0x1F', '0x0'),
    'vendor/etc/init/vendor.sensors.qti.rc': blob_fixup()
        .add_line_if_missing('    disabled'),
    'vendor/etc/init/android.hardware.drm@1.3-service.widevine.rc': blob_fixup()
        .regex_replace(r'writepid /dev/cpuset/foreground/tasks', 'task_profiles ProcessCapacityHigh'),
    'vendor/lib64/camera/components/com.qti.node.watermark.so': blob_fixup()
        .add_needed('libpiex_shim.so'),
    (
        'vendor/lib/libstagefright_soft_ddpdec.so',
        'vendor/lib/libstagefright_soft_ac4dec.so',
        'vendor/lib/libstagefrightdolby.so',
        'vendor/lib64/libstagefright_soft_ddpdec.so',
        'vendor/lib64/libdlbdsservice.so',
        'vendor/lib64/libstagefright_soft_ac4dec.so',
        'vendor/lib64/libstagefrightdolby.so',
    ): blob_fixup()
        .replace_needed('libstagefright_foundation.so', 'libstagefright_foundation-v33.so'),
}

module = ExtractUtilsModule(
    'sm6150-common',
    'xiaomi',
    blob_fixups=blob_fixups,
    lib_fixups=lib_fixups,
    namespace_imports=namespace_imports,
)

if __name__ == '__main__':
    utils = ExtractUtils.device(module)
    utils.run()
