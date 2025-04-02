#!/bin/bash

# 确保脚本可执行:
# chmod +x protos/regenerate.sh

# 获取项目根目录的绝对路径
PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"

# 切换到 protos 目录
cd "$PROJECT_ROOT/protos"

echo "Generating Dart code..."

# dart 的文件未验证.谨慎使用重新生成的文件. 建议保留就旧文件即可 2025-4-2
./protoc-3.11.4-osx-x86_64/bin/protoc --dart_out=../lib/gen/protos \
       --proto_path=. \
       flutterblue.proto

echo "Generating Java code..."
echo "需要使用 lite 版本生成,适合移动端节约内存"
# 使用指定版本的 protoc 程序 (protoc 3.11.4),不要用系统自带的 protoc,否则使用 MapEntryLite会报错
./protoc-3.11.4-osx-x86_64/bin/protoc --java_out=lite:../android/src/generated/source/proto/release/java \
       --proto_path=. \
       flutterblue.proto

echo "Generating Objective-C code..."
# 使用指定版本的 protoc 程序 (protoc 3.11.4)
# 注意：这个版本需要与 iOS 项目中的 Protobuf 库版本匹配
./protoc-3.11.4-osx-x86_64/bin/protoc --objc_out=../ios/gen \
       --proto_path=. \
       flutterblue.proto

echo "Proto files regenerated successfully!" 