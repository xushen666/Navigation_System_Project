# Navigation System

基于 `Java 17 + Maven + Swing` 的数据结构课程设计项目。系统将城市地图抽象为无向图，顶点表示地点，边表示道路，在满足大数据量场景的前提下实现地图展示、最近地点查询、最短路径、动态车流模拟与基于路况的最佳路径计算。

## 1. 项目目标

本项目面向车载导航系统的教学型实现，重点考核以下数据结构与算法能力：

- 图结构建模：地点与道路的抽象、邻接表存储
- 空间检索：基于 `KD-Tree` 的最近点与最近 100 点查询
- 路径搜索：基于 `A*` 的最短距离与最短时间路径
- 动态仿真：基于道路容量和当前车辆数的实时路况模拟
- 图形界面：支持鼠标交互、缩放平移、中文提示和动态渲染

## 2. 功能列表

项目对应任务书功能如下：

- `F1 地图显示`：输入坐标后显示最近的 100 个地点及其关联道路
- `F2 地图缩放`：支持地图缩放和平移，缩小时使用代表点抽样减少画面拥挤
- `F3 最短路径`：指定地点 A 和 B，计算按距离最短的路径并高亮显示
- `F4 车流模拟`：动态更新道路车流量，用不同颜色显示拥堵等级
- `F5 路况最优路径`：在当前车流状态下计算行车时间最短的路径

说明：任务书中两处都写了 `F2/F3`，此 README 按功能语义重新编号，实际实现与原要求一一对应。

## 3. 技术栈

- 语言：`Java 17`
- 构建工具：`Maven`
- GUI：`Swing`
- 测试：`JUnit 5`
- 数据存储：`CSV + properties`

## 4. 项目结构

```text
navigation-system/
├─ pom.xml
├─ README.md
├─ plan.md
├─ .gitignore
├─ data/
│  ├─ input/
│  └─ output/
├─ docs/
│  ├─ report/
│  ├─ ppt/
│  └─ demo/
├─ src/
│  ├─ main/
│  │  ├─ java/com/ds/navigation/
│  │  │  ├─ App.java
│  │  │  ├─ model/
│  │  │  ├─ repository/
│  │  │  ├─ service/
│  │  │  ├─ ui/
│  │  │  └─ util/
│  │  └─ resources/
│  │     └─ config.properties
│  └─ test/
│     └─ java/com/ds/navigation/
```

### 包职责

- `model`：顶点、边、图、车辆、路径结果等核心数据对象
- `repository`：地图文件保存与加载
- `service`：地图生成、KD-Tree、路径搜索、视口抽样、车流模拟
- `ui`：主窗口、地图面板、控制区、状态栏
- `util`：配置加载、坐标换算、几何计算、输入校验

## 5. 开发环境

- JDK 17 或更高版本
- Maven 3.8+
- Windows / Linux / macOS 均可运行

## 6. 编译与运行

```bash
mvn clean compile
mvn test
mvn exec:java
```

打包：

```bash
mvn clean package
java -jar target/navigation-system-1.0-SNAPSHOT.jar
```

## 7. 数据文件说明

默认地图数据保存在 `data/output/latest/` 目录下：

- `vertices.csv`：地点信息，字段为 `id,x,y,name,type`
- `edges.csv`：道路信息，字段为 `id,from,to,length,capacity,currentVehicles`

配置文件位于 `src/main/resources/config.properties`，主要包含：

- 地图大小与顶点数量
- 道路容量分档配置
- 车流阈值参数
- 界面默认缩放和仿真刷新间隔

## 8. 核心设计说明

### 8.1 地图生成

地图采用“扰动网格 + 局部连接”的方式生成：

- 先在二维平面上生成规则网格，保证整体连通
- 每个顶点在所属网格内随机扰动，增强随机性
- 邻接边以上下左右连接为主
- 每个网格小单元至多增加一条对角线，避免不合理交叉
- 道路容量按长度分档设置，更便于解释和测试：
  - 短路容量约 `30`
  - 中路容量约 `50`
  - 长路容量约 `70`

### 8.2 空间查询

为保证大规模顶点下查询效率，系统使用 `KD-Tree` 构建空间索引，支持：

- 查找最近单个地点
- 查找最近的 100 个地点

### 8.3 最短路径

系统统一使用 `A*` 搜索：

- 静态路径：边权为道路长度
- 动态路径：边权为实时行车时间 `c * L * f(n/v)`

其中：

- `L` 为道路长度
- `v` 为道路容量
- `n` 为当前道路上的车辆数
- `f(x)=1`，当 `x<=alpha`
- `f(x)=1+exp(x-alpha)`，当 `x>alpha`

### 8.4 地图显示与缩放

- 支持鼠标滚轮缩放
- 支持按住鼠标拖拽平移
- 缩小时采用“屏幕网格代表点”策略，只绘制每个屏幕小格中的一个代表点

### 8.5 车流模拟

- 仿真定时器周期性刷新车辆状态
- 随机生成车辆出发点与目的地
- 车辆进入道路时增加道路车流量，离开时减少
- 道路根据拥堵程度以绿 / 黄 / 橙 / 红四级显示

## 9. 当前实现状态

当前仓库已提供：

- 完整 Maven 项目骨架
- Swing 主窗口与基础交互
- 地图生成、读写、KD-Tree、A*、车流模拟等首版实现
- 基础单元测试
- 开发执行文档 `plan.md`

## 10. 两人分工建议

- 成员 A：
  - `model`
  - `repository`
  - `MapGeneratorService`
  - `SpatialIndexService`
  - `PathFinderService`
  - 算法测试
- 成员 B：
  - `ui`
  - `TrafficSimulationService`
  - `ViewportService`
  - 界面交互与演示整理

## 11. 测试计划

- 结构测试：Maven 工程、包结构、主窗口启动
- 数据测试：顶点数、连通性、文件读写一致性
- 算法测试：最近点查询、最短路径、路况变化
- UI 测试：缩放、拖拽、点选、路径高亮
- 异常测试：非法输入、坏文件、未选点求路径

## 12. 提交建议

建议最终提交包含：

- 源代码
- 可运行 jar
- 每位成员个人报告
- 答辩 PPT
- 演示视频
- PPT 讲解视频

详细开发步骤见 [plan.md](plan.md)。
