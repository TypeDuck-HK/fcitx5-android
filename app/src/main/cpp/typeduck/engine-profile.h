/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2026 TypeDuck Contributors
 */
#pragma once

#include <fcitx/inputmethodmanager.h>
#include <utility>

namespace typeduck {

inline void configureInputMethods(fcitx::InputMethodManager &manager) {
    fcitx::InputMethodGroup group(manager.currentGroup().name());
    group.setDefaultLayout("us");
    group.inputMethodList().emplace_back("keyboard-us");
    group.inputMethodList().emplace_back("rime");
    group.setDefaultInputMethod("rime");
    manager.setGroup(std::move(group));
    manager.save();
}

} // namespace typeduck
