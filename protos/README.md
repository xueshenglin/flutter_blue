# Flutter Blue 蓝牙通信协议使用说明

## 一, 代码生成说明

### 1. 环境准备
- 安装 Protocol Buffers 编译器 (protoc)
  * macOS: brew install protobuf
  * Linux: apt-get install protobuf-compiler
  * Windows: 从 GitHub 下载预编译二进制文件

- 安装特定平台的生成器
  * Dart：
    - 运行：pub global activate protoc_plugin
    - 这会安装 protoc-gen-dart 插件
    - protoc 在生成 Dart 代码时会自动调用此插件
  * Java: 确保 protoc-gen-java 在 PATH 中
  * Objective-C: Xcode 自带，无需额外安装

### 2. 源文件说明
- 主要定义文件：protos/flutterblue.proto
- 所有平台共用这一个文件
- 不要在其他目录创建副本

### 3. 目录准备
- 执行生成脚本前需要手动创建以下目录：
  ```bash
  # Dart 代码目录
  mkdir -p lib/gen/protos
  
  # Java 代码目录
  mkdir -p android/src/generated/source/proto/release/java
  
  # Objective-C 代码目录
  mkdir -p ios/Classes
  ```

### 4. 代码生成
- 使用项目提供的 regenerate.sh 脚本生成所有平台的代码：
  ```bash
  # 确保脚本可执行
  chmod +x protos/regenerate.sh
  
  # 执行脚本生成代码
  ./protos/regenerate.sh
  ```

- 脚本会自动执行以下操作：
  * 生成 Dart 代码到 lib/gen/protos 目录
  * 生成 Java 代码（lite 版本）到 android/src/generated/source/proto/release/java 目录
  * 生成 Objective-C 代码到 ios/Classes 目录

### 5. 生成文件说明
- Dart：
  * 生成 flutterblue.pb.dart 文件，包含：
    - 所有消息类型的 Dart 类定义
    - 类型安全的枚举定义
    - 基于 Stream 的异步操作支持
    - 内置的 JSON 序列化支持
    - 与 Flutter 的状态管理集成
    - 自动生成的序列化/反序列化方法
    - 支持空安全（Null Safety）
  * 生成 flutterblue.pbenum.dart：
    - 包含所有枚举类型的定义
    - 提供类型安全的枚举值访问
  * 生成 flutterblue.pbserver.dart（可选）：
    - 用于服务器端实现
    - 包含服务接口定义

- Java：
  * 使用 lite 版本生成 Protos.java 文件
  * 位于 android/src/generated/source/proto/release/java/com/pauldemarco/flutter_blue 包下
  * 包含所有消息类型的 Java 类定义
  * 使用 GeneratedMessageLite 而不是 GeneratedMessage
  * 更小的内存占用，更好的性能
  * 适合移动设备使用

- Objective-C：
  * 生成 Flutterblue.pbobjc.h 和 .m 文件
  * 使用 Protos 前缀
  * 需要在 Xcode 项目中引入

### 6. 注意事项
- 每次修改 .proto 文件后都需要重新运行 regenerate.sh 脚本
- 确保生成的代码与项目的目录结构匹配
- 在版本控制中包含生成的代码
- 保持各平台生成代码的同步
- 不要复制或移动 proto 文件到其他目录

### 7. 依赖配置
- Dart (pubspec.yaml)：
  ```yaml
  dependencies:
    protobuf: ^2.0.0  # 注意：Dart 的 protobuf 版本号与其他平台不同
  ```

- Android (build.gradle)：
  ```gradle
  dependencies {
      // 使用 lite 版本，更轻量级
      implementation 'com.google.protobuf:protobuf-javalite:3.11.4'
  }
  ```

- iOS (Podfile)：
  ```ruby
  pod 'Protobuf', '~> 3.11.4'  # 使用与 Android 相同的版本
  ```

### 8. 参考文件
- `