#ifndef RAWINPUT_RAW_MOTION_H
#define RAWINPUT_RAW_MOTION_H

#include <X11/extensions/XInput2.h>

static void rawinput_motion(const XIValuatorState *valuators, const double *values, double *motion) {
    for (int axis = 0; axis < valuators->mask_len * 8; axis++) {
        if (XIMaskIsSet(valuators->mask, axis)) {
            if (axis < 2) {
                motion[axis] += *values;
            }
            values++;
        }
    }
}

#endif
