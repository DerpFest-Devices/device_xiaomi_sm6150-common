LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

# files that live under device/qcom/common/rootdir/etc/

include $(CLEAR_VARS)
LOCAL_MODULE       := fstab.default
LOCAL_MODULE_TAGS  := optional
LOCAL_MODULE_CLASS := ETC
ifeq ($(PRODUCT_USE_DYNAMIC_PARTITIONS), true)
LOCAL_SRC_FILES    := etc/default/fstab_non_AB_dynamic_partition.qti
else
LOCAL_SRC_FILES    := etc/default/fstab.qcom
endif
LOCAL_MODULE_PATH  := $(TARGET_OUT_VENDOR_ETC)
include $(BUILD_PREBUILT)

include $(CLEAR_VARS)
LOCAL_MODULE       := fstab.emmc
LOCAL_MODULE_TAGS  := optional
LOCAL_MODULE_CLASS := ETC
LOCAL_SRC_FILES    := etc/emmc/fstab.qcom
LOCAL_MODULE_PATH  := $(TARGET_OUT_VENDOR_ETC)
include $(BUILD_PREBUILT)

include $(CLEAR_VARS)
LOCAL_MODULE       := fstab.ramdisk
LOCAL_MODULE_STEM  := fstab.default
LOCAL_MODULE_TAGS  := optional
LOCAL_MODULE_CLASS := ETC
LOCAL_SRC_FILES    := etc/default/fstab_non_AB_dynamic_partition.qti
LOCAL_MODULE_PATH  := $(TARGET_RAMDISK_OUT)
include $(BUILD_PREBUILT)
