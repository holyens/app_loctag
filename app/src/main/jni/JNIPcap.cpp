//
// Created by shwei on 2020/11/22.
//

#include "top_tjunet_loctag_ui_capture_CaptureFragment.h"
#include <math.h>

JNIEXPORT jint JNICALL Java_top_tjunet_loctag_ui_capture_CaptureFragment_ndkDoSometing
  (JNIEnv *env, jclass jclass, jint x) {
    return (jint)(sin(x)*100);
  }