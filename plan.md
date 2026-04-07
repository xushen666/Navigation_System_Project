# Navigation System 开发执行文档

本文件面向开发阶段，目标是把项目实现顺序、模块边界、接口、测试和验收标准固定下来，便于两人协作、联调、写报告与答辩准备。

## 1. 开发目标

- 完成一个基于图结构的导航系统桌面程序
- 满足课程设计对大数据量、图形界面、文件存储、算法分析和测试的要求
- 在 `10000+` 顶点规模上实现可演示、可运行的导航核心功能

## 2. 阶段划分

### 第 1 阶段：项目初始化与骨架

- 目标：建立 Maven 工程、包结构、主窗口和文档
- 主要类：`App`、`MainFrame`、`ControlPanel`、`StatusBar`
- 验收标准：
  - `mvn compile` 成功
  - 主窗口能够启动
  - `README.md` 和 `plan.md` 齐全

### 第 2 阶段：地图生成与文件读写

- 目标：实现 `10000+` 顶点地图生成和 CSV 持久化
- 主要类：`Vertex`、`Edge`、`Graph`、`MapGeneratorService`、`GraphFileRepository`
- 输入输出：
  - 输入：顶点数量、随机种子、地图尺寸
  - 输出：`Graph`、`vertices.csv`、`edges.csv`
- 验收标准：
  - 顶点数量正确
  - 图连通
  - 地图重新读取后一致
  - 道路容量按短路/中路/长路三档生成，便于答辩说明

### 第 3 阶段：地图显示、缩放、附近查询

- 目标：实现地图绘制、视口抽样和附近 100 点查询
- 主要类：`MapPanel`、`ViewportService`、`SpatialIndexService`
- 输入输出：
  - 输入：坐标 `(x,y)`
  - 输出：最近 100 个地点及关联边的可视化结果
- 验收标准：
  - 地图可平移缩放
  - 查询结果能正确显示
  - 缩小时点位不会过密不可读

### 第 4 阶段：静态最短路径

- 目标：实现按道路长度求最短路径
- 主要类：`PathFinderService`、`PathResult`
- 输入输出：
  - 输入：起点、终点
  - 输出：顶点序列、边序列、总距离
- 验收标准：
  - 小图结果与人工计算一致
  - 大图查询时间可接受

### 第 5 阶段：车流模拟与路况最优路径

- 目标：实现车辆动态更新和按时间最优路径
- 主要类：`Vehicle`、`TrafficSimulationService`
- 输入输出：
  - 输入：仿真开始/暂停、起点、终点
  - 输出：实时拥堵颜色、最优路径、总行车时间
- 验收标准：
  - 车流会动态变化
  - 最优路径会因拥堵状态变化而变化

### 第 6 阶段：测试、优化、答辩材料

- 目标：补齐测试、截图、视频、PPT、个人报告
- 验收标准：
  - 所有核心功能可演示
  - 文档与最终程序一致
  - 提交材料完整

## 3. 模块与负责人

### 成员 A

- `model`
- `repository`
- `MapGeneratorService`
- `SpatialIndexService`
- `PathFinderService`
- 单元测试

### 成员 B

- `ui`
- `TrafficSimulationService`
- `ViewportService`
- 交互提示、状态栏、演示脚本

### 共享文件

- `pom.xml`
- `README.md`
- `plan.md`
- `config.properties`

## 4. 核心接口清单

### 数据模型

- `Vertex`
- `Edge`
- `Graph`
- `Vehicle`
- `PathResult`

### 服务接口

- `MapGeneratorService.generateMap(int vertexCount, long seed)`
- `GraphFileRepository.saveGraph(Graph graph, Path dir)`
- `GraphFileRepository.loadGraph(Path dir)`
- `SpatialIndexService.buildIndex(Graph graph)`
- `SpatialIndexService.findNearest(double x, double y)`
- `SpatialIndexService.findKNearest(double x, double y, int k)`
- `PathFinderService.findShortestByDistance(int startId, int endId)`
- `PathFinderService.findShortestByTime(int startId, int endId)`
- `TrafficSimulationService.start()`
- `TrafficSimulationService.pause()`
- `TrafficSimulationService.reset()`
- `TrafficSimulationService.tick()`
- `ViewportService.getVisibleVertices(...)`

## 5. 联调顺序

1. 打通 `Graph -> MapPanel`，确认能画出地图
2. 打通 `SpatialIndexService -> ControlPanel`，确认坐标查询能高亮
3. 打通 `PathFinderService -> MapPanel`，确认路径能显示
4. 打通 `TrafficSimulationService -> MapPanel`，确认颜色动态变化
5. 打通菜单、按钮、状态栏，形成完整演示流程

## 6. 风险点与应对

- 风险：顶点过多导致绘制卡顿
  - 应对：缩小时采用代表点抽样
- 风险：随机地图出现交叉道路
  - 应对：使用扰动网格和单对角线规则
- 风险：道路容量设置难以解释
  - 应对：采用按道路长度分档的容量规则，当前分为 `30 / 50 / 70`
- 风险：车流仿真过慢
  - 应对：控制每个 tick 的新车数量，使用增量更新
- 风险：联调时接口不一致
  - 应对：核心类名和方法签名固定，不随意改动
- 风险：报告后期难写
  - 应对：开发过程中同步截图、记录测试数据和结果

## 7. 测试清单

- 地图顶点数是否达到 `10000`
- 地图是否连通
- 文件保存和重新加载是否一致
- 最近 100 点查询是否正确
- 缩放后是否进行了代表点抽样
- 静态最短路径结果是否正确
- 车流颜色是否随拥堵等级变化
- 路况最优路径是否能动态变化
- 所有提示是否为中文
- 非法输入是否有异常提示

## 8. 报告素材准备

开发过程中应及时保留：

- 系统主界面截图
- 地图查询结果截图
- 最短路径截图
- 车流模拟截图
- 路况最优路径截图
- 测试数据表
- 算法流程图草稿
- 时间复杂度和空间复杂度分析笔记

## 9. 开发命令

```bash
mvn clean compile
mvn test
mvn exec:java
mvn clean package
```

## 10. 完成标准

- 程序可启动并稳定运行
- 所有核心功能都有可演示入口
- README 与 `plan.md` 已同步更新
- 测试结果可写入报告
- 可运行文件、PPT 和视频录制方案已准备完毕
