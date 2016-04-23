# thrift-test
protobuf 测试

1. 首先安装protobuf
   安装目录 /usr/local/app/protoc
   下载源码包 https://github.com/google/protobuf/releases 
   直接下载 protobuf-java-3.0.0-beta-2.tar.gz
   解压, 参考 README.md 进行安装,安装后which protoc 查看安装目标路径
2. maven 工程配置插件 
   maven插件根据IDL文件生成代码的时候需要本地protoc执行命令路径   
3. 定义IDL文件 (文件目录 src/main/protobuf)
   参考文档 https://github.com/google/protobuf
   参考文档 https://developers.google.com/protocol-buffers/
4. mvn package 根据插件配置output目录,生成代码   
  