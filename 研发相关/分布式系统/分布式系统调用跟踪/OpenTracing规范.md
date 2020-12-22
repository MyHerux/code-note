# OpenTracing规范
**Version: 1.1**

## Overview

`OpenTracing` 致力于为分布式跟踪创建更标准化的 `API` 和工具，它由完整的 `API` 规范、实现该规范的框架、库以及项目文档组成。
`OpenTracing` 提供了一套语言无关、平台无关、厂商无关的 `API` ，这样不同的组织或者开发人员就能够更加方便的添加或更换追踪系统的实现。 `OpenTracingAPI` 中的一些概念和术语，在不同的语言环境下都是共享的。

## OpenTracing 数据模型

`Opentracing` 规范中，一条 `trace` 链路是由多个与之关联的 `span` 组成，一条链路整体可以看做是一张有向无环图，各个 `span` 之间的边缘关系被称之为 `References` 。

例如，以下是由 `8` 个 `span` 组成的示例 `trace`：

>单个 `Trace` 中 `Spans` 之间的因果关系。

```
        [Span A]  ←←←(the root span)
            |
     +------+------+
     |             |
 [Span B]      [Span C] ←←←(Span C is a `ChildOf` Span A)
     |             |
 [Span D]      +---+-------+
               |           |
           [Span E]    [Span F] >>> [Span G] >>> [Span H]
                                       ↑
                                       ↑
                                       ↑
                         (Span G `FollowsFrom` Span F)
```

有时，使用时间轴来可视化跟踪更容易，如下图所示：

> 单个`Trace` 中 `Spans` 之间的时间关系。

```
––|–––––––|–––––––|–––––––|–––––––|–––––––|–––––––|–––––––|–> time

 [Span A···················································]
   [Span B··············································]
      [Span D··········································]
    [Span C········································]
         [Span E·······]        [Span F··] [Span G··] [Span H··]
```
每个 `Span` 封装以下状态：

- 操作名称
- 开始时间戳
- 结束时间戳
- 一组零个或多个 `key:value` 的 `Span Tags`。`key` 必须是字符串。 `value` 可以是字符串，布尔值或数字类型。
- 一组零个或多个 `Span Logs`，每个 `Span Log` 本身就是与时间戳匹配的 `key:value` 映射。键必须是字符串，尽管值可以是任何类型。但是并非所有 `OpenTracing` 实现都必须支持每种值类型。
- `SpanContext`
- `References` 零个或多个因果相关的 `Spans`（通过那些相关 `Spans` 的 `SpanContext`）

每个 `SpanContext` 封装以下状态：

- 引用跨过程边界的不同 `Span` 所需的任何 `OpenTracing` 实现依赖状态（例如，`trace` 和 `span` ID）
- `Baggage Items`，跨越过程边界的 `key:value` 对。

## Spans之间的引用

`Span` 可以引用因果相关的零个或多个其他 `SpanContext` 。`OpenTracing` 当前定义了两种类型的引用：`ChildOf` 和 `FollowsFrom` 。**两种参考类型都专门为子 `Span` 和父 `Span` 之间的直接因果关系建模。** 将来，OpenTracing可能还会支持具有非因果关系的Span的引用类型（例如，批处理的 `span` ，卡在同一队列中的 `span` 等）。

`ChildOf` 引用：`Span` 可以是父 `Span` 的 `ChildOf` 。在 `ChildOf` 引用中，父 `Span` 在某种程度上取决于子 `Span` 。以下所有内容将构成 `ChildOf` 关系：

- 代表`RPC` 的服务器端的 `Span` 可以是代表该 `RPC` 客户端的 `Span` 的 `ChildOf`
- 表示 `SQL` 插入的 `Span` 可以是表示 `ORM` 保存方法的 `Span` 的 `ChildOf`
