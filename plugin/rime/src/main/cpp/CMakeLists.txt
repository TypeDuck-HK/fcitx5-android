cmake_minimum_required(VERSION 3.18)

project(fcitx5-android-plugin-rime VERSION ${VERSION_NAME})

# For reproducible build
add_link_options("LINKER:--hash-style=gnu,--build-id=none")

# prefab dependency
find_package(fcitx5 REQUIRED CONFIG)
get_target_property(FCITX5_CMAKE_MODULES fcitx5::cmake INTERFACE_INCLUDE_DIRECTORIES)
set(CMAKE_MODULE_PATH ${FCITX5_CMAKE_MODULES} ${CMAKE_MODULE_PATH})

find_package(ECM MODULE)
find_package(Fcitx5Core MODULE)
find_package(Fcitx5Module MODULE)

set(Rime_VERSION "1.12.0")
set(Rime_dependencies
        log # glog links to android log
        "${PREBUILT_DIR}/glog/${ANDROID_ABI}/lib/libglog.a"
        "${PREBUILT_DIR}/leveldb/${ANDROID_ABI}/lib/libleveldb.a"
        "${PREBUILT_DIR}/lua/${ANDROID_ABI}/lib/liblua_static.a"
        "${PREBUILT_DIR}/marisa/${ANDROID_ABI}/lib/libmarisa.a"
        "${PREBUILT_DIR}/opencc/${ANDROID_ABI}/lib/libopencc.a"
        "${PREBUILT_DIR}/yaml-cpp/${ANDROID_ABI}/lib/libyaml-cpp.a"
        )

add_library(Rime_static STATIC IMPORTED)
set_target_properties(Rime_static PROPERTIES
        IMPORTED_LOCATION "${PREBUILT_DIR}/librime/${ANDROID_ABI}/lib/librime.a"
        INTERFACE_INCLUDE_DIRECTORIES "${PREBUILT_DIR}/librime/${ANDROID_ABI}/include"
        INTERFACE_LINK_LIBRARIES "${Rime_dependencies}"
        )

# https://github.com/rime/librime/blob/1.9.0/src/rime_api.h#L663
# https://stackoverflow.com/questions/805555/ld-linker-question-the-whole-archive-option
# use $<LINK_LIBRARY:WHOLE_ARCHIVE,Rime_static> when cmake updated to 3.24+
# https://cmake.org/cmake/help/latest/manual/cmake-generator-expressions.7.html#link-features
set(RIME_TARGET "-Wl,--whole-archive" Rime_static "-Wl,--no-whole-archive")
set(RIME_DATA_DIR "${CMAKE_INSTALL_DATADIR}/rime-data")
add_subdirectory(fcitx5-rime)

install(FILES
    typeduck-data/cangjie3.dict.yaml
    typeduck-data/cangjie3.schema.yaml
    typeduck-data/cangjie5.dict.yaml
    typeduck-data/cangjie5.schema.yaml
    typeduck-data/common.custom.yaml
    typeduck-data/common.yaml
    typeduck-data/default.custom.yaml
    typeduck-data/default.yaml
    typeduck-data/include.yaml
    typeduck-data/jyut6ping3.dict.yaml
    typeduck-data/jyut6ping3.schema.yaml
    typeduck-data/jyut6ping3_mobile.schema.yaml
    typeduck-data/jyut6ping3_mobile_10keys.schema.yaml
    typeduck-data/jyut6ping3_mobile_longpress.schema.yaml
    typeduck-data/jyut6ping3_scolar.dict.yaml
    typeduck-data/jyut6ping3_scolar.schema.yaml
    typeduck-data/loengfan.dict.yaml
    typeduck-data/loengfan.schema.yaml
    typeduck-data/loengfan_longpress.schema.yaml
    typeduck-data/luna_pinyin.dict.yaml
    typeduck-data/luna_pinyin.schema.yaml
    typeduck-data/template.yaml
    typeduck-data/trime.yaml
    typeduck-data/weasel.yaml
    DESTINATION "${FCITX_INSTALL_DATADIR}/rime-data"
    COMPONENT prebuilt-assets
)
install(DIRECTORY "typeduck-data/opencc/"
        DESTINATION "${FCITX_INSTALL_DATADIR}/rime-data/opencc"
        COMPONENT prebuilt-assets)