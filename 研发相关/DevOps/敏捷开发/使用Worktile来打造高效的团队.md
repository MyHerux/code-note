# 使用Worktile来打造高效的团队

> 本文章使用的是企业版和普通版有些许不同，不过敏捷思想是一样的

## 高效团队的定义

- 需求的明确定义与拆分

- 新任务的敏捷开发

- 已完成任务的快速验收，问题任务的反馈

> 以上三点已经包括一个开发团队的全部：`产品经理`，`开发人员`，`测试`


## 需求的明确定义与拆分

- 需求的定义

    现在需要写一篇文章《使用Worktile来打造高效的团队》

    ![mark](http://of0qa2hzs.bkt.clouddn.com/blog/171127/gdgCKGdEKc.png?imageslim)

- 需求的拆分

    - 现在我们将需求拆分为4个放到需求池里面：

        > 需求的拆分尽量保持功能之间 `划分清晰`，工作量尽量符合 `3天原则` ,保证在 `3天内` 。

        ![mark](http://of0qa2hzs.bkt.clouddn.com/blog/171127/D3BGJJi4ke.png?imageslim)

    - 每个任务的详细描述

        > 对于每个具体任务，尽量说明要点，附上文档。

        ![mark](http://of0qa2hzs.bkt.clouddn.com/blog/171127/1jff1kGG9H.png?imageslim)

    - 标签的定义

        > 我们使用三种标签来区分任务，方便统计。

        - `Feat`：新的需求，新的功能点。
        - `Fix`：旧的功能点出现问题，需要修复的 `bug`。
        - `Refactor`: 旧的功能点，需要重构的部分。
        - 标签与 `git commit` 的使用可以参考 [Git的使用](https://github.com/MyHerux/program-beginer/blob/master/2.md)

            ![mark](http://of0qa2hzs.bkt.clouddn.com/blog/171127/4be2Ffci2E.png?imageslim)

## 新任务的敏捷开发

- 任务的接取

    > 需求池中的需求被相关程序员接取，原则上应该保证不让自己负责的任务跨度过大。

    - 将任务划分到自己下面。

    - 确定开始时间和截止时间，如果对于任务完成时间（超过 `3天原则` ）有异议的应当及时找产品经理确定。

    - 新建子任务，将3天的任务进一步细分。

        ![mark](http://of0qa2hzs.bkt.clouddn.com/blog/171127/FiJ5GjhhFK.png?imageslim)

- 任务状态的变更

    > 完成后的任务需要变更其任务组（可以自己设置更合适的分组），方便观察每天的任务变动情况

    - 接取的任务应该划分到《在做》

    - 完成后的任务在子任务中添加划分的测试人员，然后划分到《测试》

    - 测试验收后的任务划分到《完成》
    
    ![mark](http://of0qa2hzs.bkt.clouddn.com/blog/171127/e6Ceg4A707.png?imageslim)

    ![mark](http://of0qa2hzs.bkt.clouddn.com/blog/171127/2aD0JGbbg6.png?imageslim)

    ![mark](http://of0qa2hzs.bkt.clouddn.com/blog/171127/Dc82cCL5Ef.png?imageslim)

## 已完成任务的快速验收，问题任务的反馈

- 任务验收

    > 对于每个划分到《测试》组的任务进行验收

    - 未通过任务标注具体信息，@任务人

    - 收到消息的任务人会修改标注的错误重新提交

    - 重复验收过程，通过后的任务划分任务到《完成》

        ![mark](http://of0qa2hzs.bkt.clouddn.com/blog/171127/DlL53fdD11.png?imageslim)

- `Bug` 反馈

    > 回归测试中的 `Bug` 需要反馈到需求池

    ![mark](http://of0qa2hzs.bkt.clouddn.com/blog/171127/EbaA8EeEAf.png?imageslim)

## 统计与分析

- 甘特图

    > 通过甘特图来分析任务的跨度与进行状态

    ![mark](http://of0qa2hzs.bkt.clouddn.com/blog/171127/9mg78gHLGj.png?imageslim)

- 任务统计

    > 通过任务统计来分析任务完成状况

    ![mark](http://of0qa2hzs.bkt.clouddn.com/blog/171127/BA7272bII9.png?imageslim)