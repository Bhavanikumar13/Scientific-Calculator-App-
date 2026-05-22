cmake_minimum_required(VERSION 3.10)
project(CasioEngine CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

add_executable(casio_engine
    main.cpp
    parser.cpp
    parser.h
)
