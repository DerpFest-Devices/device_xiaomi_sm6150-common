#
# Copyright (C) 2018-2020 The LineageOS Project
#
# SPDX-License-Identifier: Apache-2.0
#

LOCAL_PATH := $(call my-dir)

ifneq ($(filter davinci phoenix violet,$(TARGET_DEVICE)),)
include $(call all-makefiles-under,$(LOCAL_PATH))

endif
