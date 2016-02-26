# eagleeye-android

## \[English\]
A static code scanning project for android, aimed to resolve potential bugs, performance bottlenecks and security risks.
## \[Chinese\]
EagleEye是一个用于代码静态扫描，在开发阶段排查问题的工具，本工程为EagleEye的Android版。
<br><br>
与一些类似功能的工具不同，EagleEye并不是个独立的工具，它是一个Android Lint规则库的扩展集，本身并不包含独立的语法分析功能。基于Android Lint，它扩展了一组自定义规则，主要涵盖潜在bug、性能、安全三方面的问题。
以下是规则列表：
###1、潜在bug
###2、性能
####2.1、BothWrapContentDetector
检查layout资源文件，对于layout_width和layout_height都设为wrap_content的View（暂时只控制在TextView），提示最好至少有一个属性为固定值或match_parent。
<br>
####2.2、EnumDetector
提示尽量避免使用枚举。
####2.3、NewMessageDetector
检查代码中使用new Message()创建Message对象的地方，提示改用Message.obtain()。
####2.4、ThreadPriorityDetector
检查代码中用new Thread()创建新线程的地方，如果创建完成后未显式调用Thread.setPriority()，或者调用设置了较高优先级，则报警。
####2.5、WrongAllocationDetector
在View的onMeasure()、onLayout()、onDraw()等可能被频繁调用的函数中，提示避免新建对象。
###3、安全
