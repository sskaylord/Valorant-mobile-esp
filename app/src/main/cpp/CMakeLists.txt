cmake_minimum_required(VERSION 3.18)
project(valorant_esp)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

add_library(native_esp SHARED
    memory_reader.cpp
    anti_detect.cpp
)

find_library(log-lib log)

target_link_libraries(native_esp ${log-lib})
