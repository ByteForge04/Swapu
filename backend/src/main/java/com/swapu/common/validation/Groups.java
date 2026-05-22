package com.swapu.common.validation;

import jakarta.validation.groups.Default;

public interface Groups {
    interface Create extends Default {}
    interface Update extends Default {}
}
