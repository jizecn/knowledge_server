cmake_minimum_required(VERSION 2.4.6)
include($ENV{ROS_ROOT}/core/rosbuild/rosbuild.cmake)
set(ROS_BUILD_TYPE Debug)
rosbuild_init()

rosbuild_genmsg()
rosbuild_gensrv()

# find needed paths
rosbuild_find_ros_package(srs_pellet)
add_jar_dir(${srs_pellet_PACKAGE_PATH}/common/lib)
add_jar_dir(${srs_pellet_PACKAGE_PATH}/common/lib/jena)
add_java_source_dir(${PROJECT_SOURCE_DIR}/src)

rosbuild_find_ros_package(knowledge_server)

include(cmake/utils.cmake)
set(OWL_PATH_PREFIX ${knowledge_server_PACKAGE_PATH})
fix_ros_destination_path(OWL_PATH_PREFIX)

file(GLOB inputs conf/ontologies/ipa-kitchen-map/*.in conf/ontologies/empty-map/*.in)

foreach( _in ${inputs} )
  string(REPLACE ".in" "" _out ${_in})
  configure_file( ${_in} ${_out} )
endforeach( _in )

rospack_add_java_executable(bin/knowledgeServer  org.srs.knowledge_server.ros.KnowledgeServer)
