package com.kmaebashi.nctfwimpl;

import com.kmaebashi.nctfw.InvokerOption;

class Util {
    static boolean containsOption(InvokerOption[] options, InvokerOption target) {
        for (InvokerOption opt : options) {
            if (opt == target) {
                return true;
            }
        }
        return false;
    }
}
