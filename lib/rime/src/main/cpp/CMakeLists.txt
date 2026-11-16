cmake_minimum_required(VERSION 3.18)
project(typeduck-rime VERSION ${VERSION_NAME})

add_link_options("LINKER:--hash-style=gnu,--build-id=none")

find_package(fcitx5 REQUIRED CONFIG)
get_target_property(FCITX5_CMAKE_MODULES fcitx5::cmake INTERFACE_INCLUDE_DIRECTORIES)
set(CMAKE_MODULE_PATH ${FCITX5_CMAKE_MODULES} ${CMAKE_MODULE_PATH})
find_package(ECM MODULE)
find_package(Fcitx5Core MODULE)
find_package(Fcitx5Module MODULE)

# TypeDuck: link the pinned engine and dictionary filter built by our prebuilder.
set(Rime_VERSION "1.1.5")
set(Rime_dependencies log)
foreach(dependency glog leveldb marisa opencc yaml-cpp)
    list(APPEND Rime_dependencies
        "${PREBUILT_DIR}/${dependency}/${ANDROID_ABI}/lib/lib${dependency}.a")
endforeach()
list(APPEND CMAKE_FIND_ROOT_PATH "${PREBUILT_DIR}/boost/${ANDROID_ABI}/lib/cmake")
find_package(Boost 1.86.0 REQUIRED COMPONENTS regex CONFIG)
list(APPEND Rime_dependencies Boost::regex)

add_library(Rime_static STATIC IMPORTED)
set_target_properties(Rime_static PROPERTIES
    IMPORTED_LOCATION "${PREBUILT_DIR}/librime/${ANDROID_ABI}/lib/librime.a"
    INTERFACE_INCLUDE_DIRECTORIES "${PREBUILT_DIR}/librime/${ANDROID_ABI}/include"
    INTERFACE_LINK_LIBRARIES "${Rime_dependencies}")
set(RIME_TARGET $<LINK_LIBRARY:WHOLE_ARCHIVE,Rime_static>)
set(RIME_DATA_DIR "${CMAKE_INSTALL_DATADIR}/rime-data")
add_subdirectory(fcitx5-rime)
