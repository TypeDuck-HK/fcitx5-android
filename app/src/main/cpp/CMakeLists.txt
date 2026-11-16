cmake_minimum_required(VERSION 3.18)

project(fcitx5-android VERSION ${VERSION_NAME})

# For reproducible build
add_link_options("LINKER:--hash-style=gnu,--build-id=none")

# prefab dependency
find_package(fcitx5 REQUIRED CONFIG)
get_target_property(FCITX5_CMAKE_MODULES fcitx5::cmake INTERFACE_INCLUDE_DIRECTORIES)
set(CMAKE_MODULE_PATH ${FCITX5_CMAKE_MODULES} ${CMAKE_MODULE_PATH})

find_package(Fcitx5Core MODULE)
find_package(Fcitx5Module MODULE)

find_package(rime REQUIRED CONFIG)

include("${FCITX_INSTALL_CMAKECONFIG_DIR}/Fcitx5Utils/Fcitx5CompilerSettings.cmake")

add_subdirectory(po)
add_subdirectory(androidfrontend)
add_subdirectory(androidkeyboard)
add_subdirectory(androidnotification)

# prebuilt libuv
set(libuv_DIR "${PREBUILT_DIR}/libuv/${ANDROID_ABI}/lib/cmake/libuv")
find_package(libuv)

add_library(native-lib SHARED native-lib.cpp androidaddonloader/androidaddonloader.cpp)
target_link_libraries(native-lib
        log
        libuv::uv_a
        Fcitx5::Utils
        Fcitx5::Config
        Fcitx5::Core
        Fcitx5::Module::QuickPhrase
        Fcitx5::Module::Unicode
        Fcitx5::Module::Clipboard
        )

# copy module libraries from dependency lib
add_custom_target(copy-fcitx5-modules
        COMMAND ${CMAKE_COMMAND} -E copy_if_different
        $<TARGET_PROPERTY:fcitx5::clipboard,IMPORTED_LOCATION>
        $<TARGET_PROPERTY:fcitx5::imselector,IMPORTED_LOCATION>
        $<TARGET_PROPERTY:fcitx5::quickphrase,IMPORTED_LOCATION>
        $<TARGET_PROPERTY:fcitx5::spell,IMPORTED_LOCATION>
        $<TARGET_PROPERTY:fcitx5::unicode,IMPORTED_LOCATION>
        $<TARGET_PROPERTY:rime::rime,IMPORTED_LOCATION>
        ${CMAKE_LIBRARY_OUTPUT_DIRECTORY}
        COMMENT "Copying fcitx5 module libraries to :app"
        )

# install prebuilt assets
install(FILES "${PREBUILT_DIR}/spell-dict/en_dict.fscd" DESTINATION "${FCITX_INSTALL_PKGDATADIR}/spell" COMPONENT prebuilt-assets)
install(FILES "${CMAKE_CURRENT_SOURCE_DIR}/../../../../lib/rime/src/main/cpp/fcitx5-rime/data/fcitx5.yaml"
        DESTINATION "${FCITX_INSTALL_DATADIR}/rime-data" COMPONENT prebuilt-assets)
# TypeDuck: package the shared schema repository as a complete runtime data set.
install(DIRECTORY "${CMAKE_CURRENT_SOURCE_DIR}/../../../../lib/rime/src/main/schema/"
        DESTINATION "${FCITX_INSTALL_DATADIR}/rime-data"
        COMPONENT prebuilt-assets
        PATTERN ".git" EXCLUDE
        PATTERN ".github" EXCLUDE
        PATTERN "notes" EXCLUDE
        PATTERN ".gitignore" EXCLUDE)
