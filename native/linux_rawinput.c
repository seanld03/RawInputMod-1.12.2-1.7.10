#include <jni.h>
#include <jawt.h>
#include <dlfcn.h>
#include <stdint.h>
#include <stdlib.h>
#include <X11/Xlib.h>
#include <X11/extensions/XInput2.h>
#include "raw_motion.h"

typedef struct {
    Display *display;
    int opcode;
    JAWT awt;
    void *awt_library;
} RawInput;

static void fail(JNIEnv *env, const char *message) {
    if ((*env)->ExceptionCheck(env)) {
        return;
    }
    jclass exception = (*env)->FindClass(env, "java/io/IOException");
    if (exception != NULL) {
        (*env)->ThrowNew(env, exception, message);
    }
}

static void unlock_awt(JNIEnv *env, JAWT *awt) {
    jthrowable pending = (*env)->ExceptionOccurred(env);
    if (pending != NULL) {
        (*env)->ExceptionClear(env);
    }
    awt->Unlock(env);
    jboolean failed = (*env)->ExceptionCheck(env);
    if (pending != NULL) {
        if (failed) {
            (*env)->ExceptionClear(env);
        }
        (*env)->Throw(env, pending);
        (*env)->DeleteLocalRef(env, pending);
    }
}

static jlong open_display(JNIEnv *env, JAWT awt) {
    Display *display = XOpenDisplay(NULL);
    if (display == NULL) {
        fail(env, "Unable to open the X11 display");
        return 0;
    }
    int opcode;
    int event;
    int error;
    int major = 2;
    int minor = 1;
    if (!XQueryExtension(display, "XInputExtension", &opcode, &event, &error)
            || XIQueryVersion(display, &major, &minor) != Success
            || major < 2 || (major == 2 && minor < 1)) {
        XCloseDisplay(display);
        fail(env, "XInput 2.1 is not available");
        return 0;
    }
    unsigned char bits[XIMaskLen(XI_RawMotion)] = {0};
    XISetMask(bits, XI_RawMotion);
    XIEventMask mask = {XIAllMasterDevices, sizeof(bits), bits};
    for (int screen = 0; screen < ScreenCount(display); screen++) {
        if (XISelectEvents(display, RootWindow(display, screen), &mask, 1) != Success) {
            XCloseDisplay(display);
            fail(env, "Unable to select raw mouse events");
            return 0;
        }
    }
    XSync(display, False);
    RawInput *input = calloc(1, sizeof(*input));
    if (input == NULL) {
        XCloseDisplay(display);
        fail(env, "Unable to allocate raw mouse input");
        return 0;
    }
    input->display = display;
    input->opcode = opcode;
    input->awt = awt;
    return (jlong) (intptr_t) input;
}

JNIEXPORT jlong JNICALL Java_mod_seanld_rawinput_input_LinuxMouseBackend_open(JNIEnv *env, jclass type, jstring awt_path) {
    (void) type;
    if (awt_path == NULL) {
        fail(env, "Invalid AWT input library path");
        return 0;
    }
    const char *path = (*env)->GetStringUTFChars(env, awt_path, NULL);
    if (path == NULL) {
        return 0;
    }
    void *awt_library = dlopen(path, RTLD_NOW | RTLD_LOCAL);
    (*env)->ReleaseStringUTFChars(env, awt_path, path);
    if (awt_library == NULL) {
        fail(env, "Unable to open the AWT input library");
        return 0;
    }
    typedef jboolean (JNICALL *GetAWT)(JNIEnv *, JAWT *);
    GetAWT get_awt = (GetAWT) dlsym(awt_library, "JAWT_GetAWT");
    if (get_awt == NULL) {
        dlclose(awt_library);
        fail(env, "Unable to find AWT input locking");
        return 0;
    }
    JAWT awt = {0};
    awt.version = JAWT_VERSION_1_4;
    jboolean available = get_awt(env, &awt);
    if ((*env)->ExceptionCheck(env)) {
        dlclose(awt_library);
        return 0;
    }
    if (available != JNI_TRUE || awt.Lock == NULL || awt.Unlock == NULL) {
        dlclose(awt_library);
        fail(env, "Unable to lock X11 input");
        return 0;
    }
    awt.Lock(env);
    if ((*env)->ExceptionCheck(env)) {
        dlclose(awt_library);
        return 0;
    }
    jlong handle = open_display(env, awt);
    unlock_awt(env, &awt);
    if (handle == 0) {
        dlclose(awt_library);
    } else {
        ((RawInput *) (intptr_t) handle)->awt_library = awt_library;
    }
    return handle;
}

JNIEXPORT void JNICALL Java_mod_seanld_rawinput_input_LinuxMouseBackend_poll(JNIEnv *env, jclass type, jlong handle, jdoubleArray result) {
    (void) type;
    RawInput *input = (RawInput *) (intptr_t) handle;
    if (input == NULL || result == NULL || (*env)->GetArrayLength(env, result) < 2) {
        fail(env, "Invalid raw mouse input state");
        return;
    }
    input->awt.Lock(env);
    if ((*env)->ExceptionCheck(env)) {
        return;
    }
    double motion[2] = {0, 0};
    int count = 0;
    while (XPending(input->display) && count++ < 4096) {
        XEvent event;
        XNextEvent(input->display, &event);
        if (event.type == GenericEvent && event.xcookie.extension == input->opcode
                && XGetEventData(input->display, &event.xcookie)) {
            if (event.xcookie.evtype == XI_RawMotion) {
                XIRawEvent *raw = event.xcookie.data;
                rawinput_motion(&raw->valuators, raw->raw_values, motion);
            }
            XFreeEventData(input->display, &event.xcookie);
        }
    }
    (*env)->SetDoubleArrayRegion(env, result, 0, 2, motion);
    unlock_awt(env, &input->awt);
}

JNIEXPORT void JNICALL Java_mod_seanld_rawinput_input_LinuxMouseBackend_close(JNIEnv *env, jclass type, jlong handle) {
    (void) type;
    RawInput *input = (RawInput *) (intptr_t) handle;
    if (input != NULL) {
        input->awt.Lock(env);
        if ((*env)->ExceptionCheck(env)) {
            return;
        }
        XCloseDisplay(input->display);
        unlock_awt(env, &input->awt);
        dlclose(input->awt_library);
        free(input);
    }
}
